package uk.tvidal.data.schema

import uk.tvidal.data.TableName
import uk.tvidal.data.fieldName
import uk.tvidal.data.fields
import uk.tvidal.data.keyField
import uk.tvidal.data.table
import uk.tvidal.data.returnValueType
import kotlin.reflect.KClass

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

  companion object Factory {

    fun primaryKey(primaryKeyName: String? = null, vararg fields: String): Constraint =
      PrimaryKey(fields.map(FieldReference::asc), primaryKeyName)

    fun primaryKey(primaryKeyName: String? = null, vararg fields: FieldReference): Constraint =
      PrimaryKey(fields.toList(), primaryKeyName)

    fun unique(uniqueName: String? = null, vararg fields: String): Constraint =
      UniqueKey(fields.map(FieldReference::asc), uniqueName)

    fun unique(uniqueName: String? = null, vararg fields: FieldReference): Constraint =
      UniqueKey(fields.toList(), uniqueName)

    fun on(fieldName: String, referenceField: String = fieldName) =
      ForeignKeyReference(fieldName, referenceField)

    fun <E : Any> foreignKeys(entity: KClass<E>) = entity.fields.mapNotNull { field ->
      val table = field.returnValueType
      table.keyField?.let { idField ->
        ForeignKey(
          table = table.table,
          references = listOf(
            on(
              fieldName = field.fieldName,
              referenceField = idField.fieldName
            )
          )
        )
      }
    }
  }
}
