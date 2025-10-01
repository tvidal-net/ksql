package uk.tvidal.data.schema

import uk.tvidal.data.fieldName
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

val KProperty<*>.asc: ColumnReference
  get() = ColumnReference.Ascending(fieldName)

val KProperty<*>.desc: ColumnReference
  get() = ColumnReference.Descending(fieldName)

val KClass<*>.primaryKey: Constraint.PrimaryKey?
  get() = Constraint.PrimaryKey(this)

fun primaryKey(primaryKeyName: String? = null, vararg columns: String): Constraint =
  Constraint.PrimaryKey(columns.map(ColumnReference::asc), primaryKeyName)

fun unique(uniqueName: String? = null, vararg columns: String): Constraint =
  Constraint.Unique(columns.map(ColumnReference::asc), uniqueName)

fun unique(uniqueName: String? = null, vararg columns: ColumnReference): Constraint =
  Constraint.Unique(columns.toList(), uniqueName)

fun index(indexName: String? = null, vararg columns: ColumnReference) =
  Index(columns.toList(), indexName)

fun index(indexName: String? = null, vararg columns: String) =
  Index(columns.map(ColumnReference::asc), indexName)
