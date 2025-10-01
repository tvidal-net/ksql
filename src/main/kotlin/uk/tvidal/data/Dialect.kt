package uk.tvidal.data

import uk.tvidal.data.filter.*
import uk.tvidal.data.model.*
import uk.tvidal.data.query.*
import uk.tvidal.data.query.Statement.Companion.FIRST_PARAM
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1

open class Dialect(val namingStrategy: NamingStrategy = NamingStrategy.SNAKE_CASE) {

  open fun select(table: KClass<*>, whereClause: SqlFilter? = null) = simpleQuery { params ->
    selectFrom(table)
    where(params, whereClause)
  }

  open fun <E : Any> save(
    table: KClass<out E>,
    updateColumns: Collection<KProperty1<out E, *>> = table.nonKeyColumns,
    keyColumns: Collection<KProperty1<out E, *>> = table.keyColumns
  ): EntityQuery<E> = throw NotImplementedError("saveQuery is not implemented for the default Dialect!")

  open fun <E : Any> delete(
    table: KClass<out E>,
    keyColumns: Collection<KProperty1<out E, *>> = table.keyColumns
  ) = tableQuery<E> { params ->
    deleteQuery(params, table, equalsFilter(keyColumns))
  }

  open fun delete(table: KClass<*>, whereClause: SqlFilter) = simpleQuery { params ->
    deleteQuery(params, table, whereClause)
  }

  open fun <E : Any> update(
    entity: KClass<out E>,
    updateColumns: Collection<KProperty1<out E, *>>,
    keyColumns: Collection<KProperty1<out E, *>>
  ) = tableQuery<E> { params ->
    update(entity)
    setColumns(params, updateColumns)
    where(params, equalsFilter(keyColumns))
  }

  private fun <E : Any, P : QueryParam> StringBuilder.deleteQuery(
    params: MutableCollection<in P>,
    entity: KClass<out E>,
    where: SqlFilter
  ) {
    deleteFrom(entity)
    where(params, where)
  }

  open fun <E : Any> insert(
    entity: KClass<out E>,
    insertFields: Collection<KProperty1<out E, *>>
  ) = tableQuery<E> { params ->
    insertInto(entity)
    insertFields(insertFields)
    insertValues(params, insertFields)
  }

  protected inline fun simpleQuery(
    builder: QueryBuilder<QueryParam.Value>
  ) = LinkedList<QueryParam.Value>().let { params ->
    SimpleQuery(
      sql = buildString {
        builder(params)
      },
      parameters = params
    )
  }

  protected inline fun <E> tableQuery(
    builder: QueryBuilder<EntityQuery.Param<E>>
  ) = LinkedList<EntityQuery.Param<E>>().let { params ->
    EntityQuery(
      sql = buildString {
        builder(params)
      },
      parameters = params
    )
  }

  @Suppress("UNCHECKED_CAST")
  protected fun <E, P : QueryParam> StringBuilder.setColumns(
    params: MutableCollection<in P>,
    columns: Collection<KProperty1<out E, *>>
  ) {
    append("SET ")
    for ((i, col) in columns.withIndex()) {
      if (i > 0) {
        listSeparator()
      }
      columnFilter(
        params = params as MutableCollection<QueryParam>,
        columnFilter = SqlPropertyParamFilter.Equals(col),
      )
    }
  }

  @Suppress("UNCHECKED_CAST")
  protected fun <E, P : QueryParam> StringBuilder.columnParams(
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

      is SqlPropertyFilter<*> -> columnFilter(
        params = params as MutableCollection<QueryParam>,
        columnFilter = filter
      )
    }
  }

  private fun StringBuilder.columnFilter(
    params: MutableCollection<in QueryParam>,
    columnFilter: SqlPropertyFilter<*>
  ) {
    columnName(columnFilter.property)
    when (columnFilter) {
      is SqlPropertyFilter.IsNull -> append(" IS NULL")
      is SqlPropertyFilter.IsNotNull -> append(" IS NOT NULL")
      is SqlPropertyParamFilter<*> -> paramFilter(params, columnFilter)
      is SqlPropertyValueFilter<*> -> valueFilter(params, columnFilter)
      is SqlPropertyMultiValueFilter.Between<*> -> betweenFilter(params, columnFilter)
      is SqlPropertyMultiValueFilter.In<*> -> inFilter(params, columnFilter)
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

  private fun <E> StringBuilder.fieldParam(
    params: MutableCollection<in QueryParam>,
    field: KProperty1<in E, Any?>
  ) {
    EntityQuery.Param(params.nextIndex, field).also { newParam ->
      params.add(newParam)
      param(newParam)
    }
  }

  private fun StringBuilder.valueParam(params: MutableCollection<in QueryParam>, name: String, value: Any?) {
    QueryParam.Value(params.nextIndex, name, value).also { newParam ->
      params.add(newParam)
      param(newParam)
    }
  }

  protected fun StringBuilder.tableName(entity: KClass<*>) {
    val (name, schema) = entity.tableName
    if (!schema.isNullOrBlank()) {
      name(schema)
      schemaSeparator()
    }
    name(name)
  }

  protected fun StringBuilder.fieldNames(entity: KClass<*>) {
    fieldNames(entity.fields)
  }

  protected fun StringBuilder.fieldNames(fields: Collection<KProperty1<*, *>>) {
    for ((i, property) in fields.withIndex()) {
      if (i > 0) {
        listSeparator()
      }
      columnName(property)
    }
  }

  protected fun StringBuilder.columnName(field: KProperty<*>) {
    name(field.fieldName)
  }

  protected fun StringBuilder.name(name: String) {
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
    tableName(entity)
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
    columnParams(params, insertFields)
  }

  protected open fun StringBuilder.update(entity: KClass<*>) {
    append("UPDATE ")
    tableName(entity)
  }

  protected open fun StringBuilder.deleteFrom(entity: KClass<*>) {
    append("DELETE FROM ")
    tableName(entity)
  }

  protected open fun StringBuilder.from(entity: KClass<*>) {
    append("\n\tFROM ")
    tableName(entity)
  }

  protected open fun <P : QueryParam> StringBuilder.where(params: MutableCollection<in P>, clause: SqlFilter?) {
    if (clause != null) {
      append("\n\tWHERE ")
      filter(params, clause)
    }
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
