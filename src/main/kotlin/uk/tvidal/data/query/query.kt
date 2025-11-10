package uk.tvidal.data.query

import uk.tvidal.data.fields
import uk.tvidal.data.filter.SqlFilter
import uk.tvidal.data.filter.SqlPropertyJoinFilter
import uk.tvidal.data.receiverType
import uk.tvidal.data.table
import uk.tvidal.data.tableName
import java.sql.PreparedStatement
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

fun <T : Any> from(
  table: KClass<T>,
  fields: Collection<KProperty1<T, *>> = table.fields,
  alias: String? = null,
) = From.Table(
  table, fields, alias
)

fun <T : Any> innerJoin(
  type: KClass<T>,
  on: SqlFilter,
  fields: Collection<KProperty1<T, *>> = type.fields,
  alias: String = type.tableName,
) = From.Join.Type.Inner(type, on, alias, fields)

infix fun <V> KProperty1<*, V>.eq(target: KProperty1<out Any, V>) = eq(target, null)

fun <V> KProperty1<*, V>.eq(target: KProperty1<out Any, V>, alias: String?) = SqlPropertyJoinFilter.Equals(
  property = this,
  target = target,
  alias = alias ?: target.receiverType.table.name
)

fun setParamValues(st: PreparedStatement, params: Iterable<QueryParam>, values: Iterable<Any?>) {
  params.zip(values) { param, value ->
    param.encoder.setParamValue(st, param.index, value)
  }
}

internal val Query.logMessage: String
  get() = "params=$params\n$sql"
