package uk.tvidal.data

import org.junit.jupiter.api.Test
import uk.tvidal.data.TestDialect.SqlAssertions.assertQuery
import uk.tvidal.data.TestDialect.SqlAssertions.assertThat
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
import javax.persistence.Id

class DialectSchemaTest {

  @Test
  fun createTableIfNotExists() {
    data class Person(val name: String, @Id val id: Int)
    assertQuery { create(Person::class) }
      .isEqualTo("CREATE TABLE IF NOT EXISTS Person ( id INTEGER NOT NULL, name NVARCHAR(255) NOT NULL, PRIMARY KEY (id));")
  }

  @Test
  fun createTable() {
    assertQuery { create(TestTable, false) }
      .isEqualTo("CREATE TABLE $TABLE ( $NAME, $ID, $PK, $UQ);")
  }

  @Test
  fun dropTableIfExists() {
    assertQuery { drop(tableName) }
      .isEqualTo("DROP TABLE IF EXISTS $TABLE")
  }

  @Test
  fun dropTable() {
    assertQuery { drop(tableName, false) }
      .isEqualTo("DROP TABLE $TABLE")
  }

  @Test
  fun namedPrimaryKey() {
    val pk = primaryKey("test_pk", "id", "type")
    assertThat { constraint(pk) }
      .isEqualTo("CONSTRAINT test_pk PRIMARY KEY (id,type)")
  }

  @Test
  fun anonymousPrimaryKey() {
    assertThat { constraint(TestTable.primaryKey) }
      .isEqualTo("PRIMARY KEY (id)")
  }

  @Test
  fun namedUnique() {
    val uq = unique("test_uq", "id", "type")
    assertThat { constraint(uq) }
      .isEqualTo("CONSTRAINT test_uq UNIQUE (id,type)")
  }

  @Test
  fun anonymousUnique() {
    val uq = unique(null, "name")
    assertThat { constraint(uq) }
      .isEqualTo("UNIQUE (name)")
  }

  @Test
  fun anonymousForeignKey() {
    val fk = Constraint.ForeignKey(
      table = tableName,
      references = listOf(on("id", "other"))
    )
    assertThat { constraint(fk) }
      .isEqualTo("FOREIGN KEY (id) REFERENCES $TABLE (other)")
  }

  @Test
  fun foreignKeyWithDeleteAction() {
    val fk = Constraint.ForeignKey(
      table = tableName,
      name = "test_fk",
      deleteAction = Constraint.ForeignKeyAction.SetNull,
      references = listOf(on("id")),
    )
    assertThat { constraint(fk) }
      .isEqualTo("FOREIGN KEY test_fk (id) REFERENCES $TABLE (id) ON DELETE SET NULL")
  }

  @Test
  fun foreignKeyWithUpdateAction() {
    val fk = Constraint.ForeignKey(
      table = tableName,
      updateAction = Constraint.ForeignKeyAction.Cascade,
      references = listOf(on("id"), on("type")),
    )
    assertThat { constraint(fk) }
      .isEqualTo("FOREIGN KEY (id,type) REFERENCES $TABLE (id,type) ON UPDATE CASCADE")
  }

  @Test
  fun anonymousIndex() {
    val index = Index(listOf(asc("id"), desc("name")))
    assertQuery { create(tableName, index) }
      .isEqualTo("CREATE INDEX ON $TABLE (id,name DESC)")
  }

  @Test
  fun namedIndex() {
    val index = Index(listOf(asc("id"), asc("name")), "test_idx")
    assertQuery { create(tableName, index) }
      .isEqualTo("CREATE INDEX test_idx ON $TABLE (id,name)")
  }

  companion object {
    val name = SchemaColumn("name", DataType.VarChar(20))
    val id = SchemaColumn("id", DataType.UUID, false)
    val tableName = TableName("table", "test")
    val pk = primaryKey(null, "id")
    val uq = unique(null, desc("name"), asc("id"))

    val TestTable = SchemaTable(
      name = tableName,
      columns = listOf(name, id),
      constraints = listOf(pk, uq)
    )

    const val TABLE = "test.table"
    const val NAME = "name VARCHAR(20)"
    const val ID = "id UUID NOT NULL"
    const val PK = "PRIMARY KEY (id)"
    const val UQ = "UNIQUE (name DESC,id)"
  }
}
