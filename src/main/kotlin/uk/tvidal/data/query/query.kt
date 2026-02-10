package uk.tvidal.data.query

import uk.tvidal.data.fieldName
import uk.tvidal.data.fields
import uk.tvidal.data.filter.SqlPropertyJoinFilter
import uk.tvidal.data.isNullable
import uk.tvidal.data.keyField
import uk.tvidal.data.query.SelectFrom.Join
import uk.tvidal.data.query.SelectFrom.Table
import uk.tvidal.data.receiverType
import uk.tvidal.data.returnValueType
import uk.tvidal.data.table
import uk.tvidal.data.tableName
import java.sql.PreparedStatement
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

infix fun <V> KProperty1<*, V>.eq(target: KProperty1<out Any, V>) = eq(target, null)

fun <V> KProperty1<*, V>.eq(target: KProperty1<out Any, V>, alias: String?) = SqlPropertyJoinFilter.Equals(
  property = this,
  target = target,
  alias = alias ?: target.receiverType.table.name
)

internal val Query.description: String
  get() = "params=$params\n$sql"

fun setParamValues(statement: PreparedStatement, params: Iterable<QueryParam>, values: Iterable<Any?>) {
  params.zip(values) { param, value ->
    param.encoder.setParamValue(statement, param.index, value)
  }
}

fun from(table: KClass<*>, alias: String? = null): List<SelectFrom> = buildList {
  add(Table(table, alias))
  table.fields.mapNotNull { field ->
    field.returnValueType.keyField?.let { target ->
      val targetAlias = field.fieldName
      if (any { targetAlias == it.alias }) {
        error("Duplicate reference to $targetAlias")
      }
      Join(
        from = Table(field.returnValueType, targetAlias),
        type = if (field.isNullable) Join.Type.Left else Join.Type.Inner,
        on = target.eq(field, alias ?: table.tableName)
      )
    }
  }.forEach {
    add(it)
  }
}
