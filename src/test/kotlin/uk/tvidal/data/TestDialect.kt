package uk.tvidal.data

import org.assertj.core.api.AbstractStringAssert
import org.assertj.core.api.Assertions
import uk.tvidal.data.codec.DataType
import uk.tvidal.data.filter.SqlFilter
import uk.tvidal.data.query.EntityQuery
import uk.tvidal.data.query.QueryParam
import uk.tvidal.data.query.SelectQuery
import uk.tvidal.data.query.SimpleQuery
import uk.tvidal.data.schema.ColumnReference
import uk.tvidal.data.schema.Constraint
import uk.tvidal.data.schema.SchemaColumn
import uk.tvidal.data.sql.SqlDialect
import kotlin.reflect.KClass

internal object TestDialect : SqlDialect(
  Config(namingStrategy = NamingStrategy.AsIs)
) {

  private val newLines = Regex("[\\n\\r]+\\s?", RegexOption.MULTILINE)
  private val spaces = Regex("\\s+")

  private val String.actual: String
    get() = trim()
      .replace(newLines, "")
      .replace(spaces, " ")

  fun constraint(it: Constraint) = sql {
    schemaConstraint(it)
  }

  fun column(it: ColumnReference) = sql {
    column(it)
  }

  fun column(it: SchemaColumn<*>) = sql {
    schemaColumn(it)
  }

  fun dataType(it: DataType<*, *>) = sql {
    dataType(it)
  }

  fun setFields(params: MutableCollection<QueryParam>, entity: KClass<*>) = sql {
    setFields(params, entity.fields)
  }

  fun fieldParams(params: MutableCollection<QueryParam>, entity: KClass<*>) = sql {
    fieldParams(params, entity.fields)
  }

  fun filter(params: MutableCollection<QueryParam>, whereClause: SqlFilter) = sql {
    filter(params, whereClause)
  }

  fun tableName(table: TableName) = sql {
    tableName(table)
  }

  fun fieldNames(entity: KClass<*>) = sql {
    fieldNames(entity.fields)
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
