package uk.tvidal.data.codec

import uk.tvidal.data.logging.KLogging
import uk.tvidal.data.str
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Types
import java.time.LocalDateTime
import java.util.Objects
import kotlin.reflect.KClass
import kotlin.reflect.full.allSupertypes
import kotlin.reflect.full.isSubclassOf

open class ValueType<J, T : Any>(
  val jdbcCodec: JdbcValueCodec<J, T>,
  val setParam: SetParamValue<J>,
  val getValue: GetResultSetValue<J>,
) : ParamValueEncoder<T>, ResultSetDecoder<T> {

  open val length: Int?
    get() = null

  open val dataType: String
    get() = "VARCHAR(${LENGTH})"

  override fun setParamValue(st: PreparedStatement, index: Int, value: T?) {
    if (value != null) {
      val encodedValue = jdbcCodec.encode(value).trace {
        val message = if ("$it" == "$value") "" else " value=${str(it)}"
        "setParamValue($index=${str(value)})$message"
      }
      setParam(st, index, encodedValue)
    } else {
      trace { "setParamValue($index=NULL)" }
      st.setNull(index, Types.NULL)
    }
  }

  override fun getResultSetValue(rs: ResultSet, field: String): T? = getValue(rs, field)?.let {
    if (rs.wasNull()) {
      null
    } else {
      jdbcCodec.decode(it)
    }.trace { value ->
      val message = if ("$it" == "$value") "" else " value=${str(value)}"
      "getResultSetValue(${str(field)}=${str(it)})$message"
    }
  }

  open class Primitive<T : Any>(
    setParam: SetParamValue<T>,
    getValue: GetResultSetValue<T>,
  ) : ValueType<T, T>(
    setParam = setParam,
    getValue = getValue,
    jdbcCodec = JdbcValueCodec.Primitive(),
  )

  object Boolean : Primitive<kotlin.Boolean>(
    setParam = PreparedStatement::setBoolean,
    getValue = ResultSet::getBoolean,
  ) {
    override val dataType: String
      get() = "BOOLEAN"
  }

  object TinyInt : Primitive<Byte>(
    setParam = PreparedStatement::setByte,
    getValue = ResultSet::getByte,
  ) {
    override val dataType: String
      get() = "TINYINT"
  }

  object SmallInt : Primitive<Short>(
    setParam = PreparedStatement::setShort,
    getValue = ResultSet::getShort,
  ) {
    override val dataType: String
      get() = "SMALLINT"
  }

  object Integer : Primitive<Int>(
    setParam = PreparedStatement::setInt,
    getValue = ResultSet::getInt
  ) {
    override val dataType: String
      get() = "INTEGER"
  }

  object BigInt : Primitive<Long>(
    setParam = PreparedStatement::setLong,
    getValue = ResultSet::getLong,
  ) {
    override val dataType: String
      get() = "BIGINT"
  }

  object Double : Primitive<kotlin.Double>(
    setParam = PreparedStatement::setDouble,
    getValue = ResultSet::getDouble,
  ) {
    override val dataType: String
      get() = "DOUBLE PRECISION"
  }

  object Float : Primitive<kotlin.Float>(
    setParam = PreparedStatement::setFloat,
    getValue = ResultSet::getFloat,
  ) {
    override val dataType: String
      get() = "FLOAT"
  }

  open class SqlTimestamp<T : Any>(
    codec: JdbcValueCodec<java.sql.Timestamp, T>,
  ) : ValueType<java.sql.Timestamp, T>(
    jdbcCodec = codec,
    setParam = PreparedStatement::setTimestamp,
    getValue = ResultSet::getTimestamp,
  ) {
    override val dataType: String
      get() = "TIMESTAMP"
  }

  object Timestamp : SqlTimestamp<java.sql.Timestamp>(
    codec = JdbcValueCodec.Primitive()
  )

  object DateTime : SqlTimestamp<LocalDateTime>(
    codec = JdbcValueCodec.LocalDateTimeCodec
  )

  object Instant : SqlTimestamp<java.time.Instant>(
    codec = JdbcValueCodec.InstantCodec
  )

  open class SqlDate<T : Any>(
    codec: JdbcValueCodec<java.sql.Date, T>,
  ) : ValueType<java.sql.Date, T>(
    jdbcCodec = codec,
    setParam = PreparedStatement::setDate,
    getValue = ResultSet::getDate,
  ) {
    override val dataType: String
      get() = "DATE"
  }

  object Date : SqlDate<java.sql.Date>(
    codec = JdbcValueCodec.Primitive()
  )

  object LocalDate : SqlDate<java.time.LocalDate>(
    codec = JdbcValueCodec.LocalDateCodec
  )

  open class SqlTime<T : Any>(
    codec: JdbcValueCodec<java.sql.Time, T>,
  ) : ValueType<java.sql.Time, T>(
    jdbcCodec = codec,
    setParam = PreparedStatement::setTime,
    getValue = ResultSet::getTime,
  ) {
    override val dataType: String
      get() = "TIME"
  }

  object Time : SqlTime<java.sql.Time>(
    codec = JdbcValueCodec.Primitive()
  )

  object LocalTime : SqlTime<java.time.LocalTime>(
    codec = JdbcValueCodec.LocalTimeCodec
  )

  open class ShortString<T : Any>(
    decoder: (String) -> T,
    override val length: Int = SHORT_STRING,
  ) : ValueType<String, T>(
    jdbcCodec = JdbcValueCodec.StringCodec(decoder),
    setParam = PreparedStatement::setString,
    getValue = ResultSet::getString,
  ) {
    override val dataType: String
      get() = "VARCHAR($length)"
  }

  class EnumType<E : Enum<E>>(
    val enumClass: KClass<E>,
    val fieldLength: Int? = null,
    val ignoreCase: kotlin.Boolean = true
  ) : ShortString<E>(
    decoder = { value ->
      enumClass.java.enumConstants.single {
        value.equals(it.name, ignoreCase)
      }
    }
  ) {
    override val length: Int
      get() = fieldLength ?: enumClass.java.enumConstants.maxOf { it.name.length }

    override val dataType: String
      get() = "CHAR($length)"
  }

  object Text : Primitive<String>(
    setParam = PreparedStatement::setNString,
    getValue = ResultSet::getNString
  ) {
    override val dataType: String
      get() = "TEXT"
  }

  object UUID : ShortString<java.util.UUID>(
    decoder = java.util.UUID::fromString,
  ) {
    override val dataType: String
      get() = "UUID"
  }

  object Duration : ShortString<java.time.Duration>(
    decoder = java.time.Duration::parse
  )

  class VarChar(override val length: Int) : Primitive<String>(
    setParam = PreparedStatement::setString,
    getValue = ResultSet::getString,
  ) {
    override val dataType: String
      get() = "VARCHAR($length)"
  }

  class NVarChar(override val length: Int) : Primitive<String>(
    setParam = PreparedStatement::setNString,
    getValue = ResultSet::getNString
  ) {
    override val dataType: String
      get() = "NVARCHAR($length)"
  }

  abstract class BigDecimal : Primitive<java.math.BigDecimal>(
    setParam = PreparedStatement::setBigDecimal,
    getValue = ResultSet::getBigDecimal,
  )

  class Numeric(
    val scale: Int,
    val precision: Int? = null
  ) : BigDecimal() {
    override val dataType: String
      get() = "NUMERIC$scale${nullable(precision)}"
  }

  class Decimal(
    val scale: Int,
    val precision: Int? = null
  ) : BigDecimal() {
    override val dataType: String
      get() = "DECIMAL($scale${nullable(precision)})"
  }

  internal fun valueType(): KClass<T> {
    val valueType = this::class.allSupertypes
      .single { it.classifier == ValueType::class }

    @Suppress("UNCHECKED_CAST")
    return valueType.arguments.last().type!!.classifier!! as KClass<T>
  }

  override fun hashCode(): Int {
    return Objects.hash(dataType, jdbcCodec, setParam, getValue)
  }

  override fun equals(other: Any?) = other is ValueType<*, *>
    && dataType == other.dataType
    && jdbcCodec == other.jdbcCodec
    && setParam == other.setParam
    && getValue == other.getValue

  override fun toString() = "${this::class.simpleName} $dataType"

  companion object : KLogging() {

    const val SHORT_STRING = 0x20
    const val LENGTH = 0x400

    const val DEFAULT_SCALE = 13
    const val DEFAULT_PRECISION = 2

    val All = ValueType::class.nestedClasses
      .mapNotNull { it.objectInstance }
      .filterIsInstance<ValueType<*, *>>()
      .map { it.valueType() to it }

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> from(type: KClass<T>): ValueType<*, T>? = All.firstNotNullOfOrNull { (value, valueType) ->
      if (value.isSubclassOf(type)) valueType
      else null
    } as? ValueType<*, T>

    private fun nullable(precision: Int?) =
      precision?.let { ",$precision" } ?: ""
  }
}
