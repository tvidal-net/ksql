package uk.tvidal.data.sql

import uk.tvidal.data.Config
import uk.tvidal.data.NamingStrategy
import uk.tvidal.data.TableName
import uk.tvidal.data.codec.CodecFactory
import uk.tvidal.data.codec.ParamValueEncoder
import uk.tvidal.data.fieldName
import uk.tvidal.data.filter.SqlFilter
import uk.tvidal.data.filter.SqlMultiFilter
import uk.tvidal.data.filter.SqlPropertyFilter
import uk.tvidal.data.filter.SqlPropertyJoinFilter
import uk.tvidal.data.filter.SqlPropertyMultiValueFilter
import uk.tvidal.data.filter.SqlPropertyParamFilter
import uk.tvidal.data.filter.SqlPropertyValueFilter
import uk.tvidal.data.query.AggregateType
import uk.tvidal.data.query.EntityQuery
import uk.tvidal.data.query.QueryParam
import uk.tvidal.data.query.QueryParam.Constants.FIRST_PARAM
import uk.tvidal.data.query.SelectFrom
import uk.tvidal.data.query.SelectQuery
import uk.tvidal.data.query.SimpleQuery
import uk.tvidal.data.query.aggregateType
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1

@Suppress("UNCHECKED_CAST")
interface BaseDialect : QueryBuilder {

  val config: Config

  val codecs: CodecFactory
    get() = CodecFactory(config)

  val namingStrategy: NamingStrategy
    get() = config.namingStrategy

  fun Appendable.newLine(level: Int = 0) {
    if (config.pretty) {
      appendLine()
      indent(level)
    } else {
      space()
    }
  }

  fun <P : QueryParam> Appendable.where(
    params: MutableCollection<in P>,
    whereClause: SqlFilter?,
    indentLevel: Int = 0,
  ) {
    if (whereClause != null) {
      newLine(indentLevel)
      append("WHERE ")
      filter(params, whereClause)
    }
  }

  fun Appendable.groupBy(from: Collection<SelectFrom>, indentLevel: Int = 0) {
    newLine(indentLevel)
    append("GROUP BY ")
    from.forEachIndexed { j, it ->
      it.groupBy.forEachIndexed { i, field ->
        if (i > 0 || j > 0) {
          listSeparator()
        }
        aliasPrefix(it.alias)
        quotedName(field.name)
      }
    }
  }

