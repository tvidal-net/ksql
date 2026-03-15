package uk.tvidal.data.codec

import uk.tvidal.data.logging.KLogging
import uk.tvidal.data.simpleName
import uk.tvidal.data.str
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Timestamp
import java.sql.Types
import java.util.Objects.hash
import java.util.SortedMap
import java.util.TreeMap
import kotlin.reflect.KClass
import kotlin.reflect.full.allSupertypes
import kotlin.reflect.full.isSubclassOf

abstract class ValueType<J, T : Any>(
  val jdbcCodec: JdbcValueCodec<J, T>,
  val setParam: SetParamValue<J>,
  val getValue: GetResultSetValue<J>,
) : ParamValueEncoder<T>, ResultSetDecoder<T> {

  open val length: Int?
    get() = null

  open val sqlDataType: String
    get() = "VARCHAR(${length ?: LENGTH})"

  override fun setParamValue(statement: PreparedStatement, index: Int, value: T?) {
    if (value != null) {
      val encodedValue = jdbcCodec.encode(value).alsoTrace {
        val logMessage = if ("$it" == "$value") "" else " value=${str(it)}"
        "$simpleName::setParamValue($index=${str(value)})$logMessage"
      }
      setParam(statement, index, encodedValue)
    } else {
      alsoTrace { "$simpleName::setParamValue($index=NULL)" }
      statement.setNull(index, Types.NULL)
    }
  }

  override fun getResultSetValue(
    resultSet: ResultSet,
    fieldName: String
  ): T? = getValue(resultSet, fieldName)?.let {
    if (resultSet.wasNull()) {
      null
    } else {
      jdbcCodec.decode(it)
    }.alsoTrace { value ->
      val logMessage = if ("$it" == "$value") "" else " value=${str(value)}"
      "$simpleName::getResultSetValue($fieldName=${str(it)})$logMessage"
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

  object Bit : Primitive<Boolean>(
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

  object DoublePrecision : Primitive<Double>(
    setParam = PreparedStatement::setDouble,
    getValue = ResultSet::getDouble,
  ) {
    override val sqlDataType: String
      get() = "DOUBLE PRECISION"
  }

  object SinglePrecision : Primitive<Float>(
    setParam = PreparedStatement::setFloat,
    getValue = ResultSet::getFloat,
  ) {
    override val sqlDataType: String
      get() = "FLOAT"
  }

  open class SqlTimestampType<T : Any>(
    codec: JdbcValueCodec<Timestamp, T>,
  ) : ValueType<Timestamp, T>(
    jdbcCodec = codec,
    setParam = PreparedStatement::setTimestamp,
    getValue = ResultSet::getTimestamp,
  ) {
    override val sqlDataType: String
      get() = "TIMESTAMP"
  }

  object SqlTimestamp : SqlTimestampType<Timestamp>(
    JdbcValueCodec.Primitive()
  )

  object JavaDate : SqlTimestampType<java.util.Date>(
    JdbcValueCodec.JavaDateCodec
  )

  object DateTime : SqlTimestampType<java.time.LocalDateTime>(
    JdbcValueCodec.LocalDateTimeCodec
  )

  object Instant : SqlTimestampType<java.time.Instant>(
    JdbcValueCodec.InstantCodec
  )

  open class SqlDateType<T : Any>(
    codec: JdbcValueCodec<java.sql.Date, T>,
  ) : ValueType<java.sql.Date, T>(
    jdbcCodec = codec,
    setParam = PreparedStatement::setDate,
    getValue = ResultSet::getDate,
  ) {
    override val sqlDataType: String
      get() = "DATE"
  }

  object SqlDate : SqlDateType<java.sql.Date>(
    JdbcValueCodec.Primitive()
  )

  object Date : SqlDateType<java.time.LocalDate>(
    JdbcValueCodec.LocalDateCodec
  )

  open class SqlTimeType<T : Any>(
    codec: JdbcValueCodec<java.sql.Time, T>,
  ) : ValueType<java.sql.Time, T>(
    jdbcCodec = codec,
    setParam = PreparedStatement::setTime,
    getValue = ResultSet::getTime,
  ) {
    override val sqlDataType: String
      get() = "TIME"
  }

  object SqlTime : SqlTimeType<java.sql.Time>(
    JdbcValueCodec.Primitive()
  )

  object Time : SqlTimeType<java.time.LocalTime>(
    JdbcValueCodec.LocalTimeCodec
  )

  open class ShortString<T : Any>(
    decoder: (String) -> T,
    override val length: Int = SHORT_STRING,
    override val sqlDataType: String = "NVARCHAR($length)"
  ) : ValueType<String, T>(
    jdbcCodec = JdbcValueCodec.StringCodec(decoder),
    setParam = PreparedStatement::setString,
    getValue = ResultSet::getString,
  )

  class EnumType<E : Enum<E>>(
    val enum: KClass<E>,
    length: Int? = null,
    ignoreCase: Boolean = true
  ) : ShortString<E>(
    decoder = enumValuesMap(enum, ignoreCase)::getValue
  ) {
    override val length = length ?: enumMaxNameLength(enum)
  }

  object Text : Primitive<String>(
    setParam = PreparedStatement::setNString,
    getValue = ResultSet::getNString
  ) {
    override val sqlDataType: String
      get() = "TEXT"
  }

  object UUID : ShortString<java.util.UUID>(
    java.util.UUID::fromString,
  ) {
    override val sqlDataType: String
      get() = "UUID"
  }

  object Duration : ShortString<kotlin.time.Duration>(
    kotlin.time.Duration::parse
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

  open class BigDecimal<T : Any>(
    codec: JdbcValueCodec<java.math.BigDecimal, T>
  ) : ValueType<java.math.BigDecimal, T>(
    jdbcCodec = codec,
    setParam = PreparedStatement::setBigDecimal,
    getValue = ResultSet::getBigDecimal,
  )

  class Numeric(
    val scale: Int,
    val precision: Int? = null
  ) : BigDecimal<java.math.BigDecimal>(
    JdbcValueCodec.DecimalCodec(::identity)
  ) {
    override val sqlDataType: String
      get() = "NUMERIC($scale${nullable(precision)})"
  }

  class Decimal(
    val scale: Int,
    val precision: Int? = null
  ) : BigDecimal<java.math.BigDecimal>(
    JdbcValueCodec.DecimalCodec(::identity)
  ) {
    override val sqlDataType: String
      get() = "DECIMAL($scale${nullable(precision)})"
  }

  override fun hashCode() = hash(
    sqlDataType,
    jdbcCodec,
    setParam,
    getValue
  )

  override fun equals(other: Any?) = other is ValueType<*, *>
    && sqlDataType == other.sqlDataType
    && jdbcCodec == other.jdbcCodec
    && setParam == other.setParam
    && getValue == other.getValue

  override fun toString() = "${this::class.simpleName} $sqlDataType"

  companion object : KLogging() {

    private fun <T : Any> identity(param: T): T = param

    const val SHORT_STRING = 0x20
    const val LENGTH = 0x400

    const val DEFAULT_SCALE = 13
    const val DEFAULT_PRECISION = 2

    val All by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
      ValueType::class.nestedClasses
        .mapNotNull { it.objectInstance }
        .filterIsInstance<ValueType<Any, Any>>()
        .associateBy { valueType(it::class) }
    }

    @Suppress("UNCHECKED_CAST")
    private fun valueType(type: KClass<out ValueType<Any, Any>>): KClass<out Any> = type
      .allSupertypes.single { it.classifier == ValueType::class }
      .arguments.last()
      .type!!
      .classifier!! as KClass<out Any>

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> of(type: KClass<T>): ValueType<*, T>? {
      val codec = All[type] ?: All.firstNotNullOfOrNull { (value, valueType) ->
        if (value.isSubclassOf(type)) valueType else null
      }
      alsoTrace { "ValueType.of($type) = $codec" }
      return codec as? ValueType<*, T>
    }

    fun <E : Enum<E>> enumMaxNameLength(enum: KClass<E>) = enum.java
      .enumConstants
      .maxOf { it.name.length }

    fun <E : Enum<E>> enumValuesMap(
      enum: KClass<E>,
      ignoreCase: Boolean,
    ): SortedMap<String, E> = enum.java.enumConstants.associateByTo(
      TreeMap(stringComparator(ignoreCase))
    ) {
      it.name
    }

    fun stringComparator(ignoreCase: Boolean): Comparator<String> =
      if (ignoreCase) String.CASE_INSENSITIVE_ORDER
      else Comparator.naturalOrder()

    private fun nullable(precision: Int?) =
      precision?.let { ", $precision" } ?: ""
  }
}
