package uk.tvidal.data.sql

import uk.tvidal.data.Config
import uk.tvidal.data.TableName
import uk.tvidal.data.equalsFilter
import uk.tvidal.data.filter.SqlFilter
import uk.tvidal.data.query.EntityQuery
import uk.tvidal.data.query.QueryParam
import uk.tvidal.data.query.SelectFrom
import uk.tvidal.data.schema.Index
import uk.tvidal.data.schema.SchemaTable
import uk.tvidal.data.table
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

open class SqlDialect(
  override val config: Config = Config.Default
) : StatementDialect, SchemaBuilder, QueryDialect, SchemaDialect {

  override fun createTable(schemaTable: SchemaTable, ifNotExists: Boolean) = sqlQuery {
    append("CREATE TABLE ")
    ifNotExists(ifNotExists)
    tableName(schemaTable.table)
    space()
    openBlock()
    for ((i, col) in schemaTable.fields.withIndex()) {
      if (i > 0) listSeparator()
      appendLine()
      indent()
      field(col)
    }
    schemaTable.constraints.forEach {
      listSeparator()
      appendLine()
      indent()
      schemaConstraint(it)
    }
    appendLine()
    closeBlock()
    terminate()
    schemaTable.indices.forEach { index ->
      appendLine()
      createIndex(index, schemaTable.table)
      terminate()
    }
  }

  override fun dropTable(tableName: TableName, ifExists: Boolean) = sqlQuery {
    append("DROP TABLE ")
    ifExists(ifExists)
    tableName(tableName)
  }

  override fun createIndex(index: Index, tableName: TableName, ifNotExists: Boolean) = sqlQuery {
    append("CREATE INDEX ")
    ifNotExists(ifNotExists)
    if (index.name != null) {
      quotedName(index.name)
      space()
    }
    append("ON ")
    tableName(tableName)
    space()
    fields(index.fields)
  }

  override fun dropIndex(index: Index, tableName: TableName, ifExists: Boolean) = sqlQuery {
    requireNotNull(index.name) {
      "Cannot drop index without a name"
    }
    append("DROP INDEX")
    ifExists(ifExists)
    quotedName(index.name)
  }

  override fun <E : Any> select(
    projection: KClass<E>,
    whereClause: SqlFilter?,
    from: Collection<SelectFrom>
  ) = selectQuery(projection, alias(from)) { params ->
    select(from)
    from(from)
    for (join in from.filterIsInstance<SelectFrom.Join>()) {
      join(params, join)
    }
    where(params, whereClause)
    if (from.any { it.isAggregate }) {
      groupBy(from)
    }
  }

  private fun Appendable.from(from: Collection<SelectFrom>) {
    appendLine()
    append("FROM ")
    val fromTables = from.filterIsInstance<SelectFrom.Table<*>>()
    for ((i, table) in fromTables.withIndex()) {
      if (i > 0) listSeparator()
      tableName(
        table = table.type.table,
        alias = alias(table, from.size)
      )
    }
  }

  private fun <P : QueryParam> Appendable.join(
    params: MutableCollection<in P>,
    join: SelectFrom.Join,
  ) {
    appendLine()
    append(join.type)
    space()
    val from = join.from
    require(from is SelectFrom.Table<*>) {
      "Invalid Join Type: $from"
    }
    tableName(from.type.table, from.alias)
    join.on?.let {
      appendLine()
      indent()
      append("ON ")
      filter(params, it, alias(join))
    }
  }

  override fun delete(
    entity: KClass<*>,
    whereClause: SqlFilter
  ) = paramQuery { params ->
    deleteQuery(params, entity.table, whereClause)
  }

  override fun <E : Any> save(
    entity: KClass<E>,
    updateFields: Collection<KProperty1<E, *>>,
    keyFields: Collection<KProperty1<E, *>>
  ): EntityQuery<E> = throw NotImplementedError(
    "save is not implemented for the default Dialect!"
  )

  override fun <E : Any> delete(
    entity: KClass<E>,
    keyFields: Collection<KProperty1<E, *>>
  ) = entityQuery<E> { params ->
    deleteQuery(params, entity.table, equalsFilter(keyFields))
  }

  override fun <E : Any> update(
    entity: KClass<E>,
    updateFields: Collection<KProperty1<E, *>>,
    keyFields: Collection<KProperty1<E, *>>
  ) = entityQuery<E> { params ->
    append("UPDATE ")
    tableName(entity.table)
    appendLine()
    indent()
    setFields(params, updateFields)
    where(params, equalsFilter(keyFields))
  }

  override fun <E : Any> insert(
    entity: KClass<E>,
    insertFields: Collection<KProperty1<E, *>>
  ) = entityQuery<E> { params ->
    insertInto(entity.table, params, insertFields)
  }
}
