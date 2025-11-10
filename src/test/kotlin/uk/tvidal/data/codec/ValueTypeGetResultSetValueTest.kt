package uk.tvidal.data.codec

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.math.BigDecimal
import java.sql.Date
import java.sql.ResultSet
import java.sql.Time
import java.sql.Timestamp
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.UUID

class ValueTypeGetResultSetValueTest {

  val rs = mock<ResultSet>()

  @Test
  fun booleanValue() {
    whenever(rs.getBoolean(eq("boolean")))
      .thenReturn(true)

    assertThat(ValueType.Boolean.getResultSetValue(rs, "boolean"))
      .isTrue

    verify(rs).getBoolean(eq("boolean"))
  }

  @Test
  fun tinyIntValue() {
    whenever(rs.getByte(eq("tinyint")))
      .thenReturn(Byte.MAX_VALUE)

    assertThat(ValueType.TinyInt.getResultSetValue(rs, "tinyint"))
      .isEqualTo(Byte.MAX_VALUE)

    verify(rs).getByte(eq("tinyint"))
  }

  @Test
  fun smallIntValue() {
    whenever(rs.getShort(eq("smallint")))
      .thenReturn(Short.MAX_VALUE)

    assertThat(ValueType.SmallInt.getResultSetValue(rs, "smallint"))
      .isEqualTo(Short.MAX_VALUE)

    verify(rs).getShort(eq("smallint"))
  }

  @Test
  fun integerValue() {
    whenever(rs.getInt(eq("integer")))
      .thenReturn(Int.MAX_VALUE)

    assertThat(ValueType.Integer.getResultSetValue(rs, "integer"))
      .isEqualTo(Int.MAX_VALUE)

    verify(rs).getInt(eq("integer"))
  }

  @Test
  fun bigIntValue() {
    whenever(rs.getLong(eq("bigint")))
      .thenReturn(Long.MAX_VALUE)

    assertThat(ValueType.BigInt.getResultSetValue(rs, "bigint"))
      .isEqualTo(Long.MAX_VALUE)

    verify(rs).getLong(eq("bigint"))
  }

  @Test
  fun doubleValue() {
    whenever(rs.getDouble(eq("double")))
      .thenReturn(Double.MAX_VALUE)

    assertThat(ValueType.Double.getResultSetValue(rs, "double"))
      .isEqualTo(Double.MAX_VALUE)

    verify(rs).getDouble(eq("double"))
  }

  @Test
  fun floatValue() {
    whenever(rs.getFloat(eq("float")))
      .thenReturn(Float.MAX_VALUE)

    assertThat(ValueType.Float.getResultSetValue(rs, "float"))
      .isEqualTo(Float.MAX_VALUE)

    verify(rs).getFloat(eq("float"))
  }

  @Test
  fun timestampValue() {
    val expected = Timestamp.valueOf(LocalDateTime.now())
    whenever(rs.getTimestamp(eq("timestamp")))
      .thenReturn(expected)

    assertThat(ValueType.Timestamp.getResultSetValue(rs, "timestamp"))
      .isEqualTo(expected)

    verify(rs).getTimestamp(eq("timestamp"))
  }

  @Test
  fun dateTimeValue() {
    val expected = LocalDateTime.now()
    whenever(rs.getTimestamp(eq("localDateTime")))
      .thenReturn(Timestamp.valueOf(expected))

    assertThat(ValueType.DateTime.getResultSetValue(rs, "localDateTime"))
      .isEqualTo(expected)

    verify(rs).getTimestamp(eq("localDateTime"))
  }

  @Test
  fun instantValue() {
    val expected = Instant.now()
    whenever(rs.getTimestamp(eq("instant")))
      .thenReturn(Timestamp.from(expected))

    assertThat(ValueType.Instant.getResultSetValue(rs, "instant"))
      .isEqualTo(expected)

    verify(rs).getTimestamp(eq("instant"))
  }

