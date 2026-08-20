package uk.tvidal.data.sql

import uk.tvidal.data.TableName
import uk.tvidal.data.filter.SqlFilter
import uk.tvidal.data.filter.SqlOperator
import uk.tvidal.data.filter.SqlPropertyParamFilter
import uk.tvidal.data.query.QueryParam
import kotlin.reflect.KProperty1

@Suppress("UNCHECKED_CAST")
interface StatementDialect : BaseDialect {

  fun <E, P : QueryParam> Appendable.setFields(
    params: MutableCollection<in P>,
    fields: Collection<KProperty1<E, *>>
  ) {
    append("SET ")
    for ((i, field) in fields.withIndex()) {
      if (i > 0) {
        listSeparator()
      }
      fieldFilter(
        params = params as MutableCollection<QueryParam>,
        filter = SqlPropertyParamFilter(field, SqlOperator.Equals),
      )
    }
  }

  fun <E, P : QueryParam> Appendable.fieldParams(
    params: MutableCollection<in P>,
    fields: Collection<KProperty1<in E, *>>
  ) {
    openBlock()
    for ((i, field) in fields.withIndex()) {
      if (i > 0) {
        listSeparator()
      }
      fieldParam(
        params = params as MutableCollection<QueryParam>,
        property = field
      )
    }
    closeBlock()
  }

  fun <E, P : QueryParam> Appendable.insertInto(
    table: TableName,
    params: MutableCollection<in P>,
    insertFields: Collection<KProperty1<E, *>>,
    indentLevel: Int = 0,
  ) {
    append("INSERT INTO ")
    newLine(indentLevel + 1)
    tableName(table)
    space()
    fieldNames(insertFields)
    insertValues(params, insertFields, indentLevel)
  }

  fun <E, P : QueryParam> Appendable.insertValues(
    params: MutableCollection<in P>,
    insertFields: Collection<KProperty1<E, *>>,
    indentLevel: Int = 0,
  ) {
    newLine(indentLevel + 1)
    append("VALUES ")
    fieldParams(params, insertFields)
  }

  fun <P : QueryParam> Appendable.deleteQuery(
    params: MutableCollection<in P>,
    table: TableName,
    whereClause: SqlFilter,
    indentLevel: Int = 0,
  ) {
    append("DELETE FROM ")
    tableName(table)
    where(params, whereClause, indentLevel)
  }
}
