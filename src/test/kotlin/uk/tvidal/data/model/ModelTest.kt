package uk.tvidal.data.model

import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class ModelTest {

  private data class NoAnnotation(
    val field: String
  )

  @Test
  fun testTableWithoutKeys() {
    assertContentEquals(
      emptyList(),
      NoAnnotation::class.keyFields
    )
    assertContentEquals(
      listOf(NoAnnotation::field),
      NoAnnotation::class.nonKeyFields
    )
  }

  @Test
  fun testTableNameNoAnnotation() {
    assertEquals(
      TableName("NoAnnotation"),
      NoAnnotation::class.tableName
    )
  }

  @Test
  fun testFieldNameNoAnnotation() {
    assertEquals(
      "field",
      NoAnnotation::field.fieldName
    )
  }

  @Table(name = "tableName")
  private data class AnnotatedClass(
    @Field(name = "fieldName")
    val field: String,
  )

  @Test
  fun testTableNameAnnotated() {
    assertEquals(
      TableName("tableName"),
      AnnotatedClass::class.tableName
    )
  }

  @Test
  fun testFieldNameAnnotated() {
    assertEquals(
      "fieldName",
      AnnotatedClass::field.fieldName
    )
  }

  @Table(name = "tableName", schema = "tableSchema")
  private class TableWithSchema

  @Test
  fun testTableWithSchema() {
    assertEquals(
      TableName("tableName", "tableSchema"),
      TableWithSchema::class.tableName
    )
  }

  @Table(name = "", schema = "")
  private data class EmptyAnnotatedClass(
    @Field(name = "")
    val field: String,
  )

  @Test
  fun testTableWithEmptyName() {
    assertEquals(
      TableName("EmptyAnnotatedClass"),
      EmptyAnnotatedClass::class.tableName
    )
  }

  @Test
  fun testFieldWithEmptyName() {
    assertEquals(
      "field",
      EmptyAnnotatedClass::field.fieldName
    )
  }

  private data class KeyClass(
    @Key val id: UUID,
    val name: String
  )

  @Test
  fun testKeyAnnotatedFields() {
    assertContentEquals(
      listOf(KeyClass::id),
      KeyClass::class.keyFields
    )
    assertContentEquals(
      listOf(KeyClass::name),
      KeyClass::class.nonKeyFields
    )
  }
}
