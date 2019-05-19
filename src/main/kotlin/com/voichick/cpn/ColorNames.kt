package com.voichick.cpn

import org.bukkit.ChatColor
import java.util.*

class ColorNames private constructor(private val set: Set<String>, private val map: Map<ChatColor, String>) : Set<String> by set {

    constructor(sections: Map<ChatColor, ColorSection>) : this(createSet(sections), createMap(sections))

    operator fun get(color: ChatColor) = map[color]

    private companion object {

        fun createSet(sections: Map<ChatColor, ColorSection>): Set<String> {
            val result = TreeSet<String>()
            for (section in sections.values)
                result.add(section.name)
            return result
        }

        fun createMap(sections: Map<ChatColor, ColorSection>): Map<ChatColor, String> {
            val result = EnumMap<ChatColor, String>(ChatColor::class.java)
            for (section in sections.values)
                result[section.color] = section.name
            return result
        }

    }

}
