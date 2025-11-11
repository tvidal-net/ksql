package uk.tvidal.data.filter

import kotlin.reflect.KProperty1

sealed class SqlPropertyFilter<out V> : SqlFilter {

  abstract val alias: String?
  abstract val property: KProperty1<*, V>

  override val values: Collection<Any?>
    get() = emptyList()

  data class IsNull(override val property: KProperty1<*, Any?>, override val alias: String? = null) : SqlPropertyFilter<Any?>() {
    override fun toString() = "${property.name}${SqlFilter.IS_NULL}"
  }

  data class IsNotNull(override val property: KProperty1<*, Any?>, override val alias: String? = null) : SqlPropertyFilter<Any?>() {
    override fun toString() = "${property.name}${SqlFilter.IS_NOT_NULL}"
  }
}
