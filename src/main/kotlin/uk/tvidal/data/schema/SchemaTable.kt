package uk.tvidal.data.schema

import uk.tvidal.data.Config
import uk.tvidal.data.TableName
import uk.tvidal.data.fieldName
import uk.tvidal.data.fields
import uk.tvidal.data.keyFields
import uk.tvidal.data.table
import uk.tvidal.data.valueType
import kotlin.reflect.KClass

data class SchemaTable(
  val name: TableName,
  val columns: Collection<SchemaColumn<*>>,
  val constraints: Collection<Constraint> = listOf(),
  val indices: Collection<Index> = listOf(),
) {

  val primaryKey: Constraint.PrimaryKey
    get() = constraints.filterIsInstance<Constraint.PrimaryKey>().single()

  val uniqueKeys: Collection<Constraint.UniqueKey>
    get() = constraints.filterIsInstance<Constraint.UniqueKey>()

  val foreignKeys: Collection<Constraint.ForeignKey>
    get() = constraints.filterIsInstance<Constraint.ForeignKey>()

  companion object Factory {

    fun foreignKeys(entity: KClass<*>) = entity.fields.mapNotNull { field ->
      val entity = field.valueType
      val keyFields = entity.keyFields
      if (keyFields.size == 1) {
        val idField = keyFields.single()
        Constraint.ForeignKey(
          table = entity.table,
          references = listOf(
            Constraint.ForeignKeyReference(
              columnName = field.fieldName,
              referenceColumn = idField.fieldName
            )
          )
        )
      } else {
        null
      }
    }

    fun from(entity: KClass<*>, config: Config = Config.Default) = SchemaTable(
      name = entity.table,
      columns = entity.fields.map { SchemaColumn.from(it, config) },
      constraints = listOfNotNull(entity.primaryKey) + foreignKeys(entity)
    )
  }
}
