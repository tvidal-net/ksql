package uk.tvidal.data.query

import uk.tvidal.data.filter.SqlFilter
import kotlin.reflect.KClass

class ComplexQuery(
  val where: MutableCollection<SqlFilter> = ArrayList(),
  val from: MutableCollection<From> = ArrayList(),
) {

  private val nextAlias: String
    get() = "Q${from.size}"

  fun from(entity: KClass<*>) = apply {
    from.add(From.Entity(nextAlias, entity))
  }

  fun from(query: ComplexQuery) = apply {
    from.add(From.SubQuery(nextAlias, query))
  }

  enum class JoinType {
    Cross,
    Inner,
    Left,
    Right,
    Full
  }

  sealed interface From {
    val alias: String

    data class Entity(
      override val alias: String,
      val entity: KClass<*>
    ) : From

    data class SubQuery(
      override val alias: String,
      val query: ComplexQuery
    ) : From

    data class Join(
      override val alias: String,
      val type: JoinType,
      val from: From
    ) : From
  }
}
