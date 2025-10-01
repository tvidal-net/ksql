package uk.tvidal.data.schema

import uk.tvidal.data.model.RandomUUID
import java.util.*
import javax.persistence.Entity
import javax.persistence.Id

class SchemaTest {

  @Entity
  data class Person(
    val name: String,
    @Id val id: UUID = RandomUUID
  )
}
