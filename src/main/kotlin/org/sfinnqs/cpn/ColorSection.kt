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
package org.sfinnqs.cpn

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
