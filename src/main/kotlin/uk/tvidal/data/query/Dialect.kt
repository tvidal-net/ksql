package uk.tvidal.data.query

import uk.tvidal.data.QueryBuilder
import uk.tvidal.data.equalsFilter
import uk.tvidal.data.filter.SqlFieldFilter
import uk.tvidal.data.filter.SqlFieldMultiValueFilter
import uk.tvidal.data.filter.SqlFieldParamFilter
import uk.tvidal.data.filter.SqlFieldValueFilter
import uk.tvidal.data.filter.SqlFilter
import uk.tvidal.data.filter.SqlMultiFilter
import uk.tvidal.data.model.fieldName
import uk.tvidal.data.model.fields
import uk.tvidal.data.model.tableName
import java.util.LinkedList
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1

open class Dialect(val namingStrategy: NamingStrategy = NamingStrategy.SNAKE_CASE) {

  open fun select(entity: KClass<*>, where: SqlFilter?) = query { params ->
    appendSelect()
    appendFieldNames(entity.fields)

    append(FROM)
    appendTableName(entity)

    if (where != null) {
      append(WHERE)
      appendFilter(params, where)
    }
  }

  open fun <E : Any> save(
    entity: KClass<out E>,
    updateFields: Collection<KProperty1<out E, *>>,
    keyFields: Collection<KProperty1<out E, *>>
  ): EntityQuery<E> = throw NotImplementedError("saveQuery is not implemented for the default Dialect!")

  open fun <E : Any> delete(entity: KClass<out E>, keyFields: Collection<KProperty1<out E, *>>) = entityQuery<E> { params ->
    val where = equalsFilter(keyFields)
    delete(params, entity, where)
  }

  open fun delete(entity: KClass<*>, where: SqlFilter) = query { params ->
    delete(params, entity, where)
  }

  open fun <E : Any> update(
    entity: KClass<out E>,
    updateFields: Collection<KProperty1<out E, *>>,
    keyFields: Collection<KProperty1<out E, *>>
  ) = entityQuery<E> { params ->
    appendUpdate()
    appendTableName(entity)

    append(SET)
    appendSetFields(params, updateFields)

    append(WHERE)
    appendFilter(params, equalsFilter(keyFields))
  }

  private fun <E : Any, P : QueryParameter> StringBuilder.delete(
    params: MutableCollection<in P>,
    entity: KClass<out E>,
    where: SqlFilter
  ) {
    appendDeleteFrom()
    appendTableName(entity)

    append(WHERE)
    appendFilter(params, where)
  }

  open fun <E : Any> insert(
    entity: KClass<out E>,
    insertFields: Collection<KProperty1<out E, *>>
  ) = entityQuery<E> { params ->
    appendInsertInto()
    appendTableName(entity)
    append(SPACE)

    openBlock()
    appendFieldNames(insertFields)
    closeBlock()

    append(VALUES)
    appendFieldParams(params, insertFields)
  }

  protected inline fun query(buildQuery: QueryBuilder<ParameterValue>) = LinkedList<ParameterValue>().let { params ->
    Query(
      sql = buildString {
        buildQuery(params)
      },
      parameters = params
    )
  }

  protected inline fun <E> entityQuery(
    buildQuery: QueryBuilder<ParameterProperty<E>>
  ) = LinkedList<ParameterProperty<E>>().let { params ->
    EntityQuery(
      sql = buildString {
        buildQuery(params)
      },
      parameters = params
    )
  }

  @Suppress("UNCHECKED_CAST")
  protected fun <E, P : QueryParameter> StringBuilder.appendSetFields(
    params: MutableCollection<in P>,
    fields: Collection<KProperty1<out E, *>>
  ) {
    for ((i, field) in fields.withIndex()) {
      if (i > 0) {
        appendListSeparator()
      }
      appendFieldFilter(
        params = params as MutableCollection<QueryParameter>,
        fieldFilter = SqlFieldParamFilter.Equals(field),
      )
    }
  }

  @Suppress("UNCHECKED_CAST")
  protected fun <E, P : QueryParameter> StringBuilder.appendFieldParams(
    params: MutableCollection<in P>,
    fields: Collection<KProperty1<in E, *>>
  ) {
    openBlock()
    for ((i, field) in fields.withIndex()) {
      if (i > 0) {
        appendListSeparator()
      }
      appendFieldParam(
        params = params as MutableCollection<QueryParameter>,
        field = field
      )
    }
    closeBlock()
  }

  @Suppress("UNCHECKED_CAST")
  protected fun <P : QueryParameter> StringBuilder.appendFilter(params: MutableCollection<in P>, filter: SqlFilter) {
    when (filter) {
      is SqlMultiFilter.And, is SqlMultiFilter.Or -> {
        if (filter.operands.size > 1) {
          openBlock()
          for ((i, operand) in filter.operands.withIndex()) {
            if (i > 0) {
              append(filter.separator)
            }
            appendFilter(params, operand)
          }
          closeBlock()
        } else {
          appendFilter(params, filter.operands.single())
        }
      }

      is SqlFieldFilter<*> -> appendFieldFilter(
        params = params as MutableCollection<QueryParameter>,
        fieldFilter = filter
      )
    }
  }

