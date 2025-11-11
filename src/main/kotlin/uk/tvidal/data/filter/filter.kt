package uk.tvidal.data.filter

import uk.tvidal.data.sql.SqlQueryBuilder.Constants.SCHEMA_SEP

internal fun String?.whenNotNull(suffix: Any): String =
  this?.let { "$it$suffix" } ?: ""

internal val String?.dot: String
  get() = whenNotNull(SCHEMA_SEP)
