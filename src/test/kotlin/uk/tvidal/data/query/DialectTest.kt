package uk.tvidal.data.query

import org.junit.jupiter.api.Test
import uk.tvidal.data.WhereClauseBuilder
import uk.tvidal.data.filter.SqlFieldFilter
import uk.tvidal.data.filter.SqlFieldMultiValueFilter
import uk.tvidal.data.filter.SqlFieldValueFilter
import uk.tvidal.data.filter.SqlFilter
import uk.tvidal.data.filter.SqlMultiFilter
import uk.tvidal.data.model.Key
import uk.tvidal.data.model.Table
import uk.tvidal.data.model.fields
import uk.tvidal.data.model.nonKeyFields
import uk.tvidal.data.sqlFilter
import java.util.LinkedList
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class DialectTest {

  private object TestDialect : Dialect(NamingStrategy.AS_IS) {

    fun tableName(entity: KClass<*>) = buildString {
      appendTableName(entity)
    }

    fun fieldNames() = buildString {
      appendFieldNames(TestTable::class.fields)
    }

    fun fieldParams(params: MutableCollection<QueryParameter>) = buildString {
      appendFieldParams(params, TestTable::class.fields)
    }

    fun setFields(params: MutableCollection<QueryParameter>) = buildString {
      appendSetFields(params, TestTable::class.nonKeyFields)
    }

    fun where(where: SqlFilter, params: MutableCollection<QueryParameter>) = buildString {
      appendFilter(params, where)
    }
  }

  private class SimpleTableName

  @Test
  fun testTableName() {
    test("SimpleTableName") {
      tableName(SimpleTableName::class)
    }
  }

  @Table("tableName", schema = "tableSchema")
  private data class TestTable(
    val name: String,
    @Key val id: Int,
  )

  @Test
  fun testTableNameWithSchema() {
    test("tableSchema.tableName") {
      tableName(TestTable::class)
    }
  }

  @Test
  fun testFieldNames() {
    test("id,name") {
      fieldNames()
    }
  }

  @Test
  fun testFieldParams() {
    val params = ArrayList<QueryParameter>()
    test("(?,?)") {
      fieldParams(params)
    }
    assertContentEquals(
      listOf(TestTable::id, TestTable::name),
      params.filterIsInstance<ParameterProperty<TestTable>>()
        .map(ParameterProperty<TestTable>::property)
    )
  }

  @Test
  fun testSetFields() {
    val params = ArrayList<QueryParameter>()
    test("name = ?") {
      setFields(params)
    }
    assertContentEquals(
      listOf(TestTable::name),
      params.filterIsInstance<ParameterProperty<TestTable>>()
        .map(ParameterProperty<TestTable>::property)
    )
  }

  @Test
  fun testWhereIsNull() {
    whereValues("id IS NULL") {
      TestTable::id.isNull
    }
  }

  @Test
  fun testWhereIsNotNull() {
    whereValues("id IS NOT NULL") {
      TestTable::id.isNotNull
    }
  }

  @Test
  fun testWhereEquals() {
    whereFields("id = ?") {
      TestTable::id.eq()
    }
    whereValues("id = ?") {
      TestTable::id.eq(0)
    }
  }

  @Test
  fun testWhereNotEquals() {
    whereFields("id != ?") {
      TestTable::id.ne()
    }
    whereValues("id != ?") {
      TestTable::id.ne(0)
    }
  }

  @Test
  fun testWhereGreaterThan() {
    whereFields("id > ?") {
      TestTable::id.gt()
    }
    whereValues("id > ?") {
      TestTable::id.gt(0)
    }
  }

  @Test
  fun testWhereLessThan() {
    whereFields("id < ?") {
      TestTable::id.lt()
    }
    whereValues("id < ?") {
      TestTable::id.lt(0)
    }
  }

  @Test
  fun testWhereGreaterEquals() {
    whereFields("id >= ?") {
      TestTable::id.ge()
    }
    whereValues("id >= ?") {
      TestTable::id.ge(0)
    }
  }

  @Test
  fun testWhereLessEquals() {
    whereFields("id <= ?") {
      TestTable::id.le()
    }
    whereValues("id <= ?") {
      TestTable::id.le(0)
    }
  }

  @Test
  fun testWhereLike() {
    whereValues("name LIKE ?") {
      TestTable::name.like("NAME%")
    }
  }

  @Test
  fun testWhereBetween() {
    whereValues("id BETWEEN ? AND ?") {
      TestTable::id.between(0, 1)
    }
  }

  @Test
  fun testWhereInValues() {
    whereValues("id IN (?,?,?)") {
      TestTable::id.inValues(1, 2, 3)
    }
  }

  @Test
  fun testMultiFilter() {
    whereValues("(id IS NOT NULL AND (id >= ? OR id <= ?))") {
      TestTable::id.isNotNull
      TestTable::id.ge(20).or(TestTable::id.le(10))
    }
  }

  private inline fun test(expected: String, actual: TestDialect.() -> String) {
    assertEquals(expected, actual(TestDialect))
  }

  private inline fun whereFields(expected: String, builder: WhereClauseBuilder<TestTable>) {
    val params = LinkedList<QueryParameter>()
    val filter = sqlFilter(builder)
    test(expected) {
      where(filter, params)
    }
    val filterFields = filter.fields()
    val paramFields = params.fields()
    assertContentEquals(filterFields, paramFields)
  }

  private inline fun whereValues(expected: String, builder: WhereClauseBuilder<TestTable>) {
    val params = LinkedList<QueryParameter>()
    val filter = sqlFilter(builder)
    test(expected) {
      where(filter, params)
    }
    val filterValues = filter.values()
    val paramValues = params.values()
    assertContentEquals(filterValues, paramValues)
  }

  private fun SqlFilter.fields(): Collection<KProperty1<*, *>> = when (this) {
    is SqlFieldFilter<*> -> listOf(field)
    is SqlMultiFilter -> operands.flatMap { it.fields() }
  }

  private fun SqlFilter.values(): Collection<Any?> = when (this) {
    is SqlFieldValueFilter<*> -> listOf(value)
    is SqlFieldMultiValueFilter<*> -> values
    is SqlMultiFilter -> operands.flatMap { it.values() }
    else -> emptyList()
  }

  private fun Collection<QueryParameter>.fields() = filterIsInstance<ParameterProperty<*>>()
    .map(ParameterProperty<*>::property)

  private fun Collection<QueryParameter>.values() = filterIsInstance<ParameterValue>()
    .map(ParameterValue::value)
}
