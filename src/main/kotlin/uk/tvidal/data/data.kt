package uk.tvidal.data

import uk.tvidal.data.filter.SqlFieldParamFilter
import uk.tvidal.data.filter.SqlFilter
import uk.tvidal.data.filter.SqlFilterBuilder
import uk.tvidal.data.filter.SqlMultiFilter
import uk.tvidal.data.model.keyColumns
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

internal typealias QueryBuilder<E> = StringBuilder.(MutableCollection<in E>) -> Unit

typealias WhereClauseBuilder<E> = SqlFilterBuilder<E>.(KClass<out E>) -> Unit

val <E : Any> KClass<out E>.keyFilter: SqlFilter
  get() = equalsFilter(keyColumns)

internal fun equalsFilter(filterFields: Collection<KProperty1<*, *>>): SqlFilter {
  if (filterFields.isEmpty()) {
    throw IllegalArgumentException("filterFields cannot be empty!")
  }
  val keyFilters = filterFields.map { field ->
    SqlFieldParamFilter.Equals(field)
  }
  return if (keyFilters.size > 1) {
    SqlMultiFilter.And(keyFilters)
  } else {
    keyFilters.single()
  }
}

inline fun <reified E : Any> sqlFilter(builder: WhereClauseBuilder<E>): SqlFilter =
  SqlFilterBuilder<E>()
    .apply { builder(E::class) }
    .build()

inline fun <reified E : Any> Repository<E>.where(builder: WhereClauseBuilder<E>) =
  select(sqlFilter(builder))
