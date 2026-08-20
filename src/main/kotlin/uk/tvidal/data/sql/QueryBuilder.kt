package uk.tvidal.data.sql

import uk.tvidal.data.filter.SqlOperator
import uk.tvidal.data.query.QueryParam
import uk.tvidal.data.query.SelectFrom

interface QueryBuilder {

  fun Appendable.param(index: Int, paramName: CharSequence) {
    append(PARAM_CHAR)
  }

  fun Appendable.param(param: QueryParam) {
    param(param.index, param.name)
  }

  fun Appendable.append(joinType: SelectFrom.Join.Type) {
    append(joinType.sql)
  }

  fun Appendable.append(operator: SqlOperator) {
    append(operator.sql)
  }

  fun Appendable.ifExists(value: Boolean) {
    if (value) append("IF EXISTS ")
  }

  fun Appendable.ifNotExists(value: Boolean) {
    if (value) append("IF NOT EXISTS ")
  }

  fun Appendable.notNull(notNull: Boolean) {
    if (notNull) append(" NOT NULL")
  }

  fun Appendable.isNotNull() {
    append(" IS NOT NULL")
  }

  fun Appendable.isNull() {
    append(" IS NULL")
  }

  fun Appendable.indent(size: Int = 1) {
    repeat(size) {
      append("  ")
    }
  }

  fun Appendable.space() {
    append(' ')
  }

  fun Appendable.listSeparator() {
    append(',')
    space()
  }

  fun Appendable.schemaSeparator() {
    append(SCHEMA_SEP)
  }

  fun Appendable.terminate() {
    append(';')
  }

  fun Appendable.openBlock() {
    append('(')
  }

  fun Appendable.closeBlock() {
    append(')')
  }

  fun Appendable.openQuote() {}

  fun Appendable.closeQuote() {}

  companion object Constants {

    const val PARAM_CHAR = '?'
    const val SCHEMA_SEP = '.'
  }
}
