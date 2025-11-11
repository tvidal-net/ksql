package uk.tvidal.data.sql

import org.junit.jupiter.api.Test
import uk.tvidal.data.TestDialect.assertSelect
import uk.tvidal.data.query.SelectFrom
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
      SelectFrom.Table(
        Transaction::class
      ),
      SelectFrom.Join(
        from = SelectFrom.Table(Account::class),
        type = SelectFrom.Join.Type.Inner,
        on = Account::id eq Transaction::credit
      ),
    )
    assertSelect { select(Transaction::class, null, from) }.isEqualTo(
      "SELECT [Transaction].[credit] AS [Transaction_credit], [Transaction].[debit] AS [Transaction_debit], " +
        "[Transaction].[description] AS [Transaction_description], [Transaction].[id] AS [Transaction_id], [Account].[id] AS [Account_id], " +
        "[Account].[name] AS [Account_name] FROM [Transaction] INNER JOIN [Account] ON [Account].[id] = [Transaction].[credit]"
    )
  }

  @Test
  fun testDoubleJoin() {
    val from = from(Transaction::class)
    assertSelect { select(Transaction::class, null, from) }.isEqualTo(
      "SELECT [Transaction].[credit] AS [Transaction_credit], [Transaction].[debit] AS [Transaction_debit], [Transaction].[description] AS " +
        "[Transaction_description], [Transaction].[id] AS [Transaction_id], [credit].[id] AS [credit_id], [credit].[name] AS [credit_name], " +
        "[debit].[id] AS [debit_id], [debit].[name] AS [debit_name] FROM [Transaction] INNER JOIN [Account] AS [credit] " +
        "ON [credit].[id] = [Transaction].[credit] INNER JOIN [Account] AS [debit] ON [debit].[id] = [Transaction].[debit]"
    )
  }
}
