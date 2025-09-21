package uk.tvidal.data.filter

import uk.tvidal.data.query.Dialect
import java.util.Objects
import kotlin.reflect.KProperty1

sealed class SqlFieldParamFilter<out V> : SqlFieldFilter<V>() {

  abstract val operator: String

  class Equals<T>(override val field: KProperty1<*, T>) : SqlFieldParamFilter<T>() {
    override val operator: String
      get() = SqlFilter.EQ
  }

  class NotEquals<T>(override val field: KProperty1<*, T>) : SqlFieldParamFilter<T>() {
    override val operator: String
      get() = SqlFilter.NE
  }

  class GreaterThan<T>(override val field: KProperty1<*, T>) : SqlFieldParamFilter<T>() {
    override val operator: String
      get() = SqlFilter.GT
  }

  class LessThan<T>(override val field: KProperty1<*, T>) : SqlFieldParamFilter<T>() {
    override val operator: String
      get() = SqlFilter.LT
  }

  class GreaterEquals<T>(override val field: KProperty1<*, T>) : SqlFieldParamFilter<T>() {
    override val operator: String
      get() = SqlFilter.GE
  }

  class LessEquals<T>(override val field: KProperty1<*, T>) : SqlFieldParamFilter<T>() {
    override val operator: String
      get() = SqlFilter.LE
  }

  override fun hashCode() = Objects.hash(super.hashCode(), operator)

  override fun equals(other: Any?) = other is SqlFieldParamFilter<*>
    && super.equals(other)
    && operator == other.operator

  override fun toString() = "${field.name}$operator${Dialect.PARAM_CHAR}"
}
