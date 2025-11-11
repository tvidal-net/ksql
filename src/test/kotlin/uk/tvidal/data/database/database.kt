package uk.tvidal.data.database

import uk.tvidal.data.Database
import uk.tvidal.data.Now
import uk.tvidal.data.RandomUUID
import uk.tvidal.data.Today
import uk.tvidal.data.delete
import uk.tvidal.data.schema.Decimal
import uk.tvidal.data.where
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID
import javax.persistence.Id

enum class Currency {
  GBP, EUR, USD;
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

fun runTestSuite(db: Database) {
  db.create(
    Account::class,
    Transaction::class,
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