  private fun StringBuilder.appendFieldFilter(params: MutableCollection<in QueryParameter>, fieldFilter: SqlFieldFilter<*>) {
    appendFieldName(fieldFilter.field)
    when (fieldFilter) {
      is SqlFieldFilter.IsNull -> append(IS_NULL)
      is SqlFieldFilter.IsNotNull -> append(IS_NOT_NULL)
      is SqlFieldParamFilter<*> -> appendParamFilter(params, fieldFilter)
      is SqlFieldValueFilter<*> -> appendValueFilter(params, fieldFilter)
      is SqlFieldMultiValueFilter.Between<*> -> appendBetweenFilter(params, fieldFilter)
      is SqlFieldMultiValueFilter.In<*> -> appendInFilter(params, fieldFilter)
    }
  }

  private fun StringBuilder.appendParamFilter(params: MutableCollection<in QueryParameter>, paramFilter: SqlFieldParamFilter<*>) {
    append(paramFilter.operator)
    appendFieldParam(params, paramFilter.field)
  }

  private fun StringBuilder.appendValueFilter(params: MutableCollection<in QueryParameter>, valueFilter: SqlFieldValueFilter<*>) {
    append(valueFilter.operator)
    appendValueParam(params, valueFilter.field.fieldName, valueFilter.value)
  }

  private fun StringBuilder.appendBetweenFilter(
    params: MutableCollection<in QueryParameter>,
    betweenFilter: SqlFieldMultiValueFilter.Between<*>
  ) {
    append(betweenFilter.operator)
    for ((i, value) in betweenFilter.values.withIndex()) {
      if (i > 0) {
        append(SqlFilter.AND)
      }
      appendValueParam(params, "${betweenFilter.field.fieldName}_$i", value)
    }
  }

  private fun StringBuilder.appendInFilter(params: MutableCollection<in QueryParameter>, inFilter: SqlFieldMultiValueFilter.In<*>) {
    append(inFilter.operator)
    openBlock()
    for ((i, value) in inFilter.values.withIndex()) {
      if (i > 0) {
        appendListSeparator()
      }
      appendValueParam(params, "${inFilter.field.fieldName}_$i", value)
    }
    closeBlock()
  }

  private fun <E> StringBuilder.appendFieldParam(params: MutableCollection<in QueryParameter>, field: KProperty1<in E, Any?>) {
    ParameterProperty(params.nextIndex, field).also { newParam ->
      params.add(newParam)
      appendParam(newParam)
    }
  }

  private fun StringBuilder.appendValueParam(params: MutableCollection<in QueryParameter>, name: String, value: Any?) {
    ParameterValue(params.nextIndex, name, value).also { newParam ->
      params.add(newParam)
      appendParam(newParam)
    }
  }

  protected fun StringBuilder.appendTableName(entity: KClass<*>) {
    val (name, schema) = entity.tableName
    if (!schema.isNullOrBlank()) {
      appendName(schema)
      appendNameSeparator()
    }
    appendName(name)
  }

  protected fun StringBuilder.appendFieldNames(fields: Collection<KProperty1<*, *>>) {
    for ((i, property) in fields.withIndex()) {
      if (i > 0) {
        appendListSeparator()
      }
      appendFieldName(property)
    }
  }

  protected fun StringBuilder.appendFieldName(field: KProperty<*>) {
    appendName(field.fieldName)
  }

  protected fun StringBuilder.appendName(name: String) {
    openQuote()
    namingStrategy.appendDatabaseName(this, name)
    closeQuote()
  }

  protected open fun StringBuilder.appendSelect() {
    append(SELECT)
  }

  protected open fun StringBuilder.appendInsertInto() {
    append(INSERT_INTO)
  }

  protected open fun StringBuilder.appendUpdate() {
    append(UPDATE)
  }

  protected open fun StringBuilder.appendDeleteFrom() {
    append(DELETE_FROM)
  }

  protected open fun StringBuilder.appendParam(param: QueryParameter) {
    append(PARAM_CHAR)
  }

  protected open fun StringBuilder.appendListSeparator() {
    append(LIST_SEP)
  }

  protected open fun StringBuilder.appendNameSeparator() {
    append(NAME_SEP)
  }

  protected open fun StringBuilder.openQuote() {}

  protected open fun StringBuilder.closeQuote() {}

  protected open fun StringBuilder.openBlock() {
    append(OPEN_BLOCK)
  }

  protected open fun StringBuilder.closeBlock() {
    append(CLOSE_BLOCK)
  }

  companion object Constants {
    const val SELECT = "SELECT "
    const val INSERT_INTO = "INSERT INTO "
    const val DELETE_FROM = "DELETE FROM "
    const val UPDATE = "UPDATE "
    const val FROM = "\n\tFROM "
    const val SET = "\n\tSET "
    const val VALUES = "\n\tVALUES "
    const val WHERE = "\n\tWHERE "

    const val SPACE = ' '
    const val NAME_SEP = '.'
    const val LIST_SEP = ','
    const val OPEN_BLOCK = '('
    const val CLOSE_BLOCK = ')'
    const val PARAM_CHAR = '?'
    const val IS_NULL = " IS NULL"
    const val IS_NOT_NULL = " IS NOT NULL"

    private val Collection<*>.nextIndex: Int
      get() = size + 1
  }
}
