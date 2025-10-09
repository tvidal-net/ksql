package uk.tvidal.data.database

import org.h2.jdbcx.JdbcDataSource
import uk.tvidal.data.Database
import uk.tvidal.data.NamingStrategy
import uk.tvidal.data.sql.SqlDialect
import uk.tvidal.data.tableName
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

class H2DB(namingStrategy: NamingStrategy = NamingStrategy.SnakeCase) : SqlDialect(namingStrategy) {

  override fun <E : Any> save(
    entity: KClass<E>,
    updateFields: Collection<KProperty1<E, *>>,
    keyFields: Collection<KProperty1<E, *>>
  ) = entityQuery<E> { params ->
    append("MERGE INTO ")
    tableName(entity.tableName)
    appendLine()
    indent()
    fieldNames(updateFields)
    append("KEY ")
    fieldNames(keyFields)
    appendLine()
    indent()
    append("VALUES ")
    insertValues(params, updateFields + keyFields)
  }

  companion object {

    val Default = H2DB()

    fun createDatabase(
      url: String,
      username: String? = null,
      password: String? = null,
      dialect: SqlDialect = Default
    ): Database = JdbcDataSource().apply {
      setUrl(url)
      setUser(username)
      setPassword(password)
    }.let { ds ->
      Database(dialect, ds::getConnection)
    }
  }
}
