package uk.tvidal.data.sql

import jakarta.persistence.Id
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.tvidal.data.Config
import uk.tvidal.data.NamingStrategy
import uk.tvidal.data.where
import java.util.UUID

class SqlDialectIndentTest {

  private data class Person(
    val name: String,
    val age: Int,
    @Id val id: UUID
  )

  @Test
  fun defaultConfigIsPrettyWithTwoSpaceIndent() {
    assertThat(Config.Default.pretty).isTrue()
    assertThat(Config.Default.indentSize).isEqualTo(2)
  }

  @Test
  fun compactModeRendersOnASingleLine() {
    // insertInto/insertValues/where/deleteQuery are indentLevel-aware; update()'s own
    // "UPDATE table" -> "SET ..." line break is a literal appendLine()/indent() left
    // untouched by Phase A's scoped conversion, so it stays multi-line regardless of pretty.
    val dialect = SqlDialect(Config(namingStrategy = NamingStrategy.AsIs, pretty = false))
    val filter = where<Person> { Person::age.gt(10) }

    assertThat(dialect.insert(Person::class).sql).doesNotContain("\n")
    assertThat(dialect.delete(Person::class, filter).sql)
      .doesNotContain("\n")
      .isEqualTo("DELETE FROM Person WHERE age > ?")
  }

  @Test
  fun prettyModeHonoursIndentSize() {
    val dialect = SqlDialect(Config(namingStrategy = NamingStrategy.AsIs, pretty = true, indentSize = 4))

    val insertSql = dialect.insert(Person::class).sql
    assertThat(insertSql).contains("\n    Person")
    assertThat(insertSql).contains("\n    VALUES")

    val filter = where<Person> { Person::age.gt(10) }
    assertThat(dialect.delete(Person::class, filter).sql).contains("\nWHERE age > ?")
  }
}
