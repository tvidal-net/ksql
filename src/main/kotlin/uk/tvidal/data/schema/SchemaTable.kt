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

    internal fun <T : Any> fields(type: KClass<T>, config: Config): Collection<SchemaField<*>> {
      val fields = type.fields
        .associateBy { it.name }

      val parameters = type.primaryConstructor?.parameters ?: emptyList()
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
      type: KClass<E>,
      config: Config = Config.Default,
    ) = SchemaTable(
      table = type.table,
      fields = fields(type, config),
      constraints = listOfNotNull(
        type.primaryKey
      ) + foreignKeys(
        type
      ),
    )
  }
}
