package uk.tvidal.data.query

enum class NamingStrategy {

  AS_IS {
    override fun appendDatabaseName(sb: StringBuilder, name: CharSequence) {
      sb.append(name)
    }
  },

  SNAKE_CASE {
    override fun appendDatabaseName(sb: StringBuilder, name: CharSequence) {
      for ((i, ch) in name.withIndex()) {
        if (ch.isUpperCase() && i > 0) {
          sb.append('_')
        }
        sb.append(ch.lowercase())
      }
    }
  };

  abstract fun appendDatabaseName(sb: StringBuilder, name: CharSequence)

  operator fun get(name: CharSequence) = buildString(name.length) {
    appendDatabaseName(this, name)
  }
}
