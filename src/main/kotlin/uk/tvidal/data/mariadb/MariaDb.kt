package uk.tvidal.data.mariadb

import org.mariadb.jdbc.MariaDbPoolDataSource
import uk.tvidal.data.Database
import uk.tvidal.data.query.Dialect
import uk.tvidal.data.query.NamingStrategy
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

class MariaDb(namingStrategy: NamingStrategy = NamingStrategy.SNAKE_CASE) : Dialect(namingStrategy) {

  override fun StringBuilder.openQuote() {
    append(MARIA_DB_QUOTE_CHAR)
  }

  override fun StringBuilder.closeQuote() {
    append(MARIA_DB_QUOTE_CHAR)
  }

  override fun <E : Any> save(
    entity: KClass<out E>,
    updateFields: Collection<KProperty1<out E, *>>,
    keyFields: Collection<KProperty1<out E, *>>
  ) = entityQuery<E> { params ->
    appendInsertInto()
    appendTableName(entity)
    append(SPACE)

    openBlock()
    appendFieldNames(updateFields + keyFields)
    closeBlock()

    append(VALUES)
    appendFieldParams(params, updateFields + keyFields)

    append(MARIA_DB_ON_DUPLICATE_UPDATE)
    appendMariaDbSetValues(updateFields)
  }

  private fun <E> StringBuilder.appendMariaDbSetValues(fields: Collection<KProperty1<in E, *>>) {
    for ((i, field) in fields.withIndex()) {
      if (i > 0) {
        appendListSeparator()
      }
      appendFieldName(field)
      append("=VALUES")
      openBlock()
      appendFieldName(field)
      closeBlock()
    }
  }

  companion object {

    const val MARIA_DB_ON_DUPLICATE_UPDATE = "\n\tON DUPLICATE KEY UPDATE "
    const val MARIA_DB_QUOTE_CHAR = '`'

    val Default = MariaDb()

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
