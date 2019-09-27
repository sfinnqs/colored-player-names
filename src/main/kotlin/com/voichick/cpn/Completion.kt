/**
 * ColoredPlayerNames - A Bukkit plugin for changing name colors
 * Copyright (C) 2019 sfinnqs
 *
 * This file is part of ColoredPlayerNames.
 *
 * ColoredPlayerNames is free software; you can redistribute it and/or modify it
 * under the terms of version 3 of the GNU General Public License as published
 * by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, see <https://www.gnu.org/licenses>.
 */
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
