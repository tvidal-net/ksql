package uk.tvidal.data.query

import uk.tvidal.data.asAlias
import uk.tvidal.data.fields
import uk.tvidal.data.filter.SqlFilter
import uk.tvidal.data.simpleName
import uk.tvidal.data.tableName
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

sealed interface SelectFrom {

  val fields: Collection<KProperty<*>>
  val alias: String?
  val name: String

  class Table<T : Any>(
    val type: KClass<T>,
    override val alias: String? = null,
  ) : SelectFrom {
    override val name: String
      get() = type.tableName

    override val fields: Collection<KProperty<*>>
      get() = type.fields

    override fun toString() = "$simpleName($name${asAlias(alias)})"
  }

  class Join(
    val from: SelectFrom,
    val type: Type,
    val on: SqlFilter?,
  ) : SelectFrom {

    init {
      require(from !is Join) {
        "Unable to Join with another Join!"
      }
    }

    override val alias: String?
      get() = from.alias

    override val name: String
      get() = from.name

    override val fields: Collection<KProperty<*>>
      get() = from.fields

    override fun toString() = "$type$simpleName($name${asAlias(alias)} ON $on)"

    enum class Type(val sql: String) {
      Cross("CROSS JOIN"),
      Inner("INNER JOIN"),
      Left("LEFT OUTER JOIN"),
      Right("RIGHT OUTER JOIN"),
      Full("FULL OUTER JOIN");
    }
  }
}
