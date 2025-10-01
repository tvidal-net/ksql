package uk.tvidal.data

import uk.tvidal.data.logging.KLogger
import uk.tvidal.data.mariadb.MariaDB
import uk.tvidal.data.model.RandomUUID
import uk.tvidal.data.model.Today
import java.time.LocalDate
import java.util.*
import javax.persistence.Id
import javax.persistence.Table

const val DB_URL = "jdbc:mariadb://mini/test"
const val DB_USER = "test"
val DB_PASSWORD: String = System.getenv("DB_PASSWORD")

private val log = KLogger("uk.tvidal.data.PersonTestKt")

private val db = MariaDB.createDatabase(DB_URL, DB_USER, DB_PASSWORD)

enum class Name {
  Thiago,
  Kamila;
}

data class PersonDetails(
  val dob: LocalDate
)

@Table(name = "person")
data class DataPerson(
  val name: Name? = null,
  val age: Int = 0,
  val details: PersonDetails? = null,
  @Id val id: UUID = RandomUUID,
)

private val dataPeople = db.repository<DataPerson>()

class Person(var name: String) {
  var age: Int = 0
  var details: PersonDetails? = null

  @Id
  var id: UUID = RandomUUID

  override fun toString() = "Person(name=$name, age=$age, details=$details, id=$id)"
}

private val people = db.repository<Person>(
  decoder = db.byProperties("")
)

val thiago = DataPerson(Name.Thiago, 43, PersonDetails(Today))
val kamila = DataPerson(Name.Kamila, 43)

fun main() {
  dataPeople.insert(thiago, kamila)
  log.info { people[thiago.id] }
  log.info { people[kamila.id] }

  people.forEach {
    log.info { it }
  }

  dataPeople.update(kamila.copy(age = 42))
  log.info { people[kamila.id] }

  dataPeople += thiago.copy(age = 41)
  log.info { people[thiago.id] }

  dataPeople -= listOf(thiago, kamila)
  people.forEach(System.out::println)

  db.delete<Person> {
    Person::age.gt(0)
  }
}
