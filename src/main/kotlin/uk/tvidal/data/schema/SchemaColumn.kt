package uk.tvidal.data.schema

import uk.tvidal.data.codec.DataType
import uk.tvidal.data.fieldName
import uk.tvidal.data.isNullable
import uk.tvidal.data.keyFields
import uk.tvidal.data.valueType
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

data class SchemaColumn<T : Any>(
  val name: String,
  val dataType: DataType<*, T>,
  val nullable: Boolean = true,
) {

  override fun toString() = "$name $dataType ${nullDef(nullable)}"

  companion object {
    fun nullDef(nullable: Boolean) =
      (if (!nullable) "NOT " else "") + "NULL"

    private fun keyType(entity: KClass<*>, config: SchemaConfig): DataType<*, *> {
      val keyFields = entity.keyFields
      require(keyFields.size == 1) {
        "Referenced entity ${entity.qualifiedName} must have exactly ONE Id Field"
      }
      return requireNotNull(DataType.from(keyFields.single(), config)) {
        "Unable to get a DataType for ${keyFields.single()}"
      }
    }

    private fun dataType(property: KProperty1<*, *>, config: SchemaConfig): DataType<*, *> {
      return DataType.from(property) ?: keyType(property.valueType(), config)
    }

    fun from(property: KProperty1<*, *>, config: SchemaConfig = SchemaConfig.Default) = SchemaColumn(
      name = property.fieldName,
      dataType = dataType(property, config),
      nullable = property.isNullable
    )
  }
}
