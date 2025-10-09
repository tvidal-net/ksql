package uk.tvidal.data.database

import org.mariadb.jdbc.MariaDbPoolDataSource
import uk.tvidal.data.Database
import uk.tvidal.data.NamingStrategy
import uk.tvidal.data.fieldName
import uk.tvidal.data.sql.SqlDialect
import uk.tvidal.data.tableName
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

class MariaDB(namingStrategy: NamingStrategy = NamingStrategy.SnakeCase) : SqlDialect(namingStrategy) {

  override fun Appendable.openQuote() {
    append(QUOTE_CHAR)
  }

  override fun Appendable.closeQuote() {
    append(QUOTE_CHAR)
  }

  override fun <E : Any> save(
    entity: KClass<E>,
    updateFields: Collection<KProperty1<E, *>>,
    keyFields: Collection<KProperty1<E, *>>
  ) = entityQuery<E> { params ->
    insertInto(entity.tableName, params, updateFields + keyFields)
    onDuplicateKey(updateFields)
  }

  private fun <E> Appendable.onDuplicateKey(fields: Collection<KProperty1<E, *>>) {
    appendLine()
    indent()
    append("ON DUPLICATE KEY UPDATE ")
    for ((i, field) in fields.withIndex()) {
      if (i > 0) listSeparator()
      quotedName(field.fieldName)
      append("=VALUES")
      openBlock()
      quotedName(field.fieldName)
      closeBlock()
    }
  }

  companion object {

    const val QUOTE_CHAR = '`'

    val Default = MariaDB()

    fun createDatabase(
      url: String,
      username: String? = null,
      password: String? = null,
      dialect: SqlDialect = Default,
    ): Database = MariaDbPoolDataSource().apply {
      setUrl(url)
      setUser(username)
      setPassword(password)
    }.let { ds ->
      Database(dialect) {
        ds.connection
      }
    }
  }
}
