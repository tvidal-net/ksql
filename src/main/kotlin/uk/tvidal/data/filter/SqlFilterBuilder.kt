package uk.tvidal.data.filter

import kotlin.reflect.KProperty1

class SqlFilterBuilder<in E> {

  private val filters = ArrayList<SqlFilter>()

  internal fun <T : SqlFilter> add(filter: T): T =
    filter.also(filters::add)

  val KProperty1<out E, Any>.isNull
    get() = add(SqlPropertyFilter.IsNull(this))

  val KProperty1<out E, Any>.isNotNull
    get() = add(SqlPropertyFilter.IsNotNull(this))

  internal fun <V> KProperty1<out E, V>.eq() = add(
    SqlPropertyParamFilter.Equals(this)
  )

  fun <V> KProperty1<out E, V>.eq(value: V) = add(
    SqlPropertyValueFilter.Equals(this, value)
  )

  internal fun <V> KProperty1<out E, V>.ne() = add(
    SqlPropertyParamFilter.NotEquals(this)
  )

  fun <V> KProperty1<out E, V>.ne(value: V) = add(
    SqlPropertyValueFilter.NotEquals(this, value)
  )

  internal fun <V> KProperty1<out E, V>.gt() = add(
    SqlPropertyParamFilter.GreaterThan(this)
  )

  fun <V> KProperty1<out E, V>.gt(value: V) = add(
    SqlPropertyValueFilter.GreaterThan(this, value)
  )

  internal fun <V> KProperty1<out E, V>.lt() = add(
    SqlPropertyParamFilter.LessThan(this)
  )

  fun <V> KProperty1<out E, V>.lt(value: V) = add(
    SqlPropertyValueFilter.LessThan(this, value)
  )

  internal fun <V> KProperty1<out E, V>.ge() = add(
    SqlPropertyParamFilter.GreaterEquals(this)
  )

  fun <V> KProperty1<out E, V>.ge(value: V) = add(
    SqlPropertyValueFilter.GreaterEquals(this, value)
  )

  internal fun <V> KProperty1<out E, V>.le() = add(
    SqlPropertyParamFilter.LessEquals(this)
  )

  fun <V> KProperty1<out E, V>.le(value: V) = add(
    SqlPropertyValueFilter.LessEquals(this, value)
  )

  fun KProperty1<out E, String>.like(value: String) = add(
    SqlPropertyValueFilter.Like(this, value)
  )

  fun <V> KProperty1<out E, V>.between(fromValue: V, toValue: V) = add(
    SqlPropertyMultiValueFilter.Between(this, fromValue, toValue)
  )

  fun <V> KProperty1<out E, V>.inValues(values: Collection<V>) = add(
    SqlPropertyMultiValueFilter.In(this, values)
  )

  fun <V> KProperty1<out E, V>.inValues(vararg values: V) =
    inValues(values.toList())

  fun SqlFilter.or(vararg others: SqlFilter): SqlFilter {
    val operands = listOf(this, *others)
    filters.removeAll(operands)
    return add(SqlMultiFilter.Or(operands))
  }

  fun build(): SqlFilter = if (filters.size > 1) {
    SqlMultiFilter.And(filters)
  } else {
    filters.single()
  }
}
