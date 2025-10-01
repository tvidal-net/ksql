package uk.tvidal.data.mariadb

import org.mariadb.jdbc.MariaDbPoolDataSource
import uk.tvidal.data.Database
import uk.tvidal.data.Dialect
import uk.tvidal.data.NamingStrategy
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

class MariaDB(namingStrategy: NamingStrategy = NamingStrategy.SNAKE_CASE) : Dialect(namingStrategy) {

  override fun StringBuilder.openQuote() {
    append(QUOTE_CHAR)
  }

  override fun StringBuilder.closeQuote() {
    append(QUOTE_CHAR)
  }

  override fun <E : Any> save(
    table: KClass<out E>,
    updateColumns: Collection<KProperty1<out E, *>>,
    keyColumns: Collection<KProperty1<out E, *>>
  ) = tableQuery<E> { params ->
    insertInto(table)
    insertFields(updateColumns + keyColumns)
    insertValues(params, updateColumns + keyColumns)
    onDuplicateKey(updateColumns)
  }

  private fun <E> StringBuilder.onDuplicateKey(fields: Collection<KProperty1<in E, *>>) {
    append("\n\tON DUPLICATE KEY UPDATE ")
    for ((i, field) in fields.withIndex()) {
      if (i > 0) {
        listSeparator()
      }
      columnName(field)
      append("=VALUES")
      openBlock()
      columnName(field)
      closeBlock()
    }
  }

  companion object {

    const val QUOTE_CHAR = '`'

    val Default = MariaDB()

    @Suppress("UsePropertyAccessSyntax")
    fun createDatabase(
      url: String,
      username: String? = null,
      password: String? = null,
      dialect: Dialect = Default,
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
