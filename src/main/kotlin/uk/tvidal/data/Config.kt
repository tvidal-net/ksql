package uk.tvidal.data

import uk.tvidal.data.codec.ValueType
import uk.tvidal.data.codec.returnValueType
import uk.tvidal.data.logging.KLogging
import uk.tvidal.data.schema.SchemaField
import uk.tvidal.data.schema.SchemaTable
import uk.tvidal.data.schema.foreignKeys
import uk.tvidal.data.schema.primaryKey
import java.math.BigDecimal
import javax.persistence.Column
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.primaryConstructor

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

  fun <T : Any> keyType(table: KClass<*>): ValueType<*, T>? = table.keyField?.let {
    fieldType(it as KProperty<T>)
  }

  fun <T : Any> valueType(type: KClass<T>, column: Column? = null): ValueType<*, T>? = when {
    type.java.isEnum -> enumType(type as KClass<out Enum<*>>, column)
    type.isSubclassOf(CharSequence::class) -> string(column)
    else -> ValueType.of(type) ?: when {
      type.isSubclassOf(Number::class) -> decimal(column)
      else -> null.also { _ ->
        warn { "Unable to find a suitable ValueType for $type" }
      }
    }
  } as? ValueType<*, T>

  fun <T : Any> fields(type: KClass<T>): Collection<SchemaField<*>> {
    val fields = type.fields
      .associateBy { it.name }

    val parameters = type.primaryConstructor?.parameters ?: emptyList()
    val parameterNames = parameters
      .map { it.name }
      .toSet()

    val allFields = parameters.mapNotNull {
      fields[it.name]
    } + fields.values.filterNot {
      it.name in parameterNames
    }
    return allFields.map {
      schema(it)
    }
  }

  fun <E : Any> schema(type: KClass<E>) = SchemaTable(
    table = type.table,
    fields = fields(type),
    constraints = listOfNotNull(
      type.primaryKey
    ) + foreignKeys(
      type
    ),
  )

  fun <E : Any, T> schema(field: KProperty1<E, T>) = SchemaField(
    name = field.fieldName,
    type = requireNotNull(fieldType(field) ?: keyType(field.receiverType)) {
      "Unable to find a suitable ValueType for $field"
    },
    nullable = field.isNullable
  )

  companion object Constants : KLogging() {

    val Default = Config()

    private fun precision(column: Column?): Int? = column?.run {
      if (precision > 0) precision else null
    }
  }
}
