package uk.tvidal.data.codec

import uk.tvidal.data.returnValueType
import java.sql.PreparedStatement
import java.sql.ResultSet
import javax.persistence.Column
import kotlin.reflect.KCallable
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.findAnnotation

typealias SetParamValue<T> = PreparedStatement.(Int, T) -> Unit
typealias GetResultSetValue<T> = ResultSet.(String) -> T?

internal val KParameter.returnValueType: KClass<*>
  get() = type.classifier as KClass<*>

internal val KParameter.fieldName: String
  get() = findAnnotation<Column>()?.name?.ifBlank { null } ?: name!!

internal val KParameter.logMessage: String
  get() = "$name: ${returnValueType.simpleName}"

internal val KCallable<*>.logMessage: String
  get() = "${returnValueType.simpleName}(${parameters.joinToString { it.logMessage }})"
