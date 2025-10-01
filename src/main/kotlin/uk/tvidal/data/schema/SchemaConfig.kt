package uk.tvidal.data.schema

import uk.tvidal.data.codec.DataType
import java.math.BigDecimal
import javax.persistence.Column

data class SchemaConfig(
  val stringDataType: DataType<String> = DataType.NVarChar(STRING_SIZE),
  val shortStringDataType: DataType<String> = DataType.VarChar(SHORT_STRING_SIZE),
  val decimalDataType: DataType<BigDecimal> = DataType.Decimal(SCALE, PRECISION),
) {

  fun string(column: Column? = null): DataType<String> =
    column?.let { DataType.NVarChar(it) } ?: stringDataType

  fun shortString(column: Column? = null): DataType<String> =
    column?.let { DataType.VarChar(it) } ?: shortStringDataType

  fun decimal(column: Column? = null): DataType<BigDecimal> =
    column?.let { DataType.Decimal(it) } ?: decimalDataType

  companion object Constants {
    const val SHORT_STRING_SIZE = 0x40
    const val STRING_SIZE = 0xFF
    const val SCALE = 9
    const val PRECISION = 2

    val Default = SchemaConfig()
  }
}
