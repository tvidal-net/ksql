package uk.tvidal.data.filter

sealed interface SqlFilter {

  val values: Collection<Any?>

  companion object {
    const val AND = " AND "
    const val OR = " OR "
    const val EQ = " = "
    const val NE = " != "
    const val GT = " > "
    const val LT = " < "
    const val GE = " >= "
    const val LE = " <= "
    const val LIKE = " LIKE "
    const val BETWEEN = " BETWEEN "
    const val IN = " IN "
    const val IS_NULL = " IS NULL"
    const val IS_NOT_NULL = " IS NOT NULL"
  }
}
