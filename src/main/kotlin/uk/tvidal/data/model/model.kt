package uk.tvidal.data.model

import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.memberProperties

val RandomUUID: UUID
  get() = UUID.randomUUID()

val EmptyUUID = UUID(0, 0)

val Today: LocalDate
  get() = LocalDate.now()

val Now: LocalDateTime
  get() = LocalDateTime.now()

private fun Table?.tableName(entityName: String) = TableName(
  name = this?.name?.ifBlank { null } ?: entityName,
  schema = this?.schema?.ifBlank { null },
)

internal val KClass<*>.tableName: TableName
  get() = findAnnotation<Table>()
    .tableName(simpleName!!)

private fun Field?.fieldName(propertyName: String) =
  this?.name?.ifBlank { null } ?: propertyName

internal val KProperty<*>.fieldName: String
  get() = findAnnotation<Field>()
    .fieldName(name)

internal val <E : Any> KClass<out E>.fields: Collection<KProperty1<out E, *>>
  get() = memberProperties

internal val <E : Any> KClass<out E>.nonKeyFields: Collection<KProperty1<out E, *>>
  get() = fields.filterNot { it.hasAnnotation<Key>() }

internal val <E : Any> KClass<out E>.keyFields: Collection<KProperty1<out E, *>>
  get() = fields.filter { it.hasAnnotation<Key>() }
