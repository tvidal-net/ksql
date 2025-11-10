package uk.tvidal.data.schema

import uk.tvidal.data.TableName

data class SchemaTable(
  val table: TableName,
  val fields: Collection<SchemaField<*>>,
  val constraints: Collection<Constraint> = listOf(),
  val indices: Collection<Index> = listOf(),
) {

  val primaryKey: Constraint.PrimaryKey
    get() = constraints
      .filterIsInstance<Constraint.PrimaryKey>()
      .single()

  val uniqueKeys: Collection<Constraint.UniqueKey>
    get() = constraints
      .filterIsInstance<Constraint.UniqueKey>()

  val foreignKeys: Collection<Constraint.ForeignKey>
    get() = constraints
      .filterIsInstance<Constraint.ForeignKey>()
}
