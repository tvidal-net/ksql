package uk.tvidal.data.schema

import uk.tvidal.data.codec.ValueType.Companion.DEFAULT_PRECISION
import uk.tvidal.data.codec.ValueType.Companion.DEFAULT_SCALE
import uk.tvidal.data.fieldName
import uk.tvidal.data.fields
import uk.tvidal.data.keyField
import uk.tvidal.data.keyFields
import uk.tvidal.data.returnValueType
import uk.tvidal.data.schema.Constraint.ForeignKey
import uk.tvidal.data.schema.Constraint.ForeignKeyReference
import uk.tvidal.data.schema.Constraint.PrimaryKey
import uk.tvidal.data.schema.Constraint.UniqueKey
import uk.tvidal.data.table
import javax.persistence.Column
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

@Target(AnnotationTarget.PROPERTY)
annotation class Decimal(
  val scale: Int = DEFAULT_SCALE,
  val precision: Int = DEFAULT_PRECISION
)

internal val Decimal.column: Column
  get() = Column(scale = scale, precision = precision)

val KProperty<*>.asc: FieldReference
  get() = FieldReference.Ascending(fieldName)

val KProperty<*>.desc: FieldReference
  get() = FieldReference.Descending(fieldName)

val KClass<*>.primaryKey: Constraint
  get() = PrimaryKey(keyFields.map { it.asc })

fun primaryKey(primaryKeyName: String? = null, vararg fields: String) =
  PrimaryKey(fields.map(FieldReference::asc), primaryKeyName)

fun primaryKey(primaryKeyName: String? = null, vararg fields: FieldReference) =
  PrimaryKey(fields.toList(), primaryKeyName)

fun unique(uniqueName: String? = null, vararg fields: String) =
  UniqueKey(fields.map(FieldReference::asc), uniqueName)

fun unique(uniqueName: String? = null, vararg fields: FieldReference) =
  UniqueKey(fields.toList(), uniqueName)

fun on(fieldName: String, referenceField: String = fieldName) =
  ForeignKeyReference(fieldName, referenceField)

fun <E : Any> foreignKeys(table: KClass<E>) = table.fields.mapNotNull { field ->
  val type = field.returnValueType
  type.keyField?.let { idField ->
    ForeignKey(
      table = type.table,
      references = listOf(
        on(field.fieldName, idField.fieldName)
      )
    )
  }
}
