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

import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.InvalidConfigurationException
import java.util.*

data class PlayerSection(val name: String, val uuid: UUID, val color: ChatColor) {

    constructor(section: ConfigurationSection, colors: Colors) : this(getName(section), getUuid(section), getColor(section, colors))

    constructor(name: String, color: ChatColor) : this(name, getUuid(name), color)

    companion object {

        @Suppress("DEPRECATION")
        fun getUuid(name: String) = Bukkit.getServer().getOfflinePlayer(name).uniqueId

        private fun getName(section: ConfigurationSection): String {
            val uuidString = section.getString("uuid")
            if (uuidString != null) {
                val name = Bukkit.getServer().getOfflinePlayer(UUID.fromString(uuidString)).name
                if (name != null)
                    return name
            }
            return section.name
        }

        private fun getUuid(section: ConfigurationSection): UUID {
            val uuidString = section.getString("uuid")
            return if (uuidString == null) {
                getUuid(section.name)
            } else {
                UUID.fromString(uuidString)
            }
        }

        private fun getColor(section: ConfigurationSection, colors: Colors): ChatColor {
            val colorString = section.getString("color")
                    ?: throw InvalidConfigurationException("No color specified for player: ${section.name}")
            return colors[colorString]
                    ?: throw InvalidConfigurationException("Unrecognized color: $colorString")
        }
    }
}
