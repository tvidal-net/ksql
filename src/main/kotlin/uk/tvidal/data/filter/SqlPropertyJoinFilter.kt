package uk.tvidal.data.filter

import kotlin.reflect.KProperty1

data class SqlPropertyJoinFilter<out V>(
  override val property: KProperty1<*, V>,
  val target: KProperty1<*, V>,
  override val alias: String,
  val operator: SqlOperator = SqlOperator.Equals,
) : SqlPropertyFilter<V>() {

  override val values: Collection<Any?>
    get() = emptyList()

  override fun toString() = "${property.name}$operator${alias.dot}${target.name}"
}
