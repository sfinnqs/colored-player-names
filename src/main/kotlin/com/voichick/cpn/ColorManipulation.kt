package com.voichick.cpn

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
