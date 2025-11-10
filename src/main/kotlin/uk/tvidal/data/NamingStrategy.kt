package uk.tvidal.data

enum class NamingStrategy {

  AsIs {
    override fun appendName(s: Appendable, name: CharSequence) {
      s.append(name)
    }
  },

  LowerCase {
    override fun appendName(s: Appendable, name: CharSequence) {
      name.forEach {
        s.append(it.lowercaseChar())
      }
    }
  },

  CamelCase {
    override fun appendName(s: Appendable, name: CharSequence) {
      for ((i, ch) in name.withIndex()) {
        s.append(if (i == 0) ch.lowercaseChar() else ch)
      }
    }
  },

  PascalCase {
    override fun appendName(s: Appendable, name: CharSequence) {
      for ((i, ch) in name.withIndex()) {
        s.append(if (i == 0) ch.uppercaseChar() else ch)
      }
    }
  },

  ScreamingCase {
    override fun appendName(s: Appendable, name: CharSequence) {
      name.forEach {
        s.append(it.uppercaseChar())
      }
    }
  },

  ScreamingSnakeCase {
    override fun appendName(s: Appendable, name: CharSequence) {
      for ((i, ch) in name.withIndex()) {
        if (ch.isUpperCase() && i > 0) s.append(NAME_SEP)
        s.append(ch.uppercaseChar())
      }
    }
  },

  SnakeCase {
    override fun appendName(s: Appendable, name: CharSequence) {
      for ((i, ch) in name.withIndex()) {
        if (ch.isUpperCase() && i > 0) s.append(NAME_SEP)
        s.append(ch.lowercaseChar())
      }
    }
  };

  abstract fun appendName(s: Appendable, name: CharSequence)

  operator fun get(name: CharSequence, alias: String? = null) = buildString {
    alias?.let {
      appendName(this, it)
      append(NAME_SEP)
    }
    appendName(this, name)
  }

  companion object Constants {
    const val NAME_SEP = '_'
  }
}
