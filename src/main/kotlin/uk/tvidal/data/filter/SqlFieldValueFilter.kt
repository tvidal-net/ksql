package uk.tvidal.data.filter

import java.util.Objects
import kotlin.reflect.KProperty1

sealed class SqlFieldValueFilter<out V> : SqlFieldFilter<V>() {

  abstract val operator: String
  abstract val value: V

  class Equals<T>(override val field: KProperty1<*, T>, override val value: T) : SqlFieldValueFilter<T>() {
    override val operator: String
      get() = SqlFilter.EQ
  }

  class NotEquals<T>(override val field: KProperty1<*, T>, override val value: T) : SqlFieldValueFilter<T>() {
    override val operator: String
      get() = SqlFilter.NE
  }

  class GreaterThan<T>(override val field: KProperty1<*, T>, override val value: T) : SqlFieldValueFilter<T>() {
    override val operator: String
      get() = SqlFilter.GT
  }

  class LessThan<T>(override val field: KProperty1<*, T>, override val value: T) : SqlFieldValueFilter<T>() {
    override val operator: String
      get() = SqlFilter.LT
  }

  class GreaterEquals<T>(override val field: KProperty1<*, T>, override val value: T) : SqlFieldValueFilter<T>() {
    override val operator: String
      get() = SqlFilter.GE
  }

  class LessEquals<T>(override val field: KProperty1<*, T>, override val value: T) : SqlFieldValueFilter<T>() {
    override val operator: String
      get() = SqlFilter.LE
  }

  class Like(override val field: KProperty1<*, String>, override val value: String) : SqlFieldValueFilter<String>() {
    override val operator: String
      get() = SqlFilter.LIKE
  }

  override fun hashCode() = Objects.hash(super.hashCode(), operator, value)

  override fun equals(other: Any?) = other is SqlFieldValueFilter<*>
    && super.equals(other)
    && operator == other.operator
    && value == other.value

  override fun toString() = "${field.name}$operator'$value'"
}
