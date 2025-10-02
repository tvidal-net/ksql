package uk.tvidal.data.filter

import uk.tvidal.data.sql.SqlQueryBuilder.Constants.PARAM_CHAR
import kotlin.reflect.KProperty1

sealed class SqlPropertyParamFilter<out V> : SqlPropertyFilter<V>() {

  abstract val operator: String

  data class Equals<T>(override val property: KProperty1<*, T>) : SqlPropertyParamFilter<T>() {
    override val operator: String
      get() = SqlFilter.EQ
  }

  data class NotEquals<T>(override val property: KProperty1<*, T>) : SqlPropertyParamFilter<T>() {
    override val operator: String
      get() = SqlFilter.NE
  }

  data class GreaterThan<T>(override val property: KProperty1<*, T>) : SqlPropertyParamFilter<T>() {
    override val operator: String
      get() = SqlFilter.GT
  }

  data class LessThan<T>(override val property: KProperty1<*, T>) : SqlPropertyParamFilter<T>() {
    override val operator: String
      get() = SqlFilter.LT
  }

  data class GreaterEquals<T>(override val property: KProperty1<*, T>) : SqlPropertyParamFilter<T>() {
    override val operator: String
      get() = SqlFilter.GE
  }

  data class LessEquals<T>(override val property: KProperty1<*, T>) : SqlPropertyParamFilter<T>() {
    override val operator: String
      get() = SqlFilter.LE
  }

  override fun toString() = "${property.name}$operator$PARAM_CHAR"
}
