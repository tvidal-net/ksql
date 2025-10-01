package uk.tvidal.data

import uk.tvidal.data.filter.*
import uk.tvidal.data.query.EntityQuery
import uk.tvidal.data.query.QueryParam
import uk.tvidal.data.query.SimpleQuery
import uk.tvidal.data.query.Statement.Companion.FIRST_PARAM
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1

open class Dialect(val namingStrategy: NamingStrategy = NamingStrategy.SnakeCase) {

  open fun select(
    entity: KClass<*>,
    whereClause: SqlFilter? = null
  ) = simpleQuery { params ->
    selectFrom(entity)
    where(params, whereClause)
  }

  open fun <E : Any> save(
    entity: KClass<out E>,
    updateFields: Collection<KProperty1<out E, *>> = entity.updateFields,
    keyFields: Collection<KProperty1<out E, *>> = entity.keyFields,
  ): EntityQuery<E> = throw NotImplementedError("saveQuery is not implemented for the default Dialect!")

  open fun <E : Any> delete(
    entity: KClass<out E>,
    keyFields: Collection<KProperty1<out E, *>> = entity.keyFields,
  ) = entityQuery<E> { params ->
    deleteQuery(params, entity, equalsFilter(keyFields))
  }

  open fun delete(entity: KClass<*>, whereClause: SqlFilter) = simpleQuery { params ->
    deleteQuery(params, entity, whereClause)
  }

  open fun <E : Any> update(
    entity: KClass<out E>,
    updateFields: Collection<KProperty1<out E, *>> = entity.updateFields,
    keyFields: Collection<KProperty1<out E, *>> = entity.keyFields,
  ) = entityQuery<E> { params ->
    update(entity)
    setFields(params, updateFields)
    where(params, equalsFilter(keyFields))
  }

  fun <E : Any> drop(entity: KClass<out E>, ifExists: Boolean = true) =
    drop(entity.tableName)

  open fun drop(table: TableName, ifExists: Boolean = true) = simpleQuery { _ ->
    dropTable(table, ifExists)
  }

  open fun <E : Any> insert(
    entity: KClass<out E>,
    insertFields: Collection<KProperty1<out E, *>> = entity.insertFields
  ) = entityQuery<E> { params ->
    insertInto(entity)
    insertFields(insertFields)
    insertValues(params, insertFields)
  }

  protected inline fun simpleQuery(
    builder: QueryBuilder<QueryParam.Value>
  ) = arrayListOf<QueryParam.Value>().let { params ->
    SimpleQuery(
      sql = buildString {
        builder(params)
      },
      parameters = params
    )
  }

  protected inline fun <E> entityQuery(
    builder: QueryBuilder<EntityQuery.Param<E>>
  ) = arrayListOf<EntityQuery.Param<E>>().let { params ->
    EntityQuery(
      sql = buildString {
        builder(params)
      },
      parameters = params
    )
  }

  private fun <E : Any, P : QueryParam> StringBuilder.deleteQuery(
    params: MutableCollection<in P>,
    entity: KClass<out E>,
    whereClause: SqlFilter
  ) {
    deleteFrom(entity)
    where(params, whereClause)
  }

