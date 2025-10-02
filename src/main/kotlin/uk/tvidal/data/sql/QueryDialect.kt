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
    entity: KClass<out E>,
    updateFields: Collection<KProperty1<out E, *>> = entity.updateFields,
    keyFields: Collection<KProperty1<out E, *>> = entity.keyFields
  ): EntityQuery<E>

  fun <E : Any> delete(
    entity: KClass<out E>,
    keyFields: Collection<KProperty1<out E, *>> = entity.keyFields,
  ): EntityQuery<E>

  fun delete(
    entity: KClass<*>,
    whereClause: SqlFilter
  ): SimpleQuery

  fun <E : Any> update(
    entity: KClass<out E>,
    updateFields: Collection<KProperty1<out E, *>> = entity.updateFields,
    keyFields: Collection<KProperty1<out E, *>> = entity.keyFields,
  ): EntityQuery<E>

  fun <E : Any> insert(
    entity: KClass<out E>,
    insertFields: Collection<KProperty1<out E, *>> = entity.insertFields
  ): EntityQuery<E>
}
