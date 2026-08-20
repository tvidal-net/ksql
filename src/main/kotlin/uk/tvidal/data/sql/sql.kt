package uk.tvidal.data.sql

import uk.tvidal.data.column
import uk.tvidal.data.fields
import uk.tvidal.data.isKeyField
import uk.tvidal.data.query.SelectFrom
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

internal val <E : Any> KClass<E>.insertFields: Collection<KProperty1<E, *>>
  get() = fields.filterNot { it.column?.insertable == false }

internal val <E : Any> KClass<E>.updateFields: Collection<KProperty1<E, *>>
  get() = fields.filterNot { it.isKeyField || it.column?.updatable == false }

fun alias(from: Collection<SelectFrom>) = from
  .first { it is SelectFrom.Table<*> }
  .let { alias(it, from.size) }

fun alias(from: SelectFrom, count: Int = Int.MAX_VALUE): String? =
  from.alias ?: if (count == 1) null else from.name
