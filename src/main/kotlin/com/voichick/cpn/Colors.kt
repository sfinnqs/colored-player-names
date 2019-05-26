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
