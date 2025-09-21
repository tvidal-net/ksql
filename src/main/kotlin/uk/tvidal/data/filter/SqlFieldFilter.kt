package uk.tvidal.data.filter

import java.util.Objects
import kotlin.reflect.KProperty1

sealed class SqlFieldFilter<out V> : SqlFilter {

  abstract val field: KProperty1<*, V>

  class IsNull(override val field: KProperty1<*, Any>) : SqlFieldFilter<Any>() {
    override fun toString() = "${field.name}${SqlFilter.IS_NULL}"
  }

  class IsNotNull(override val field: KProperty1<*, Any>) : SqlFieldFilter<Any>() {
    override fun toString() = "${field.name}${SqlFilter.IS_NOT_NULL}"
  }

  override fun hashCode() = Objects.hash(this::class, field)

  override fun equals(other: Any?) = other is SqlFieldFilter<V>
    && this::class === other::class
    && field == other.field
}
