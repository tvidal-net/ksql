package uk.tvidal.data.schema

import uk.tvidal.data.fieldName
import uk.tvidal.data.keyFields
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

val KProperty<*>.asc: ColumnReference
  get() = ColumnReference.Ascending(fieldName)

val KProperty<*>.desc: ColumnReference
  get() = ColumnReference.Descending(fieldName)

val KClass<*>.primaryKey: Constraint
  get() = Constraint.PrimaryKey(keyFields.map(KProperty<*>::asc))
