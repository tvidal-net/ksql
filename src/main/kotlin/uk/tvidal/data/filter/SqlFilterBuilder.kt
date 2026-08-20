package uk.tvidal.data.filter

import kotlin.reflect.KProperty1

class SqlFilterBuilder<E> {

  private val filters = ArrayList<SqlFilter>()

  internal fun <T : SqlFilter> add(filter: T): T =
    filter.also(filters::add)

  private fun <V> KProperty1<E, V>.paramFilter(operator: SqlOperator) =
    add(SqlPropertyParamFilter(this, operator))

  private fun <V> KProperty1<E, V>.valueFilter(operator: SqlOperator, value: V) =
    add(SqlPropertyValueFilter(this, operator, value))

  val KProperty1<E, *>.isNull
    get() = add(SqlPropertyFilter.IsNull(this))

  val KProperty1<E, *>.isNotNull
    get() = add(SqlPropertyFilter.IsNotNull(this))

  internal fun <V> KProperty1<E, V>.eq() = paramFilter(SqlOperator.Equals)

  fun <V> KProperty1<E, V>.eq(value: V) = valueFilter(SqlOperator.Equals, value)

  internal fun <V> KProperty1<E, V>.ne() = paramFilter(SqlOperator.NotEquals)

  fun <V> KProperty1<E, V>.ne(value: V) = valueFilter(SqlOperator.NotEquals, value)

  internal fun <V> KProperty1<E, V>.gt() = paramFilter(SqlOperator.GreaterThan)

  fun <V> KProperty1<E, V>.gt(value: V) = valueFilter(SqlOperator.GreaterThan, value)

  internal fun <V> KProperty1<E, V>.lt() = paramFilter(SqlOperator.LessThan)

  fun <V> KProperty1<E, V>.lt(value: V) = valueFilter(SqlOperator.LessThan, value)

  internal fun <V> KProperty1<E, V>.ge() = paramFilter(SqlOperator.GreaterEquals)

  fun <V> KProperty1<E, V>.ge(value: V) = valueFilter(SqlOperator.GreaterEquals, value)

  internal fun <V> KProperty1<E, V>.le() = paramFilter(SqlOperator.LessEquals)

  fun <V> KProperty1<E, V>.le(value: V) = valueFilter(SqlOperator.LessEquals, value)

  fun KProperty1<E, String>.like(value: String) = valueFilter(SqlOperator.Like, value)

  fun <V> KProperty1<E, V>.between(fromValue: V, toValue: V) = add(
    SqlPropertyMultiValueFilter.Between(this, fromValue, toValue)
  )

  fun <V> KProperty1<E, V>.inValues(values: Collection<V>) = add(
    SqlPropertyMultiValueFilter.In(this, values)
  )

  fun <V> KProperty1<E, V>.inValues(vararg values: V) =
    inValues(values.toList())

  fun SqlFilter.or(vararg others: SqlFilter): SqlFilter {
    val operands = setOf(this, *others)
    filters.removeAll(operands)
    return add(SqlMultiFilter.Or(operands))
  }

  fun build(): SqlFilter = if (filters.size > 1) {
    SqlMultiFilter.And(filters)
  } else {
    filters.single()
  }
}
