package uk.tvidal.data.filter

import uk.tvidal.data.sql.QueryBuilder.Constants.PARAM_CHAR
import kotlin.reflect.KProperty1

data class SqlPropertyParamFilter<out V>(
  override val property: KProperty1<*, V>,
  val operator: SqlOperator,
  override val alias: String? = null,
) : SqlPropertyFilter<V>() {

  override val values: Collection<V>
    get() = emptyList()

  override fun toString() = "${alias.dot}${property.name}$operator$PARAM_CHAR"
}
