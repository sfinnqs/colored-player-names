package com.voichick.cpn

import org.bukkit.ChatColor
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

    private fun possibleColors(): Map<ChatColor, Double> {
        val colors = plugin.playerColors
        val weights = plugin.cpnConfig.weights
        val lowestCount = weights.keys.map { colors.count(it) }.min()
        return weights.filterKeys { colors.count(it) == lowestCount }
    }

}
