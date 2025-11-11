package uk.tvidal.data.codec

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.tvidal.data.Config
import java.math.BigDecimal
import java.sql.Timestamp
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.Date
import java.util.UUID
import java.util.regex.Pattern
import javax.persistence.Column
import javax.persistence.Id

class ValueTypeTest {

  private val config = Config.Default

  private enum class EnumTestType {
    NONE,
    OTHER;
  }

  private data class JsonType(
    val contents: String
  )

  private data class T(
    val boolean: Boolean,
    val tinyInt: Byte,
    val smallInt: Short,
    val integer: Int,
    val bigInt: Long,
    val double: Double,
    val float: Float,
    val localDateTime: LocalDateTime,
    val date: Date,
    val timestamp: Timestamp,
    val localDate: LocalDate,
    val sqlDate: java.sql.Date,
    val localTime: LocalTime,
    val sqlTime: java.sql.Time,
    val decimal: BigDecimal,
    @Column(scale = 3) val scaleDecimal: BigDecimal,
    @Column(scale = 5, precision = 2) val precisionDecimal: BigDecimal,
    val string: String,
    val nullableString: String?,
    val regex: Regex?,
    val pattern: Pattern?,
    @Id val uuid: UUID,
    val enum: EnumTestType,
    val json: JsonType
  )

  @Test
  fun fromBoolean() {
    assertThat(config.fieldType(T::boolean)).isEqualTo(ValueType.Boolean)
  }

  @Test
  fun fromIntegers() {
    assertThat(config.fieldType(T::tinyInt)).isEqualTo(ValueType.TinyInt)
    assertThat(config.fieldType(T::smallInt)).isEqualTo(ValueType.SmallInt)
    assertThat(config.fieldType(T::integer)).isEqualTo(ValueType.Integer)
    assertThat(config.fieldType(T::bigInt)).isEqualTo(ValueType.BigInt)
  }

  @Test
  fun fromFloatingPoint() {
    assertThat(config.fieldType(T::double)).isEqualTo(ValueType.DoublePrecision)
    assertThat(config.fieldType(T::float)).isEqualTo(ValueType.SinglePrecision)
  }

  @Test
  fun fromDateType() {
    assertThat(config.fieldType(T::localDateTime)).isEqualTo(ValueType.LocalDateTime)
    assertThat(config.fieldType(T::date)).isEqualTo(ValueType.SqlTimestamp)
    assertThat(config.fieldType(T::timestamp)).isEqualTo(ValueType.SqlTimestamp)
    assertThat(config.fieldType(T::localDate)).isEqualTo(ValueType.LocalDate)
    assertThat(config.fieldType(T::sqlDate)).isEqualTo(ValueType.SqlDate)
    assertThat(config.fieldType(T::localTime)).isEqualTo(ValueType.LocalTime)
    assertThat(config.fieldType(T::sqlTime)).isEqualTo(ValueType.SqlTime)
  }

  @Test
  fun fromDecimal() {
    assertThat(config.fieldType(T::decimal)).isEqualTo(config.decimal(null))
    assertThat(config.fieldType(T::scaleDecimal)).isEqualTo(ValueType.Decimal(3))
    assertThat(config.fieldType(T::precisionDecimal)).isEqualTo(ValueType.Decimal(5, 2))
  }

  @Test
  fun fromString() {
    assertThat(config.fieldType(T::string)).isEqualTo(config.string(null))
    assertThat(config.fieldType(T::enum))
      .isExactlyInstanceOf(ValueType.EnumType::class.java)
  }

  @Test
  fun fromNullable() {
    assertThat(config.fieldType(T::nullableString)).isEqualTo(config.string(null))
  }

  @Test
  fun fromOtherTypes() {
    assertThat(config.fieldType(T::uuid)).isEqualTo(ValueType.UUID)
    //assertThat(DataType.from(T::json)).isEqualTo(DataType.Json(JsonType::class))
  }
}
