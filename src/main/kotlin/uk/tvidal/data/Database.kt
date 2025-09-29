package uk.tvidal.data

import uk.tvidal.data.codec.EntityDecoder
import uk.tvidal.data.logging.KLogging
import uk.tvidal.data.query.Dialect
import uk.tvidal.data.query.EntityQuery
import uk.tvidal.data.query.Query
import uk.tvidal.data.query.Statement
import java.sql.Connection
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

class Database(
  val dialect: Dialect,
  private val createConnection: () -> Connection,
) {

  private val connection = ThreadLocal<Connection>()

  val currentTransaction: Connection?
    get() = connection.get()

  inline fun <reified E : Any> byConstructor(): EntityDecoder<E> = EntityDecoder.ByConstructor(
    constructor = E::class.primaryConstructor!!,
    namingStrategy = dialect.namingStrategy,
  )

  // inject factory method / supplier
  inline fun <reified E : Any> byProperties(vararg constructorArgs: Any?): EntityDecoder<E> = EntityDecoder.ByProperties(
    constructor = E::class.primaryConstructor!!,
    namingStrategy = dialect.namingStrategy,
    constructorArgs = constructorArgs,
  )

  fun <E : Any> repository(
    entity: KClass<out E>,
    decoder: EntityDecoder<E>,
  ): Repository<E> = RepositoryImpl(
    db = this,
    entity = entity,
    decoder = decoder,
  )

  inline fun <reified E : Any> repository(
    decoder: EntityDecoder<E> = byConstructor()
  ) = repository(
    entity = E::class,
    decoder = decoder
  )

  fun <T> select(query: Query, decoder: Statement.() -> T): T = invoke { cnn ->
    info { "select: $query" }
    Statement(cnn, query)
      .use(decoder)
  }

  fun execute(query: Query): Int = invoke { cnn ->
    Statement(cnn, query).use {
      it.executeSingle()
    }.also {
      log.info("executed: affected={}, {}", it, query)
    }
  }

  fun <E> execute(query: EntityQuery<E>, value: E): Int = invoke { cnn ->
    Statement(cnn, query).use {
      it.setParams(query[value])
      it.executeSingle()
    }.also {
      log.info("executed: affected={}, {}", it, query)
    }
  }

  fun <E> execute(query: EntityQuery<E>, values: Iterable<E>): IntArray = invoke { cnn ->
    Statement(cnn, query).use {
      for (value in values) {
        it.setParams(query[value])
        it.statement.addBatch()
      }
      it.executeBatch()
    }.also {
      info { "executed: affected=${it.sum()}, $query" }
    }
  }

  inline fun <reified E : Any> delete(builder: WhereClauseBuilder<E>) = execute(
    dialect.delete(E::class, sqlFilter<E>(builder))
  )

  fun beginTransaction() = currentTransaction ?: createConnection()
    .also(connection::set)

  fun closeTransaction() {
    val transaction = currentTransaction
    if (transaction?.isClosed == false) {
      transaction.close()
    }
    connection.remove()
  }

  inline operator fun <T> invoke(executeTransaction: (Connection) -> T): T = currentTransaction.let { transaction ->
    if (transaction == null) {
      val newTransaction = beginTransaction()
      try {
        executeTransaction(newTransaction)
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
      executeTransaction(transaction)
    }
  }

  companion object : KLogging()
}
