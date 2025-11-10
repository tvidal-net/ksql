package uk.tvidal.data

import uk.tvidal.data.codec.CodecFactory
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

  fun <E : Any> repository(entity: KClass<E>): EntityRepository<E> = RepositoryImpl(
    db = this,
    entity = entity,
  )

  inline fun <reified E : Any> repository() =
    repository(E::class)

  fun execute(sql: String, vararg paramValues: Any?) =
    execute(SimpleQuery(sql), *paramValues)

  fun execute(query: SimpleQuery, vararg paramValues: Any?) = invoke { cnn ->
    query.execute(cnn, *paramValues)
  }

  fun <E> execute(query: EntityQuery<E>, value: E) = invoke { cnn ->
    query.execute(cnn, value)
  }

  fun <E> execute(query: EntityQuery<E>, values: Iterable<E>) = invoke { cnn ->
    query.execute(cnn, values)
  }

  inline fun <reified E : Any> delete(builder: WhereClauseBuilder<E>) = execute(
    dialect.delete(E::class, where<E>(builder))
  )

  fun create(vararg tables: KClass<*>) = invoke { cnn ->
    tables.forEach { table ->
      dialect.create(config.schema(table), config.createIfNotExists)
        .execute(cnn)
    }
  }

  fun drop(vararg entities: KClass<*>) = invoke { cnn ->
    entities.forEach { table ->
      dialect.drop(table, config.createIfNotExists)
        .execute(cnn)
    }
  }

  fun beginTransaction() = currentTransaction ?: createConnection()
    .also(connection::set)

  fun closeTransaction() {
    currentTransaction?.run {
      if (!isClosed()) close()
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
}
