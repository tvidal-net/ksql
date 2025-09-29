package uk.tvidal.data.codec

import java.sql.ResultSet
import kotlin.reflect.KClass

class EnumDecoder(val type: KClass<out Enum<*>>) : ResultSetDecoder<Enum<*>> {

  private val values: Map<String, Enum<*>> = type.java.enumConstants
    .associateBy { it.name.uppercase() }

  override fun invoke(rs: ResultSet, fieldName: String): Enum<*>? {
    val name = rs.getString(fieldName)
    if (name != null) {
      val enumValue = values[name.uppercase()]
        ?: throw IllegalArgumentException("$name is not a valid value for enum ${type.qualifiedName}")

      return enumValue
    }
    return null
  }
}
