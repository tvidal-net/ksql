package uk.tvidal.data.model

import jakarta.persistence.Column
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.Transient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.tvidal.data.TableName
import uk.tvidal.data.fieldName
import uk.tvidal.data.fields
import uk.tvidal.data.keyFields
import uk.tvidal.data.sql.insertFields
import uk.tvidal.data.sql.updateFields
import uk.tvidal.data.table

class ModelTest {

  private data class NoAnnotation(
    val field: String
  )

  @Test
  fun tableWithoutId() {
    assertThat(NoAnnotation::class.keyFields)
      .isEmpty()

    assertThat(NoAnnotation::class.insertFields)
      .containsExactly(NoAnnotation::field)

    assertThat(NoAnnotation::class.updateFields)
      .containsExactly(NoAnnotation::field)
  }

  @Test
  fun tableNameNoAnnotation() {
    assertThat(NoAnnotation::class.table)
      .isEqualTo(TableName("NoAnnotation"))
  }

  @Test
  fun fieldNameNoAnnotation() {
    assertThat(NoAnnotation::field.fieldName)
      .isEqualTo("field")
  }

  @Table(name = "tableName")
  private class AnnotatedClass(
    @Column(name = "fieldName") val field: String,
  )

  @Test
  fun tableNameAnnotated() {
    assertThat(AnnotatedClass::class.table)
      .isEqualTo(TableName("tableName"))
  }

  @Test
  fun fieldNameAnnotated() {
    assertThat(AnnotatedClass::field.fieldName)
      .isEqualTo("fieldName")
  }

  @Table(name = "tableName", schema = "tableSchema", catalog = "tableCatalog")
  private class TableWithSchema

  @Test
  fun tableWithSchema() {
    assertThat(TableWithSchema::class.table)
      .isEqualTo(TableName("tableName", "tableSchema"))
  }

  @Table(name = "tableName", catalog = "tableCatalog")
  private class TableWithCatalog

  @Test
  fun tableWithCatalog() {
    assertThat(TableWithCatalog::class.table)
      .isEqualTo(TableName("tableName", "tableCatalog"))
  }

  @Table
  private class EmptyAnnotatedClass(
    @Column val field: String,
    @Id val id: Long,
  )

  @Test
  fun tableWithEmptyName() {
    assertThat(EmptyAnnotatedClass::class.table)
      .isEqualTo(TableName("EmptyAnnotatedClass"))
  }

  @Test
  fun fieldWithEmptyName() {
    assertThat(EmptyAnnotatedClass::field.fieldName)
      .isEqualTo("field")

    assertThat(EmptyAnnotatedClass::id.fieldName)
      .isEqualTo("id")
  }

  @Test
  fun singleKeyField() {
    assertThat(EmptyAnnotatedClass::class.keyFields)
      .containsExactly(EmptyAnnotatedClass::id)
  }

  private class CompositeKey(
    val name: String,
    @Id val type: Byte,
    @Id val id: Long
  )

  @Test
  fun compositeKeyTable() {
    assertThat(CompositeKey::class.keyFields)
      .contains(CompositeKey::type, CompositeKey::id)

    assertThat(CompositeKey::class.insertFields)
      .contains(CompositeKey::name, CompositeKey::type, CompositeKey::id)

    assertThat(CompositeKey::class.updateFields)
      .containsExactly(CompositeKey::name)
  }

  private class NonUpdatableFields(
    @Column(updatable = false) val nonUpdatable: String,
    @Column(insertable = false) val nonInsertable: String,
    @Id val id: Long
  )

  @Test
  fun nonUpdatableFields() {
    assertThat(NonUpdatableFields::class.updateFields)
      .containsExactly(NonUpdatableFields::nonInsertable)
  }

  @Test
  fun nonInsertableFields() {
    assertThat(NonUpdatableFields::class.insertFields)
      .contains(NonUpdatableFields::id, NonUpdatableFields::nonUpdatable)
  }

  private class TransientFields(
    val persisted: String,
    @Transient val ignored: String,
    @Id val id: Long
  )

  @Test
  fun transientFieldsAreExcluded() {
    assertThat(TransientFields::class.fields)
      .contains(TransientFields::persisted, TransientFields::id)
      .doesNotContain(TransientFields::ignored)

    assertThat(TransientFields::class.insertFields)
      .doesNotContain(TransientFields::ignored)

    assertThat(TransientFields::class.updateFields)
      .containsExactly(TransientFields::persisted)
  }
}
