package uk.tvidal.data.database

import uk.tvidal.data.Now
import uk.tvidal.data.RandomUUID
import java.math.BigDecimal
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
  val creditAmount: Double,
  val creditAccount: Account,
  val debitAmount: Double,
  val debitAccount: Account,
  val updatedAt: LocalDateTime = Now,
  @Id val id: UUID = RandomUUID,
)
