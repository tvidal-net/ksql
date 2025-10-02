package uk.tvidal.data.database

import uk.tvidal.data.Now
import uk.tvidal.data.RandomUUID
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID
import javax.persistence.Id

enum class Currency {
  GBP, EUR, USD, BRL;
}

data class Account(
  val name: String,
  val alias: String? = null,
  val currency: Currency? = null,
  val summary: String? = null,
  val hidden: Boolean = false,
  val parent: UUID? = null,
  val updatedAt: LocalDateTime = Now,
  @Id val id: UUID = RandomUUID,
)

data class Tag(
  val name: String,
  val parent: Tag? = null,
  val hidden: Boolean = false,
  val updatedAt: LocalDateTime = Now,
  @Id val id: UUID = RandomUUID,
)

data class Transaction(
  val name: String,
  val date: LocalDate,
  val creditAmount: BigDecimal = BigDecimal.ZERO,
  val creditAccount: Account? = null,
  val debitAmount: BigDecimal = BigDecimal.ZERO,
  val debitAccount: Account? = null,
  val details: Map<String, String> = mapOf(),
  val summary: String? = null,
  val updatedAt: LocalDateTime = Now,
  @Id val id: UUID = RandomUUID,
)

data class TransactionTag(
  @Id val transaction: Transaction,
  @Id val tag: Tag,
  val updatedAt: LocalDateTime = Now,
)

fun main() {
  val db = H2DB.createDatabase("jdbc:h2:mem:temp")

  val createAccount = db.dialect.create(Account::class)
  println(createAccount)

  val createTag = db.dialect.create(Tag::class)
  println(createTag)

  val createTransaction = db.dialect.create(Transaction::class)
  println(createTransaction)

  val createTransactionTag = db.dialect.create(TransactionTag::class)
  println(createTransactionTag)
}
