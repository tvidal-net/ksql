package uk.tvidal.data.filter

import java.util.Objects
import kotlin.reflect.KProperty1

sealed class SqlFieldMultiValueFilter<out V> : SqlFieldFilter<V>() {

  abstract val operator: String
  abstract val values: Collection<V>

  class Between<T>(override val field: KProperty1<*, T>, fromValue: T, toValue: T) : SqlFieldMultiValueFilter<T>() {

    override val values: Collection<T> = listOf(fromValue, toValue)

    override val operator: String
      get() = SqlFilter.BETWEEN
  }

  class In<T>(override val field: KProperty1<*, T>, override val values: Collection<T>) : SqlFieldMultiValueFilter<T>() {
    override val operator: String
      get() = SqlFilter.IN
  }

  override fun hashCode() = Objects.hash(super.hashCode(), operator, values)

  override fun equals(other: Any?) = other is SqlFieldMultiValueFilter<*>
    && super.equals(other)
    && operator == other.operator
    && values == other.values

  override fun toString() = "${field.name}$operator$values"
}