  fun <P : QueryParam> Appendable.filter(
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

  fun Appendable.fieldFilter(
    params: MutableCollection<in QueryParam>,
    filter: SqlPropertyFilter<*>,
    alias: String? = null,
  ) {
    aliasPrefix(alias)
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

  fun Appendable.joinFilter(joinFilter: SqlPropertyJoinFilter<*>) {
    append(joinFilter.operator)
    aliasPrefix(joinFilter.alias)
    quotedName(joinFilter.target.fieldName)
  }

  fun Appendable.paramFilter(
    params: MutableCollection<in QueryParam>,
    paramFilter: SqlPropertyParamFilter<*>,
  ) {
    append(paramFilter.operator)
    fieldParam(params, paramFilter.property)
  }

  fun Appendable.valueFilter(
    params: MutableCollection<in QueryParam>,
    valueFilter: SqlPropertyValueFilter<*>
  ) {
    append(valueFilter.operator)
    valueParam(
      params,
      valueFilter.property.fieldName,
      codecs.encoder(valueFilter.property as KProperty<Any>)
    )
  }

  fun Appendable.betweenFilter(
    params: MutableCollection<in QueryParam>,
    betweenFilter: SqlPropertyMultiValueFilter.Between<*>
  ) {
    append(betweenFilter.operator)
    val encoder = codecs.encoder(betweenFilter.property as KProperty<Any>)
    repeat(betweenFilter.values.size) {
      if (it > 0) append(SqlFilter.AND)
      valueParam(params, "${betweenFilter.property.fieldName}_$it", encoder)
    }
  }

  fun Appendable.inFilter(
    params: MutableCollection<in QueryParam>,
    inFilter: SqlPropertyMultiValueFilter.In<*>
  ) {
    append(inFilter.operator)
    openBlock()
    val encoder = codecs.encoder(inFilter.property as KProperty<Any>)
    repeat(inFilter.values.size) {
      if (it > 0) listSeparator()
      valueParam(params, "${inFilter.property.fieldName}_$it", encoder)
    }
    closeBlock()
  }

  fun <E> Appendable.fieldParam(
    params: MutableCollection<in QueryParam>,
    property: KProperty1<E, *>
  ) {
    EntityQuery.Param(
      index = params.nextIndex,
      encoder = codecs.encoder(property as KProperty<Any>),
      property = property,
    ).also { newParam ->
      params.add(newParam)
      param(newParam)
    }
  }

  fun Appendable.valueParam(
    params: MutableCollection<in QueryParam>,
    name: String,
    encoder: ParamValueEncoder<Any>,
  ) {
    QueryParam(params.nextIndex, name, encoder).also { newParam ->
      params.add(newParam)
      param(newParam)
    }
  }

  fun Appendable.tableName(table: TableName, alias: String? = null) {
    table.schema?.let {
      quotedName(it)
      schemaSeparator()
    }
    quotedName(table.name)
    if (alias != table.name && alias != null) {
      append(" AS ")
      quotedName(alias)
    }
  }

  fun Appendable.select(selectFrom: Collection<SelectFrom>) {
    append("SELECT ")
    for ((i, from) in selectFrom.withIndex()) {
      if (i > 0) listSeparator()
      val alias = alias(from, selectFrom.size)
      for ((j, field) in from.fields.withIndex()) {
        if (j > 0) listSeparator()
        selectField(field, alias)
      }
    }
  }

  fun Appendable.fieldNames(
    fields: Collection<KProperty1<*, *>>,
    block: Boolean = true
  ) {
    quotedNames(
      fields.map { it.fieldName },
      block
    )
  }

  fun Appendable.selectField(field: KProperty<*>, alias: CharSequence?) {
    val aggregateType = field.aggregateType?.also {
      append(it.name)
      openBlock()
    }
    if (aggregateType == AggregateType.COUNT) {
      append("*")
      closeBlock()
    } else {
      aliasPrefix(alias)
      quotedName(field.fieldName)
      aggregateType?.run {
        closeBlock()
      }
    }
    if (alias != null || aggregateType == AggregateType.COUNT) {
      append(" AS ")
      openQuote()
      namingStrategy.appendName(this, field.fieldName, alias)
      closeQuote()
    }
  }

  fun Appendable.aliasPrefix(alias: CharSequence?, separator: Char = QueryBuilder.SCHEMA_SEP) {
    alias?.let {
      quotedName(it)
      append(separator)
    }
  }

  fun Appendable.quotedName(name: CharSequence, alias: CharSequence? = null) {
    openQuote()
    databaseName(name, alias)
    closeQuote()
  }

  fun Appendable.quotedNames(names: Collection<String>, block: Boolean = true) {
    if (block) openBlock()
    for ((i, name) in names.withIndex()) {
      if (i > 0) listSeparator()
      quotedName(name)
    }
    if (block) closeBlock()
  }

  fun Appendable.databaseName(name: CharSequence, alias: CharSequence? = null) {
    namingStrategy.appendName(this, name, alias)
  }

  fun sqlQuery(
    builder: Appendable.() -> Unit
  ) = buildString {
    builder()
  }.let {
    SimpleQuery(it)
  }

  fun paramQuery(
    builder: Appendable.(MutableCollection<QueryParam>) -> Unit
  ) = arrayListOf<QueryParam>().let { params ->
    SimpleQuery(
      sql = buildString {
        builder(params)
      },
      params = params,
    )
  }

  fun <E : Any> selectQuery(
    projection: KClass<E>,
    alias: String?,
    builder: Appendable.(MutableCollection<QueryParam>) -> Unit
  ) = arrayListOf<QueryParam>().let { params ->
    SelectQuery(
      decode = codecs.decoder(projection, alias),
      sql = buildString {
        builder(params)
      },
      params = params,
    )
  }

  fun <E> entityQuery(
    builder: Appendable.(MutableCollection<EntityQuery.Param<E>>) -> Unit
  ) = arrayListOf<EntityQuery.Param<E>>().let { params ->
    EntityQuery(
      sql = buildString {
        builder(params)
      },
      params = params
    )
  }

  private companion object {

    private val Collection<*>.nextIndex: Int
      get() = size + FIRST_PARAM
  }
}
