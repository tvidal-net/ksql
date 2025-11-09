package uk.tvidal.data

import uk.tvidal.data.codec.DataType
import java.math.BigDecimal
import javax.persistence.Column
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

class Config(

  val namingStrategy: NamingStrategy = NamingStrategy.SnakeCase,

  val createIfNotExists: Boolean = true,

  val enumIgnoreCase: Boolean = true,

  val stringDataType: DataType<String, String> =
    DataType.NVarChar(DataType.LENGTH),

  val decimalDataType: DataType<BigDecimal, BigDecimal> =
    DataType.Decimal(DataType.DEFAULT_SCALE, DataType.DEFAULT_PRECISION),
) {

  @Suppress("UNCHECKED_CAST")
  fun enumType(valueType: KClass<*>, column: Column? = null) = DataType.EnumType(
    enumClass = valueType as KClass<out Enum<*>>,
    fieldLength = column?.length,
    ignoreCase = enumIgnoreCase
  )

  fun string(column: Column? = null) = column
    ?.let { DataType.NVarChar(it.length) }
    ?: stringDataType

  fun decimal(column: Column? = null) = column
    ?.let { DataType.Decimal(it.scale, it.nullablePrecision) }
    ?: decimalDataType

  fun <T : Any> dataType(type: KClass<T>, column: Column? = null) = when {
    type.java.isEnum ->
      enumType(type, column)

    type.isSubclassOf(CharSequence::class) ->
      string(column)

    else -> DataType.from(type) ?: when {
      type.isSubclassOf(Number::class) ->
        decimal(column)

      else -> null
    }
  }

  companion object Constants {

    val Default = Config()

    private val Column.nullablePrecision: Int?
      get() = if (precision == 0) null else precision
  }
}
