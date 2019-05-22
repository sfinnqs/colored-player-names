package com.voichick.cpn

import org.bukkit.ChatColor
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.InvalidConfigurationException
import org.bukkit.entity.Player
import java.util.*
import kotlin.collections.ArrayList

class CpnConfig(private val plugin: ColoredPlayerNames) {

    val autoUpdate = plugin.config.getBoolean("auto-update")
    val scoreboard = plugin.config.getBoolean("scoreboard")

    private val colorSections: Map<ChatColor, ColorSection>

    init {
        val colorsSection = getSectionOrSet("colors")
        colorSections = getColorSections(colorsSection)
    }

    val colors = Colors(colorSections)
    val colorNames = getColorNames(colorSections)
    val aliases = getAliases(colorSections)
    val weights = getWeights(colorSections)

    private val playerSections: MutableMap<UUID, PlayerSection>

    init {
        val playersSection = getSectionOrSet("players")
        playerSections = getPlayerSections(playersSection, colors, colorNames)
    }

    fun getStaticColor(player: Player) = playerSections[player.uniqueId]?.color

    fun updateWithPlayer(player: Player): Boolean {
        val permColor = getPermissionColor(player) ?: return false
        val playerSection = PlayerSection(player.name, player.uniqueId, permColor)
        return updateWithPlayerSection(playerSection)
    }

    fun updateWithPlayerSection(playerSection: PlayerSection): Boolean {
        updateAllNames()
        val uuid = playerSection.uuid
        val oldSection = playerSections[uuid]
        if (oldSection?.color == playerSection.color)
            return false
        playerSections[uuid] = playerSection
        return true
    }

    fun writeToFile() {
        val config = plugin.config
        config["auto-update"] = autoUpdate
        config["scoreboard"] = scoreboard
        val colorsSection = config.createSection("colors")
        for (colorSection in colorSections.values) {
            val officialName = colorSection.color.officialName
            val section = colorsSection.createSection(officialName)
            val name = colorSection.name
            if (name != officialName)
                section["name"] = name
            val weight = colorSection.weight
            if (weight > 0.0)
                section["weight"] = weight
            val aliases = colorSection.aliases
            if (aliases.isNotEmpty())
                section["aliases"] = aliases.toList()
        }
        val playersSection = config.createSection("players")
        for (playerSection in playerSections.values) {
            val section = playersSection.createSection(playerSection.name)
            section["uuid"] = playerSection.uuid.toString()
            section["color"] = colorNames[playerSection.color]
        }
        plugin.saveConfig()
    }

    private fun getSectionOrSet(path: String): ConfigurationSection {
        val config = plugin.config
        val result = config.getConfigurationSection(path) ?: return config.createSection(path)
        return if (config.isSet(path) && config.isConfigurationSection(path)) {
            result
        } else {
            val default = result.defaultSection ?: return config.createSection(path)
            config.createSection(path, default.getValues(true))
        }
    }

    private fun getPermissionColor(player: Player): ChatColor? {
        for (color in ChatColor.values()) {
            val colorName = colorNames[color] ?: color.officialName
            val permission = "coloredplayernames.$colorName"
            if (player.isPermissionSet(permission) && player.hasPermission(permission)) {
                plugin.logger.warning {
                    "Player \"${player.name}\" still has deprecated permission: $permission"
                }
                return color
            }
        }
        return null
    }

    private fun updateAllNames() {
        val playerSectionsCopy = ArrayList(playerSections.values)
        for (playerSection in playerSectionsCopy) {
            val uuid = playerSection.uuid
            val name = plugin.server.getOfflinePlayer(uuid).name
            if (name == null || name == playerSection.name) continue
            val newSection = PlayerSection(name, uuid, playerSection.color)
            playerSections[uuid] = newSection
        }
    }

    private companion object {

        fun getColorSections(colorsSection: ConfigurationSection): Map<ChatColor, ColorSection> {
            val result = EnumMap<ChatColor, ColorSection>(ChatColor::class.java)
            for (key in colorsSection.getKeys(false)) {
                val section = colorsSection.getConfigurationSection(key) ?: continue
                val colorSection = ColorSection(section)
                result.merge(colorSection.color, colorSection) { a, b ->
                    val color = a.color
                    assert(b.color == color)
                    val name = a.name
                    if (b.name != name) {
                        throw InvalidConfigurationException("Two different names used for same color: ${a.name} vs. ${b.name}")
                    }
                    val weight = a.weight + b.weight
                    val aliases = TreeSet<String>()
                    aliases.addAll(a.aliases)
                    aliases.addAll(b.aliases)
                    ColorSection(color, name, weight, aliases)
                }
            }
            val allNames = mutableSetOf<String>()
            for (section in result.values) {
                for (name in section.aliases + section.name) {
                    val newName = allNames.add(name)
                    if (!newName) {
                        throw InvalidConfigurationException("Duplicate name: $name")
                    }
                }
            }
            return result
        }

        fun getColorNames(colorSections: Map<ChatColor, ColorSection>): Map<ChatColor, String> {
            val result = EnumMap<ChatColor, String>(ChatColor::class.java)
            for (section in colorSections.values)
                result[section.color] = section.name
            return result
        }

        fun getWeights(colorSections: Map<ChatColor, ColorSection>): Map<ChatColor, Double> {
            val result = EnumMap<ChatColor, Double>(ChatColor::class.java)
            for (section in colorSections.values)
                result[section.color] = section.weight
            return result
        }

        fun getAliases(colorSections: Map<ChatColor, ColorSection>): Map<ChatColor, Set<String>> {
            val result = EnumMap<ChatColor, Set<String>>(ChatColor::class.java)
            for (section in colorSections.values)
                result[section.color] = section.aliases
            return result
        }

        fun getPlayerSections(playersSection: ConfigurationSection, colors: Colors, colorNames: Map<ChatColor, String>): MutableMap<UUID, PlayerSection> {
            val result = TreeMap<UUID, PlayerSection>()
            for (key in playersSection.getKeys(false)) {
                val section = playersSection.getConfigurationSection(key)
                val playerSection = if (section == null) {
                    val colorString = playersSection.getString(key) ?: continue
                    val color = colors[colorString]
                            ?: throw InvalidConfigurationException("Unrecognized color: $colorString")
                    PlayerSection(key, color)
                } else {
                    PlayerSection(section, colors)
                }
                result.merge(playerSection.uuid, playerSection) { a, b ->
                    assert(a.uuid == b.uuid)
                    val name = a.name
                    assert(b.name == name)
                    val aColor = a.color
                    val bColor = b.color
                    if (aColor != bColor) {
                        val aColorName = colorNames[aColor]
                        val bColorName = colorNames[bColor]
                        throw InvalidConfigurationException("Two different colors ($aColorName vs. $bColorName) assigned to player: $name")
                    }
                    a
                }
            }
            return result
        }

    }
}
