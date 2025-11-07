package uk.tvidal.data.filter

import uk.tvidal.data.dot
import kotlin.reflect.KProperty

sealed class SqlPropertyValueFilter<out V> : SqlPropertyFilter<V>() {

  abstract val operator: String
  abstract val value: V

  data class Equals<T>(
    override val property: KProperty<T>,
    override val value: T,
    override val alias: String? = null,
  ) : SqlPropertyValueFilter<T>() {
    override val operator: String
      get() = SqlFilter.EQ
  }

  data class NotEquals<T>(
    override val property: KProperty<T>,
    override val value: T,
    override val alias: String? = null,
  ) : SqlPropertyValueFilter<T>() {
    override val operator: String
      get() = SqlFilter.NE
  }

  data class GreaterThan<T>(
    override val property: KProperty<T>,
    override val value: T,
    override val alias: String? = null,
  ) : SqlPropertyValueFilter<T>() {
    override val operator: String
      get() = SqlFilter.GT
  }

  data class LessThan<T>(
    override val property: KProperty<T>,
    override val value: T,
    override val alias: String? = null,
  ) : SqlPropertyValueFilter<T>() {
    override val operator: String
      get() = SqlFilter.LT
  }

  data class GreaterEquals<T>(
    override val property: KProperty<T>,
    override val value: T,
    override val alias: String? = null,
  ) : SqlPropertyValueFilter<T>() {
    override val operator: String
      get() = SqlFilter.GE
  }

  data class LessEquals<T>(
    override val property: KProperty<T>,
    override val value: T,
    override val alias: String? = null,
  ) : SqlPropertyValueFilter<T>() {
    override val operator: String
      get() = SqlFilter.LE
  }

  data class Like(
    override val property: KProperty<String>,
    override val value: String,
    override val alias: String? = null,
  ) : SqlPropertyValueFilter<String>() {
    override val operator: String
      get() = SqlFilter.LIKE
  }

  override fun toString() = "${alias.dot}${property.name}$operator'$value'"
}
