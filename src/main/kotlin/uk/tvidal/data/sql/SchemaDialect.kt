package uk.tvidal.data.sql

import uk.tvidal.data.TableName
import uk.tvidal.data.query.SimpleQuery
import uk.tvidal.data.schema.Index
import uk.tvidal.data.schema.SchemaTable
import uk.tvidal.data.table
import kotlin.reflect.KClass

interface SchemaDialect {
  /**
   *
   */
  fun createTable(
    schemaTable: SchemaTable,
    ifNotExists: Boolean = true,
  ): SimpleQuery

  fun createTable(
    entity: KClass<*>,
    ifNotExists: Boolean = true
  ) = createTable(
    SchemaTable.from(entity),
    ifNotExists
  )

  fun createIndex(
    index: Index,
    tableName: TableName,
    ifNotExists: Boolean = true,
  ): SimpleQuery

  /**
   *
   */
  fun dropTable(
    tableName: TableName,
    ifExists: Boolean = true
  ): SimpleQuery

  fun dropTable(
    schemaTable: SchemaTable,
    ifExists: Boolean = true,
  ) = dropTable(
    schemaTable.table,
    ifExists
  )

  fun <E : Any> dropTable(
    entity: KClass<E>,
    ifExists: Boolean = true
  ) = dropTable(
    entity.table,
    ifExists
  )

  fun dropIndex(
    index: Index,
    tableName: TableName,
    ifExists: Boolean = true,
  ): SimpleQuery
}
