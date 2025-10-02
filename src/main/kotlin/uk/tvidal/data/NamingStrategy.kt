package uk.tvidal.data

enum class NamingStrategy {

  AsIs {
    override fun databaseName(s: Appendable, name: CharSequence) {
      s.append(name)
    }
  },

  LowerCase {
    override fun databaseName(s: Appendable, name: CharSequence) {
      name.forEach {
        s.append(it.lowercaseChar())
      }
    }
  },

  CamelCase {
    override fun databaseName(s: Appendable, name: CharSequence) {
      for ((i, ch) in name.withIndex()) {
        s.append(if (i == 0) ch.lowercaseChar() else ch)
      }
    }
  },

  PascalCase {
    override fun databaseName(s: Appendable, name: CharSequence) {
      for ((i, ch) in name.withIndex()) {
        s.append(if (i == 0) ch.uppercaseChar() else ch)
      }
    }
  },

  ScreamingCase {
    override fun databaseName(s: Appendable, name: CharSequence) {
      name.forEach {
        s.append(it.uppercaseChar())
      }
    }
  },

  ScreamingSnakeCase {
    override fun databaseName(s: Appendable, name: CharSequence) {
      for ((i, ch) in name.withIndex()) {
        if (ch.isUpperCase() && i > 0) s.append('_')
        s.append(ch.uppercaseChar())
      }
    }
  },

  SnakeCase {
    override fun databaseName(s: Appendable, name: CharSequence) {
      for ((i, ch) in name.withIndex()) {
        if (ch.isUpperCase() && i > 0) s.append('_')
        s.append(ch.lowercaseChar())
      }
    }
  };

  abstract fun databaseName(s: Appendable, name: CharSequence)

  operator fun get(name: CharSequence) = buildString(name.length) {
    databaseName(this, name)
  }
}
