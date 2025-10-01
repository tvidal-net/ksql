package uk.tvidal.data.filter

import kotlin.reflect.KProperty1

sealed class SqlPropertyMultiValueFilter<out V> : SqlPropertyFilter<V>() {

  abstract val operator: String
  abstract val values: Collection<V>

  data class Between<T>(
    override val property: KProperty1<*, T>,
    val fromValue: T,
    val toValue: T
  ) : SqlPropertyMultiValueFilter<T>() {
    override val operator: String
      get() = SqlFilter.BETWEEN

    override val values: Collection<T>
      get() = listOf(fromValue, toValue)
  }

  data class In<T>(
    override val property: KProperty1<*, T>,
    override val values: Collection<T>
  ) : SqlPropertyMultiValueFilter<T>() {
    override val operator: String
      get() = SqlFilter.IN
  }

  override fun toString() = "${property.name}$operator$values"
}
