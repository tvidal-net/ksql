package uk.tvidal.data.mariadb

import org.mariadb.jdbc.MariaDbPoolDataSource
import uk.tvidal.data.Database
import uk.tvidal.data.Dialect
import uk.tvidal.data.NamingStrategy
import uk.tvidal.data.fieldName
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

class MariaDB(namingStrategy: NamingStrategy = NamingStrategy.SnakeCase) : Dialect(namingStrategy) {

  override fun StringBuilder.openQuote() {
    append(QUOTE_CHAR)
  }

  override fun StringBuilder.closeQuote() {
    append(QUOTE_CHAR)
  }

  override fun <E : Any> save(
    entity: KClass<out E>,
    updateFields: Collection<KProperty1<out E, *>>,
    keyFields: Collection<KProperty1<out E, *>>
  ) = entityQuery<E> { params ->
    insertInto(entity)
    insertFields(updateFields + keyFields)
    insertValues(params, updateFields + keyFields)
    onDuplicateKey(updateFields)
  }

  private fun <E> StringBuilder.onDuplicateKey(fields: Collection<KProperty1<in E, *>>) {
    append("\n\tON DUPLICATE KEY UPDATE ")
    for ((i, field) in fields.withIndex()) {
      if (i > 0) {
        listSeparator()
      }
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
