package uk.tvidal.data.codec

import uk.tvidal.data.isNullable
import uk.tvidal.data.returnValueType
import java.sql.PreparedStatement
import java.sql.ResultSet
import javax.persistence.Column
import kotlin.reflect.KCallable
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty
import kotlin.reflect.full.findAnnotation

typealias SetParamValue<T> = PreparedStatement.(Int, T) -> Unit
typealias GetResultSetValue<T> = ResultSet.(String) -> T?

internal val KParameter.returnValueType: KClass<*>
  get() = type.classifier as KClass<*>

internal val KParameter.fieldName: String
  get() = findAnnotation<Column>()?.name?.ifBlank { null } ?: name!!

internal val <E : Any> KClass<E>.noArgsConstructor: KFunction<E>?
  get() = constructors.firstOrNull { it.parameters.isEmpty() }

internal val KParameter.description: String
  get() = "$name: ${returnValueType.simpleName}${if (isOptional) "?" else ""}"

internal val KProperty<*>.description: String
  get() = "$name: ${returnValueType.simpleName}${if (isNullable) "?" else ""}"

internal val KCallable<*>.description: String
  get() = "${returnValueType.simpleName}(${parameters.joinToString { it.description }})"

internal fun asAlias(alias: String?) =
  alias?.let { " AS $it" } ?: ""
