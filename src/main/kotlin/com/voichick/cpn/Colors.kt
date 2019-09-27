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

import org.bukkit.ChatColor
import java.util.*

class Colors private constructor(private val set: Set<ChatColor>, private val map: Map<String, ChatColor>)
    : Set<ChatColor> by set {

    constructor(sections: Map<ChatColor, ColorSection>) : this(createSet(sections), createMap(sections))

    operator fun get(colorName: String) = map[colorName]

    private companion object {

        fun createSet(sections: Map<ChatColor, ColorSection>): Set<ChatColor> {
            val result = EnumSet.noneOf(ChatColor::class.java)
            result.addAll(sections.keys)
            return result
        }

        fun createMap(sections: Map<ChatColor, ColorSection>): Map<String, ChatColor> {
            val result = TreeMap<String, ChatColor>()
            for (section in sections.values) {
                val color = section.color
                result[section.name] = color
                for (alias in section.aliases) {
                    result[alias] = color
                }
            }
            for (section in sections.values) {
                val color = section.color
                result.putIfAbsent(color.officialName.replace('_', ' '), color)
            }
            return result
        }
    }
}
