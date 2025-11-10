package uk.tvidal.data

import uk.tvidal.data.codec.ValueType
import uk.tvidal.data.codec.returnValueType
import uk.tvidal.data.schema.Constraint.Factory.foreignKeys
import uk.tvidal.data.schema.SchemaField
import uk.tvidal.data.schema.SchemaTable
import uk.tvidal.data.schema.primaryKey
import java.math.BigDecimal
import javax.persistence.Column
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.isSubclassOf

@Suppress("UNCHECKED_CAST")
class Config(
  val namingStrategy: NamingStrategy = NamingStrategy.SnakeCase,
  val createIfNotExists: Boolean = true,
  val enumIgnoreCase: Boolean = true,
  val string: ValueType<String, String> = ValueType.NVarChar(ValueType.LENGTH),
  val decimal: ValueType<BigDecimal, BigDecimal> = ValueType.Decimal(ValueType.DEFAULT_SCALE, ValueType.DEFAULT_PRECISION),
) {

  internal fun <E : Enum<E>> enumType(type: KClass<E>, column: Column? = null) = ValueType.EnumType(
    enumClass = type,
    fieldLength = column?.length,
    ignoreCase = enumIgnoreCase
  )

  internal fun string(column: Column? = null) = column
    ?.let { ValueType.NVarChar(it.length) }
    ?: string

  internal fun decimal(column: Column? = null) = column
    ?.let { ValueType.Decimal(it.scale, precision(column)) }
    ?: decimal

  fun paramType(parameter: KParameter) = parameter.run {
    valueType(returnValueType, findAnnotation())
  }

  fun <T> fieldType(field: KProperty<T>) = field.run {
    valueType(returnValueType, column)
  }

  fun <T : Any> valueType(type: KClass<T>, column: Column? = null): ValueType<*, T>? = when {
    type.java.isEnum -> enumType(type as KClass<out Enum<*>>, column)
    type.isSubclassOf(CharSequence::class) -> string(column)
    else -> ValueType.from(type) ?: when {
      type.isSubclassOf(Number::class) -> decimal(column)
      else -> null
    }
  } as? ValueType<*, T>

  fun <E : Any> schema(type: KClass<E>) = SchemaTable(
    table = type.table,
    fields = type.fields.map { schema(it) }, // TODO: use primary constructor to preserve field order
    constraints = listOfNotNull(type.primaryKey) + foreignKeys(type),
  )

  fun <E : Any, T> schema(field: KProperty1<E, T>) = SchemaField(
    name = field.fieldName,
    type = requireNotNull(fieldType(field) ?: keyType(field.receiverType)) {
      "Unable to find a suitable ValueType for $field"
    },
    nullable = field.isNullable
  )

  private fun <T : Any> keyType(table: KClass<*>): ValueType<*, T>? {
    val key = table.keyFields
    if (key.size != 1) return null
    return fieldType(key.single() as KProperty<T>)
  }

  companion object Constants {

    val Default = Config()

    private fun precision(column: Column?): Int? = column?.run {
      if (precision > 0) precision else null
    }
  }
}
