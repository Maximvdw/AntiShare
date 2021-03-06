/*******************************************************************************
 * Copyright (C) 2014 Travis Ralston (turt2live)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/

package com.turt2live.antishare.io.generics;

import com.turt2live.antishare.ASGameMode;
import com.turt2live.antishare.io.InventoryManager;
import com.turt2live.antishare.object.AInventory;
import com.turt2live.antishare.object.AWorld;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Represents a generic inventory manager
 *
 * @author turt2live
 */
// TODO: Unit test
public abstract class GenericInventoryManager implements InventoryManager {

    /**
     * Represents a structure for storing an inventory
     */
    protected final class InventoryKey {

        /**
         * The gamemode
         */
        public final ASGameMode gamemode;

        /**
         * The world
         */
        public final AWorld world;

        /**
         * The player
         */
        public final UUID player;

        /**
         * Creates a new inventory key
         *
         * @param gamemode the gamemode, can be null
         * @param world    the world, can be null
         * @param player   the player, can be null
         */
        public InventoryKey(ASGameMode gamemode, AWorld world, UUID player) {
            this.gamemode = gamemode;
            this.world = world;
            this.player = player;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof InventoryKey)) return false;

            InventoryKey that = (InventoryKey) o;

            if (gamemode != that.gamemode) return false;
            if (player != null ? !player.equals(that.player) : that.player != null) return false;
            return !(world != null ? !world.getName().equals(that.world.getName()) : that.world != null);

        }

        @Override
        public int hashCode() {
            int result = gamemode != null ? gamemode.hashCode() : 0;
            result = 31 * result + (world != null ? world.getName().hashCode() : 0);
            result = 31 * result + (player != null ? player.hashCode() : 0);
            return result;
        }
    }

    private ConcurrentMap<InventoryKey, AInventory> inventories = new ConcurrentHashMap<>();

    @Override
    public final AInventory getInventory(UUID player, ASGameMode gamemode, AWorld world) {
        if (player == null || gamemode == null || world == null) throw new IllegalArgumentException();

        InventoryKey key = createKey(player, gamemode, world);
        if (!inventories.containsKey(key)) {
            List<AInventory> loaded = getInventories(player);

            // Populate collection
            for (AInventory inv : loaded) {
                setInventory(player, inv);
            }
        }

        if (!inventories.containsKey(key)) return createEmptyInventory(player, gamemode, world);
        else return inventories.get(key);
    }

    @Override
    public final void setInventory(UUID player, AInventory inventory) {
        if (player == null || inventory == null) throw new IllegalArgumentException();

        AWorld world = inventory.getWorld();
        ASGameMode gamemode = inventory.getGameMode();

        InventoryKey key = createKey(player, gamemode, world);
        inventories.put(key, inventory);
    }

    /**
     * Loads the inventory manager. Can be safely overridden.
     */
    @Override
    public void load() {
    }

    @Override
    public final void clear() {
        inventories.clear();
    }

    @Override
    public final void save() {
        saveInventories(convertInventories());
        clear();
    }

    @Override
    public final void save(UUID player) {
        List<AInventory> applicable = new ArrayList<>();
        List<InventoryKey> keys = new ArrayList<>();

        for (Map.Entry<InventoryKey, AInventory> entry : inventories.entrySet()) {
            if (entry.getKey().player.equals(player)) {
                applicable.add(entry.getValue());
                keys.add(entry.getKey());
            }
        }

        if (!applicable.isEmpty()) {
            Map<UUID, List<AInventory>> set = new HashMap<>();
            set.put(player, applicable);
            saveInventories(set);

            for (InventoryKey key : keys) inventories.remove(key);
        }
    }

    /**
     * Gets a list of inventories for the specified player. If none are found, or the
     * UUID is null, this returns an empty list.
     *
     * @param uuid the uuid to lookup
     *
     * @return the inventories found in memory, or an empty list
     */
    protected final List<AInventory> getInventoriesFor(UUID uuid) {
        List<AInventory> inventoryList = new ArrayList<>();
        if (uuid == null) return inventoryList;

        for (Map.Entry<InventoryKey, AInventory> entry : inventories.entrySet()) {
            if (entry.getKey().player.equals(uuid)) {
                inventoryList.add(entry.getValue());
            }
        }

        return inventoryList;
    }

    /**
     * Saves a set of inventories to disk. Inventories for a single player that are
     * not in the set may be removed. Players not in the set should be left untouched.
     *
     * @param inventories the inventories to save
     */
    protected abstract void saveInventories(Map<UUID, List<AInventory>> inventories);

    /**
     * Creates an empty inventory that does not get saved to the disk
     *
     * @param player   the player, never null
     * @param gamemode the gamemode, never null
     * @param world    the world, never null
     *
     * @return the created, empty, inventory
     */
    protected abstract AInventory createEmptyInventory(UUID player, ASGameMode gamemode, AWorld world);

    private InventoryKey createKey(UUID uuid, ASGameMode gamemode, AWorld world) {
        if (uuid == null || gamemode == null || world == null)
            throw new RuntimeException("Cannot create an inventory key from null values");

        return new InventoryKey(gamemode, world, uuid);
    }

    private Map<UUID, List<AInventory>> convertInventories() {
        Map<UUID, List<AInventory>> byPlayer = new HashMap<>();

        for (Map.Entry<InventoryKey, AInventory> entry : inventories.entrySet()) {
            if (!byPlayer.containsKey(entry.getKey().player)) {
                byPlayer.put(entry.getKey().player, new ArrayList<AInventory>());
            }

            byPlayer.get(entry.getKey().player).add(entry.getValue());
        }

        return byPlayer;
    }
}
