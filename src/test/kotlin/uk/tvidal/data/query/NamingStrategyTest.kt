package uk.tvidal.data.query

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.tvidal.data.NamingStrategy

class NamingStrategyTest {

  @Test
  fun asIsNamingStrategy() {
    assertThat(NamingStrategy.AsIs["NotChangedName"])
      .isEqualTo("NotChangedName")
  }

  @Test
  fun lowercaseNamingStrategy() {
    assertThat(NamingStrategy.LowerCase["LowerCaseName"])
      .isEqualTo("lowercasename")
  }

  @Test
  fun camelCaseNamingStrategy() {
    assertThat(NamingStrategy.CamelCase["CamelCaseName"])
      .isEqualTo("camelCaseName")
  }

  @Test
  fun pascalCaseNamingStrategy() {
    assertThat(NamingStrategy.PascalCase["pascalCaseName"])
      .isEqualTo("PascalCaseName")
  }

  @Test
  fun screamingCaseNamingStrategy() {
    assertThat(NamingStrategy.ScreamingCase["ScreamingCaseName"])
      .isEqualTo("SCREAMINGCASENAME")
  }

  @Test
  fun screamingSnakeCaseNamingStrategy() {
    assertThat(NamingStrategy.ScreamingSnakeCase["ScreamingSnakeCaseName"])
      .isEqualTo("SCREAMING_SNAKE_CASE_NAME")
  }

  @Test
  fun snakeCaseNamingStrategy() {
    assertThat(NamingStrategy.SnakeCase["SnakeCaseName"])
      .isEqualTo("snake_case_name")
  }
}
