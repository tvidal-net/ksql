package uk.tvidal.data.codec

import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.FromStringDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer
import java.math.BigDecimal
import java.sql.Date
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Time
import java.sql.Timestamp
import java.sql.Types
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.UUID
import javax.persistence.Column
import kotlin.reflect.KCallable
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.findAnnotation

typealias SetParamValue<T> = PreparedStatement.(Int, T) -> Unit
typealias GetResultSetValue<T> = ResultSet.(String) -> T?

internal val KParameter.fieldName: String
  get() = findAnnotation<Column>()?.name?.ifBlank { null } ?: name!!

@JvmInline
private value class ResultSetDecoderImpl<T>(val getValue: ResultSet.(String) -> T) : ResultSetDecoder<T> {
  // just to sort the overload confusion of ResultSet::get*
  override fun getResultSetValue(rs: ResultSet, columnLabel: String): T? = getValue(rs, columnLabel)
}

private fun <T> decode(getValue: ResultSet.(String) -> T): ResultSetDecoder<T> =
  ResultSetDecoderImpl(getValue)

val LongDecoder = PrimitiveDecoder(ResultSet::getLong)

val IntDecoder = PrimitiveDecoder(ResultSet::getInt)

val ShortDecoder = PrimitiveDecoder(ResultSet::getShort)

val ByteDecoder = PrimitiveDecoder(ResultSet::getByte)

val DoubleDecoder = PrimitiveDecoder(ResultSet::getDouble)

val FloatDecoder = PrimitiveDecoder(ResultSet::getFloat)

val BigDecimalDecoder = decode(ResultSet::getBigDecimal)

val StringDecoder = decode(ResultSet::getNString)

val UuidDecoder = decode { getString(it)?.let(UUID::fromString) }

val RegexDecoder = decode { getString(it)?.let(::Regex) }

val LocalDateDecoder = decode { getDate(it)?.toLocalDate() }

val LocalTimeDecoder = decode { getTime(it)?.toLocalTime() }

val LocalDateTimeDecoder = decode { getTimestamp(it)?.toLocalDateTime() }

private val DecodeWith<*>.decoder: ResultSetDecoder<*>
  get() = value.createInstance()

private val KParameter.annotation: DecodeWith<*>?
  get() = findAnnotation() ?: type.findAnnotation()

private val KParameter.entity: KClass<*>
  get() = type.classifier as KClass<*>

internal val KParameter.resultSetDecoder: ResultSetDecoder<*>
  get() = annotation?.decoder ?: resultSetDecoder(entity)

private val KCallable<*>.annotation: DecodeWith<*>?
  get() = findAnnotation() ?: returnType.findAnnotation()

internal val KCallable<*>.entity: KClass<*>
  get() = returnType.classifier as KClass<*>

internal val KProperty<*>.resultSetDecoder: ResultSetDecoder<*>
  get() = annotation?.decoder ?: resultSetDecoder(entity)

@Suppress("UNCHECKED_CAST")
private fun resultSetDecoder(type: KClass<*>): ResultSetDecoder<Any?> = when (type) {
  String::class -> StringDecoder
  Long::class -> LongDecoder
  Int::class -> IntDecoder
  Short::class -> ShortDecoder
  Byte::class -> ByteDecoder
  Double::class -> DoubleDecoder
  Float::class -> FloatDecoder
  BigDecimal::class -> BigDecimalDecoder
  LocalDate::class -> LocalDateDecoder
  LocalTime::class -> LocalTimeDecoder
  LocalDateTime::class -> LocalDateTimeDecoder
  Regex::class -> RegexDecoder
  UUID::class -> UuidDecoder
  else -> when {
    type.java.isEnum -> EnumDecoder(type as KClass<Enum<*>>)
    else -> JsonDecoder(type)
  }
}

internal fun PreparedStatement.setParamValue(index: Int, value: Any?) = when (value) {
  null -> setNull(index, Types.NULL)
  is String -> setNString(index, value)
  is Long -> setLong(index, value)
  is Int -> setInt(index, value)
  is Short -> setShort(index, value)
  is Byte -> setByte(index, value)
  is BigDecimal -> setBigDecimal(index, value)
  is Double -> setDouble(index, value)
  is Float -> setFloat(index, value)
  is LocalTime -> setTime(index, Time.valueOf(value))
  is LocalDate -> setDate(index, Date.valueOf(value))
  is LocalDateTime -> setTimestamp(index, Timestamp.valueOf(value))
  is UUID, is Enum<*>, is Regex -> setString(index, value.toString())
  else -> setString(index, JsonDecoder.toString(value))
}

internal inline fun <reified T> SimpleModule.register(crossinline fromString: (String) -> T) {
  addSerializer(ToStringSerializer(T::class.java))
  addDeserializer(
    T::class.java,
    object : FromStringDeserializer<T>(T::class.java) {
      override fun _deserialize(value: String?, ctxt: DeserializationContext?): T? =
        value?.let(fromString)
    }
  )
}
