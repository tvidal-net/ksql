package uk.tvidal.data.schema

import kotlin.reflect.KProperty

sealed interface ColumnReference {

  val name: String

  data class Ascending(override val name: String) : ColumnReference {
    override fun toString() = name
  }

  data class Descending(override val name: String) : ColumnReference {
    override fun toString() = "$name DESC"
  }

  companion object Factory {
    operator fun invoke(name: String): ColumnReference = Ascending(name)
    operator fun invoke(property: KProperty<*>) = invoke(property.name)
  }
}
