package uk.tvidal.data.schema

import uk.tvidal.data.TableName

sealed interface Constraint {

  data class PrimaryKey(
    val index: Index
  ) : Constraint {
    constructor(
      columns: Collection<ColumnReference>,
      primaryKeyName: String? = null,
    ) : this(
      Index(columns, primaryKeyName)
    )
  }

  data class Unique(
    val index: Index
  ) : Constraint {
    constructor(
      columns: Collection<ColumnReference>,
      uniqueName: String? = null,
    ) : this(
      Index(columns, uniqueName)
    )
  }

  enum class ForeignKeyAction(internal val sql: String = "") {
    Default,
    NoAction("NO ACTION"),
    Cascade("CASCADE"),
    SetNull("SET NULL"),
    SetDefault("SET DEFAULT"),
  }

  data class ForeignKeyReference(
    val columnName: String,
    val referenceColumn: String,
  ) {
    override fun toString() = "$columnName = $referenceColumn"
  }

  data class ForeignKey(
    val table: TableName,
    val references: Collection<ForeignKeyReference>,
    val name: String? = null,
    val updateAction: ForeignKeyAction = ForeignKeyAction.Default,
    val deleteAction: ForeignKeyAction = ForeignKeyAction.Default,
  ) : Constraint {
    override fun toString() = "REFERENCES $table ON $references"
  }

  companion object Factory {

    fun primaryKey(primaryKeyName: String? = null, vararg columns: String): Constraint =
      PrimaryKey(columns.map(ColumnReference::asc), primaryKeyName)

    fun primaryKey(primaryKeyName: String? = null, vararg columns: ColumnReference): Constraint =
      PrimaryKey(columns.toList(), primaryKeyName)

    fun unique(uniqueName: String? = null, vararg columns: String): Constraint =
      Unique(columns.map(ColumnReference::asc), uniqueName)

    fun unique(uniqueName: String? = null, vararg columns: ColumnReference): Constraint =
      Unique(columns.toList(), uniqueName)

    fun on(columnName: String, referenceColumn: String = columnName) =
      ForeignKeyReference(columnName, referenceColumn)
  }
}
