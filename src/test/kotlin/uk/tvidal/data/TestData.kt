package uk.tvidal.data

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import uk.tvidal.data.database.MariaDB
import uk.tvidal.data.schema.SchemaTable
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID
import javax.persistence.Column
import javax.persistence.Id

val mapper = jacksonObjectMapper()

enum class Currency { GBP, EUR, USD }

val allTables = listOf(
  Account::class,
  Transaction::class,
  Tag::class,
  TransactionTag::class,
)

data class Account(
  val name: String,
  @Column(length = 3)
  val currency: Currency? = null,
  val parent: Account? = null,
  val updatedAt: LocalDateTime = Now,
  @Id val id: UUID = RandomUUID,
)

data class Transaction(
  val name: String,
  val creditAmount: BigDecimal,
  val creditAccount: Account,
  val debitAmount: BigDecimal,
  val debitAccount: Account,
  val updatedAt: LocalDateTime = Now,
  @Id val id: UUID = RandomUUID,
)

data class Tag(
  val name: String,
  val updatedAt: LocalDateTime = Now,
  @Id val id: UUID = RandomUUID,
)

data class TransactionTag(
  @Id val transaction: Transaction,
  @Id val tag: Tag,
)

fun main() {
  allTables.forEach { table ->
    val schema = SchemaTable.from(table)
    val query = MariaDB.Default.create(schema)
    println(query)
  }
}
