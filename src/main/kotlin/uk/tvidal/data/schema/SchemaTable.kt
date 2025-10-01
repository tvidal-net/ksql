package uk.tvidal.data.schema

import uk.tvidal.data.TableName
import uk.tvidal.data.fields
import uk.tvidal.data.tableName
import kotlin.reflect.KClass

data class SchemaTable(
  val name: TableName,
  val columns: Collection<SchemaColumn<*>>,
  val constraints: Collection<Constraint> = listOf(),
  val indices: Collection<Index> = listOf(),
) {

  val primaryKey: Constraint.PrimaryKey?
    get() = constraints.filterIsInstance<Constraint.PrimaryKey>().singleOrNull()

  val foreignKeys: Collection<Constraint.ForeignKey>
    get() = constraints.filterIsInstance<Constraint.ForeignKey>()

  val uniqueKeys: Collection<Constraint.Unique>
    get() = TODO()

  companion object Factory {

    fun from(entity: KClass<*>, config: SchemaConfig = SchemaConfig.Default) = SchemaTable(
      name = entity.tableName,
      columns = entity.fields.map { SchemaColumn.from(it, config) },
      constraints = listOfNotNull(entity.primaryKey)
    )
  }
}
