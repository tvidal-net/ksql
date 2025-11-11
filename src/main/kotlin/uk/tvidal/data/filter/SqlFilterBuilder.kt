package uk.tvidal.data.filter

import kotlin.reflect.KProperty1

class SqlFilterBuilder<E> {

  private val filters = ArrayList<SqlFilter>()

  internal fun <T : SqlFilter> add(filter: T): T =
    filter.also(filters::add)

  val KProperty1<E, *>.isNull
    get() = add(SqlPropertyFilter.IsNull(this))

  val KProperty1<E, *>.isNotNull
    get() = add(SqlPropertyFilter.IsNotNull(this))

  internal fun <V> KProperty1<E, V>.eq() = add(
    SqlPropertyParamFilter.Equals(this)
  )

  fun <V> KProperty1<E, V>.eq(value: V) = add(
    SqlPropertyValueFilter.Equals(this, value)
  )

  internal fun <V> KProperty1<E, V>.ne() = add(
    SqlPropertyParamFilter.NotEquals(this)
  )

  fun <V> KProperty1<E, V>.ne(value: V) = add(
    SqlPropertyValueFilter.NotEquals(this, value)
  )

  internal fun <V> KProperty1<E, V>.gt() = add(
    SqlPropertyParamFilter.GreaterThan(this)
  )

  fun <V> KProperty1<E, V>.gt(value: V) = add(
    SqlPropertyValueFilter.GreaterThan(this, value)
  )

  internal fun <V> KProperty1<E, V>.lt() = add(
    SqlPropertyParamFilter.LessThan(this)
  )

  fun <V> KProperty1<E, V>.lt(value: V) = add(
    SqlPropertyValueFilter.LessThan(this, value)
  )

  internal fun <V> KProperty1<E, V>.ge() = add(
    SqlPropertyParamFilter.GreaterEquals(this)
  )

  fun <V> KProperty1<E, V>.ge(value: V) = add(
    SqlPropertyValueFilter.GreaterEquals(this, value)
  )

  internal fun <V> KProperty1<E, V>.le() = add(
    SqlPropertyParamFilter.LessEquals(this)
  )

  fun <V> KProperty1<E, V>.le(value: V) = add(
    SqlPropertyValueFilter.LessEquals(this, value)
  )

  fun KProperty1<E, String>.like(value: String) = add(
    SqlPropertyValueFilter.Like(this, value)
  )

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
