package uk.tvidal.data

import uk.tvidal.data.codec.CodecFactory
import uk.tvidal.data.codec.EntityDecoder
import uk.tvidal.data.logging.KLogging
import uk.tvidal.data.query.EntityQuery
import uk.tvidal.data.query.SimpleQuery
import uk.tvidal.data.query.Statement
import uk.tvidal.data.sql.SqlDialect
import java.sql.Connection
import kotlin.reflect.KClass

class Database(
  val dialect: SqlDialect,
  val config: Config = Config.Default,
  private val createConnection: () -> Connection,
) {
  private val connection = ThreadLocal<Connection>()

  val codecs: CodecFactory
    get() = dialect.codecs

  val currentTransaction: Connection?
    get() = connection.get()

  fun <E : Any> repository(
    entity: KClass<E>,
    decoder: EntityDecoder<E>,
  ): EntityRepository<E> = RepositoryImpl(
    db = this,
    entity = entity,
    decoder = decoder,
  )

  inline fun <reified E : Any> repository(
    decoder: EntityDecoder<E> = codecs.decoder(E::class)
  ) = repository(
    entity = E::class,
    decoder = decoder
  )

  fun <T> select(query: SimpleQuery, decoder: Statement.() -> T): T = invoke { cnn ->
    info { "select: $query" }
    Statement(cnn, query)
      .use(decoder)
  }

  fun execute(sql: String): Boolean = invoke { cnn ->
    Statement(cnn, sql).use {
      it.execute()
    }.info {
      "executed: $it\n$sql"
    }
  }

  fun execute(query: SimpleQuery): Int = invoke { cnn ->
    Statement(cnn, query).use {
      it.executeSingle()
    }.info {
      "executed: affected$it\n$query"
    }
  }

  fun <E> execute(query: EntityQuery<E>, value: E): Int = invoke { cnn ->
    Statement(cnn, query).use {
      it.setParams(query[value])
      it.executeSingle()
    }.info {
      "executed: affected=$it\n$query"
    }
  }

  fun <E> execute(query: EntityQuery<E>, values: Iterable<E>): IntArray = invoke { cnn ->
    Statement(cnn, query).use {
      for (value in values) {
        it.setParams(query[value])
        it.statement.addBatch()
      }
      it.executeBatch()
    }.info {
      "executed: affected=${it.sum()}\n$query"
    }
  }

  inline fun <reified E : Any> delete(builder: WhereClauseBuilder<E>) = execute(
    dialect.delete(E::class, where<E>(builder))
  )

  fun create(vararg entities: KClass<*>) = invoke { cnn ->
    for (entity in entities) {
      execute(
        sql = dialect.create(entity, config.createIfNotExists)
      )
    }
  }

  fun drop(vararg entities: KClass<*>) = invoke { cnn ->
    for (entity in entities) {
      execute(
        sql = dialect.drop(entity, config.createIfNotExists)
      )
    }
  }

  fun beginTransaction() = currentTransaction ?: createConnection()
    .also(connection::set)

  fun closeTransaction() {
    currentTransaction?.let {
      if (!it.isClosed()) {
        it.close()
      }
      connection.remove()
    }
  }

  operator fun <T> invoke(action: (Connection) -> T): T = currentTransaction.let { transaction ->
    if (transaction == null) {
      val newTransaction = beginTransaction()
      try {
        action(newTransaction)
          .also { newTransaction.commit() }
      } catch (e: Throwable) {
        if (!newTransaction.isClosed) {
          newTransaction.rollback()
        }
        throw e
      } finally {
        closeTransaction()
      }
    } else {
      action(transaction)
    }
  }

  companion object : KLogging()
}
