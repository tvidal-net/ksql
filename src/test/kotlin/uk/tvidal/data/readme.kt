package uk.tvidal.data

import uk.tvidal.data.database.H2DB
import uk.tvidal.data.schema.Decimal
import uk.tvidal.data.schema.References
import uk.tvidal.data.schema.SchemaTable
import java.time.LocalDate
import java.util.UUID
import javax.persistence.Column
import javax.persistence.Id

enum class Currency {
  GBP, EUR, USD
}

data class Account(

  @Column(length = 128)
  var name: String,

  @References(Account::class)
  var parent: UUID? = null,

  @Id
  val id: UUID = RandomUUID
)

data class Transaction(

  @Column(length = 256)
  var description: String,

  var date: LocalDate,

  val credit: Leg,

  val debit: Leg,

  @Id
  val id: UUID = RandomUUID

) {
  data class Leg(

    @Decimal
    var amount: Double,

    @Column(length = 3)
    var currency: Currency,

    @References(Account::class)
    var account: UUID
  )
}

private var db = H2DB.createDatabase(
  url = "jdbc:h2:mem:account;DB_CLOSE_DELAY=-1"
)

fun main() {
  val tables = listOf(
    Account::class,
    Transaction::class
  )

  tables.map { type ->
    SchemaTable.from(type)
  }.forEach { schemaTable ->
    db.dialect.createTable(schemaTable, ifNotExists = true)
      .let { createTable -> println("create: ${createTable.sql}") }
    db.dialect.dropTable(schemaTable.table, ifExists = true)
      .let { dropTable -> println("drop: ${dropTable.sql}") }
  }
  db.create(tables)

  db.drop(tables.reversed())
}
