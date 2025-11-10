package uk.tvidal.data.schema

import uk.tvidal.data.codec.ValueType

data class SchemaField<T : Any>(
  val name: String,
  val type: ValueType<*, T>,
  val nullable: Boolean = true,
) {

  override fun toString() = "$name $type ${nullDef(nullable)}"

  companion object {
    fun nullDef(nullable: Boolean) =
      (if (!nullable) "NOT " else "") + "NULL"
  }
}
