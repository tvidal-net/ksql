package uk.tvidal.data

import org.assertj.core.api.AbstractStringAssert
import org.assertj.core.api.AbstractThrowableAssert
import org.assertj.core.api.Assertions
import org.assertj.core.api.ListAssert
import uk.tvidal.data.codec.DataType
import uk.tvidal.data.filter.SqlFilter
import uk.tvidal.data.query.QueryParam
import uk.tvidal.data.query.SqlQuery
import uk.tvidal.data.schema.ColumnReference
import uk.tvidal.data.schema.Constraint
import uk.tvidal.data.schema.SchemaColumn
import uk.tvidal.data.sql.SqlDialect
import kotlin.reflect.KClass

class TestDialect : SqlDialect(NamingStrategy.AsIs) {

  fun constraint(it: Constraint) = sql {
    schemaConstraint(it)
  }

  fun column(it: ColumnReference) = sql {
    column(it)
  }

  fun column(it: SchemaColumn<*>) = sql {
    schemaColumn(it)
  }

  fun dataType(it: DataType<*>) = sql {
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

  companion object SqlAssertions {
    val newLines = Regex("[\\n\\r]+\\s*", RegexOption.MULTILINE)
    val spaces = Regex("\\s+")

    private val String.actual: String
      get() = trim()
        .replace(newLines, "")
        .replace(spaces, " ")

    private fun sql(builder: Appendable.() -> Unit) =
      buildString(builder).actual

    fun assertQuery(builder: TestDialect.() -> SqlQuery): ListAssert<String> = Assertions.assertThat(
      builder(TestDialect()).sql.actual.split(";\n")
    )

    fun assertThrows(builder: TestDialect.() -> SqlQuery): AbstractThrowableAssert<*, out Throwable> = Assertions.assertThatThrownBy {
      builder(TestDialect()).sql.actual
    }

    fun assertThat(builder: TestDialect.() -> String): AbstractStringAssert<*> = Assertions.assertThat(
      builder(TestDialect())
    )
  }
}
