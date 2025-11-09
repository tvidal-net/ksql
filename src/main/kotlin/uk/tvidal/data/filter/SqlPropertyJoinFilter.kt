package uk.tvidal.data.filter

import uk.tvidal.data.dot
import kotlin.reflect.KProperty

sealed class SqlPropertyJoinFilter<out V> : SqlPropertyFilter<V>() {

  abstract val operator: String
  abstract val target: KProperty<V>

  override val values: Collection<Any?>
    get() = emptyList()

  data class Equals<out T>(
    override val property: KProperty<T>,
    override val alias: String,
    override val target: KProperty<T>,
  ) : SqlPropertyJoinFilter<T>() {
    override val operator: String
      get() = SqlFilter.EQ
  }

  override fun toString() = "${property.name}$operator${alias.dot}${target.name}"
}
