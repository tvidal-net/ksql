package uk.tvidal.data.filter

sealed interface SqlFilter {

  val values: Collection<Any?>

  companion object {
    const val AND = " AND "
    const val OR = " OR "
    const val IS_NULL = " IS NULL"
    const val IS_NOT_NULL = " IS NOT NULL"
  }
}
