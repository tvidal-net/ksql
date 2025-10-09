package uk.tvidal.data.sql

import uk.tvidal.data.filter.SqlFilter
import uk.tvidal.data.insertFields
import uk.tvidal.data.keyFields
import uk.tvidal.data.query.EntityQuery
import uk.tvidal.data.query.SimpleQuery
import uk.tvidal.data.updateFields
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

interface QueryDialect {

  fun select(
    entity: KClass<*>,
    whereClause: SqlFilter? = null
  ): SimpleQuery

  fun <E : Any> save(
    entity: KClass<E>,
    updateFields: Collection<KProperty1<E, *>> = entity.updateFields,
    keyFields: Collection<KProperty1<E, *>> = entity.keyFields
  ): EntityQuery<E>

  fun <E : Any> delete(
    entity: KClass<E>,
    keyFields: Collection<KProperty1<E, *>> = entity.keyFields,
  ): EntityQuery<E>

  fun delete(
    entity: KClass<*>,
    whereClause: SqlFilter
  ): SimpleQuery

  fun <E : Any> update(
    entity: KClass<E>,
    updateFields: Collection<KProperty1<E, *>> = entity.updateFields,
    keyFields: Collection<KProperty1<E, *>> = entity.keyFields,
  ): EntityQuery<E>

  fun <E : Any> insert(
    entity: KClass<E>,
    insertFields: Collection<KProperty1<E, *>> = entity.insertFields
  ): EntityQuery<E>
}
