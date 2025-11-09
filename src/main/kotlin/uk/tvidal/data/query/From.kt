package uk.tvidal.data.query

import uk.tvidal.data.fields
import uk.tvidal.data.filter.SqlFilter
import uk.tvidal.data.table
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1

sealed interface From {

  val fields: Collection<KProperty<*>>
  val alias: String?
  val name: String

  class Entity<T : Any>(
    val entity: KClass<T>,
    override val fields: Collection<KProperty1<T, *>> = entity.fields,
    override val alias: String? = null,
  ) : From {
    override val name: String
      get() = entity.table.name
  }

  class Join(
    val from: From,
    val type: Type,
    val on: SqlFilter?,
  ) : From {

    override val fields: Collection<KProperty<*>>
      get() = from.fields

    override val alias: String?
      get() = from.alias

    override val name: String
      get() = from.name

    enum class Type(val sql: String) {
      Cross("CROSS JOIN"),
      Inner("INNER JOIN"),
      Left("LEFT OUTER JOIN"),
      Right("RIGHT OUTER JOIN"),
      Full("FULL OUTER JOIN");

      operator fun <T : Any> invoke(
        entity: KClass<T>,
        on: SqlFilter?,
        alias: String = entity.table.name,
        fields: Collection<KProperty1<T, *>> = entity.fields,
      ) = Join(
        Entity(entity, fields, alias),
        this,
        on,
      )
    }
  }
}
