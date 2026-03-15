package uk.tvidal.data

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import uk.tvidal.data.Config.Constants.Default
import uk.tvidal.data.codec.ValueType
import uk.tvidal.data.database.Amount
import uk.tvidal.data.database.Currency
import uk.tvidal.data.database.ValueTypes.Companion.amountValueType
import java.math.BigDecimal
import javax.persistence.Column

class ConfigTest {

  @Test
  fun defaultEnumType() {
    val type = Default.enumType(Currency::class)
    assertThat(type.length).isEqualTo(3)

    val codec = type.jdbcCodec
    assertThat(codec.decode("gbp")).isEqualTo(Currency.GBP)
    assertThat(codec.decode("eur")).isEqualTo(Currency.EUR)
    assertThat(codec.decode("usd")).isEqualTo(Currency.USD)
  }

  @Test
  fun customEnumType() {
    val config = Config(enumIgnoreCase = false)
    val type = config.enumType(Currency::class)
    assertThat(type.length).isEqualTo(3)

    val codec = type.jdbcCodec
    assertThrows<NoSuchElementException> { codec.decode("gbp") }
    assertThrows<NoSuchElementException> { codec.decode("eur") }
    assertThrows<NoSuchElementException> { codec.decode("usd") }

    assertThat(codec.decode("GBP")).isEqualTo(Currency.GBP)
    assertThat(codec.decode("EUR")).isEqualTo(Currency.EUR)
    assertThat(codec.decode("USD")).isEqualTo(Currency.USD)
  }

  @Test
  fun enumTypeLength() {
    val type = Default.enumType(Currency::class, Column(length = 5))
    assertThat(type.length).isEqualTo(5)
  }

  @Test
  fun defaultStringType() {
    assertThat(Default.valueType(String::class))
      .isEqualTo(Default.string)
  }

  @Test
  fun customStringType() {
    val stringValueType = ValueType.VarChar(11)
    val config = Config(string = stringValueType)
    assertThat(config.valueType(String::class))
      .isEqualTo(stringValueType)
  }

  @Test
  fun stringTypeLength() {
    assertThat(Default.string(Column(length = 9)))
      .isEqualTo(ValueType.NVarChar(9))
  }

  @Test
  fun defaultDecimalType() {
    assertThat(Default.valueType(BigDecimal::class))
      .isEqualTo(Default.decimal)
  }

  @Test
  fun customDecimalType() {
    val decimalValueType = ValueType.Decimal(9)
    val config = Config(decimal = decimalValueType)
    assertThat(config.valueType(BigDecimal::class))
      .isEqualTo(decimalValueType)
  }

  @Test
  fun decimalTypeScale() {
    assertThat(Default.valueType(BigDecimal::class, Column(scale = 9)))
      .isEqualTo(ValueType.Decimal(9, null))
  }

  @Test
  fun decimalTypeScalePrecision() {
    assertThat(Default.valueType(BigDecimal::class, Column(scale = 11, precision = 3)))
      .isEqualTo(ValueType.Decimal(11, 3))
  }

  @Test
  fun overriddenValueTypes() {
    assertThat(Default.valueType(Amount::class))
      .isEqualTo(amountValueType)
  }

  @Test
  fun enumValueType() {
    assertThat(Default.valueType(Currency::class, Column(length = 7)))
      .isEqualTo(ValueType.EnumType(Currency::class, 7))
  }

  @Test
  fun numericValueType() {
    assertThat(Default.valueType(Long::class))
      .isEqualTo(ValueType.BigInt)
    assertThat(Default.valueType(Int::class))
      .isEqualTo(ValueType.Integer)
    assertThat(Default.valueType(Short::class))
      .isEqualTo(ValueType.SmallInt)
    assertThat(Default.valueType(Byte::class))
      .isEqualTo(ValueType.TinyInt)
    assertThat(Default.valueType(BigDecimal::class))
      .isEqualTo(Default.decimal)
  }

  companion object {

    @JvmStatic
    @BeforeAll
    fun setUp() {
      Default.register { amountValueType }
    }
  }
}
