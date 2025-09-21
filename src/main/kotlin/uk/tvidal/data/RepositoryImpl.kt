package uk.tvidal.data

import uk.tvidal.data.codec.EntityDecoder
import uk.tvidal.data.filter.SqlFilter
import uk.tvidal.data.model.fields
import uk.tvidal.data.model.keyFields
import uk.tvidal.data.model.nonKeyFields
import uk.tvidal.data.query.EntityQuery
import uk.tvidal.data.query.Query
import kotlin.reflect.KClass

internal class RepositoryImpl<E : Any>(
  override val db: Database,
  val decoder: EntityDecoder<E>,
  override val entity: KClass<out E>,
) : Repository<E> {

  private val selectByKey: Query by lazy {
    selectQuery(entity.keyFilter)
  }

  private val selectAll: Query by lazy {
    selectQuery(null)
  }

  private val save: EntityQuery<E> by lazy {
    db.dialect.save(entity, entity.nonKeyFields, entity.keyFields)
  }

  private val delete: EntityQuery<E> by lazy {
    db.dialect.delete(entity, entity.keyFields)
  }

  private val update: EntityQuery<E> by lazy {
    db.dialect.update(entity, entity.nonKeyFields, entity.keyFields)
  }

  private val insert: EntityQuery<E> by lazy {
    db.dialect.insert(entity, entity.fields)
  }

  private fun selectQuery(where: SqlFilter?): Query =
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
