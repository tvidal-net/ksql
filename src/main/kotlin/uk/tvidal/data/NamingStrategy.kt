package uk.tvidal.data

enum class NamingStrategy {

  AsIs {
    override fun databaseName(sb: StringBuilder, name: CharSequence) {
      sb.append(name)
    }
  },

  LowerCase {
    override fun databaseName(sb: StringBuilder, name: CharSequence) {
      for (ch in name) {
        sb.append(ch.lowercaseChar())
      }
    }
  },

  CamelCase {
    override fun databaseName(sb: StringBuilder, name: CharSequence) {
      for ((i, ch) in name.withIndex()) {
        sb.append(if (i == 0) ch.lowercaseChar() else ch)
      }
    }
  },

  PascalCase {
    override fun databaseName(sb: StringBuilder, name: CharSequence) {
      for ((i, ch) in name.withIndex()) {
        sb.append(if (i == 0) ch.uppercaseChar() else ch)
      }
    }
  },

  ScreamingCase {
    override fun databaseName(sb: StringBuilder, name: CharSequence) {
      for (ch in name) {
        sb.append(ch.uppercaseChar())
      }
    }
  },

  ScreamingSnakeCase {
    override fun databaseName(sb: StringBuilder, name: CharSequence) {
      for ((i, ch) in name.withIndex()) {
        if (ch.isUpperCase() && i > 0) {
          sb.append('_')
        }
        sb.append(ch.uppercaseChar())
      }
    }
  },

  SnakeCase {
    override fun databaseName(sb: StringBuilder, name: CharSequence) {
      for ((i, ch) in name.withIndex()) {
        if (ch.isUpperCase() && i > 0) {
          sb.append('_')
        }
        sb.append(ch.lowercaseChar())
      }
    }
  };

  abstract fun databaseName(sb: StringBuilder, name: CharSequence)

  operator fun get(name: CharSequence) = buildString(name.length) {
    databaseName(this, name)
  }
}
