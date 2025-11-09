package uk.tvidal.data.database

import uk.tvidal.data.RandomUUID
import java.util.UUID

fun main() {

  val db = H2DB.createDatabase(
    url = "jdbc:h2:mem:temp;DB_CLOSE_DELAY=-1"
  )

  db.create(
    Account::class,
    Transaction::class,
  )

  val accounts = db.repository<Account>()

  // insert root accounts
  val assetsId = UUID.fromString("AAAAAAAA-AAAA-AAAA-AAAA-AAAAAAAAAA")
  val liabilityId = RandomUUID
  val incomeId = RandomUUID
  val expensesId = RandomUUID
  accounts.insert(
    Account("Assets", id = assetsId),
    Account("Liability", id = liabilityId),
    Account("Income", id = incomeId),
    Account("Expenses", id = expensesId),
  )

  val assets = accounts[assetsId]!!
  val wallet = Account(
    name = "Wallet",
    currency = Currency.USD,
    parent = assets.id
  )

  val expenses = accounts[expensesId]!!
  val petrol = Account(
    name = "Petrol",
    currency = Currency.USD,
    parent = expenses.id
  )
  accounts.save(wallet, petrol)

  db.drop(
    Transaction::class,
    Account::class,
  )
}
