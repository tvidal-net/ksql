package uk.tvidal.data.query

import uk.tvidal.data.fields
import uk.tvidal.data.filter.SqlFilter
import uk.tvidal.data.tableName
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1

sealed interface From {

  val fields: Collection<KProperty<*>>
  val alias: String?
  val name: String

  class Table<T : Any>(
    val type: KClass<T>,
    override val fields: Collection<KProperty1<T, *>> = type.fields,
    override val alias: String? = null,
  ) : From {
    override val name: String
      get() = type.tableName
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
        type: KClass<T>,
        on: SqlFilter?,
        alias: String = type.tableName,
        fields: Collection<KProperty1<T, *>> = type.fields,
      ) = Join(
        Table(type, fields, alias),
        this,
        on,
      )
    }
  }
}
