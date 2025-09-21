package uk.tvidal.data.model

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Table(
  val name: String = "",
  val schema: String = ""
)
