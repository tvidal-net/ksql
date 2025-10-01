package uk.tvidal.data

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class DialectSchemaTest {

  val table = TableName("table", "test")

  val dialect = Dialect()

  @Test
  fun dropTableIfExists() {
    assertThat(dialect.drop(table, true).actual)
      .isEqualTo("DROP TABLE IF EXISTS test.table")
  }

  @Test
  fun dropTable() {
    assertThat(dialect.drop(table, false).actual)
      .isEqualTo("DROP TABLE test.table")
  }
}
