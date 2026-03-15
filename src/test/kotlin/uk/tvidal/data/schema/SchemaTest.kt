package uk.tvidal.data.schema

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.tvidal.data.Config
import uk.tvidal.data.TableName
import uk.tvidal.data.codec.ValueType
import uk.tvidal.data.database.Account
import uk.tvidal.data.database.Child
import uk.tvidal.data.database.Person

class SchemaTest {
  @Test
  fun noForeignKeys() {
    assertThat(foreignKeys(Person::class)).isEmpty()
  }

  @Test
  fun simpleForeignKey() {
    assertThat(foreignKeys(Child::class)).containsExactly(
      Constraint.ForeignKey(
        table = TableName("Parent"),
        references = listOf(on("parent", "id"))
      )
    )
  }

  @Test
  fun recursiveForeignKey() {
    assertThat(foreignKeys(Account::class)).containsExactly(
      Constraint.ForeignKey(
        table = TableName("Account"),
        references = listOf(on("parent", "id"))
      )
    )
  }

  @Test
  fun simpleSchemaField() {
    assertThat(
      SchemaField.from(Account::name)
    ).containsExactly(
      SchemaField(
        name = "name",
        type = ValueType.NVarChar(ValueType.LENGTH),
        nullable = false,
      )
    )
  }

  @Test
  fun overriddenSchemaField() {
    class JsonContainer(val json: JsonNode)

    val mapper = ObjectMapper()
    val jsonValueType = ValueType.ShortString(
      decoder = { mapper.readTree(it) },
      sqlDataType = "CLOB",
      length = 0,
    )
    val config = Config()
    config.register { jsonValueType }

    val actual = SchemaField
      .from(JsonContainer::json, config)
      .single()

    assertThat(actual.type).isSameAs(jsonValueType)
    assertThat("${actual.type.sqlDataType}").isEqualTo("CLOB")
  }

  @Test
  fun referenceSchemaField() {
    assertThat(
      SchemaField.from(Child::parent)
    ).containsExactly(
      SchemaField(
        name = "parent",
        type = ValueType.UUID,
        nullable = true,
      )
    )
  }

  @Test
  fun nestedSchemaFields() {
    assertThat(
      SchemaField.from(Person::details)
    ).containsExactlyInAnyOrder(
      SchemaField(
        name = "detailsName",
        type = ValueType.NVarChar(ValueType.LENGTH),
        nullable = true,
      ),
      SchemaField(
        name = "detailsAge",
        type = ValueType.Integer,
        nullable = true,
      )
    )
  }
}
