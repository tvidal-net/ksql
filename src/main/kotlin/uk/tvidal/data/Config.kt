package uk.tvidal.data

import uk.tvidal.data.codec.ValueType
import uk.tvidal.data.codec.returnValueType
import uk.tvidal.data.logging.KLogging
import java.math.BigDecimal
import javax.persistence.Column
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.isSubclassOf

class Config(
  val namingStrategy: NamingStrategy = NamingStrategy.SnakeCase,
  val createIfNotExists: Boolean = true,
  val enumIgnoreCase: Boolean = true,
  val string: ValueType<String, String> = ValueType.NVarChar(ValueType.LENGTH),
  val decimal: ValueType<BigDecimal, BigDecimal> = ValueType.Decimal(ValueType.DEFAULT_SCALE, ValueType.DEFAULT_PRECISION),
) {

  internal fun <E : Enum<E>> enumType(type: KClass<E>, column: Column? = null) = ValueType.EnumType(
    enum = type,
    length = column?.length,
    ignoreCase = enumIgnoreCase
  )

  internal fun string(column: Column? = null) = column
    ?.let { ValueType.NVarChar(it.length) }
    ?: string

  internal fun decimal(column: Column? = null) = column
    ?.let { ValueType.Decimal(it.scale, precision(column)) }
    ?: decimal

  internal fun paramType(parameter: KParameter) = parameter.run {
    valueType(returnValueType, findAnnotation())
  }

  internal fun <T> fieldType(field: KProperty<T>) = field.run {
    valueType(returnValueType, column)
  }

  @Suppress("UNCHECKED_CAST")
  internal fun <T : Any> valueType(type: KClass<T>, column: Column? = null): ValueType<*, T>? = when {
    type.java.isEnum -> enumType(type as KClass<out Enum<*>>, column)
    type.isSubclassOf(CharSequence::class) -> string(column)
    else -> ValueType.of(type) ?: when {
      type.isSubclassOf(Number::class) -> decimal(column)
      else -> null
    }
  } as? ValueType<*, T>

  companion object Constants : KLogging() {

    val Default = Config()

    private fun precision(column: Column?): Int? = column?.run {
      if (precision > 0) precision else null
    }
  }
}
