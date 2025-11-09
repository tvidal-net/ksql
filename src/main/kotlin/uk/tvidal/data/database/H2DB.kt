package uk.tvidal.data.database

import org.h2.jdbcx.JdbcDataSource
import uk.tvidal.data.Config
import uk.tvidal.data.Database
import uk.tvidal.data.sql.SqlDialect
import uk.tvidal.data.table
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

class H2DB(config: Config = Config.Default) : SqlDialect(config) {

  override fun <E : Any> save(
    entity: KClass<E>,
    updateFields: Collection<KProperty1<E, *>>,
    keyFields: Collection<KProperty1<E, *>>
  ) = entityQuery<E> { params ->
    append("MERGE INTO ")
    tableName(entity.table)
    space()
    fieldNames(updateFields + keyFields)
    appendLine()
    indent()
    append("KEY ")
    fieldNames(keyFields)
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
      Database(dialect, createConnection = ds::getConnection)
    }
  }
}