  @Test
  fun dateValue() {
    val expected = Date.valueOf(LocalDate.now())
    whenever(rs.getDate(eq("date")))
      .thenReturn(expected)

    assertThat(ValueType.Date.getResultSetValue(rs, "date"))
      .isEqualTo(expected)

    verify(rs).getDate(eq("date"))
  }

  @Test
  fun localDateValue() {
    val expected = LocalDate.now()
    whenever(rs.getDate(eq("localDate")))
      .thenReturn(Date.valueOf(expected))

    assertThat(ValueType.LocalDate.getResultSetValue(rs, "localDate"))
      .isEqualTo(expected)

    verify(rs).getDate(eq("localDate"))
  }

  @Test
  fun timeValue() {
    val expected = Time.valueOf(LocalTime.now())
    whenever(rs.getTime(eq("time")))
      .thenReturn(expected)

    assertThat(ValueType.Time.getResultSetValue(rs, "time"))
      .isEqualTo(expected)

    verify(rs).getTime(eq("time"))
  }

  @Test
  fun localTimeValue() {
    val expected = LocalTime.of(1, 2, 3)
    whenever(rs.getTime(eq("localTime")))
      .thenReturn(Time.valueOf(expected))

    assertThat(ValueType.LocalTime.getResultSetValue(rs, "localTime"))
      .isEqualTo(expected)

    verify(rs).getTime(eq("localTime"))
  }

  @Test
  fun enumValue() {
    whenever(rs.getString(eq("enum")))
      .thenReturn(TestEnum.Value.name)

    assertThat(ValueType.EnumType(TestEnum::class).getResultSetValue(rs, "enum"))
      .isEqualTo(TestEnum.Value)

    verify(rs).getString(eq("enum"))
  }

  @Test
  fun textValue() {
    whenever(rs.getNString(eq("text")))
      .thenReturn("expected")

    assertThat(ValueType.Text.getResultSetValue(rs, "text"))
      .isEqualTo("expected")

    verify(rs).getNString(eq("text"))
  }

  @Test
  fun uuidValue() {
    val expected = UUID.randomUUID()
    whenever(rs.getString(eq("uuid")))
      .thenReturn(expected.toString())

    assertThat(ValueType.UUID.getResultSetValue(rs, "uuid"))
      .isEqualTo(expected)

    verify(rs).getString(eq("uuid"))
  }

  @Test
  fun durationValue() {
    val expected = Duration.ofMinutes(13)
    whenever(rs.getString(eq("duration")))
      .thenReturn(expected.toString())

    assertThat(ValueType.Duration.getResultSetValue(rs, "duration"))
      .isEqualTo(expected)

    verify(rs).getString(eq("duration"))
  }

  @Test
  fun varCharValue() {
    whenever(rs.getString(eq("varchar")))
      .thenReturn("string")

    assertThat(ValueType.VarChar(64).getResultSetValue(rs, "varchar"))
      .isEqualTo("string")

    verify(rs).getString(eq("varchar"))
  }

  @Test
  fun nVarCharValue() {
    whenever(rs.getNString(eq("nvarchar")))
      .thenReturn("string")

    assertThat(ValueType.NVarChar(64).getResultSetValue(rs, "nvarchar"))
      .isEqualTo("string")

    verify(rs).getNString(eq("nvarchar"))
  }

  @Test
  fun numericValue() {
    val expected = BigDecimal("123456789.123456")
    whenever(rs.getBigDecimal(eq("numeric")))
      .thenReturn(expected)

    assertThat(ValueType.Numeric(9, 6).getResultSetValue(rs, "numeric"))
      .isEqualTo(expected)

    verify(rs).getBigDecimal(eq("numeric"))
  }

  @Test
  fun decimalValue() {
    val expected = BigDecimal("123456789.123456")
    whenever(rs.getBigDecimal(eq("decimal")))
      .thenReturn(expected)

    assertThat(ValueType.Decimal(9, 6).getResultSetValue(rs, "decimal"))
      .isEqualTo(expected)

    verify(rs).getBigDecimal(eq("decimal"))
  }

  private enum class TestEnum { Value }
}
