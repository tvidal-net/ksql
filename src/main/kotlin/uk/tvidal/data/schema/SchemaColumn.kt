package uk.tvidal.data.schema

import uk.tvidal.data.codec.DataType
import uk.tvidal.data.fieldName
import uk.tvidal.data.isNullable
import kotlin.reflect.KProperty

data class SchemaColumn<T>(
  val name: String,
  val dataType: DataType<T>,
  val nullable: Boolean = true,
) {

  override fun toString() = "$name $dataType ${nullDef(nullable)}"

  companion object {
    fun nullDef(nullable: Boolean) =
      (if (!nullable) "NOT " else "") + "NULL"

    fun from(property: KProperty<*>, config: SchemaConfig = SchemaConfig.Default) = SchemaColumn(
      name = property.fieldName,
      dataType = DataType.from(property, config),
      nullable = property.isNullable
    )
  }
}
