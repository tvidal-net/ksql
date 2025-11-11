package uk.tvidal.data.sql

import uk.tvidal.data.Config
import uk.tvidal.data.TableName
import uk.tvidal.data.codec.CodecFactory
import uk.tvidal.data.codec.ValueType
import uk.tvidal.data.equalsFilter
import uk.tvidal.data.filter.SqlFilter
import uk.tvidal.data.query.EntityQuery
import uk.tvidal.data.query.QueryParam
import uk.tvidal.data.query.SelectFrom
import uk.tvidal.data.query.SelectQuery
import uk.tvidal.data.query.SimpleQuery
import uk.tvidal.data.schema.Constraint
import uk.tvidal.data.schema.FieldReference
import uk.tvidal.data.schema.Index
import uk.tvidal.data.schema.SchemaField
import uk.tvidal.data.schema.SchemaTable
import uk.tvidal.data.table
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

open class SqlDialect(
  config: Config = Config.Default
) : SqlQueryBuilder(
  codecs = CodecFactory(config)
), QueryDialect, SchemaDialect {

  override fun create(schemaTable: SchemaTable, ifNotExists: Boolean) = sqlQuery {
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
      create(index, schemaTable.table)
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
    fields(index.fields)
  }

  override fun drop(index: Index, table: TableName, ifExists: Boolean) = sqlQuery {
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
  ) = selectQuery(projection) { params ->
    select(from)
    from(from)
    for (join in from.filterIsInstance<SelectFrom.Join>()) {
      join(params, join)
    }
    where(params, whereClause)
  }

  protected fun Appendable.from(from: Collection<SelectFrom>) {
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

  protected fun <P : QueryParam> Appendable.join(
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

  protected inline fun sqlQuery(
    builder: Appendable.() -> Unit
  ) = buildString {
    builder()
  }.let {
    SimpleQuery(it)
  }

  protected inline fun paramQuery(
    builder: Appendable.(MutableCollection<QueryParam>) -> Unit
  ) = arrayListOf<QueryParam>().let { params ->
    SimpleQuery(
      sql = buildString {
        builder(params)
      },
      params = params,
    )
  }

  protected inline fun <E : Any> selectQuery(
    projection: KClass<E>,
    builder: Appendable.(MutableCollection<QueryParam>) -> Unit
  ) = arrayListOf<QueryParam>().let { params ->
    SelectQuery(
      decode = codecs.decoder(projection),
      sql = buildString {
        builder(params)
      },
      params = params,
    )
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
      is Constraint.PrimaryKey -> constraintKey(Constraint.ConstraintKeyType.PrimaryKey, constraint.index)
      is Constraint.UniqueKey -> constraintKey(Constraint.ConstraintKeyType.UniqueKey, constraint.index)
      is Constraint.ForeignKey -> foreignKey(constraint)
    }
  }

  protected open fun Appendable.constraintKey(keyType: Constraint.ConstraintKeyType, index: Index) {
    if (index.name != null) {
      append("CONSTRAINT ")
      quotedName(index.name)
      space()
    }
    append(keyType.sql)
    space()
    fields(index.fields)
  }

  protected open fun Appendable.foreignKey(foreignKey: Constraint.ForeignKey) {
    append("FOREIGN KEY ")
    if (foreignKey.name != null) {
      quotedName(foreignKey.name)
      space()
    }
    foreignKeyFields(foreignKey)
    foreignKeyReferences(foreignKey)
    foreignKeyDeleteAction(foreignKey.deleteAction)
    foreignKeyUpdateAction(foreignKey.updateAction)
  }

  protected open fun Appendable.foreignKeyFields(foreignKey: Constraint.ForeignKey) {
    quotedNames(
      foreignKey.references
        .map(Constraint.ForeignKeyReference::fieldName)
    )
  }

  protected open fun Appendable.foreignKeyReferences(foreignKey: Constraint.ForeignKey) {
    append(" REFERENCES ")
    tableName(foreignKey.table)
    space()
    quotedNames(
      foreignKey.references.map {
        it.referenceField
      }
    )
  }

  protected open fun Appendable.foreignKeyDeleteAction(action: Constraint.ForeignKeyAction) {
    if (action != Constraint.ForeignKeyAction.Default) {
      append(" ON DELETE ")
      foreignKeyAction(action)
    }
  }

  protected open fun Appendable.foreignKeyUpdateAction(action: Constraint.ForeignKeyAction) {
    if (action != Constraint.ForeignKeyAction.Default) {
      append(" ON UPDATE ")
      foreignKeyAction(action)
    }
  }

  protected open fun Appendable.foreignKeyAction(action: Constraint.ForeignKeyAction) {
    append(action.sql)
  }

  protected fun Appendable.fields(fields: Collection<FieldReference>) {
    openBlock()
    for ((i, field) in fields.withIndex()) {
      if (i > 0) listSeparator()
      field(field)
    }
    closeBlock()
  }

  protected open fun Appendable.field(field: FieldReference) {
    quotedName(field.name)
    if (field is FieldReference.Descending) append(" DESC")
  }

  protected open fun Appendable.field(field: SchemaField<*>) {
    quotedName(field.name)
    space()
    dataType(field.type)
    notNull(!field.nullable)
  }

  protected open fun Appendable.dataType(codec: ValueType<*, *>) {
    append(codec.sqlDataType)
  }
}
