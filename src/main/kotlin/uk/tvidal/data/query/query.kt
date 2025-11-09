package uk.tvidal.data.query

import uk.tvidal.data.fields
import uk.tvidal.data.filter.SqlFilter
import uk.tvidal.data.filter.SqlPropertyJoinFilter
import uk.tvidal.data.receiverType
import uk.tvidal.data.table
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

fun <T : Any> from(
  entity: KClass<T>,
  fields: Collection<KProperty1<T, *>> = entity.fields,
  alias: String? = null,
) = From.Entity(
  entity, fields, alias
)

fun <T : Any> join(
  entity: KClass<T>,
  type: From.Join.Type,
  on: SqlFilter,
  fields: Collection<KProperty1<T, *>> = entity.fields,
  alias: String? = null,
) = From.Join(
  from(entity, fields, alias),
  type, on,
)

fun <T : Any> innerJoin(
  entity: KClass<T>,
  on: SqlFilter,
  fields: Collection<KProperty1<T, *>> = entity.fields,
  alias: String = entity.table.name,
) = join(
  entity,
  From.Join.Type.Inner,
  on,
  fields,
  alias
)

infix fun <V> KProperty1<*, V>.eq(target: KProperty1<out Any, V>) = eq(target, null)

fun <V> KProperty1<*, V>.eq(target: KProperty1<out Any, V>, alias: String?) = SqlPropertyJoinFilter.Equals(
  property = this,
  target = target,
  alias = alias ?: target.receiverType.table.name
)
