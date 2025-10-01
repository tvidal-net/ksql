package uk.tvidal.data.codec

import uk.tvidal.data.column
import uk.tvidal.data.nullablePrecision
import uk.tvidal.data.returnTypeClass
import uk.tvidal.data.schema.SchemaConfig
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.regex.Pattern
import javax.persistence.Column
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

abstract class DataType<T> {

  object Boolean : DataType<kotlin.Boolean>() {
    override fun toString() = "BOOL"
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

  object Double : DataType<kotlin.Double>() {
    override fun toString() = "DOUBLE PRECISION"
  }

  object Float : DataType<kotlin.Float>() {
    override fun toString() = "FLOAT"
  }

  object DateTime : DataType<LocalDateTime>() {
    override fun toString() = "TIMESTAMP"
  }

  object Date : DataType<LocalDate>() {
    override fun toString() = "DATE"
  }

  object Time : DataType<LocalTime>() {
    override fun toString() = "TIME"
  }

  object Text : DataType<String>() {
    override fun toString() = "TEXT"
  }

  object UUID : DataType<java.util.UUID>() {
    override fun toString() = "UUID"
  }

  data class VarChar(val size: Int) : DataType<String>() {
    constructor(column: Column) : this(column.length)

    override fun toString() = "VARCHAR($size)"
  }

  data class NVarChar(val size: Int) : DataType<String>() {
    constructor(column: Column) : this(column.length)

    override fun toString() = "NVARCHAR($size)"
  }

  data class Numeric(val scale: Int, val precision: Int? = null) : DataType<BigDecimal>() {
    constructor(column: Column) : this(column.scale, column.precision)

    override fun toString() = "NUMERIC($scale${precisionDef(precision)})"
  }

  data class Decimal(val scale: Int, val precision: Int? = null) : DataType<BigDecimal>() {
    constructor(column: Column) : this(column.scale, column.nullablePrecision)

    override fun toString() = "DECIMAL($scale${precisionDef(precision)})"
  }

  data class Json<T : Any>(val type: KClass<out T>) : DataType<T>() {
    override fun toString() = "JSON"
  }

  companion object {
    fun precisionDef(precision: Int?) =
      if (precision == null) "" else ", $precision"

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> from(property: KProperty<T?>, config: SchemaConfig = SchemaConfig.Default): DataType<T> {
      val type = property.returnTypeClass()
      return when (type) {
        // boolean
        kotlin.Boolean::class -> Boolean

        // integer types
        Byte::class -> TinyInt
        Short::class -> SmallInt
        Int::class -> Integer
        Long::class -> BigInt

        // floating point
        kotlin.Double::class -> Double
        kotlin.Float::class -> Float

        // date/time
        LocalDateTime::class,
        java.util.Date::class,
        java.sql.Timestamp::class -> DateTime

        LocalDate::class,
        java.sql.Date::class -> Date

        LocalTime::class,
        java.sql.Time::class -> Time

        // variable size
        BigDecimal::class -> config.decimal(property.column)
        String::class -> config.string(property.column)

        // other types
        Regex::class, Pattern::class -> Text
        java.util.UUID::class -> UUID

        else -> when {
          type.java.isEnum -> config.shortString(property.column)
          else -> Json(type)
        }
      } as DataType<T>
    }
  }
}
