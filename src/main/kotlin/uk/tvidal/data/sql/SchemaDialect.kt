package uk.tvidal.data.sql

import uk.tvidal.data.TableName
import uk.tvidal.data.query.SqlQuery
import uk.tvidal.data.schema.Index
import uk.tvidal.data.schema.SchemaTable
import uk.tvidal.data.tableName
import kotlin.reflect.KClass

interface SchemaDialect {

  /**
   *
   */
  fun create(
    table: SchemaTable,
    ifNotExists: Boolean = true,
  ): SqlQuery

  /**
   *
   */
  fun <E : Any> create(
    entity: KClass<out E>,
    ifNotExists: Boolean = true,
  ) = create(
    SchemaTable.from(entity),
    ifNotExists
  )

  /**
   *
   */
  fun create(
    table: TableName,
    index: Index,
  ): SqlQuery

  /**
   *
   */
  fun drop(
    table: TableName,
    ifExists: Boolean = true
  ): SqlQuery

  /**
   *
   */
  fun <E : Any> drop(
    entity: KClass<out E>,
    ifExists: Boolean = true
  ) = drop(
    entity.tableName,
    ifExists
  )
}
