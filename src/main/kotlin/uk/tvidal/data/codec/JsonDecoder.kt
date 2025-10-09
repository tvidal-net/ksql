package uk.tvidal.data.codec

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import java.sql.ResultSet
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.UUID
import kotlin.reflect.KClass

class JsonDecoder<out T : Any>(type: KClass<out T>) : ResultSetDecoder<T> {

  private val reader = json.readerFor(type.java)

  override fun getResultSetValue(rs: ResultSet, columnLabel: String): T? =
    rs.getString(columnLabel)
      ?.let(reader::readValue)

  companion object {
    private val json = ObjectMapper().registerModule(
      SimpleModule().apply {
        register(UUID::fromString)
        register(LocalDate::parse)
        register(LocalTime::parse)
        register(LocalDateTime::parse)
      }
    ).apply {
      findAndRegisterModules()
    }

    internal fun toString(value: Any?) =
      json.writeValueAsString(value)
  }
}
