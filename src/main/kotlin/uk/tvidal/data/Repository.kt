package uk.tvidal.data

import uk.tvidal.data.filter.SqlFilter
import kotlin.reflect.KClass

interface Repository<E : Any> : Iterable<E> {

  val db: Database
  val entity: KClass<out E>

  fun one(vararg keyValues: Any): E?

  operator fun get(vararg keyValues: Any) = one(*keyValues)

  fun all(): Sequence<E>

  override fun iterator() = all().iterator()

  fun select(where: SqlFilter): Sequence<E>
}
