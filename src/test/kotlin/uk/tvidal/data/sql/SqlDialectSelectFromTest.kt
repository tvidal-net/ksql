package uk.tvidal.data.sql

import org.junit.jupiter.api.Test
import uk.tvidal.data.TestDialect.assertSelect
import uk.tvidal.data.query.From
import uk.tvidal.data.query.eq
import uk.tvidal.data.query.from
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
      "SELECT [id], [name] FROM [Account]"
    )
  }

  @Test
  fun testLookupJoin() {
    val from = listOf(
      From.Table(
        Transaction::class
      ),
      From.Join(
        from = From.Table(Account::class),
        type = From.Join.Type.Inner,
        on = Account::id eq Transaction::credit
      ),
    )
    assertSelect { select(Transaction::class, from, null) }.isEqualTo(
      "SELECT [Transaction].[Transaction_credit], [Transaction].[Transaction_debit], " +
        "[Transaction].[Transaction_description], [Transaction].[Transaction_id], [Account].[Account_name] " +
        "FROM [Transaction] INNER JOIN [Account] ON [Account].[id] = [Transaction].[credit]"
    )
  }

  @Test
  fun testDoubleJoin() {
    val from = from(
      table = Transaction::class,
      alias = "t",
    )
    assertSelect { select(Transaction::class, from) }.isEqualTo(
      "SELECT [t].[t_credit], [t].[t_debit], [t].[t_description], [t].[t_id], [ac].[ac_id], [ac].[ac_name], [ad].[ad_id], [ad].[ad_name] " +
        "FROM [Transaction] AS [t] INNER JOIN [Account] AS [ac] ON [ac].[id] = [t].[credit] " +
        "INNER JOIN [Account] AS [ad] ON [ad].[id] = [t].[debit]"
    )
  }
}
