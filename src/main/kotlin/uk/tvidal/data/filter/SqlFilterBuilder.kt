package uk.tvidal.data.filter

import kotlin.reflect.KProperty1

class SqlFilterBuilder<in E> {

  private val filters = ArrayList<SqlFilter>()

  internal fun <T : SqlFilter> add(filter: T): T =
    filter.also(filters::add)

  val KProperty1<out E, Any>.isNull
    get() = add(SqlFieldFilter.IsNull(this))

  val KProperty1<out E, Any>.isNotNull
    get() = add(SqlFieldFilter.IsNotNull(this))

  internal fun <V> KProperty1<out E, V>.eq() = add(
    SqlFieldParamFilter.Equals(this)
  )

  fun <V> KProperty1<out E, V>.eq(value: V) = add(
    SqlFieldValueFilter.Equals(this, value)
  )

  internal fun <V> KProperty1<out E, V>.ne() = add(
    SqlFieldParamFilter.NotEquals(this)
  )

  fun <V> KProperty1<out E, V>.ne(value: V) = add(
    SqlFieldValueFilter.NotEquals(this, value)
  )

  internal fun <V> KProperty1<out E, V>.gt() = add(
    SqlFieldParamFilter.GreaterThan(this)
  )

  fun <V> KProperty1<out E, V>.gt(value: V) = add(
    SqlFieldValueFilter.GreaterThan(this, value)
  )

  internal fun <V> KProperty1<out E, V>.lt() = add(
    SqlFieldParamFilter.LessThan(this)
  )

  fun <V> KProperty1<out E, V>.lt(value: V) = add(
    SqlFieldValueFilter.LessThan(this, value)
  )

  internal fun <V> KProperty1<out E, V>.ge() = add(
    SqlFieldParamFilter.GreaterEquals(this)
  )

  fun <V> KProperty1<out E, V>.ge(value: V) = add(
    SqlFieldValueFilter.GreaterEquals(this, value)
  )

  internal fun <V> KProperty1<out E, V>.le() = add(
    SqlFieldParamFilter.LessEquals(this)
  )

  fun <V> KProperty1<out E, V>.le(value: V) = add(
    SqlFieldValueFilter.LessEquals(this, value)
  )

  fun KProperty1<out E, String>.like(value: String) = add(
    SqlFieldValueFilter.Like(this, value)
  )

  fun <V> KProperty1<out E, V>.between(fromValue: V, toValue: V) = add(
    SqlFieldMultiValueFilter.Between(this, fromValue, toValue)
  )

  fun <V> KProperty1<out E, V>.inValues(values: Collection<V>) = add(
    SqlFieldMultiValueFilter.In(this, values)
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
