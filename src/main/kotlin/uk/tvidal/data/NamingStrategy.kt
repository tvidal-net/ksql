package uk.tvidal.data

enum class NamingStrategy {

  AsIs {
    override fun Appendable.databaseName(name: CharSequence) {
      append(name)
    }
  },

  LowerCase {
    override fun Appendable.databaseName(name: CharSequence) {
      name.forEach {
        append(it.lowercaseChar())
      }
    }
  },

  CamelCase {
    override fun Appendable.databaseName(name: CharSequence) {
      for ((i, ch) in name.withIndex()) {
        append(if (i == 0) ch.lowercaseChar() else ch)
      }
    }
  },

  PascalCase {
    override fun Appendable.databaseName(name: CharSequence) {
      for ((i, ch) in name.withIndex()) {
        append(if (i == 0) ch.uppercaseChar() else ch)
      }
    }
  },

  ScreamingCase {
    override fun Appendable.databaseName(name: CharSequence) {
      name.forEach {
        append(it.uppercaseChar())
      }
    }
  },

  ScreamingSnakeCase {
    override fun Appendable.databaseName(name: CharSequence) {
      for ((i, ch) in name.withIndex()) {
        if (ch.isUpperCase() && i > 0) append(NAME_SEP)
        append(ch.uppercaseChar())
      }
    }
  },

  SnakeCase {
    override fun Appendable.databaseName(name: CharSequence) {
      for ((i, ch) in name.withIndex()) {
        if (ch.isUpperCase() && i > 0) append(NAME_SEP)
        append(ch.lowercaseChar())
      }
    }
  };

  protected abstract fun Appendable.databaseName(name: CharSequence)

  fun appendName(s: Appendable, name: CharSequence, alias: CharSequence? = null) = s.apply {
    alias?.let {
      databaseName(it)
      append(NAME_SEP)
    }
    databaseName(name)
  }

  operator fun get(name: CharSequence, alias: String? = null) = buildString(
    name.length + (alias?.length?.let { it + 1 } ?: 0)
  ) {
    appendName(this, name, alias)
  }

  companion object Constants {
    const val NAME_SEP = '_'
  }
}
