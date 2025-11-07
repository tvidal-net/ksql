package uk.tvidal.data.query

import uk.tvidal.data.fields
import uk.tvidal.data.filter.SqlFilter
import uk.tvidal.data.tableName
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

fun <T : Any> from(
  entity: KClass<T>,
  fields: Collection<KProperty1<T, *>> = entity.fields,
  alias: String = entity.tableName.name,
) = From.Entity(
  entity, fields, alias
)

fun <T : Any> join(
  entity: KClass<T>,
  type: From.Join.Type,
  on: SqlFilter,
  fields: Collection<KProperty1<T, *>> = entity.fields,
  alias: String = entity.tableName.name,
) = From.Join(
  from(entity, fields, alias),
  type, on,
)

fun <T : Any> innerJoin(
  entity: KClass<T>,
  on: SqlFilter,
  fields: Collection<KProperty1<T, *>> = entity.fields,
  alias: String = entity.tableName.name,
) = join(
  entity,
  From.Join.Type.Inner,
  on,
  fields,
  alias
)
