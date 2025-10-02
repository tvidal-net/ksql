package uk.tvidal.data

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.tvidal.data.codec.DataType
import uk.tvidal.data.schema.ColumnReference.Factory.asc
import uk.tvidal.data.schema.ColumnReference.Factory.desc
import uk.tvidal.data.schema.Constraint
import uk.tvidal.data.schema.Constraint.Factory.on
import uk.tvidal.data.schema.Constraint.Factory.primaryKey
import uk.tvidal.data.schema.Constraint.Factory.unique
import uk.tvidal.data.schema.Index
import uk.tvidal.data.schema.SchemaColumn
import uk.tvidal.data.schema.SchemaTable

class DialectSchemaTest {

  val dialect = TestDialect()

  val testTable = SchemaTable(
    name = TableName("table", "test"),
    columns = listOf(
      SchemaColumn("name", DataType.VarChar(20), false),
      SchemaColumn("id", DataType.UUID, false),
    ),
    constraints = listOf(
      primaryKey(null, "id"),
    )
  )

  @Test
  fun createTableIfNotExists() {
    assertThat(dialect.create(testTable, true).actual)
      .isEqualTo("CREATE TABLE IF NOT EXISTS test.table ( name VARCHAR(20) NOT NULL, id UUID NOT NULL, PRIMARY KEY (id) ); ")
  }

  @Test
  fun createTableWithNamedPrimaryKey() {
    assertThat(dialect.create(testTable, false).actual)
      .isEqualTo("CREATE TABLE test.table ( name VARCHAR(20) NOT NULL, id UUID NOT NULL, PRIMARY KEY (id) ); ")
  }

  @Test
  fun namedPrimaryKey() {
    assertThat(dialect.constraint(primaryKey("test_pk", "id", "type")))
      .isEqualTo(", CONSTRAINT test_pk PRIMARY KEY (id,type)")
  }

  @Test
  fun anonymousPrimaryKey() {
    assertThat(dialect.constraint(primaryKey(null, "id")))
      .isEqualTo(", PRIMARY KEY (id)")
  }

  @Test
  fun namedUnique() {
    assertThat(dialect.constraint(unique("test_uq", "id", "type")))
      .isEqualTo(", CONSTRAINT test_uq UNIQUE (id,type)")
  }

  @Test
  fun anonymousUnique() {
    assertThat(dialect.constraint(unique(null, "name")))
      .isEqualTo(", UNIQUE (name)")
  }

  @Test
  fun multipleConstraints() {
    val table = SchemaTable(
      name = testTable.name,
      columns = emptyList(),
      constraints = listOf(
        primaryKey(null, "id"),
        unique(null, desc("name")),
      )
    )
    assertThat(dialect.create(table, false).actual)
      .isEqualTo("CREATE TABLE test.table (, PRIMARY KEY (id), UNIQUE (name DESC) ); ")
  }

  @Test
  fun anonymousForeignKey() {
    val fk = Constraint.ForeignKey(
      table = testTable.name,
      references = listOf(on("id"))
    )
    assertThat(dialect.constraint(fk))
      .isEqualTo(", FOREIGN KEY (id) REFERENCES test.table (id)")
  }

  @Test
  fun namedForeignKeyWithDeleteAction() {
    val fk = Constraint.ForeignKey(
      table = testTable.name,
      name = "test_fk",
      deleteAction = Constraint.ForeignKeyAction.SetNull,
      references = listOf(on("id")),
    )
    assertThat(dialect.constraint(fk))
      .isEqualTo(", CONSTRAINT FOREIGN KEY test_fk (id) REFERENCES test.table (id) ON DELETE SET NULL")
  }

  @Test
  fun foreignKeyWithUpdateAction() {
    val fk = Constraint.ForeignKey(
      table = testTable.name,
      updateAction = Constraint.ForeignKeyAction.Cascade,
      references = listOf(on("id"), on("type")),
    )
    assertThat(dialect.constraint(fk))
      .isEqualTo(", FOREIGN KEY (id,type) REFERENCES test.table (id,type) ON UPDATE CASCADE")
  }

  @Test
  fun dropTableIfExists() {
    assertThat(dialect.drop(testTable.name, true).actual)
      .isEqualTo("DROP TABLE IF EXISTS test.table; ")
  }

  @Test
  fun anonymousIndex() {
    val index = Index(listOf(asc("id"), desc("name")))
    assertThat(dialect.create(testTable.name, index).actual)
      .isEqualTo("CREATE INDEX ON test.table (id,name DESC); ")
  }

  @Test
  fun namedIndex() {
    val index = Index(listOf(asc("id"), asc("name")), "test_idx")
    assertThat(dialect.create(testTable.name, index).actual)
      .isEqualTo("CREATE INDEX test_idx ON test.table (id,name); ")
  }

  @Test
  fun dropTable() {
    assertThat(dialect.drop(testTable.name, false).actual)
      .isEqualTo("DROP TABLE test.table; ")
  }
}
