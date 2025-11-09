package uk.tvidal.data.sql

import uk.tvidal.data.TableName
import uk.tvidal.data.schema.Index
import uk.tvidal.data.schema.SchemaTable
import uk.tvidal.data.table
import kotlin.reflect.KClass

interface SchemaDialect {

  /**
   *
   */
  fun create(
    table: SchemaTable,
    ifNotExists: Boolean = true,
  ): String

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
    index: Index,
    table: TableName,
    ifNotExists: Boolean = true,
  ): String

  /**
   *
   */
  fun drop(
    table: TableName,
    ifExists: Boolean = true
  ): String

  /**
   *
   */
  fun <E : Any> drop(
    entity: KClass<out E>,
    ifExists: Boolean = true
  ) = drop(
    entity.table,
    ifExists
  )

  fun drop(
    index: Index,
    table: TableName,
    ifExists: Boolean = true,
  ): String
}
