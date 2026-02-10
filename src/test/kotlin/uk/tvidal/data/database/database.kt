package uk.tvidal.data.database

import org.assertj.core.api.Assertions.assertThat
import uk.tvidal.data.Database
import uk.tvidal.data.Now
import uk.tvidal.data.RandomUUID
import uk.tvidal.data.Today
import uk.tvidal.data.delete
import uk.tvidal.data.schema.Decimal
import uk.tvidal.data.where
import java.math.BigDecimal
import java.sql.Time
import java.sql.Timestamp
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset.UTC
import java.util.Date
import java.util.UUID
import java.util.concurrent.ThreadLocalRandom
import javax.persistence.Id
import kotlin.math.absoluteValue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

private val random = ThreadLocalRandom.current()

enum class Currency {
  GBP, EUR, USD;

  companion object {
    val nextRandom: Currency
      get() = entries[random.nextInt().absoluteValue.rem(entries.size)]
  }
}

data class Account(
  val name: String,
  val currency: Currency? = null,
  val parent: UUID? = null,
  val hidden: Boolean = false,
  val updatedAt: LocalDateTime = Now,
  @Id val id: UUID = RandomUUID,
)

data class Transaction(
  val name: String,
  val date: LocalDate,
  @Decimal val creditAmount: Double,
  val creditAccount: Account,
  @Decimal val debitAmount: Double,
  val debitAccount: Account,
  val updatedAt: LocalDateTime = Now,
  @Id val id: UUID = RandomUUID,
)

data class ValueTypes(
  val booleanField: Boolean = random.nextBoolean(),
  val intField: Int = random.nextInt(),
  val byteField: Byte = intField.toByte(),
  val shortField: Short = intField.toShort(),
  val longField: Long = random.nextLong(),
  val doubleField: Double = random.nextDouble(),
  val floatField: Float = doubleField.toFloat(),
  val timestampField: Timestamp = Timestamp(longField),
  val instantField: Instant = Instant.ofEpochMilli(longField),
  val localDateTimeField: LocalDateTime = LocalDateTime.ofInstant(instantField, UTC),
  val dateField: Date = Date(longField),
  val localDateField: LocalDate = localDateTimeField.toLocalDate(),
  val sqlDateField: java.sql.Date = java.sql.Date.valueOf(localDateField),
  val localTimeField: LocalTime = localDateTimeField.toLocalTime(),
  val sqlTimeField: Time = Time.valueOf(localTimeField),
  val enumField: Currency = Currency.nextRandom,
  val durationField: Duration = longField.milliseconds,
  val textField: String = "$longField",
  val charField: String = "$longField",
  val varCharField: String = "$longField",
  val nCharField: String = "$longField",
  val nVarCharField: String = "$longField",
  val numericField: BigDecimal = BigDecimal(doubleField),
  @Decimal val decimalField: BigDecimal = BigDecimal(doubleField),
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

fun runTestSuite(db: Database) {
  db.create(
    Account::class,
    Transaction::class,
    ValueTypes::class,
  )

  val accounts = db.repository<Account>()

  // insert root accounts
  val assets = Account("Assets", id = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"))
  val liability = Account("Liability", id = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"))
  val income = Account("Income", id = UUID.fromString("11111111-1111-1111-1111-111111111111"))
  val expenses = Account("Expenses", id = UUID.fromString("eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee"))
  accounts += listOf(assets, liability, income, expenses)

  val wallet = Account(
    name = "Wallet",
    currency = Currency.USD,
    parent = assets.id
  )
  val petrol = Account(
    name = "Petrol",
    currency = Currency.USD,
    parent = expenses.id
  )
  accounts.insert(wallet, petrol)

  accounts.where {
    Account::id.inValues(
      accounts.map { it.id }
    )
  }.forEach {
    println(it)
  }

  db.repository<Transaction>().forEach {
    println(it)
  }

  accounts.update(
    liability.copy(updatedAt = Now)
  )

  accounts.delete(income)

  accounts.delete {
    Account::parent.isNull
  }

  val transactions = db.repository<Transaction>()

  val tx = Transaction(
    name = "New Transaction",
    date = Today,
    creditAmount = 3.14,
    creditAccount = petrol,
    debitAmount = 3.14,
    debitAccount = wallet,
  )

  transactions += tx

  transactions.forEach {
    println(it)
  }

  db.drop(
    Transaction::class,
    Account::class,
  )
}
