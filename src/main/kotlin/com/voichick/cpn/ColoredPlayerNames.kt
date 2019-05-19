package com.voichick.cpn

import net.gravitydevelopment.updater.Updater
import net.gravitydevelopment.updater.Updater.UpdateType.DEFAULT
import org.bukkit.Bukkit
import org.bukkit.command.TabExecutor
import org.bukkit.plugin.java.JavaPlugin
import java.util.logging.Logger

/**
 * The ColoredPlayerNames plugin. This is the main entry point into this plugin's functionality.
 */
class ColoredPlayerNames : JavaPlugin() {

    internal val colorPicker = ColorPicker(this)
    private var privateConfig: CpnConfig? = null
    internal val cpnConfig: CpnConfig
        get() = privateConfig ?: reload().first
    private var privateColors: PlayerColors? = null
    /** A data structure that keeps track of which players have which colors */
    internal val playerColors: PlayerColors
        get() = privateColors ?: reload().second


    override fun onEnable() {
        privateLogger = logger
        reload()
        if (cpnConfig.autoUpdate)
            Updater(this, ID, file, DEFAULT, true)

        server.pluginManager.registerEvents(CpnListener(this), this)

        setupCommand("changecolor", ChangeColorExecutor(this))
        setupCommand("coloredplayernames", ReloadExecutor(this))

        for (player in server.onlinePlayers)
            playerColors[player] = colorPicker()
    }

    fun reload(): Pair<CpnConfig, PlayerColors> {
        uncolorAll()
        saveDefaultConfig()
        reloadConfig()
        val newConfig = CpnConfig(this)
        privateConfig = newConfig
        newConfig.writeToFile()
        val scoreboardManager = server.scoreboardManager
        val newColors = if (scoreboardManager != null && newConfig.scoreboard) {
            PlayerColors(scoreboardManager.newScoreboard)
        } else {
            PlayerColors()
        }
        privateColors = newColors
        return newConfig to newColors
    }

    override fun onDisable() = uncolorAll()

    private fun setupCommand(name: String, executor: TabExecutor) {
        val command = getCommand(name)
        if (command == null) {
            logger.severe {
                "Command not found: $name"
            }
        } else {
            command.setExecutor(executor)
            command.tabCompleter = executor
        }
    }

    private fun uncolorAll() = server.onlinePlayers.forEach { privateColors?.set(it, null) }

    companion object {
        private var privateLogger: Logger? = null
        val logger
            get() = privateLogger ?: Bukkit.getLogger()

        private const val ID = 80947
    }

}
