package uk.tvidal.data.query

class TableQuery<in E>(
  sql: String,
  override val parameters: Collection<ParameterProperty<E>>,
) : SimpleQuery(sql) {

  operator fun get(entity: E): Collection<ParameterValue> = parameters.map {
    ParameterValue(it.index, it.name, it[entity])
  }
}
