package uk.tvidal.data.filter

import uk.tvidal.data.dot
import kotlin.reflect.KProperty1

sealed class SqlPropertyMultiValueFilter<out V> : SqlPropertyFilter<V>() {

  abstract val operator: String

  data class Between<T>(
    override val property: KProperty1<*, T>,
    val fromValue: T,
    val toValue: T,
    override val alias: String? = null,
  ) : SqlPropertyMultiValueFilter<T>() {
    override val operator: String
      get() = SqlFilter.BETWEEN
    override val values: Collection<T>
      get() = listOf(fromValue, toValue)
  }

  data class In<T>(
    override val property: KProperty1<*, T>,
    override val values: Collection<T>,
    override val alias: String? = null,
  ) : SqlPropertyMultiValueFilter<T>() {
    override val operator: String
      get() = SqlFilter.IN
  }

  override fun toString() = "${alias.dot}${property.name}$operator$values"
}
