package uk.tvidal.data.filter

import kotlin.reflect.KProperty1

sealed class SqlPropertyJoinFilter<out V> : SqlPropertyFilter<V>() {

  abstract val operator: String
  abstract val target: KProperty1<*, V>

  override val values: Collection<Any?>
    get() = emptyList()

  data class Equals<out T>(
    override val property: KProperty1<*, T>,
    override val alias: String,
    override val target: KProperty1<*, T>,
  ) : SqlPropertyJoinFilter<T>() {
    override val operator: String
      get() = SqlFilter.EQ

    override fun toString() = super.toString()
  }

  override fun toString() = "${property.name}$operator${alias.dot}${target.name}"
}
