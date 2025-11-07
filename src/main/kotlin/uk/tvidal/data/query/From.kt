package uk.tvidal.data.query

import uk.tvidal.data.fields
import uk.tvidal.data.filter.SqlFilter
import uk.tvidal.data.tableName
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1

sealed interface From {

  val fields: Collection<KProperty<*>>
  val alias: String

  class Entity<T : Any>(
    val entity: KClass<T>,
    override val fields: Collection<KProperty1<T, *>> = entity.fields,
    override val alias: String = entity.tableName.name,
  ) : From

  class Join(
    val from: From,
    val type: Type,
    val on: SqlFilter?,
  ) : From {

    override val alias: String
      get() = from.alias

    override val fields: Collection<KProperty<*>>
      get() = from.fields

    enum class Type(val sql: String) {
      Cross("CROSS JOIN"),
      Inner("INNER JOIN"),
      Left("LEFT OUTER JOIN"),
      Right("RIGHT OUTER JOIN"),
      Full("FULL OUTER JOIN");

      operator fun <T : Any> invoke(
        entity: KClass<T>,
        on: SqlFilter?,
        alias: String = entity.tableName.name,
        fields: Collection<KProperty1<T, *>> = entity.fields,
      ) = Join(
        Entity(entity, fields, alias),
        this, on,
      )
    }
  }
}
