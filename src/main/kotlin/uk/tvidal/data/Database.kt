package uk.tvidal.data

import uk.tvidal.data.codec.CodecFactory
import uk.tvidal.data.codec.EntityDecoder
import uk.tvidal.data.logging.KLogging
import uk.tvidal.data.query.EntityQuery
import uk.tvidal.data.query.SimpleQuery
import uk.tvidal.data.sql.SqlDialect
import java.sql.Connection
import kotlin.reflect.KClass

class Database(
  val dialect: SqlDialect,
  private val createConnection: () -> Connection,
) {
  private val connection = ThreadLocal<Connection>()

  val codecs: CodecFactory
    get() = dialect.codecs

  val config: Config
    get() = codecs.config

  val currentTransaction: Connection?
    get() = connection.get()

  fun <E : Any> repository(
    entity: KClass<E>,
    decoder: EntityDecoder<E> = codecs.decoder(entity)
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

  fun execute(
    sql: String,
    vararg paramValues: Any?
  ): Boolean = execute(
    SimpleQuery(sql),
    *paramValues
  )

  fun execute(
    query: SimpleQuery,
    vararg paramValues: Any?
  ): Boolean = invoke { cnn ->
    query.execute(cnn, *paramValues).info {
      "executed: affected$it\n$query"
    }
  }

  fun <E> execute(query: EntityQuery<E>, value: E): Int = invoke { cnn ->
    query.execute(cnn, value).info {
      "executed: affected=$it\n$query"
    }
  }

  fun <E> execute(query: EntityQuery<E>, values: Iterable<E>): IntArray = invoke { cnn ->
    query.execute(cnn, values).info {
      "executed: affected=${it.sum()}\n$query"
    }
  }

  inline fun <reified E : Any> delete(builder: WhereClauseBuilder<E>) = execute(
    dialect.delete(E::class, where<E>(builder))
  )

  fun create(vararg tables: KClass<*>) = invoke { cnn ->
    for (table in tables) {
      execute(
        query = dialect.create(config.schema(table), config.createIfNotExists)
      )
    }
  }

  fun drop(vararg entities: KClass<*>) = invoke { cnn ->
    for (entity in entities) {
      execute(
        query = dialect.drop(entity, config.createIfNotExists)
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
