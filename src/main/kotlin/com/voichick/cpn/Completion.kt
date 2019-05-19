package com.voichick.cpn

class Completion private constructor(val strings: List<String>, val subcompletions: Map<String, Completion>) {

    constructor(strings: List<String>) : this(strings, getSubcompletions(strings))

    override fun equals(other: Any?): Boolean {
        return this === other || strings == (other as? Completion)?.strings
    }

    override fun hashCode(): Int {
        return strings.hashCode()
    }

    private companion object {
        fun getSubcompletions(strings: List<String>) = strings.map { string ->
            string.split(' ', limit = 2)
        }.filter { it.size >= 2 }.groupBy({ it[0] }, { it[1] }).mapValues { Completion(it.value) }
    }
}
