package com.voichick.cpn

import org.bukkit.ChatColor
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.InvalidConfigurationException
import java.util.*

data class ColorSection(val color: ChatColor, val name: String, val weight: Double, val aliases: Set<String>) {

    constructor(section: ConfigurationSection) : this(getColor(section), getName(section), getWeight(section), getAliases(section))

    private companion object {
        fun getColor(section: ConfigurationSection): ChatColor {
            val officialName = section.name
            return getColorByOfficialName(officialName) ?: section.getString("code")?.let {
                ChatColor.getByChar(it)
            }
            ?: throw InvalidConfigurationException("Unable to determine color from name: $officialName")
        }

        fun getName(section: ConfigurationSection) = section.getString("name", null) ?: section.name
        fun getWeight(section: ConfigurationSection) = section.getDouble("weight", 0.0)
        fun getAliases(section: ConfigurationSection): Set<String> = if (section.isSet("aliases") && section.isList("aliases"))
            TreeSet(section.getStringList("aliases"))
        else
            emptySet()
    }
}