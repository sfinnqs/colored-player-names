package com.voichick.cpn

import org.bukkit.ChatColor
import java.util.*
import java.util.concurrent.ThreadLocalRandom

class ColorPicker(private val plugin: ColoredPlayerNames) {

    operator fun invoke(): ChatColor? {
        val colors = possibleColors()
        val weightSum = colors.values.sum()
        if (weightSum <= 0.0)
            return null
        val randomVal = ThreadLocalRandom.current().nextDouble(weightSum)
        var accumulation = 0.0
        for (entry in colors) {
            accumulation += entry.value
            if (accumulation >= randomVal)
                return entry.key
        }
        return null
    }

    fun availableColors(): Set<ChatColor> {
        val result = EnumSet.noneOf(ChatColor::class.java)
        result.addAll(possibleColors().keys)
        result.addAll(possibleColors(false).keys)
        return result
    }

    private fun possibleColors(posWeightsOnly: Boolean = true): Map<ChatColor, Double> {
        val colors = plugin.playerColors
        val weights = plugin.cpnConfig.weights
        val posWeights = if (posWeightsOnly)
            weights.filterValues { it >=0 }
        else
            weights
        val lowestCount = posWeights.keys.map { colors.count(it) }.min()
        val filtered =  posWeights.filterKeys { colors.count(it) == lowestCount }
        val result = EnumMap<ChatColor, Double>(ChatColor::class.java)
        result.putAll(filtered)
        return filtered
    }

}
