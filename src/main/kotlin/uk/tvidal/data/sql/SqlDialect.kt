package uk.tvidal.data.sql

import uk.tvidal.data.Config
import uk.tvidal.data.TableName
import uk.tvidal.data.codec.CodecFactory
import uk.tvidal.data.codec.ValueType
import uk.tvidal.data.equalsFilter
import uk.tvidal.data.filter.SqlFilter
import uk.tvidal.data.query.EntityQuery
import uk.tvidal.data.query.From
import uk.tvidal.data.query.QueryParam
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
    entity: KClass<E>,
    from: Collection<From>,
    whereClause: SqlFilter?
  ) = selectQuery(entity) { params ->
    select(from)
    from(
      from.filterIsInstance<From.Table<*>>()
    )
    for (join in from.filterIsInstance<From.Join>()) {
      join(params, join)
    }
    where(params, whereClause)
  }

  protected open fun Appendable.from(tables: Collection<From.Table<*>>) {
    appendLine()
    append("FROM ")
    for ((i, table) in tables.withIndex()) {
      if (i > 0) listSeparator()
      tableName(
        table = table.type.table,
        alias = if (tables.size == 1) null else (table.alias ?: table.name)
      )
    }
  }

  protected open fun <P : QueryParam> Appendable.join(
    params: MutableCollection<in P>,
    join: From.Join,
  ) {
    appendLine()
    append(join.type)
    space()
    when (val from = join.from) {
      is From.Table<*> -> tableName(from.type.table, from.alias)
      else -> throw IllegalArgumentException("Invalid join type: $from")
    }
    join.on?.let {
      appendLine()
      indent()
      append("ON ")
      filter(params, it, join.alias)
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
      params = params,
      sql = buildString {
        builder(params)
      }
    )
  }

  protected inline fun <E : Any> selectQuery(
    entity: KClass<E>,
    builder: Appendable.(MutableCollection<QueryParam>) -> Unit
  ) = arrayListOf<QueryParam>().let { params ->
    SelectQuery(
      params = params,
      decode = codecs.decoder(entity),
      sql = buildString {
        builder(params)
      }
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
    fields(index.fields)
  }

  protected open fun Appendable.schemaForeignKey(foreignKey: Constraint.ForeignKey) {
    append("FOREIGN KEY ")
    if (foreignKey.name != null) {
      quotedName(foreignKey.name)
      space()
    }
    quotedNames(
      foreignKey.references
        .map(Constraint.ForeignKeyReference::fieldName)
    )
    append(" REFERENCES ")
    tableName(foreignKey.table)
    space()
    quotedNames(
      foreignKey.references.map {
        it.referenceField
      }
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
    append(codec.dataType)
  }
}
