package uk.tvidal.data.query

class EntityQuery<in E>(
  sql: String,
  override val parameters: Collection<ParameterProperty<E>>,
) : Query(sql) {

  operator fun get(entity: E): Collection<ParameterValue> = parameters.map {
    ParameterValue(it.index, it.name, it[entity])
  }
}
