package uk.tvidal.data.database

import jakarta.persistence.Id
import uk.tvidal.data.RandomUUID
import uk.tvidal.data.Today
import uk.tvidal.data.codec.JdbcValueCodec
import uk.tvidal.data.codec.ValueType
import uk.tvidal.data.schema.Decimal
import uk.tvidal.data.schema.References
import java.math.BigDecimal
import java.sql.Time
import java.sql.Timestamp
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset.UTC
import java.time.temporal.ChronoUnit
import java.util.Date
import java.util.UUID
import java.util.concurrent.ThreadLocalRandom
import kotlin.math.absoluteValue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.toJavaDuration

private val random = ThreadLocalRandom.current()

enum class Currency {
  GBP, EUR, USD;

  companion object {
    val nextRandom: Currency
      get() = entries[random.nextInt().absoluteValue % entries.size]
  }
}

data class PersonDetails(
  val name: String,
  val age: Int,
)

data class Person(
  val details: PersonDetails?,
  @Id val id: UUID = RandomUUID
)

data class Parent(
  val name: String,
  @Id val id: UUID = RandomUUID
)

data class Child(
  val parent: Parent? = null,
  @Id val id: UUID = RandomUUID
)

data class Account(
  val name: String,
  @References(Account::class)
  val parent: UUID? = null,
  @Id val id: UUID = RandomUUID
)

@JvmInline
value class Amount(val value: Long) {
  override fun toString() = "$value"
}

data class ValueTypes(
  val boolean: Boolean = random.nextBoolean(),
  val int: Int = random.nextInt(),
  val byte: Byte = int.toByte(),
  val short: Short = int.toShort(),
  val long: Long = random.nextLong(),
  val double: Double = random.nextDouble(),
  val float: Float = double.toFloat(),
  val timestamp: Timestamp = Timestamp(long),
  val duration: Duration = short.toInt().milliseconds,
  val localDateTime: LocalDateTime = Today.atStartOfDay() + duration.toJavaDuration(),
  val instant: Instant = localDateTime.toInstant(UTC),
  val javaDate: Date = Date(instant.toEpochMilli()),
  val localDate: LocalDate = localDateTime.toLocalDate(),
  val sqlDate: java.sql.Date = java.sql.Date.valueOf(localDate),
  val localTime: LocalTime = localDateTime.toLocalTime().truncatedTo(ChronoUnit.SECONDS),
  val sqlTime: Time = Time.valueOf(localTime),
  val enum: Currency = Currency.nextRandom,
  val text: String = "text $long",
  val char: String = "char $long",
  val varChar: String = "varChar $long",
  val nChar: String = "nChar $long",
  val nVarChar: String = "nVarChar $long",
  val numeric: BigDecimal = BigDecimal("$short.00"),
  val amount: Amount = Amount(long),
  @Decimal val decimal: BigDecimal = numeric,
  @Id val id: UUID = RandomUUID,
) {
  companion object {
    val amountValueType = ValueType.BigDecimal(
      JdbcValueCodec.DecimalCodec {
        Amount(it.toLong())
      }
    )
  }
}
