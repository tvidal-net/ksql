package uk.tvidal.data.schema

import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*

abstract class DataType<T> {

  object Bit : DataType<Boolean>() {
    override fun toString() = "BIT"
  }

  object TinyInt : DataType<Byte>() {
    override fun toString() = "TINYINT"
  }

  object SmallInt : DataType<Short>() {
    override fun toString() = "SMALLINT"
  }

  object Integer : DataType<Int>() {
    override fun toString() = "INTEGER"
  }

  object BigInt : DataType<Long>() {
    override fun toString() = "BIGINT"
  }

  object Uuid : DataType<UUID>() {
    override fun toString() = "UUID"
  }

  object Date : DataType<LocalDate>() {
    override fun toString() = "DATE"
  }

  object Time : DataType<LocalTime>() {
    override fun toString() = "TIME"
  }

  object DateTime : DataType<LocalDateTime>() {
    override fun toString() = "TIMESTAMP"
  }

  object Text : DataType<String>() {
    override fun toString() = "TEXT"
  }

  data class VarChar(val size: Int) : DataType<String>() {
    override fun toString() = "VARCHAR($size)"
  }

  data class NVarChar(val size: Int) : DataType<String>() {
    override fun toString() = "NVARCHAR($size)"
  }

  data class Numeric(val size: Int, val precision: Int? = null) : DataType<BigDecimal>() {
    override fun toString() = "NUMERIC($size${precisionDef(precision)})"
  }

  data class Decimal(val size: Int, val precision: Int? = null) : DataType<BigDecimal>() {
    override fun toString() = "DECIMAL($size${precisionDef(precision)})"
  }

  companion object {
    fun precisionDef(precision: Int?) =
      if (precision == null) "" else ", $precision"
  }
}
