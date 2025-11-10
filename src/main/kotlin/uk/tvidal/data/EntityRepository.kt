package uk.tvidal.data

import uk.tvidal.data.filter.SqlFilter

interface EntityRepository<E : Any> : Repository<E> {

  fun save(value: E): Int

  operator fun plusAssign(value: E) {
    save(value)
  }

  fun save(values: Collection<E>): IntArray

  fun save(vararg values: E) = save(values.toList())

  operator fun plusAssign(values: Collection<E>) {
    save(values)
  }

  fun delete(where: SqlFilter): Int

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
