package uk.tvidal.data.filter

enum class SqlOperator(val sql: String) {

  Equals(" = "),
  NotEquals(" != "),
  GreaterThan(" > "),
  LessThan(" < "),
  GreaterEquals(" >= "),
  LessEquals(" <= "),
  Like(" LIKE "),
  Between(" BETWEEN "),
  In(" IN ");

  override fun toString() = sql
}
