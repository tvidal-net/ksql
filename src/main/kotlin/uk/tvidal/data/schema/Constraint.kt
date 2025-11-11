package uk.tvidal.data.schema

import uk.tvidal.data.TableName

sealed interface Constraint {

  data class PrimaryKey(val index: Index) : Constraint {
    constructor(
      fields: Collection<FieldReference>,
      primaryKeyName: String? = null,
    ) : this(
      Index(fields, primaryKeyName)
    )
  }

  data class UniqueKey(val index: Index) : Constraint {
    constructor(
      fields: Collection<FieldReference>,
      uniqueName: String? = null,
    ) : this(
      Index(fields, uniqueName)
    )
  }

  enum class ConstraintKeyType(internal val sql: String) {
    PrimaryKey("PRIMARY KEY"),
    UniqueKey("UNIQUE");
  }

  enum class ForeignKeyAction(internal val sql: String = "") {
    Default,
    NoAction("NO ACTION"),
    Cascade("CASCADE"),
    SetNull("SET NULL"),
    SetDefault("SET DEFAULT"),
  }

  data class ForeignKeyReference(
    val fieldName: String,
    val referenceField: String,
  ) {
    override fun toString() = "$fieldName = $referenceField"
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
