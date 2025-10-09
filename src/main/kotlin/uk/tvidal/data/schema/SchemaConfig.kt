package uk.tvidal.data.schema

import uk.tvidal.data.codec.DataType
import java.math.BigDecimal
import javax.persistence.Column

data class SchemaConfig(
  val enumIgnoreCase: Boolean = true,
  val stringDataType: DataType<String, String> = DataType.NVarChar(DataType.STRING_LENGTH),
  val shortStringDataType: DataType<String, String> = DataType.VarChar(DataType.SHORT_STRING),
  val decimalDataType: DataType<BigDecimal, BigDecimal> = DataType.Decimal(DataType.DEFAULT_SCALE, DataType.DEFAULT_PRECISION),
) {

  fun string(column: Column? = null): DataType<String, String> =
    column?.let { DataType.NVarChar(it) } ?: stringDataType

  fun shortString(column: Column? = null): DataType<String, String> =
    column?.let { DataType.VarChar(it) } ?: shortStringDataType

  fun decimal(column: Column? = null): DataType<BigDecimal, BigDecimal> =
    column?.let { DataType.Decimal(it) } ?: decimalDataType

  companion object Constants {
    val Default = SchemaConfig()
  }
}
