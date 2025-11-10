package uk.tvidal.data.codec

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.sql.Date
import java.sql.Time
import java.sql.Timestamp
import java.time.Duration
import java.time.LocalDateTime

class JdbcValueCodecTest {

  @Test
  fun decodePrimitive() {
    val codec = JdbcValueCodec.Primitive<JdbcValueCodecTest>()
    assertThat(codec.decode(this))
      .isSameAs(this)
  }

  @Test
  fun encodePrimitive() {
    val codec = JdbcValueCodec.Primitive<JdbcValueCodecTest>()
    assertThat(codec.encode(this))
      .isSameAs(this)
  }

  @Test
  fun decodeInstant() {
    val codec = JdbcValueCodec.InstantCodec
    assertThat(codec.decode(timestamp))
      .isEqualTo(timestamp.toInstant())
  }

  @Test
  fun encodeInstant() {
    val codec = JdbcValueCodec.InstantCodec
    assertThat(codec.encode(timestamp.toInstant()))
      .isEqualTo(timestamp)
  }

  @Test
  fun decodeLocalDateTime() {
    val codec = JdbcValueCodec.LocalDateTimeCodec
    assertThat(codec.decode(timestamp))
      .isEqualTo(localDateTime)
  }

  @Test
  fun encodeLocalDateTime() {
    val codec = JdbcValueCodec.LocalDateTimeCodec
    assertThat(codec.encode(localDateTime))
      .isEqualTo(timestamp)
  }

  @Test
  fun decodeLocalDate() {
    val codec = JdbcValueCodec.LocalDateCodec
    assertThat(codec.decode(date))
      .isEqualTo(localDate)
  }

  @Test
  fun encodeLocalDate() {
    val codec = JdbcValueCodec.LocalDateCodec
    assertThat(codec.encode(localDate))
      .isEqualTo(date)
  }

  @Test
  fun decodeLocalTime() {
    val codec = JdbcValueCodec.LocalTimeCodec
    assertThat(codec.decode(time))
      .isEqualTo(localTime)
  }

  @Test
  fun encodeLocalTime() {
    val codec = JdbcValueCodec.LocalTimeCodec
    assertThat(codec.encode(localTime))
      .isEqualTo(time)
  }

  @Test
  fun decodeString() {
    val codec = JdbcValueCodec.StringCodec(Duration::parse)
    assertThat(codec.decode(duration.toString()))
      .isEqualTo(duration)
  }

  @Test
  fun encodeString() {
    val codec = JdbcValueCodec.StringCodec(Duration::parse)
    assertThat(codec.encode(duration))
      .isEqualTo(duration.toString())
  }

  @Test
  fun decodeEnum() {
    val codec = JdbcValueCodec.StringCodec(TestEnum::valueOf)
    assertThat(codec.decode("Some"))
      .isEqualTo(TestEnum.Some)
  }

  @Test
  fun encodeEnum() {
    val codec = JdbcValueCodec.StringCodec(TestEnum::valueOf)
    assertThat(codec.encode(TestEnum.None))
      .isEqualTo("None")
  }

  @Test
  fun decodeJackson() {
    val codec = JdbcValueCodec.Jackson(mapper, TestClass::class)
    assertThat(codec.decode(json))
      .isEqualTo(instance)
  }

  @Test
  fun encodeJackson() {
    val codec = JdbcValueCodec.Jackson(mapper, TestClass::class)
    assertThat(codec.encode(instance))
      .isEqualTo(json)
  }

  private enum class TestEnum { None, Some }

  data class TestClass(
    val name: String,
    val age: Int
  )

  companion object {
    private val mapper = ObjectMapper()
      .registerKotlinModule()

    private val localDateTime = LocalDateTime.of(1982, 1, 18, 5, 45, 13)
    private val timestamp = Timestamp.valueOf(localDateTime)
    private val localDate = localDateTime.toLocalDate()
    private val date = Date.valueOf(localDate)
    private val localTime = localDateTime.toLocalTime()
    private val time = Time.valueOf(localTime)
    private val duration = Duration.ofMinutes(1)

    private val instance = TestClass("John Smith", 39)
    private val json = mapper.writeValueAsString(instance)
  }
}
