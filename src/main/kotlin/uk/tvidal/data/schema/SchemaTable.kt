package uk.tvidal.data.schema

import uk.tvidal.data.Config
import uk.tvidal.data.TableName
import uk.tvidal.data.fields
import uk.tvidal.data.table
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

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

  companion object Factory {

    internal fun <T : Any> fields(entity: KClass<T>, config: Config): Collection<SchemaField<*>> {
      val fields = entity.fields
        .associateBy { it.name }

      val parameters = entity.primaryConstructor?.parameters ?: emptyList()
      val parameterNames = parameters
        .map { it.name }
        .toSet()

      val allFields = parameters.mapNotNull {
        fields[it.name]
      } + fields.values.filterNot {
        it.name in parameterNames
      }
      return allFields.flatMap {
        SchemaField.from(it, config)
      }
    }

    fun <E : Any> from(
      entity: KClass<E>,
      config: Config = Config.Default,
    ) = SchemaTable(
      table = entity.table,
      fields = fields(entity, config),
      constraints = listOfNotNull(
        entity.primaryKey
      ) + foreignKeys(
        entity
      ),
    )
  }
}
