package uk.tvidal.data.sql

import org.junit.jupiter.api.Test
import uk.tvidal.data.TestDialect.SqlAssertions.assertQuery
import uk.tvidal.data.filter.SqlPropertyJoinFilter
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
  fun testDoubleJoin() {
    val from = listOf(
      from(
        entity = Transaction::class,
        alias = "t",
      ),
      innerJoin(
        entity = Account::class,
        on = SqlPropertyJoinFilter.Equals(Account::id, "t", Transaction::credit),
        alias = "ac"
      ),
      innerJoin(
        entity = Account::class,
        on = SqlPropertyJoinFilter.Equals(Account::id, "t", Transaction::debit),
        alias = "ad"
      ),
    )
    assertQuery { select(from) }.isEqualTo(
      "SELECT [t].[t_credit],[t].[t_debit],[t].[t_description],[t].[t_id],[ac].[ac_id],[ac].[ac_name],[ad].[ad_id],[ad].[ad_name] " +
        "FROM [Transaction] AS [t] INNER JOIN [Account] AS [ac] ON [ac].[id] = [t].[credit] " +
        "INNER JOIN [Account] AS [ad] ON [ad].[id] = [t].[debit]"
    )
  }
}
