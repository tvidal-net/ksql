package uk.tvidal.data.sql

import uk.tvidal.data.NamingStrategy
import uk.tvidal.data.NamingStrategy.Constants.NAME_SEP
import uk.tvidal.data.TableName
import uk.tvidal.data.codec.CodecFactory
import uk.tvidal.data.fieldName
import uk.tvidal.data.filter.SqlFilter
import uk.tvidal.data.filter.SqlMultiFilter
import uk.tvidal.data.filter.SqlPropertyFilter
import uk.tvidal.data.filter.SqlPropertyJoinFilter
import uk.tvidal.data.filter.SqlPropertyMultiValueFilter
import uk.tvidal.data.filter.SqlPropertyParamFilter
import uk.tvidal.data.filter.SqlPropertyValueFilter
import uk.tvidal.data.query.EntityQuery
import uk.tvidal.data.query.From
import uk.tvidal.data.query.QueryParam
import uk.tvidal.data.query.Statement.Companion.FIRST_PARAM
import kotlin.reflect.KProperty1

abstract class SqlQueryBuilder(val codecs: CodecFactory) {

  protected val namingStrategy: NamingStrategy
    get() = codecs.databaseName

  protected fun <P : QueryParam> Appendable.where(
    params: MutableCollection<in P>,
    whereClause: SqlFilter?
  ) {
    if (whereClause != null) {
      appendLine()
      indent()
      append("WHERE ")
      filter(params, whereClause)
    }
  }

  @Suppress("UNCHECKED_CAST")
  protected fun <E, P : QueryParam> Appendable.setFields(
    params: MutableCollection<in P>,
    fields: Collection<KProperty1<E, *>>
  ) {
    append("SET ")
    for ((i, field) in fields.withIndex()) {
      if (i > 0) {
        listSeparator()
      }
      fieldFilter(
        params = params as MutableCollection<QueryParam>,
        filter = SqlPropertyParamFilter.Equals(field),
      )
    }
  }

  @Suppress("UNCHECKED_CAST")
  protected fun <E, P : QueryParam> Appendable.fieldParams(
    params: MutableCollection<in P>,
    fields: Collection<KProperty1<in E, *>>
  ) {
    openBlock()
    for ((i, field) in fields.withIndex()) {
      if (i > 0) {
        listSeparator()
      }
      fieldParam(
        params = params as MutableCollection<QueryParam>,
        field = field
      )
    }
    closeBlock()
  }

  @Suppress("UNCHECKED_CAST")
  protected fun <P : QueryParam> Appendable.filter(
    params: MutableCollection<in P>,
    filter: SqlFilter,
    alias: String? = null,
  ) {
    when (filter) {
      is SqlMultiFilter -> {
        if (filter.operands.size > 1) {
          openBlock()
          for ((i, operand) in filter.operands.withIndex()) {
            if (i > 0) append(filter.separator)
            filter(params, operand)
          }
          closeBlock()
        } else {
          filter(params, filter.operands.single())
        }
      }

      is SqlPropertyFilter<*> -> fieldFilter(
        params = params as MutableCollection<QueryParam>,
        filter = filter,
        alias = alias,
      )
    }
  }

  private fun Appendable.fieldFilter(
    params: MutableCollection<in QueryParam>,
    filter: SqlPropertyFilter<*>,
    alias: String? = null,
  ) {
    alias(alias)
    quotedName(filter.property.fieldName)
    when (filter) {
      is SqlPropertyFilter.IsNull -> isNull()
      is SqlPropertyFilter.IsNotNull -> isNotNull()
      is SqlPropertyJoinFilter -> joinFilter(filter)
      is SqlPropertyParamFilter<*> -> paramFilter(params, filter)
      is SqlPropertyValueFilter<*> -> valueFilter(params, filter)
      is SqlPropertyMultiValueFilter.Between<*> -> betweenFilter(params, filter)
      is SqlPropertyMultiValueFilter.In<*> -> inFilter(params, filter)
    }
  }

  private fun Appendable.joinFilter(joinFilter: SqlPropertyJoinFilter<*>) {
    append(joinFilter.operator)
    alias(joinFilter.alias)
    quotedName(joinFilter.target.fieldName)
  }

  private fun Appendable.paramFilter(
    params: MutableCollection<in QueryParam>,
    paramFilter: SqlPropertyParamFilter<*>,
  ) {
    append(paramFilter.operator)
    fieldParam(params, paramFilter.property as KProperty1<*, *>)
  }

  private fun Appendable.valueFilter(
    params: MutableCollection<in QueryParam>,
    valueFilter: SqlPropertyValueFilter<*>
  ) {
    append(valueFilter.operator)
    valueParam(params, valueFilter.property.fieldName, valueFilter.value)
  }

