package uk.tvidal.data.schema

import kotlin.reflect.KProperty

fun primaryKey(name: String? = null, vararg properties: KProperty<*>) =
  Constraint.PrimaryKey(columns(properties), name)

fun unique(name: String? = null, vararg properties: KProperty<*>) =
  Constraint.Unique(columns(properties), name)

fun index(name: String? = null, vararg properties: KProperty<*>) =
  Index(columns(properties), name)

fun desc(name: String): ColumnReference =
  ColumnReference.Descending(name)

fun desc(property: KProperty<*>) =
  desc(property.name)

fun desc(properties: Array<out KProperty<*>>): Collection<ColumnReference> =
  properties.map(::desc)

fun columns(properties: Array<out KProperty<*>>): Collection<ColumnReference> =
  properties.map(ColumnReference::invoke)
