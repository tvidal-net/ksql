package uk.tvidal.data

import uk.tvidal.data.filter.SqlFilter
import uk.tvidal.data.filter.SqlFilterBuilder
import uk.tvidal.data.filter.SqlMultiFilter
import uk.tvidal.data.filter.SqlPropertyParamFilter
import uk.tvidal.data.model.keyFields
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

internal typealias QueryBuilder<P> = StringBuilder.(MutableCollection<in P>) -> Unit

typealias WhereClauseBuilder<E> = SqlFilterBuilder<E>.(KClass<out E>) -> Unit

val <E : Any> KClass<out E>.keyFilter: SqlFilter
  get() = equalsFilter(keyFields)

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
