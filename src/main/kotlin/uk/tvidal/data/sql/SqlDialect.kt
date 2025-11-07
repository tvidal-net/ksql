package uk.tvidal.data.sql

import uk.tvidal.data.NamingStrategy
import uk.tvidal.data.TableName
import uk.tvidal.data.codec.DataType
import uk.tvidal.data.equalsFilter
import uk.tvidal.data.fields
import uk.tvidal.data.filter.SqlFilter
import uk.tvidal.data.query.EntityQuery
import uk.tvidal.data.query.From
import uk.tvidal.data.query.QueryParam
import uk.tvidal.data.query.SimpleQuery
import uk.tvidal.data.schema.ColumnReference
import uk.tvidal.data.schema.Constraint
import uk.tvidal.data.schema.Index
import uk.tvidal.data.schema.SchemaColumn
import uk.tvidal.data.schema.SchemaTable
import uk.tvidal.data.tableName
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

open class SqlDialect(
  namingStrategy: NamingStrategy = NamingStrategy.SnakeCase
) : SqlQueryBuilder(namingStrategy), QueryDialect, SchemaDialect {

  override fun create(table: SchemaTable, ifNotExists: Boolean) = sqlQuery {
    append("CREATE TABLE ")
    ifNotExists(ifNotExists)
    tableName(table.name)
    space()
    openBlock()
    for ((i, col) in table.columns.withIndex()) {
      if (i > 0) listSeparator()
      appendLine()
      indent()
      schemaColumn(col)
    }
    table.constraints.forEach {
      listSeparator()
      appendLine()
      indent()
      schemaConstraint(it)
    }
    appendLine()
    closeBlock()
    terminate()
    table.indices.forEach { index ->
      appendLine()
      create(index, table.name)
      terminate()
    }
  }

  override fun drop(table: TableName, ifExists: Boolean) = sqlQuery {
    append("DROP TABLE ")
    ifExists(ifExists)
    tableName(table)
  }

  override fun create(index: Index, table: TableName, ifNotExists: Boolean) = sqlQuery {
    append("CREATE INDEX ")
    ifNotExists(ifNotExists)
    if (index.name != null) {
      quotedName(index.name)
      space()
    }
    append("ON ")
    tableName(table)
    space()
    columns(index.columns)
  }

  override fun drop(index: Index, table: TableName, ifExists: Boolean) = sqlQuery {
    requireNotNull(index.name) {
      "Cannot drop index without a name"
    }
    append("DROP INDEX")
    ifExists(ifExists)
    quotedName(index.name)
  }

  protected inline fun sqlQuery(
    builder: Appendable.() -> Unit
  ) = buildString {
    builder()
  }

  override fun select(
    from: Collection<From>,
    whereClause: SqlFilter?
  ) = simpleQuery { params ->
    select(from)
    from.filterIsInstance<From.Entity<*>>().let { e ->
      require(e.size == 1) {
        "Invalid from clause, expected a single one, received $e"
      }
      e.single().let {
        from(it.entity, it.alias)
      }
    }
    for (join in from.filterIsInstance<From.Join>()) {
      join(params, join)
    }
    where(params, whereClause)
  }

  override fun select(
    entity: KClass<*>,
    whereClause: SqlFilter?
  ) = simpleQuery { params ->
    append("SELECT ")
    fieldNames(entity.fields, false)
    from(entity)
    where(params, whereClause)
  }

  protected open fun Appendable.from(entity: KClass<*>, alias: String? = null) {
    appendLine()
    indent()
    append("FROM ")
    tableName(entity.tableName)
    alias?.let {
      append(" AS ")
      quotedName(it)
    }
  }

  protected open fun <P : QueryParam> Appendable.join(
    params: MutableCollection<in P>,
    join: From.Join,
  ) {
    appendLine()
    indent()
    append(join.type)
    space()
    join.from.let {
      if (it is From.Entity<*>) {
        tableName(it.entity.tableName)
      }
      append(" AS ")
      quotedName(it.alias)
    }
    appendLine()
    indent()
    append("ON ")
    join.on?.let {
      filter(params, it, join.alias)
    }
  }

  override fun delete(
    entity: KClass<*>,
    whereClause: SqlFilter
  ) = simpleQuery { params ->
    deleteQuery(params, entity.tableName, whereClause)
  }

  protected inline fun simpleQuery(
    builder: Appendable.(MutableCollection<QueryParam.Value>) -> Unit
  ) = arrayListOf<QueryParam.Value>().let { params ->
    SimpleQuery(
      sql = buildString {
        builder(params)
      },
      params = params
    )
  }

  override fun <E : Any> save(
    entity: KClass<E>,
    updateFields: Collection<KProperty1<E, *>>,
    keyFields: Collection<KProperty1<E, *>>
  ): EntityQuery<E> = throw NotImplementedError(
    "saveQuery is not implemented for the default Dialect!"
  )

  override fun <E : Any> delete(
    entity: KClass<E>,
    keyFields: Collection<KProperty1<E, *>>
  ) = entityQuery<E> { params ->
    deleteQuery(params, entity.tableName, equalsFilter(keyFields))
  }

  override fun <E : Any> update(
    entity: KClass<E>,
    updateFields: Collection<KProperty1<E, *>>,
    keyFields: Collection<KProperty1<E, *>>
  ) = entityQuery<E> { params ->
    append("UPDATE ")
    tableName(entity.tableName)
    space()
    setFields(params, updateFields)
    where(params, equalsFilter(keyFields))
  }

  override fun <E : Any> insert(
    entity: KClass<E>,
    insertFields: Collection<KProperty1<E, *>>
  ) = entityQuery<E> { params ->
    insertInto(entity.tableName, params, insertFields)
  }

  protected open fun <E, P : QueryParam> Appendable.insertInto(
    table: TableName,
    params: MutableCollection<in P>,
    insertFields: Collection<KProperty1<E, *>>
  ) {
    append("INSERT INTO ")
    appendLine()
    indent()
    tableName(table)
    space()
    fieldNames(insertFields)
    insertValues(params, insertFields)
  }

  protected open fun <E, P : QueryParam> Appendable.insertValues(
    params: MutableCollection<in P>,
    insertFields: Collection<KProperty1<E, *>>
  ) {
    appendLine()
    indent()
    append("VALUES ")
    fieldParams(params, insertFields)
  }

  protected inline fun <E> entityQuery(
    builder: Appendable.(MutableCollection<EntityQuery.Param<E>>) -> Unit
  ) = arrayListOf<EntityQuery.Param<E>>().let { params ->
    EntityQuery(
      sql = buildString {
        builder(params)
      },
      params = params
    )
  }

  private fun <P : QueryParam> Appendable.deleteQuery(
    params: MutableCollection<in P>,
    table: TableName,
    whereClause: SqlFilter
  ) {
    append("DELETE FROM ")
    tableName(table)
    where(params, whereClause)
  }

  protected fun Appendable.schemaConstraint(constraint: Constraint) {
    when (constraint) {
      is Constraint.PrimaryKey -> schemaConstraintKey(Constraint.ConstraintKeyType.PrimaryKey, constraint.index)
      is Constraint.UniqueKey -> schemaConstraintKey(Constraint.ConstraintKeyType.UniqueKey, constraint.index)
      is Constraint.ForeignKey -> schemaForeignKey(constraint)
    }
  }

  protected open fun Appendable.schemaConstraintKey(keyType: Constraint.ConstraintKeyType, index: Index) {
    if (index.name != null) {
      append("CONSTRAINT ")
      quotedName(index.name)
      space()
    }
    append(keyType.sql)
    space()
    columns(index.columns)
  }

  protected open fun Appendable.schemaForeignKey(foreignKey: Constraint.ForeignKey) {
    append("FOREIGN KEY ")
    if (foreignKey.name != null) {
      quotedName(foreignKey.name)
      space()
    }
    quotedNames(
      foreignKey.references
        .map(Constraint.ForeignKeyReference::columnName)
    )
    append(" REFERENCES ")
    tableName(foreignKey.table)
    space()
    quotedNames(
      foreignKey.references
        .map(Constraint.ForeignKeyReference::referenceColumn)
    )
    if (foreignKey.deleteAction != Constraint.ForeignKeyAction.Default) {
      append(" ON DELETE ")
      append(foreignKey.deleteAction.sql)
    }
    if (foreignKey.updateAction != Constraint.ForeignKeyAction.Default) {
      append(" ON UPDATE ")
      append(foreignKey.updateAction.sql)
    }
  }

  protected fun Appendable.columns(columns: Collection<ColumnReference>) {
    openBlock()
    for ((i, col) in columns.withIndex()) {
      if (i > 0) listSeparator()
      column(col)
    }
    closeBlock()
  }

  protected open fun Appendable.column(column: ColumnReference) {
    quotedName(column.name)
    if (column is ColumnReference.Descending) append(" DESC")
  }

  protected open fun Appendable.schemaColumn(column: SchemaColumn<*>) {
    quotedName(column.name)
    space()
    dataType(column.dataType)
    notNull(!column.nullable)
  }

  protected open fun Appendable.dataType(dataType: DataType<*, *>) {
    append(dataType.toString())
  }
}
