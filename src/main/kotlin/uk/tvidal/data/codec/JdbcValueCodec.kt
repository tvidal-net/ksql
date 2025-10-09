package uk.tvidal.data.codec

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.ObjectReader
import com.fasterxml.jackson.databind.ObjectWriter
import java.sql.Date
import java.sql.Time
import java.sql.Timestamp
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import kotlin.reflect.KClass

interface JdbcValueCodec<J, T> {

  fun decode(value: J): T

  fun encode(value: T): J

  class Primitive<T> : JdbcValueCodec<T, T> {
    override fun decode(value: T) = value
    override fun encode(value: T) = value
  }

  object InstantCodec : JdbcValueCodec<Timestamp, Instant> {
    override fun decode(value: Timestamp): Instant = value.toInstant()
    override fun encode(value: Instant): Timestamp = Timestamp.from(value)
  }

  object LocalDateTimeCodec : JdbcValueCodec<Timestamp, LocalDateTime> {
    override fun decode(value: Timestamp): LocalDateTime = value.toLocalDateTime()
    override fun encode(value: LocalDateTime): Timestamp = Timestamp.valueOf(value)
  }

  object LocalDateCodec : JdbcValueCodec<Date, LocalDate> {
    override fun decode(value: Date): LocalDate = value.toLocalDate()
    override fun encode(value: LocalDate): Date = Date.valueOf(value)
  }

  object LocalTimeCodec : JdbcValueCodec<Time, LocalTime> {
    override fun decode(value: Time): LocalTime = value.toLocalTime()
    override fun encode(value: LocalTime): Time = Time.valueOf(value)
  }

  class StringCodec<T>(val decoder: (String) -> T) : JdbcValueCodec<String, T> {
    override fun encode(value: T) = value.toString()
    override fun decode(value: String): T = decoder(value)
  }

  class Jackson<T : Any>(val reader: ObjectReader, val writer: ObjectWriter) : JdbcValueCodec<String, T> {
    constructor(codec: ObjectMapper, valueType: KClass<out T>) : this(
      reader = codec.readerFor(valueType.java),
      writer = codec.writerFor(valueType.java)
    )

    override fun decode(value: String): T = reader.readValue(value)
    override fun encode(value: T): String = writer.writeValueAsString(value)
  }
}
