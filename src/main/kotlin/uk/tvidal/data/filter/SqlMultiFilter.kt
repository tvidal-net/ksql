package uk.tvidal.data.filter

import java.util.Objects

sealed class SqlMultiFilter : SqlFilter {

  abstract val operands: Collection<SqlFilter>
  abstract val separator: String

  class And(override val operands: Collection<SqlFilter>) : SqlMultiFilter() {
    override val separator: String
      get() = SqlFilter.AND
  }

  class Or(override val operands: Collection<SqlFilter>) : SqlMultiFilter() {
    override val separator: String
      get() = SqlFilter.OR
  }

  override fun hashCode() = Objects.hash(this::class, separator, operands)

  override fun equals(other: Any?) = other is SqlMultiFilter
    && this::class === other::class
    && separator == other.separator
    && operands == other.operands

  override fun toString() = operands.joinToString(separator)
}
