package uk.tvidal.data

import uk.tvidal.data.filter.SqlFilter
import kotlin.reflect.KClass

interface Repository<E : Any> : Iterable<E> {

  val db: Database
  val entity: KClass<E>

  fun one(vararg keyValues: Any): E?

  operator fun get(vararg keyValues: Any) = one(*keyValues)

  fun select(where: SqlFilter?): List<E>

  override fun iterator() = select(null).iterator()
}
