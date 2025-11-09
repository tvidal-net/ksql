package uk.tvidal.data.codec

import uk.tvidal.data.valueType
import java.sql.PreparedStatement
import java.sql.ResultSet
import javax.persistence.Column
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty
import kotlin.reflect.full.findAnnotation

typealias SetParamValue<T> = PreparedStatement.(Int, T) -> Unit
typealias GetResultSetValue<T> = ResultSet.(String) -> T?
typealias FieldDecoder<T> = (ResultSet) -> T?

internal val KParameter.valueType: KClass<*>
  get() = type.classifier as? KClass<*> ?: type as KClass<*>

internal val KParameter.fieldName: String
  get() = findAnnotation<Column>()?.name?.ifBlank { null } ?: name!!
