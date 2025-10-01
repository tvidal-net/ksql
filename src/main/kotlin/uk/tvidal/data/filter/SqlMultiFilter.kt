package uk.tvidal.data.filter

sealed class SqlMultiFilter : SqlFilter {

  abstract val operands: Collection<SqlFilter>
  abstract val separator: String

  data class And(override val operands: Collection<SqlFilter>) : SqlMultiFilter() {
    override val separator: String
      get() = SqlFilter.AND
  }

  data class Or(override val operands: Collection<SqlFilter>) : SqlMultiFilter() {
    override val separator: String
      get() = SqlFilter.OR
  }

  override fun toString() = operands.joinToString(separator)
}
