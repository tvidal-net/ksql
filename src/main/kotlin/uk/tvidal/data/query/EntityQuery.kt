package uk.tvidal.data.query

import uk.tvidal.data.fieldName
import kotlin.reflect.KProperty1

class EntityQuery<in E>(
  sql: String,
  override val params: Collection<Param<E>>,
) : SimpleQuery(sql) {

  operator fun get(entity: E): Collection<QueryParam.Value> = params.map {
    QueryParam.Value(it.index, it.name, it[entity])
  }

  data class Param<in E>(
    override val index: Int,
    val property: KProperty1<in E, *>,
  ) : QueryParam {

    override val name: String
      get() = property.fieldName

    operator fun get(value: E): Any? = property(value)

    override fun toString() = "$index:$name"
  }
}