  @Suppress("UNCHECKED_CAST")
  protected fun <E, P : QueryParam> StringBuilder.setFields(
    params: MutableCollection<in P>,
    fields: Collection<KProperty1<out E, *>>
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
  protected fun <E, P : QueryParam> StringBuilder.fieldParams(
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
  protected fun <P : QueryParam> StringBuilder.filter(params: MutableCollection<in P>, filter: SqlFilter) {
    when (filter) {
      is SqlMultiFilter.And, is SqlMultiFilter.Or -> {
        if (filter.operands.size > 1) {
          openBlock()
          for ((i, operand) in filter.operands.withIndex()) {
            if (i > 0) {
              append(filter.separator)
            }
            filter(params, operand)
          }
          closeBlock()
        } else {
          filter(params, filter.operands.single())
        }
      }

      is SqlPropertyFilter<*> -> fieldFilter(
        params = params as MutableCollection<QueryParam>,
        filter = filter
      )
    }
  }

  private fun StringBuilder.fieldFilter(
    params: MutableCollection<in QueryParam>,
    filter: SqlPropertyFilter<*>
  ) {
    fieldName(filter.property)
    when (filter) {
      is SqlPropertyFilter.IsNull -> isNull()
      is SqlPropertyFilter.IsNotNull -> isNotNull()
      is SqlPropertyParamFilter<*> -> paramFilter(params, filter)
      is SqlPropertyValueFilter<*> -> valueFilter(params, filter)
      is SqlPropertyMultiValueFilter.Between<*> -> betweenFilter(params, filter)
      is SqlPropertyMultiValueFilter.In<*> -> inFilter(params, filter)
    }
  }

  private fun StringBuilder.paramFilter(
    params: MutableCollection<in QueryParam>,
    paramFilter: SqlPropertyParamFilter<*>
  ) {
    append(paramFilter.operator)
    fieldParam(params, paramFilter.property)
  }

  private fun StringBuilder.valueFilter(
    params: MutableCollection<in QueryParam>,
    valueFilter: SqlPropertyValueFilter<*>
  ) {
    append(valueFilter.operator)
    valueParam(params, valueFilter.property.fieldName, valueFilter.value)
  }

  private fun StringBuilder.betweenFilter(
    params: MutableCollection<in QueryParam>,
    betweenFilter: SqlPropertyMultiValueFilter.Between<*>
  ) {
    append(betweenFilter.operator)
    for ((i, value) in betweenFilter.values.withIndex()) {
      if (i > 0) {
        append(SqlFilter.AND)
      }
      valueParam(params, "${betweenFilter.property.fieldName}_$i", value)
    }
  }

  private fun StringBuilder.inFilter(
    params: MutableCollection<in QueryParam>,
    inFilter: SqlPropertyMultiValueFilter.In<*>
  ) {
    append(inFilter.operator)
    openBlock()
    for ((i, value) in inFilter.values.withIndex()) {
      if (i > 0) {
        listSeparator()
      }
      valueParam(params, "${inFilter.property.fieldName}_$i", value)
    }
    closeBlock()
  }

  protected fun <E> StringBuilder.fieldParam(
    params: MutableCollection<in QueryParam>,
    field: KProperty1<in E, Any?>
  ) {
    EntityQuery.Param(params.nextIndex, field).also { newParam ->
      params.add(newParam)
      param(newParam)
    }
  }

  protected fun StringBuilder.valueParam(
    params: MutableCollection<in QueryParam>,
    name: String, value: Any?
  ) {
    QueryParam.Value(params.nextIndex, name, value).also { newParam ->
      params.add(newParam)
      param(newParam)
    }
  }

  protected fun StringBuilder.tableName(table: TableName) {
    val (name, schema) = table
    if (!schema.isNullOrBlank()) {
      quotedName(schema)
      schemaSeparator()
    }
    quotedName(name)
  }

  protected fun StringBuilder.fieldNames(entity: KClass<*>) {
    fieldNames(entity.fields)
  }

  protected fun StringBuilder.fieldNames(fields: Collection<KProperty1<*, *>>) {
    for ((i, field) in fields.withIndex()) {
      if (i > 0) {
        listSeparator()
      }
      fieldName(field)
    }
  }

  protected fun StringBuilder.fieldName(field: KProperty<*>) {
    quotedName(field.fieldName)
  }

  protected fun StringBuilder.quotedName(name: String) {
    openQuote()
    namingStrategy.databaseName(this, name)
    closeQuote()
  }

  protected open fun StringBuilder.selectFrom(entity: KClass<*>) {
    append("SELECT ")
    fieldNames(entity)
    from(entity)
  }

  protected open fun StringBuilder.insertInto(entity: KClass<*>) {
    append("INSERT INTO ")
    tableName(entity.tableName)
  }

  protected open fun <E> StringBuilder.insertFields(fields: Collection<KProperty1<out E, *>>) {
    space()
    openBlock()
    fieldNames(fields)
    closeBlock()
  }

  protected open fun <E, P : QueryParam> StringBuilder.insertValues(
    params: MutableCollection<in P>,
    insertFields: Collection<KProperty1<out E, *>>
  ) {
    append("\n\tVALUES ")
    fieldParams(params, insertFields)
  }

  protected open fun StringBuilder.update(entity: KClass<*>) {
    append("UPDATE ")
    tableName(entity.tableName)
  }

  protected open fun StringBuilder.deleteFrom(entity: KClass<*>) {
    append("DELETE FROM ")
    tableName(entity.tableName)
  }

  protected open fun StringBuilder.from(entity: KClass<*>) {
    append("\n\tFROM ")
    tableName(entity.tableName)
  }

  protected open fun <P : QueryParam> StringBuilder.where(params: MutableCollection<in P>, clause: SqlFilter?) {
    if (clause != null) {
      append("\n\tWHERE ")
      filter(params, clause)
    }
  }

  protected open fun StringBuilder.dropTable(table: TableName, ifExists: Boolean) {
    append("DROP TABLE ")
    if (ifExists) {
      append("IF EXISTS ")
    }
    tableName(table)
  }

  protected open fun StringBuilder.isNotNull() {
    append(" IS NOT NULL")
  }

  protected open fun StringBuilder.isNull() {
    append(" IS NULL")
  }

  protected open fun StringBuilder.param(param: QueryParam) {
    append(PARAM_CHAR)
  }

  protected open fun StringBuilder.space() {
    append(' ')
  }

  protected open fun StringBuilder.listSeparator() {
    append(',')
  }

  protected open fun StringBuilder.nameSeparator() {
    append('_')
  }

  protected open fun StringBuilder.schemaSeparator() {
    append('.')
  }

  protected open fun StringBuilder.openQuote() {}

  protected open fun StringBuilder.closeQuote() {}

  protected open fun StringBuilder.openBlock() {
    append('(')
  }

  protected open fun StringBuilder.closeBlock() {
    append(')')
  }

  companion object Constants {

    const val PARAM_CHAR = '?'

    private val Collection<*>.nextIndex: Int
      get() = size + FIRST_PARAM
  }
}
