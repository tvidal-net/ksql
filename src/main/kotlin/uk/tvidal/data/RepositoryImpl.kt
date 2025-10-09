package uk.tvidal.data

import uk.tvidal.data.codec.EntityDecoder
import uk.tvidal.data.filter.SqlFilter
import uk.tvidal.data.query.EntityQuery
import uk.tvidal.data.query.SimpleQuery
import kotlin.reflect.KClass

internal class RepositoryImpl<E : Any>(
  override val db: Database,
  val decoder: EntityDecoder<E>,
  override val entity: KClass<E>,
) : EntityRepository<E> {

  private val selectByKey: SimpleQuery by lazy {
    selectQuery(entity.keyFilter)
  }

  private val selectAll: SimpleQuery by lazy {
    selectQuery(null)
  }

  private val save: EntityQuery<E> by lazy {
    db.dialect.save(entity)
  }

  private val delete: EntityQuery<E> by lazy {
    db.dialect.delete(entity)
  }

  private val update: EntityQuery<E> by lazy {
    db.dialect.update(entity)
  }

  private val insert: EntityQuery<E> by lazy {
    db.dialect.insert(entity)
  }

  private fun selectQuery(where: SqlFilter?): SimpleQuery =
    db.dialect.select(entity, where)

  override fun one(vararg keyValues: Any) = db.select(selectByKey) {
    setParams(*keyValues)
    one(decoder)
  }

  override fun all() = db.select(selectAll) {
    all(decoder)
  }

  override fun select(where: SqlFilter) = db.select(selectQuery(where)) {
    all(decoder)
  }

  override fun save(value: E) =
    db.execute(save, value)

  override fun save(values: Collection<E>) =
    db.execute(save, values)

  override fun delete(value: E) =
    db.execute(delete, value)

  override fun delete(values: Collection<E>) =
    db.execute(delete, values)

  override fun update(value: E) =
    db.execute(update, value)

  override fun update(values: Collection<E>) =
    db.execute(update, values)

  override fun insert(value: E) =
    db.execute(insert, value)

  override fun insert(values: Collection<E>) =
    db.execute(insert, values)
}
