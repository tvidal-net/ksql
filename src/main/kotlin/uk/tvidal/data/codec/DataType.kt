package uk.tvidal.data.codec

import uk.tvidal.data.Config
import uk.tvidal.data.column
import uk.tvidal.data.logging.KLogging
import uk.tvidal.data.str
import uk.tvidal.data.valueType
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Types
import java.time.LocalDateTime
import java.util.Objects
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.full.allSupertypes
import kotlin.reflect.full.isSubclassOf

open class DataType<J, T : Any>(
  val jdbcValueCodec: JdbcValueCodec<J, T>,
  val setParam: SetParamValue<J>,
  val getValue: GetResultSetValue<J>,
) : ParamValueEncoder<T>, ResultSetDecoder<T> {

  open val length: Int?
    get() = null

  open val sqlDataType: String
    get() = "VARCHAR(${LENGTH})"

  override fun setParamValue(st: PreparedStatement, index: Int, value: T?) {
    if (value != null) {
      val encodedValue = jdbcValueCodec.encode(value).debug {
        "setParamValue(index=$index, value=${str(value)}) encoded=${str(it)}"
      }
      setParam(st, index, encodedValue)
    } else {
      debug { "setParamValue(index=$index, value=${str(value)}" }
      st.setNull(index, Types.NULL)
    }
  }

  override fun getResultSetValue(rs: ResultSet, field: String): T? = getValue(rs, field)?.let {
    if (rs.wasNull()) {
      null
    } else {
      jdbcValueCodec.decode(it)
    }.debug { value ->
      "getResultSetValue(field=${str(field)} value=${str(it)} decoded=${str(value)}"
    }
  }

  open class Primitive<T : Any>(
    setParam: SetParamValue<T>,
    getValue: GetResultSetValue<T>,
  ) : DataType<T, T>(
    setParam = setParam,
    getValue = getValue,
    jdbcValueCodec = JdbcValueCodec.Primitive(),
  )

  object Boolean : Primitive<kotlin.Boolean>(
    setParam = PreparedStatement::setBoolean,
    getValue = ResultSet::getBoolean,
  ) {
    override val sqlDataType: String
      get() = "BOOLEAN"
  }

  object TinyInt : Primitive<Byte>(
    setParam = PreparedStatement::setByte,
    getValue = ResultSet::getByte,
  ) {
    override val sqlDataType: String
      get() = "TINYINT"
  }

  object SmallInt : Primitive<Short>(
    setParam = PreparedStatement::setShort,
    getValue = ResultSet::getShort,
  ) {
    override val sqlDataType: String
      get() = "SMALLINT"
  }

  object Integer : Primitive<Int>(
    setParam = PreparedStatement::setInt,
    getValue = ResultSet::getInt
  ) {
    override val sqlDataType: String
      get() = "INTEGER"
  }

  object BigInt : Primitive<Long>(
    setParam = PreparedStatement::setLong,
    getValue = ResultSet::getLong,
  ) {
    override val sqlDataType: String
      get() = "BIGINT"
  }

  object Double : Primitive<kotlin.Double>(
    setParam = PreparedStatement::setDouble,
    getValue = ResultSet::getDouble,
  ) {
    override val sqlDataType: String
      get() = "DOUBLE PRECISION"
  }

  object Float : Primitive<kotlin.Float>(
    setParam = PreparedStatement::setFloat,
    getValue = ResultSet::getFloat,
  ) {
    override val sqlDataType: String
      get() = "FLOAT"
  }

  open class SqlTimestamp<T : Any>(
    codec: JdbcValueCodec<java.sql.Timestamp, T>,
  ) : DataType<java.sql.Timestamp, T>(
    jdbcValueCodec = codec,
    setParam = PreparedStatement::setTimestamp,
    getValue = ResultSet::getTimestamp,
  ) {
    override val sqlDataType: String
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
  ) : DataType<java.sql.Date, T>(
    jdbcValueCodec = codec,
    setParam = PreparedStatement::setDate,
    getValue = ResultSet::getDate,
  ) {
    override val sqlDataType: String
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
  ) : DataType<java.sql.Time, T>(
    jdbcValueCodec = codec,
    setParam = PreparedStatement::setTime,
    getValue = ResultSet::getTime,
  ) {
    override val sqlDataType: String
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
  ) : DataType<String, T>(
    jdbcValueCodec = JdbcValueCodec.StringCodec(decoder),
    setParam = PreparedStatement::setString,
    getValue = ResultSet::getString,
  ) {
    override val sqlDataType: String
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

    override val sqlDataType: String
      get() = "CHAR($length)"
  }

  object Text : Primitive<String>(
    setParam = PreparedStatement::setNString,
    getValue = ResultSet::getNString
  ) {
    override val sqlDataType: String
      get() = "TEXT"
  }

  object UUID : ShortString<java.util.UUID>(
    decoder = java.util.UUID::fromString,
  ) {
    override val sqlDataType: String
      get() = "UUID"
  }

  object Duration : ShortString<java.time.Duration>(
    decoder = java.time.Duration::parse
  )

  class VarChar(override val length: Int) : Primitive<String>(
    setParam = PreparedStatement::setString,
    getValue = ResultSet::getString,
  ) {
    override val sqlDataType: String
      get() = "VARCHAR($length)"
  }

  class NVarChar(override val length: Int) : Primitive<String>(
    setParam = PreparedStatement::setNString,
    getValue = ResultSet::getNString
  ) {
    override val sqlDataType: String
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
    override val sqlDataType: String
      get() = "NUMERIC$scale${nullable(precision)}"
  }

  class Decimal(
    val scale: Int,
    val precision: Int? = null
  ) : BigDecimal() {
    override val sqlDataType: String
      get() = "DECIMAL($scale${nullable(precision)})"
  }

  internal fun valueType(): KClass<T> {
    val dataType = this::class.allSupertypes
      .single { it.classifier == DataType::class }

    @Suppress("UNCHECKED_CAST")
    return dataType.arguments.last().type!!.classifier!! as KClass<T>
  }

  override fun hashCode(): Int {
    return Objects.hash(sqlDataType, jdbcValueCodec, setParam, getValue)
  }

  override fun equals(other: Any?) = other is DataType<*, *>
    && sqlDataType == other.sqlDataType
    && jdbcValueCodec == other.jdbcValueCodec
    && setParam == other.setParam
    && getValue == other.getValue

  override fun toString() = "${this::class.simpleName} $sqlDataType"

  companion object : KLogging() {

    const val SHORT_STRING = 0x20
    const val LENGTH = 0x400

    const val DEFAULT_SCALE = 13
    const val DEFAULT_PRECISION = 2

    val All = DataType::class
      .nestedClasses
      .mapNotNull { it.objectInstance }
      .filterIsInstance<DataType<*, *>>()
      .map { it.valueType() to it }

    fun from(type: KClass<*>) = All.firstNotNullOfOrNull { (valueType, dataType) ->
      if (!valueType.isSubclassOf(type)) null
      else dataType
    }

    private fun nullable(precision: Int?) =
      precision?.let { ",$precision" } ?: ""

    fun from(property: KProperty<*>, config: Config = Config.Default): DataType<*, *>? {
      val valueType = property.valueType
      return when {
        valueType.java.isEnum ->
          config.enumType(valueType, property.column)

        valueType.isSubclassOf(CharSequence::class) ->
          config.string(property.column)

        else -> from(valueType) ?: when {
          valueType.isSubclassOf(Number::class) ->
            config.decimal(property.column)

          else ->
            null
        }
      }
    }
  }
}
