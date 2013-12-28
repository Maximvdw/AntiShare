/*******************************************************************************
 * Copyright (c) 2013 Travis Ralston.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 *
 * Contributors:
 * turt2live (Travis Ralston) - initial API and implementation
 ******************************************************************************/
package com.turt2live.antishare.manager;

import com.turt2live.antishare.AntiShare;
import com.turt2live.antishare.compatibility.*;
import com.turt2live.antishare.compatibility.other.MagicSpells;
import com.turt2live.antishare.compatibility.other.PlayerVaults;
import com.turt2live.antishare.compatibility.other.WorldEdit;
import com.turt2live.antishare.compatibility.type.BlockLogger;
import com.turt2live.antishare.compatibility.type.BlockProtection;
import com.turt2live.antishare.compatibility.type.RegionProtection;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages hooks into other plugins
 *
 * @author turt2live
 */
public class HookManager {

    private AntiShare plugin = AntiShare.p;
    private final List<BlockProtection> blocks = new ArrayList<BlockProtection>();
    private final List<RegionProtection> regions = new ArrayList<RegionProtection>();
    private final List<BlockLogger> loggers = new ArrayList<BlockLogger>();
    private MagicSpells spells;
    private WorldEdit worldedit;
    private PlayerVaults pv;

    /**
     * Sends a block break to all block logging plugins
     *
     * @param playerName the player name involved, or null for no associated player
     * @param location   the location
     * @param before     the material before the break
     * @param data       the data before the break
     */
    public void sendBlockBreak(String playerName, Location location, Material before, byte data) {
        for (BlockLogger logger : loggers) {
            logger.breakBlock(playerName != null ? BlockLogger.PLAYER_NAME + "_" + playerName : BlockLogger.PLAYER_NAME, location, before, data);
        }
    }

    /**
     * Sends a block place to all block logging plugins
     *
     * @param playerName the player name involved, or null for no associated player
     * @param location   the location
     * @param after      the material after the break
     * @param data       the data after the break
     */
    public void sendBlockPlace(String playerName, Location location, Material after, byte data) {
        for (BlockLogger logger : loggers) {
            logger.placeBlock(playerName != null ? BlockLogger.PLAYER_NAME + "_" + playerName : BlockLogger.PLAYER_NAME, location, after, data);
        }
    }

    /**
     * Sends an entity break to all block logging plugins
     *
     * @param playerName the player name involved, or null for no associated player
     * @param location   the location
     * @param before     the material before the break
     * @param data       the data before the break
     */
    public void sendEntityBreak(String playerName, Location location, Material before, byte data) {
        for (BlockLogger logger : loggers) {
            logger.breakHanging(playerName != null ? BlockLogger.PLAYER_NAME + "_" + playerName : BlockLogger.PLAYER_NAME, location, before, data);
        }
    }

    /**
     * Sends an entity place to all block logging plugins
     *
     * @param playerName the player name involved, or null for no associated player
     * @param location   the location
     * @param after      the material after the break
     * @param data       the data after the break
     */
    public void sendEntityPlace(String playerName, Location location, Material after, byte data) {
        for (BlockLogger logger : loggers) {
            logger.placeHanging(playerName != null ? BlockLogger.PLAYER_NAME + "_" + playerName : BlockLogger.PLAYER_NAME, location, after, data);
        }
    }

    /**
     * Determines if there is a region at a location
     *
     * @param location the location
     * @return true if a region was found, false otherwise
     */
    public boolean checkForRegion(Location location) {
        for (RegionProtection protection : regions) {
            if (protection.isRegion(location)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks for a player placing/breaking a block into a region
     *
     * @param player the player
     * @param block  the block
     * @return true if region found (not allowed), false otherwise
     */
    public boolean checkForRegion(Player player, Block block) {
        for (RegionProtection protection : regions) {
            if (!protection.isAllowed(player, block)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks a block for sign protection
     *
     * @param block the block
     * @return true if a sign protection plugin claims this block protected
     */
    public boolean checkForSignProtection(Block block) {
        return checkForBlockProtection(block);
    }

    /**
     * Checks a block for protection
     *
     * @param block the block
     * @return true if a plugin claims this block protected
     */
    public boolean checkForBlockProtection(Block block) {
        for (BlockProtection protection : blocks) {
            if (protection.isProtected(block)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Determines if the WorldEdit hook has been loaded
     *
     * @return true if there is a valid WorldEdit hook
     */
    public boolean hasWorldEdit() {
        return worldedit != null;
    }

    /**
     * Gets the WorldEdit hook, may be null
     *
     * @return the WorldEdit hook, if any
     */
    public WorldEdit getWorldEdit() {
        return worldedit;
    }

    private void hooked(Plugin hook) {
        plugin.getLogger().info(plugin.getMessages().getMessage("hooked", hook.getName()));
    }

    private void notHooked(Plugin hook) {
        plugin.getLogger().info(plugin.getMessages().getMessage("not-hooked", hook.getName()));
    }

    /**
     * Reloads the hook manager
     */
    public void reload() {
        blocks.clear();
        loggers.clear();
        regions.clear();
        spells = null;
        worldedit = null;
        pv = null;
        load();
    }

    /**
     * Loads all the hooks for AntiShare
     */
    public void load() {
        // Clear
        blocks.clear();
        loggers.clear();
        regions.clear();
        spells = null;
        worldedit = null;
        pv = null;

        // Find plugins
        Plugin chestshop = plugin.getServer().getPluginManager().getPlugin("ChestShop");
        if (chestshop != null) {
            hooked(chestshop);
            blocks.add(new ChestShop());
        }
        Plugin lwc = plugin.getServer().getPluginManager().getPlugin("LWC");
        if (lwc != null) {
            hooked(lwc);
            blocks.add(new LWC());
        }
        Plugin lockette = plugin.getServer().getPluginManager().getPlugin("Lockette");
        if (lockette != null) {
            hooked(lockette);
            blocks.add(new Lockette());
        }
        Plugin towny = plugin.getServer().getPluginManager().getPlugin("Towny");
        if (towny != null) {
            hooked(towny);
            regions.add(new Towny());
        }
        Plugin logblock = plugin.getServer().getPluginManager().getPlugin("LogBlock");
        if (logblock != null) {
            hooked(logblock);
            loggers.add(new LogBlock());
        }
        Plugin magicspells = plugin.getServer().getPluginManager().getPlugin("MagicSpells");
        if (magicspells != null) {
            hooked(magicspells);
            spells = new MagicSpells();
            plugin.getServer().getPluginManager().registerEvents(spells, plugin);
        }
        Plugin worldedit = plugin.getServer().getPluginManager().getPlugin("WorldEdit");
        if (worldedit != null) {
            hooked(worldedit);
            this.worldedit = new WorldEdit(worldedit);
        }
        Plugin playervaults = plugin.getServer().getPluginManager().getPlugin("PlayerVaults");
        if (playervaults != null) {
            // We need to do an extra class check on PlayerVaults
            try {
                if (Class.forName("com.drtshock.playervaults.util.VaultHolder") != null) {
                    hooked(playervaults);
                    pv = new PlayerVaults();
                    plugin.getServer().getPluginManager().registerEvents(pv, plugin);
                } else {
                    notHooked(playervaults);
                }
            } catch (ClassNotFoundException e) {
                notHooked(playervaults);
            }
        }
    }

}
