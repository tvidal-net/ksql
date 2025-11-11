package uk.tvidal.data

import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class NamingStrategyTest {

  @Test
  fun asIsNamingStrategy() {
    assertThat(NamingStrategy.AsIs["NotChangedName"])
      .isEqualTo("NotChangedName")
    assertThat(NamingStrategy.AsIs["NotChangedName", "Alias"])
      .isEqualTo("Alias_NotChangedName")
  }

  @Test
  fun lowercaseNamingStrategy() {
    assertThat(NamingStrategy.LowerCase["LowerCaseName"])
      .isEqualTo("lowercasename")
    assertThat(NamingStrategy.LowerCase["LowerCaseName", "Alias"])
      .isEqualTo("alias_lowercasename")
  }

  @Test
  fun camelCaseNamingStrategy() {
    assertThat(NamingStrategy.CamelCase["CamelCaseName"])
      .isEqualTo("camelCaseName")
    assertThat(NamingStrategy.CamelCase["CamelCaseName", "Alias"])
      .isEqualTo("alias_camelCaseName")
  }

  @Test
  fun pascalCaseNamingStrategy() {
    assertThat(NamingStrategy.PascalCase["pascalCaseName"])
      .isEqualTo("PascalCaseName")
    assertThat(NamingStrategy.PascalCase["pascalCaseName", "Alias"])
      .isEqualTo("Alias_PascalCaseName")
  }

  @Test
  fun screamingCaseNamingStrategy() {
    assertThat(NamingStrategy.ScreamingCase["ScreamingCaseName"])
      .isEqualTo("SCREAMINGCASENAME")
    assertThat(NamingStrategy.ScreamingCase["ScreamingCaseName", "Alias"])
      .isEqualTo("ALIAS_SCREAMINGCASENAME")
  }

  @Test
  fun screamingSnakeCaseNamingStrategy() {
    assertThat(NamingStrategy.ScreamingSnakeCase["ScreamingSnakeCaseName"])
      .isEqualTo("SCREAMING_SNAKE_CASE_NAME")
    assertThat(NamingStrategy.ScreamingSnakeCase["ScreamingSnakeCaseName", "Alias"])
      .isEqualTo("ALIAS_SCREAMING_SNAKE_CASE_NAME")
  }

  @Test
  fun snakeCaseNamingStrategy() {
    assertThat(NamingStrategy.SnakeCase["SnakeCaseName"])
      .isEqualTo("snake_case_name")
    assertThat(NamingStrategy.SnakeCase["SnakeCaseName", "Alias"])
      .isEqualTo("alias_snake_case_name")
  }
}
