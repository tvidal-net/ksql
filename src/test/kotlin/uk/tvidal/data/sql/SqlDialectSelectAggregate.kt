package uk.tvidal.data.sql

import org.junit.jupiter.api.Test
import uk.tvidal.data.TestDialect.assertSelect
import uk.tvidal.data.query.Count
import uk.tvidal.data.query.Max
import uk.tvidal.data.query.SelectFrom
import uk.tvidal.data.query.eq
import java.util.UUID
import javax.persistence.Id
import javax.persistence.Transient

class SqlDialectSelectAggregate {

  @Test
  fun simple() {
    val from = listOf(
      SelectFrom.Table(Table::class),
    )
    assertSelect { select(Table::class, null, from) }.isEqualTo(
      "SELECT COUNT(*) AS [count], [name] FROM [Table] GROUP BY [name]"
    )
  }

  @Test
  fun crossJoin() {
    val from = listOf(
      SelectFrom.Table(Table::class, "a"),
      SelectFrom.Join(
        SelectFrom.Table(Parent::class, "b"),
        SelectFrom.Join.Type.Cross,
        null
      ),
    )
    assertSelect { select(Table::class, null, from) }.isEqualTo(
      "SELECT COUNT(*) AS [a_count], [a].[name] AS [a_name], [b].[id] AS [b_id], [b].[name] AS [b_name], MAX([b].[size]) AS [b_size] " +
        "FROM [Table] AS [a] CROSS JOIN [Parent] AS [b] GROUP BY [a].[name], [b].[id], [b].[name]"
    )
  }

  @Test
  fun leftJoin() {
    val from = listOf(
      SelectFrom.Table(Table::class, "a"),
      SelectFrom.Join(
        SelectFrom.Table(Parent::class, "b"),
        SelectFrom.Join.Type.Left,
        Parent::id.eq(Child::id, "a")
      ),
    )
    assertSelect { select(Table::class, null, from) }.isEqualTo(
      "SELECT COUNT(*) AS [a_count], [a].[name] AS [a_name], [b].[id] AS [b_id], [b].[name] AS [b_name], MAX([b].[size]) AS [b_size] " +
        "FROM [Table] AS [a] LEFT OUTER JOIN [Parent] AS [b] ON [b].[id] = [a].[id] GROUP BY [a].[name], [b].[id], [b].[name]"
    )
  }

  private class Table(
    val name: String,
    @Count val count: Int,
  )

  private class Parent(
    val name: String,
    @Max val size: Int,
    @Id @Transient val id: UUID,
  )

  private class Child(
    val name: String,
    val parent: Parent?,
    @Id val id: UUID,
  )
}
