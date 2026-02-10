package uk.tvidal.data.database

import org.assertj.core.api.Assertions.assertThat
import uk.tvidal.data.Database
import uk.tvidal.data.RandomUUID
import uk.tvidal.data.Today
import uk.tvidal.data.schema.Decimal
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
import javax.persistence.Id
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
  @Decimal val decimal: BigDecimal = numeric,
  @Id val id: UUID = RandomUUID,
)

fun testValueTypes(db: Database) {
  db.create(ValueTypes::class)

  val repo = db.repository<ValueTypes>()

  val saved = ValueTypes()
  repo.save(saved)
  assertThat(repo[saved.id]).isEqualTo(saved)

  val updateSaved = ValueTypes(id = saved.id)
  repo.save(updateSaved)
  assertThat(repo[saved.id]).isEqualTo(updateSaved)

  val inserted = ValueTypes()
  repo.insert(inserted)
  assertThat(repo[inserted.id]).isEqualTo(inserted)

  val updated = ValueTypes(id = inserted.id)
  repo.update(updated)
  assertThat(repo[updated.id]).isEqualTo(updated)

  repo.delete(saved, inserted)
  assertThat(repo[saved.id]).isNull()
  assertThat(repo[updated.id]).isNull()

  db.drop(ValueTypes::class)
}
