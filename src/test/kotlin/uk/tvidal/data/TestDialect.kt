package uk.tvidal.data

import org.assertj.core.api.AbstractStringAssert
import org.assertj.core.api.Assertions
import uk.tvidal.data.codec.ValueType
import uk.tvidal.data.filter.SqlFilter
import uk.tvidal.data.query.EntityQuery
import uk.tvidal.data.query.QueryParam
import uk.tvidal.data.query.SelectQuery
import uk.tvidal.data.query.SimpleQuery
import uk.tvidal.data.schema.FieldReference
import uk.tvidal.data.schema.Constraint
import uk.tvidal.data.schema.SchemaField
import uk.tvidal.data.sql.SqlDialect
import kotlin.reflect.KClass

internal object TestDialect : SqlDialect(
  Config(namingStrategy = NamingStrategy.AsIs)
) {

  private val newLines = Regex("[\\n\\r]+\\s?", RegexOption.MULTILINE)
  private val spaces = Regex("\\s+")

  private val String.actual: String
    get() = trim()
      .replace(newLines, " ")
      .replace(spaces, " ")

  fun constraint(it: Constraint) = sql {
    schemaConstraint(it)
  }

  fun field(it: FieldReference) = sql {
    field(it)
  }

  fun field(it: SchemaField<*>) = sql {
    field(it)
  }

  fun dataType(it: ValueType<*, *>) = sql {
    dataType(it)
  }

  fun setFields(params: MutableCollection<QueryParam>, table: KClass<*>) = sql {
    setFields(params, table.fields)
  }

  fun fieldParams(params: MutableCollection<QueryParam>, table: KClass<*>) = sql {
    fieldParams(params, table.fields)
  }

  fun filter(params: MutableCollection<QueryParam>, whereClause: SqlFilter) = sql {
    filter(params, whereClause)
  }

  fun tableName(table: TableName) = sql {
    tableName(table)
  }

  fun fieldNames(table: KClass<*>) = sql {
    fieldNames(table.fields)
  }

  override fun Appendable.openQuote() {
    append('[')
  }

  override fun Appendable.closeQuote() {
    append(']')
  }

  private fun sql(builder: Appendable.() -> Unit) =
    buildString(builder).actual

  internal fun assertSql(builder: TestDialect.() -> SimpleQuery) = Assertions.assertThat(
    builder().sql.actual
  )

  internal fun <E> assertSelect(builder: TestDialect.() -> SelectQuery<E>) = Assertions.assertThat(
    builder().sql.actual
  )

  internal fun <E> assertQuery(builder: TestDialect.() -> EntityQuery<E>) = Assertions.assertThat(
    builder().sql.actual
  )

  internal fun <E> assertThrows(builder: TestDialect.() -> EntityQuery<E>) =
    Assertions.assertThatThrownBy {
      builder().sql.actual
    }

  internal fun assertSqlThrows(builder: TestDialect.() -> String) = Assertions.assertThatThrownBy {
    builder().actual
  }

  internal fun assertThat(builder: TestDialect.() -> String): AbstractStringAssert<*> = Assertions.assertThat(
    builder()
  )
}
