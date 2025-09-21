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

  fun save(value: E): Int

  operator fun plusAssign(value: E) {
    save(value)
  }

  fun save(values: Collection<E>): IntArray

  fun save(vararg values: E) = save(values.toList())

  operator fun plusAssign(values: Collection<E>) {
    save(values)
  }

  fun delete(value: E): Int

  operator fun minusAssign(value: E) {
    delete(value)
  }

  fun delete(values: Collection<E>): IntArray

  fun delete(vararg values: E) = delete(values.toList())

  operator fun minusAssign(values: Collection<E>) {
    delete(values)
  }

  fun update(value: E): Int

  fun update(values: Collection<E>): IntArray

  fun update(vararg values: E) = update(values.toList())

  fun insert(value: E): Int

  fun insert(values: Collection<E>): IntArray

  fun insert(vararg values: E) = insert(values.toList())
}
