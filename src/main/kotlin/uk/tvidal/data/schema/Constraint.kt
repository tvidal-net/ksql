package uk.tvidal.data.schema

import uk.tvidal.data.TableName

sealed interface Constraint {

  data class PrimaryKey(
    val index: Index
  ) : Constraint {
    constructor(
      columns: Collection<ColumnReference>,
      name: String? = null
    ) : this(
      Index(columns, name)
    )
  }

  data class Unique(
    val index: Index
  ) : Constraint {
    constructor(
      columns: Collection<ColumnReference>,
      name: String? = null
    ) : this(
      Index(columns, name)
    )
  }

  enum class ForeignKeyAction {
    Default,
    Nothing,
    Cascade,
    SetNull,
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
}
