package uk.tvidal.data.codec

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.tvidal.data.schema.SchemaConfig
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

@Suppress("AssertBetweenInconvertibleTypes")
class DataTypeTest {

  val config = SchemaConfig.Default

  enum class EnumTestType {
    NONE,
    OTHER;
  }

  data class JsonType(
    val contents: String
  )

  data class T(
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
    @Column(scale = 3) val scaledDecimal: BigDecimal,
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
    assertThat(DataType.from(T::boolean)).isEqualTo(DataType.Boolean)
  }

  @Test
  fun fromIntegers() {
    assertThat(DataType.from(T::tinyInt)).isEqualTo(DataType.TinyInt)
    assertThat(DataType.from(T::smallInt)).isEqualTo(DataType.SmallInt)
    assertThat(DataType.from(T::integer)).isEqualTo(DataType.Integer)
    assertThat(DataType.from(T::bigInt)).isEqualTo(DataType.BigInt)
  }

  @Test
  fun fromFloatingPoint() {
    assertThat(DataType.from(T::double)).isEqualTo(DataType.Double)
    assertThat(DataType.from(T::float)).isEqualTo(DataType.Float)
  }

  @Test
  fun fromDateType() {
    assertThat(DataType.from(T::localDateTime)).isEqualTo(DataType.DateTime)
    assertThat(DataType.from(T::date)).isEqualTo(DataType.DateTime)
    assertThat(DataType.from(T::timestamp)).isEqualTo(DataType.DateTime)
    assertThat(DataType.from(T::localDate)).isEqualTo(DataType.Date)
    assertThat(DataType.from(T::sqlDate)).isEqualTo(DataType.Date)
    assertThat(DataType.from(T::localTime)).isEqualTo(DataType.Time)
    assertThat(DataType.from(T::sqlTime)).isEqualTo(DataType.Time)
  }

  @Test
  fun fromDecimal() {
    assertThat(DataType.from(T::decimal)).isEqualTo(config.decimal())
    assertThat(DataType.from(T::scaledDecimal)).isEqualTo(DataType.Decimal(3))
    assertThat(DataType.from(T::precisionDecimal)).isEqualTo(DataType.Decimal(5, 2))
  }

  @Test
  fun fromString() {
    assertThat(DataType.from(T::string)).isEqualTo(config.string())
    assertThat(DataType.from(T::regex)).isEqualTo(DataType.Text)
    assertThat(DataType.from(T::pattern)).isEqualTo(DataType.Text)
    assertThat(DataType.from(T::enum)).isEqualTo(config.shortString())
  }

  @Test
  fun fromNullable() {
    assertThat(DataType.from(T::nullableString)).isEqualTo(config.string())
  }

  @Test
  fun fromOtherTypes() {
    assertThat(DataType.from(T::uuid)).isEqualTo(DataType.UUID)
    //assertThat(DataType.from(T::json)).isEqualTo(DataType.Json(JsonType::class))
  }
}
