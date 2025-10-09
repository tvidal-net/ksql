package uk.tvidal.data.schema

import uk.tvidal.data.codec.DataType
import java.math.BigDecimal
import javax.persistence.Column
import kotlin.reflect.KClass

@Suppress("UNCHECKED_CAST")
data class SchemaConfig(
  val enumIgnoreCase: Boolean = true,
  val stringDataType: DataType<String, String> = DataType.NVarChar(DataType.STRING_LENGTH),
  val shortStringDataType: DataType<String, String> = DataType.VarChar(DataType.SHORT_STRING),
  val decimalDataType: DataType<BigDecimal, BigDecimal> = DataType.Decimal(DataType.DEFAULT_SCALE, DataType.DEFAULT_PRECISION),
) {

  fun enumType(valueType: KClass<*>, column: Column?) = DataType.EnumType(
    enumClass = valueType as KClass<out Enum<*>>,
    length = shortString(column).length,
    ignoreCase = enumIgnoreCase
  )

  fun string(column: Column?) = column
    ?.let { DataType.NVarChar(it.length) }
    ?: stringDataType

  fun shortString(column: Column?) = DataType.VarChar(
    length = column?.length ?: DataType.SHORT_STRING
  )

  fun decimal(column: Column?) = column
    ?.let { DataType.Decimal(it.scale, it.nullablePrecision) }
    ?: decimalDataType

  companion object Constants {
    val Default = SchemaConfig()

    private val Column.nullablePrecision: Int?
      get() = if (precision != 0) precision else null
  }
}
