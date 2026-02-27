package uk.tvidal.data.schema

import uk.tvidal.data.Config
import uk.tvidal.data.NamingStrategy.PascalCase
import uk.tvidal.data.codec.ValueType
import uk.tvidal.data.fieldName
import uk.tvidal.data.fields
import uk.tvidal.data.isNullable
import uk.tvidal.data.keyField
import uk.tvidal.data.returnValueType
import java.math.BigDecimal
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1
import kotlin.reflect.full.findAnnotation

@Suppress("UNCHECKED_CAST")
data class SchemaField<T : Any>(
  val name: String,
  val type: ValueType<*, T>,
  val nullable: Boolean = true,
) {

  override fun toString() = "$name $type ${nullDef(nullable)}"

  companion object Factory {
    private fun nullDef(nullable: Boolean) =
      (if (!nullable) "NOT " else "") + "NULL"

    private fun <T : Any> Config.keyType(table: KClass<*>): ValueType<*, T>? = table.keyField?.let {
      fieldType(it as KProperty<T>)
    }

    internal fun <V : Any> Config.schemaFieldType(field: KProperty<V?>) = field.run {
      findAnnotation<Decimal>()?.let { valueType(BigDecimal::class, it.column) }
        ?: fieldType(field)
        ?: keyType(returnValueType)
    }

    fun <E : Any, T> from(
      field: KProperty1<E, T>,
      config: Config = Config.Default,
      namePrefix: String? = null,
      parentNullable: Boolean = false
    ): Collection<SchemaField<*>> = when (val fieldType = config.schemaFieldType(field)) {
      null -> field.returnValueType.fields.flatMap {
        from(it, config, field.fieldName, field.isNullable)
      }

      else -> listOf(
        field.run {
          val prefixedName = if (namePrefix?.isNotBlank() == true) {
            "$namePrefix${PascalCase[fieldName]}"
          } else {
            fieldName
          }
          SchemaField(prefixedName, fieldType, isNullable || parentNullable)
        }
      )
    }
  }
}
