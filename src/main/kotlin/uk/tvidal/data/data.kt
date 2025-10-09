package uk.tvidal.data

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import uk.tvidal.data.filter.SqlFilter
import uk.tvidal.data.filter.SqlFilterBuilder
import uk.tvidal.data.filter.SqlMultiFilter
import uk.tvidal.data.filter.SqlPropertyParamFilter
import uk.tvidal.data.logging.KLogger.Companion.loggerName
import java.lang.reflect.Field
import java.sql.Connection
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID
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

typealias WhereClauseBuilder<E> = SqlFilterBuilder<E>.(KClass<out E>) -> Unit

val RandomUUID: UUID
  get() = UUID.randomUUID()

val EmptyUUID = UUID(0, 0)

val Today: LocalDate
  get() = LocalDate.now()

val Now: LocalDateTime
  get() = LocalDateTime.now()

@Suppress("UNCHECKED_CAST")
internal fun <T : Any> KCallable<T?>.valueType(): KClass<out T> =
  returnType.classifier as? KClass<out T> ?: returnType as KClass<out T>

private inline fun <reified T : Annotation> Field?.findAnnotation(): T? =
  this?.getAnnotation<T>(T::class.java)

private inline fun <reified T : Annotation> Field?.hasAnnotation(): Boolean =
  this?.findAnnotation<T>() != null

internal fun Column?.fieldName(fallback: String) =
  this?.name?.ifBlank { null } ?: fallback

internal val KProperty<*>.column: Column?
  get() = findAnnotation() ?: javaField.findAnnotation()

internal val KProperty<*>.isNullable: Boolean
  get() = returnType.isMarkedNullable || column?.nullable ?: false

internal val KProperty<*>.isKeyField: Boolean
  get() = hasAnnotation<Id>() || javaField.hasAnnotation<Id>()

internal val KProperty<*>.isTransient: Boolean
  get() = hasAnnotation<Transient>() || javaField.hasAnnotation<Transient>()

internal val KProperty<*>.fieldName: String
  get() = column.fieldName(name)

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

internal val <E : Any> KClass<E>.fields: Collection<KProperty1<E, *>>
  get() = memberProperties.filterNot(KProperty<*>::isTransient)

internal val <E : Any> KClass<E>.insertFields: Collection<KProperty1<E, *>>
  get() = fields.filterNot { it.column?.insertable == false }

internal val <E : Any> KClass<E>.updateFields: Collection<KProperty1<E, *>>
  get() = fields.filterNot { it.isKeyField || it.column?.updatable == false }

internal val <E : Any> KClass<E>.keyFields: Collection<KProperty1<E, *>>
  get() = fields.filter(KProperty<*>::isKeyField)

internal val <E : Any> KClass<E>.keyFilter: SqlFilter
  get() = equalsFilter(keyFields)

internal fun equalsFilter(filterColumns: Collection<KProperty1<*, *>>): SqlFilter {
  require(filterColumns.isNotEmpty()) {
    "filterColumns cannot be empty!"
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

inline fun <reified E : Any> where(builder: WhereClauseBuilder<E>): SqlFilter =
  SqlFilterBuilder<E>()
    .apply { builder(E::class) }
    .build()

inline fun <reified E : Any> Repository<E>.where(builder: WhereClauseBuilder<E>) =
  select(uk.tvidal.data.where(builder))

inline fun Connection.execute(
  log: Logger = LoggerFactory.getLogger({}::class.java.loggerName),
  builder: () -> String
): Boolean = builder().let { sql ->
  prepareStatement(
    sql
  ).use { st ->
    st.execute().also { result ->
      log.info("executed: {}\n{}", result, sql)
    }
  }
}
