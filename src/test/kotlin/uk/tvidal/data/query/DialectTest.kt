package uk.tvidal.data.query

import org.junit.jupiter.api.Test
import uk.tvidal.data.Dialect
import uk.tvidal.data.NamingStrategy
import uk.tvidal.data.WhereClauseBuilder
import uk.tvidal.data.filter.*
import uk.tvidal.data.model.Key
import uk.tvidal.data.model.Table
import uk.tvidal.data.model.fields
import uk.tvidal.data.model.nonKeyColumns
import uk.tvidal.data.sqlFilter
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class DialectTest {

  private object TestDialect : Dialect(NamingStrategy.AS_IS) {

    fun tableName(entity: KClass<*>) = buildString {
      this.tableName(entity)
    }

    fun fieldNames() = buildString {
      fieldNames(TestTable::class.fields)
    }

    fun fieldParams(params: MutableCollection<QueryParam>) = buildString {
      columnParams(params, TestTable::class.fields)
    }

    fun setFields(params: MutableCollection<QueryParam>) = buildString {
      setColumns(params, TestTable::class.nonKeyColumns)
    }

    fun where(where: SqlFilter, params: MutableCollection<QueryParam>) = buildString {
      filter(params, where)
    }
  }

  private class SimpleTableName

  @Test
  fun testTableName() {
    test("SimpleTableName") {
      this.tableName(SimpleTableName::class)
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
      this.tableName(TestTable::class)
    }
  }

  @Test
  fun testColumnNames() {
    test("id,name") {
      fieldNames()
    }
  }

  @Test
  fun testColumnParams() {
    val params = ArrayList<QueryParam>()
    test("(?,?)") {
      fieldParams(params)
    }
    assertContentEquals(
      listOf(TestTable::id, TestTable::name),
      params.filterIsInstance<EntityQuery.Param<TestTable>>()
        .map(EntityQuery.Param<TestTable>::property)
    )
  }

  @Test
  fun testSetColumns() {
    val params = ArrayList<QueryParam>()
    test("SET name = ?") {
      setFields(params)
    }
    assertContentEquals(
      listOf(TestTable::name),
      params.filterIsInstance<EntityQuery.Param<TestTable>>()
        .map(EntityQuery.Param<TestTable>::property)
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
    val params = LinkedList<QueryParam>()
    val filter = sqlFilter(builder)
    test(expected) {
      where(filter, params)
    }
    val filterFields = filter.fields()
    val paramFields = params.fields()
    assertContentEquals(filterFields, paramFields)
  }

  private inline fun whereValues(expected: String, builder: WhereClauseBuilder<TestTable>) {
    val params = LinkedList<QueryParam>()
    val filter = sqlFilter(builder)
    test(expected) {
      where(filter, params)
    }
    val filterValues = filter.values()
    val paramValues = params.values()
    assertContentEquals(filterValues, paramValues)
  }

  private fun SqlFilter.fields(): Collection<KProperty1<*, *>> = when (this) {
    is SqlPropertyFilter<*> -> listOf(property)
    is SqlMultiFilter -> operands.flatMap { it.fields() }
  }

  private fun SqlFilter.values(): Collection<Any?> = when (this) {
    is SqlPropertyValueFilter<*> -> listOf(value)
    is SqlPropertyMultiValueFilter<*> -> values
    is SqlMultiFilter -> operands.flatMap { it.values() }
    else -> emptyList()
  }

  private fun Collection<QueryParam>.fields() =
    filterIsInstance<EntityQuery.Param<*>>().map(EntityQuery.Param<*>::property)

  private fun Collection<QueryParam>.values() =
    filterIsInstance<QueryParam.Value>()
      .map(QueryParam.Value::value)
}
