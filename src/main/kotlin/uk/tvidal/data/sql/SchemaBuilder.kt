package uk.tvidal.data.sql

import uk.tvidal.data.codec.ValueType
import uk.tvidal.data.schema.Constraint
import uk.tvidal.data.schema.FieldReference
import uk.tvidal.data.schema.Index
import uk.tvidal.data.schema.SchemaField

interface SchemaBuilder : BaseDialect {

  fun Appendable.schemaConstraint(constraint: Constraint) {
    when (constraint) {
      is Constraint.PrimaryKey -> constraintKey(Constraint.ConstraintKeyType.PrimaryKey, constraint.index)
      is Constraint.UniqueKey -> constraintKey(Constraint.ConstraintKeyType.UniqueKey, constraint.index)
      is Constraint.ForeignKey -> foreignKey(constraint)
    }
  }

  fun Appendable.constraintKey(keyType: Constraint.ConstraintKeyType, index: Index) {
    if (index.name != null) {
      append("CONSTRAINT ")
      quotedName(index.name)
      space()
    }
    append(keyType.sql)
    space()
    fields(index.fields)
  }

  fun Appendable.foreignKey(foreignKey: Constraint.ForeignKey) {
    append("FOREIGN KEY ")
    if (foreignKey.name != null) {
      quotedName(foreignKey.name)
      space()
    }
    foreignKeyFields(foreignKey)
    foreignKeyReferences(foreignKey)
    foreignKeyDeleteAction(foreignKey.deleteAction)
    foreignKeyUpdateAction(foreignKey.updateAction)
  }

  fun Appendable.foreignKeyFields(foreignKey: Constraint.ForeignKey) {
    quotedNames(
      foreignKey.references
        .map(Constraint.ForeignKeyReference::fieldName)
    )
  }

  fun Appendable.foreignKeyReferences(foreignKey: Constraint.ForeignKey) {
    append(" REFERENCES ")
    tableName(foreignKey.table)
    space()
    quotedNames(
      foreignKey.references.map {
        it.referenceField
      }
    )
  }

  fun Appendable.foreignKeyDeleteAction(action: Constraint.ForeignKeyAction) {
    if (action != Constraint.ForeignKeyAction.Default) {
      append(" ON DELETE ")
      foreignKeyAction(action)
    }
  }

  fun Appendable.foreignKeyUpdateAction(action: Constraint.ForeignKeyAction) {
    if (action != Constraint.ForeignKeyAction.Default) {
      append(" ON UPDATE ")
      foreignKeyAction(action)
    }
  }

  fun Appendable.foreignKeyAction(action: Constraint.ForeignKeyAction) {
    append(action.sql)
  }

  fun Appendable.fields(fields: Collection<FieldReference>) {
    openBlock()
    for ((i, field) in fields.withIndex()) {
      if (i > 0) listSeparator()
      field(field)
    }
    closeBlock()
  }

  fun Appendable.field(field: FieldReference) {
    quotedName(field.name)
    if (field is FieldReference.Descending) append(" DESC")
  }

  fun Appendable.field(field: SchemaField<*>) {
    quotedName(field.name)
    space()
    dataType(field.type)
    notNull(!field.nullable)
  }

  fun Appendable.dataType(codec: ValueType<*, *>) {
    append(codec.sqlDataType)
  }
}
