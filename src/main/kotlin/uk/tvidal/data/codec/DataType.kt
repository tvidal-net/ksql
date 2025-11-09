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
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.full.allSupertypes
import kotlin.reflect.full.isSubclassOf

open class DataType<J, T : Any>(
  val sqlDataType: String,
  val codec: JdbcValueCodec<J, T>,
  val setParam: SetParamValue<J>,
  val getValue: GetResultSetValue<J>,
) : ParamValueEncoder<T>, ResultSetDecoder<T> {

  open val length: Int?
    get() = null

  override fun setParamValue(st: PreparedStatement, parameterIndex: Int, value: T?) {
    if (value != null) {
      val encodedValue = codec.encode(value).debug {
        "setParamValue(index=$parameterIndex, value=${str(value)}) encoded=${str(it)}"
      }
      setParam(st, parameterIndex, encodedValue)
    } else {
      debug { "setParamValue(index=$parameterIndex, value=${str(value)}" }
      st.setNull(parameterIndex, Types.NULL)
    }
  }

  override fun getResultSetValue(rs: ResultSet, field: String): T? = getValue(rs, field)?.let {
    if (rs.wasNull()) {
      null
    } else {
      codec.decode(it)
    }.debug { value ->
      "getResultSetValue(field=${str(field)} value=${str(it)} decoded=${str(value)}"
    }
  }

  open class Primitive<T : Any>(
    sqlDataType: String,
    setParam: SetParamValue<T>,
    getValue: GetResultSetValue<T>,
  ) : DataType<T, T>(
    sqlDataType = sqlDataType,
    setParam = setParam,
    getValue = getValue,
    codec = JdbcValueCodec.Primitive(),
  )

  object Boolean : Primitive<kotlin.Boolean>(
    sqlDataType = "BOOLEAN",
    setParam = PreparedStatement::setBoolean,
    getValue = ResultSet::getBoolean,
  )

  object TinyInt : Primitive<Byte>(
    sqlDataType = "TINYINT",
    setParam = PreparedStatement::setByte,
    getValue = ResultSet::getByte,
  )

  object SmallInt : Primitive<Short>(
    sqlDataType = "SMALLINT",
    setParam = PreparedStatement::setShort,
    getValue = ResultSet::getShort,
  )

  object Integer : Primitive<Int>(
    sqlDataType = "INTEGER",
    setParam = PreparedStatement::setInt,
    getValue = ResultSet::getInt
  )

  object BigInt : Primitive<Long>(
    sqlDataType = "BIGINT",
    setParam = PreparedStatement::setLong,
    getValue = ResultSet::getLong,
  )

  object Double : Primitive<kotlin.Double>(
    sqlDataType = "DOUBLE PRECISION",
    setParam = PreparedStatement::setDouble,
    getValue = ResultSet::getDouble,
  )

  object Float : Primitive<kotlin.Float>(
    sqlDataType = "FLOAT",
    setParam = PreparedStatement::setFloat,
    getValue = ResultSet::getFloat,
  )

  open class SqlTimestamp<T : Any>(
    codec: JdbcValueCodec<java.sql.Timestamp, T>,
  ) : DataType<java.sql.Timestamp, T>(
    codec = codec,
    sqlDataType = "TIMESTAMP",
    setParam = PreparedStatement::setTimestamp,
    getValue = ResultSet::getTimestamp,
  )

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
    codec = codec,
    sqlDataType = "DATE",
    setParam = PreparedStatement::setDate,
    getValue = ResultSet::getDate,
  )

  object Date : SqlDate<java.sql.Date>(
    codec = JdbcValueCodec.Primitive()
  )

  object LocalDate : SqlDate<java.time.LocalDate>(
    codec = JdbcValueCodec.LocalDateCodec
  )

  open class SqlTime<T : Any>(
    codec: JdbcValueCodec<java.sql.Time, T>,
  ) : DataType<java.sql.Time, T>(
    codec = codec,
    sqlDataType = "TIME",
    setParam = PreparedStatement::setTime,
    getValue = ResultSet::getTime,
  )

  object Time : SqlTime<java.sql.Time>(
    codec = JdbcValueCodec.Primitive()
  )

  object LocalTime : SqlTime<java.time.LocalTime>(
    codec = JdbcValueCodec.LocalTimeCodec
  )

  open class ShortString<T : Any>(
    decoder: (String) -> T,
    override val length: Int = SHORT_STRING,
    sqlDataType: String = "VARCHAR($length)",
  ) : DataType<String, T>(
    codec = JdbcValueCodec.StringCodec(decoder),
    sqlDataType = sqlDataType,
    setParam = PreparedStatement::setString,
    getValue = ResultSet::getString,
  )

  class EnumType<E : Enum<E>>(
    val enumClass: KClass<E>,
    override val length: Int = SHORT_STRING,
    val ignoreCase: kotlin.Boolean = true
  ) : ShortString<E>(
    length = length,
    sqlDataType = "VARCHAR($length)",
    decoder = { value ->
      enumClass.java.enumConstants.single {
        value.equals(it.name, ignoreCase)
      }
    }
  )

  object Text : Primitive<String>(
    sqlDataType = "TEXT",
    setParam = PreparedStatement::setNString,
    getValue = ResultSet::getNString
  )

  object UUID : ShortString<java.util.UUID>(
    decoder = java.util.UUID::fromString,
    sqlDataType = "UUID",
  )

  object Duration : ShortString<java.time.Duration>(
    decoder = java.time.Duration::parse
  )

  class VarChar(override val length: Int) : Primitive<String>(
    sqlDataType = "VARCHAR($length)",
    setParam = PreparedStatement::setString,
    getValue = ResultSet::getString,
  )

  class NVarChar(override val length: Int) : Primitive<String>(
    sqlDataType = "NVARCHAR($length)",
    setParam = PreparedStatement::setNString,
    getValue = ResultSet::getNString
  )

  abstract class BigDecimal(
    sqlDataType: String,
  ) : Primitive<java.math.BigDecimal>(
    sqlDataType = sqlDataType,
    setParam = PreparedStatement::setBigDecimal,
    getValue = ResultSet::getBigDecimal,
  )

  class Numeric(
    val scale: Int,
    val precision: Int? = null
  ) : BigDecimal(
    sqlDataType = "NUMERIC$scale${nullable(precision)}"
  )

  class Decimal(
    val scale: Int,
    val precision: Int? = null
  ) : BigDecimal(
    sqlDataType = "DECIMAL($scale${nullable(precision)})"
  )

  internal fun valueType(): KClass<T> {
    val dataType = this::class.allSupertypes
      .single { it.classifier == DataType::class }

    @Suppress("UNCHECKED_CAST")
    return dataType.arguments.last().type!!.classifier!! as KClass<T>
  }

  override fun toString() = "${this::class.simpleName} $sqlDataType"

  companion object : KLogging() {

    const val SHORT_STRING = 0x20
    const val STRING_LENGTH = 0x400

    const val DEFAULT_SCALE = 13
    const val DEFAULT_PRECISION = 2

    val All = DataType::class
      .nestedClasses
      .mapNotNull { it.objectInstance }
      .filterIsInstance<DataType<*, *>>()
      .map { it.valueType() to it }

    fun from(type: KClass<*>) = All.firstNotNullOfOrNull { (valueType, dataType) ->
      if (valueType.isSubclassOf(type)) {
        dataType
      } else {
        null
      }
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
