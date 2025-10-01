package uk.tvidal.data.schema

import kotlin.reflect.KProperty1

data class Column<T>(
  val name: String,
  val dataType: DataType<T>,
  val nullable: Boolean = true,
) {

  override fun toString() = "$name $dataType ${nullDef(nullable)}"

  companion object {
    fun nullDef(nullable: Boolean) =
      (if (!nullable) "NOT " else "") + "NULL"

    fun <T> from(property: KProperty1<*, T>): Column<T> {
      TODO()
    }
  }
}
