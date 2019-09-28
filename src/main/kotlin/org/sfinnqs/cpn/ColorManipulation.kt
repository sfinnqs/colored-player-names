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
import org.bukkit.ChatColor.MAGIC
import java.util.*

fun condenseString(string: String): String {
    return string.toLowerCase(Locale.ROOT).replace(Regex("[^a-z]+"), "")
}

fun String.equalsCondensed(other: String) = condenseString(this) == condenseString(other)

val ChatColor.officialName
    get() = when (this) {
        MAGIC -> "obfuscated"
        else -> name.toLowerCase(Locale.ROOT)
    }

fun getColorByOfficialName(officialName: String) = ChatColor.values().firstOrNull {
    it.officialName.equalsCondensed(officialName)
}
