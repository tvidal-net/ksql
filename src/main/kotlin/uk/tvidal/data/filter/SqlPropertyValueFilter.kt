package uk.tvidal.data.filter

import kotlin.reflect.KProperty1

data class SqlPropertyValueFilter<out V>(
  override val property: KProperty1<*, V>,
  val operator: SqlOperator,
  val value: V,
  override val alias: String? = null,
) : SqlPropertyFilter<V>() {

  override val values: Collection<Any?>
    get() = listOf(value)

  override fun toString() = "${alias.dot}${property.name}$operator'$value'"
}