  private fun Appendable.betweenFilter(
    params: MutableCollection<in QueryParam>,
    betweenFilter: SqlPropertyMultiValueFilter.Between<*>
  ) {
    append(betweenFilter.operator)
    for ((i, value) in betweenFilter.values.withIndex()) {
      if (i > 0) append(SqlFilter.AND)
      valueParam(params, "${betweenFilter.property.fieldName}_$i", value)
    }
  }

  private fun Appendable.inFilter(
    params: MutableCollection<in QueryParam>,
    inFilter: SqlPropertyMultiValueFilter.In<*>
  ) {
    append(inFilter.operator)
    openBlock()
    for ((i, value) in inFilter.values.withIndex()) {
      if (i > 0) listSeparator()
      valueParam(params, "${inFilter.property.fieldName}_$i", value)
    }
    closeBlock()
  }

  protected fun <E> Appendable.fieldParam(
    params: MutableCollection<in QueryParam>,
    field: KProperty1<in E, Any?>
  ) {
    EntityQuery.Param(params.nextIndex, field).also { newParam ->
      params.add(newParam)
      param(newParam)
    }
  }

  protected fun Appendable.valueParam(
    params: MutableCollection<in QueryParam>,
    name: String, value: Any?
  ) {
    QueryParam.Value(params.nextIndex, name, value).also { newParam ->
      params.add(newParam)
      param(newParam)
    }
  }

  protected fun Appendable.tableName(table: TableName, alias: String? = null) {
    val (name, schema) = table
    if (!schema.isNullOrBlank()) {
      quotedName(schema)
      schemaSeparator()
    }
    quotedName(name)
    if (alias != null && alias != name) {
      append(" AS ")
      quotedName(alias)
    }
  }

  protected fun Appendable.select(selectFrom: Collection<From>) {
    append("SELECT ")
    for ((i, from) in selectFrom.withIndex()) {
      if (i > 0) listSeparator()
      val alias = alias(from, selectFrom.size)
      for ((j, field) in from.fields.withIndex()) {
        if (j > 0) listSeparator()
        aliasName(field.fieldName, alias)
      }
    }
  }

  protected fun Appendable.fieldNames(
    fields: Collection<KProperty1<*, *>>,
    block: Boolean = true
  ) {
    quotedNames(
      fields.map { it.fieldName },
      block
    )
  }

  protected fun Appendable.quotedNames(names: Collection<String>, block: Boolean = true, alias: String? = null) {
    if (block) openBlock()
    for ((i, name) in names.withIndex()) {
      if (i > 0) listSeparator()
      aliasName(name, alias)
    }
    if (block) closeBlock()
  }

  protected open fun Appendable.append(join: From.Join.Type) {
    append(join.sql)
  }

  protected open fun Appendable.aliasName(name: String, alias: String? = null) {
    alias(alias)
    openQuote()
    alias?.let {
      namingStrategy.databaseName(this, it)
      nameSeparator()
    }
    namingStrategy.databaseName(this, name)
    closeQuote()
  }

  protected fun Appendable.alias(alias: String?, separator: Char = SCHEMA_SEP) {
    alias?.let {
      quotedName(it)
      append(separator)
    }
  }

  protected open fun Appendable.quotedName(name: String) {
    openQuote()
    namingStrategy.databaseName(this, name)
    closeQuote()
  }

  protected fun Appendable.ifExists(value: Boolean) {
    if (value) {
      append("IF EXISTS ")
    }
  }

  protected fun Appendable.ifNotExists(value: Boolean) {
    if (value) {
      append("IF NOT EXISTS ")
    }
  }

  protected fun Appendable.notNull(notNull: Boolean) {
    if (notNull) {
      append(" NOT NULL")
    }
  }

  protected fun Appendable.isNotNull() {
    append(" IS NOT NULL")
  }

  protected fun Appendable.isNull() {
    append(" IS NULL")
  }

  protected fun Appendable.param(param: QueryParam) {
    param(param.index, param.name)
  }

  protected open fun Appendable.param(index: Int, paramName: String) {
    append(PARAM_CHAR)
  }

  protected fun Appendable.indent(size: Int = 1) {
    repeat(size) {
      append("  ")
    }
  }

  protected fun Appendable.space() {
    append(' ')
  }

  protected fun Appendable.listSeparator() {
    append(',')
  }

  protected fun Appendable.nameSeparator() {
    append(NAME_SEP)
  }

  protected fun Appendable.schemaSeparator() {
    append(SCHEMA_SEP)
  }

  protected fun Appendable.terminate() {
    append(';')
  }

  protected fun Appendable.openBlock() {
    append('(')
  }

  protected fun Appendable.closeBlock() {
    append(')')
  }

  protected open fun Appendable.openQuote() {}

  protected open fun Appendable.closeQuote() {}

  companion object Constants {

    const val PARAM_CHAR = '?'
    const val SCHEMA_SEP = '.'

    private val Collection<*>.nextIndex: Int
      get() = size + FIRST_PARAM

    internal fun alias(from: From, count: Int): String? =
      from.alias ?: if (count == 1) null else from.name
  }
}
