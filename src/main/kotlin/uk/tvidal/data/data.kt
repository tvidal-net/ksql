package uk.tvidal.data

import uk.tvidal.data.filter.SqlFilter
import uk.tvidal.data.filter.SqlFilterBuilder
import uk.tvidal.data.filter.SqlMultiFilter
import uk.tvidal.data.filter.SqlPropertyParamFilter
import java.lang.reflect.Field
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table
import kotlin.reflect.KCallable
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaField

internal typealias QueryBuilder<P> = StringBuilder.(MutableCollection<in P>) -> Unit

typealias WhereClauseBuilder<E> = SqlFilterBuilder<E>.(KClass<out E>) -> Unit

val <E : Any> KClass<out E>.keyFilter: SqlFilter
  get() = equalsFilter(keyFields)

val RandomUUID: UUID
  get() = UUID.randomUUID()

val EmptyUUID = UUID(0, 0)

val Today: LocalDate
  get() = LocalDate.now()

val Now: LocalDateTime
  get() = LocalDateTime.now()

private fun Table?.tableName(fallback: String) = TableName(
  name = this?.name?.ifBlank { null } ?: fallback,
  schema = this?.schema?.ifBlank { null } ?: this?.catalog?.ifBlank { null },
)

private fun Entity?.tableName(fallback: String) =
  this?.name?.ifBlank { null } ?: fallback

internal val KClass<*>.tableName: TableName
  get() = findAnnotation<Table>().tableName(
    findAnnotation<Entity>().tableName(simpleName!!)
  )

@Suppress("UNCHECKED_CAST")
internal fun <T : Any> KCallable<T?>.returnTypeClass(): KClass<out T> =
  returnType.classifier as? KClass<out T> ?: returnType as KClass<out T>

private inline fun <reified T : Annotation> Field?.findAnnotation(): T? =
  this?.getAnnotation<T>(T::class.java)

private inline fun <reified T : Annotation> Field?.hasAnnotation(): Boolean =
  this?.findAnnotation<T>() != null

internal val KProperty<*>.column: Column?
  get() = findAnnotation() ?: javaField.findAnnotation()

internal fun Column?.fieldName(fallback: String) =
  this?.name?.ifBlank { null } ?: fallback

internal val Column.nullablePrecision: Int?
  get() = if (precision != 0) precision else null

internal val KProperty<*>.fieldName: String
  get() = column.fieldName(name)

internal val <E : Any> KClass<out E>.fields: Collection<KProperty1<out E, *>>
  get() = memberProperties

internal val KProperty<*>.isKeyField: Boolean
  get() = hasAnnotation<Id>() || javaField.hasAnnotation<Id>()

internal val <E : Any> KClass<out E>.insertFields: Collection<KProperty1<out E, *>>
  get() = fields.filterNot { it.column?.insertable == false }

internal val <E : Any> KClass<out E>.updateFields: Collection<KProperty1<out E, *>>
  get() = fields.filterNot { it.isKeyField || it.column?.updatable == false }

internal val <E : Any> KClass<out E>.keyFields: Collection<KProperty1<out E, *>>
  get() = fields.filter(KProperty<*>::isKeyField)

internal fun equalsFilter(filterColumns: Collection<KProperty1<*, *>>): SqlFilter {
  if (filterColumns.isEmpty()) {
    throw IllegalArgumentException("filterColumns cannot be empty!")
  }
  val keyFilters = filterColumns.map { col ->
    SqlPropertyParamFilter.Equals(col)
  }
  return if (keyFilters.size > 1) {
    SqlMultiFilter.And(keyFilters)
  } else {
    keyFilters.single()
  }
}

inline fun <reified E : Any> whereClause(builder: WhereClauseBuilder<E>): SqlFilter =
  SqlFilterBuilder<E>()
    .apply { builder(E::class) }
    .build()

inline fun <reified E : Any> Repository<E>.where(builder: WhereClauseBuilder<E>) =
  select(whereClause(builder))
