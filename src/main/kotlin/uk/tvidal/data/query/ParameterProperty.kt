package uk.tvidal.data.query

import uk.tvidal.data.model.fieldName
import kotlin.reflect.KProperty1

class ParameterProperty<in E>(
  override val index: Int,
  val property: KProperty1<in E, *>
) : QueryParameter {

  override val name: String
    get() = property.fieldName

  operator fun get(value: E): Any? = property(value)

  override fun toString() = "$index:$name"
}
