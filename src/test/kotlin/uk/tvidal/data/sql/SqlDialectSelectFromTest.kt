package uk.tvidal.data.sql

import org.junit.jupiter.api.Test
import uk.tvidal.data.TestDialect.assertSelect
import uk.tvidal.data.query.eq
import uk.tvidal.data.query.from
import uk.tvidal.data.query.innerJoin
import java.util.UUID
import javax.persistence.Id

class SqlDialectSelectFromTest {

  private class Account(
    val name: String,
    @Id val id: UUID
  )

  private class Transaction(
    val description: String,
    val credit: Account,
    val debit: Account,
    @Id val id: UUID
  )

  @Test
  fun testSimpleSelectFrom() {
    assertSelect {
      select(Account::class)
    }.isEqualTo(
      "SELECT [id],[name] FROM [Account]"
    )
  }

  @Test
  fun testLookupJoin() {
    val from = listOf(
      from(
        entity = Transaction::class
      ),
      innerJoin(
        entity = Account::class,
        fields = listOf(Account::name),
        on = Account::id eq Transaction::credit
      ),
    )
    assertSelect { select(Transaction::class, from) }.isEqualTo(
      "SELECT [Transaction].[Transaction_credit],[Transaction].[Transaction_debit]," +
        "[Transaction].[Transaction_description],[Transaction].[Transaction_id],[Account].[Account_name] " +
        "FROM [Transaction] INNER JOIN [Account] ON [Account].[id] = [Transaction].[credit]"
    )
  }

  @Test
  fun testDoubleJoin() {
    val from = listOf(
      from(
        entity = Transaction::class,
        alias = "t",
      ),
      innerJoin(
        entity = Account::class,
        on = Account::id.eq(Transaction::credit, "t"),
        alias = "ac",
      ),
      innerJoin(
        entity = Account::class,
        on = Account::id.eq(Transaction::debit, "t"),
        alias = "ad"
      ),
    )
    assertSelect { select(Transaction::class, from) }.isEqualTo(
      "SELECT [t].[t_credit],[t].[t_debit],[t].[t_description],[t].[t_id],[ac].[ac_id],[ac].[ac_name],[ad].[ad_id],[ad].[ad_name] " +
        "FROM [Transaction] AS [t] INNER JOIN [Account] AS [ac] ON [ac].[id] = [t].[credit] " +
        "INNER JOIN [Account] AS [ad] ON [ad].[id] = [t].[debit]"
    )
  }
}
