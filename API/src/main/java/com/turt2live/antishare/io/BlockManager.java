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

package com.turt2live.antishare.io;

import com.turt2live.antishare.object.ASLocation;
import com.turt2live.antishare.object.attribute.ObjectType;

import java.util.List;

/**
 * Handles block information
 *
 * @author turt2live
 */
public interface BlockManager {

    /**
     * Gets the store for a specified location
     *
     * @param x the x location
     * @param y the y location
     * @param z the z location
     *
     * @return the block store for the specified location, may be null
     */
    public BlockStore getStore(int x, int y, int z);

    /**
     * Gets the store for a specified location
     *
     * @param location the location, cannot be null
     *
     * @return the block store for the specified location, may be null
     */
    public BlockStore getStore(ASLocation location);

    /**
     * Sets a block's type for the given location
     *
     * @param x    the X location
     * @param y    the Y location
     * @param z    the Z location
     * @param type the new block type. Null is assumed to be {@link com.turt2live.antishare.object.attribute.ObjectType#UNKNOWN}
     */
    public void setBlockType(int x, int y, int z, ObjectType type);

    /**
     * Sets a block's type for the given location
     *
     * @param location the location, cannot be null
     * @param type     the new block type. Null is assumed to be {@link com.turt2live.antishare.object.attribute.ObjectType#UNKNOWN}
     */
    public void setBlockType(ASLocation location, ObjectType type);

    /**
     * Gets the block type for the given location
     *
     * @param x the X location
     * @param y the Y location
     * @param z the Z location
     *
     * @return the block type
     */
    public ObjectType getBlockType(int x, int y, int z);

    /**
     * Gets the block type for the given location
     *
     * @param location the location, cannot be null
     *
     * @return the block type
     */
    public ObjectType getBlockType(ASLocation location);

    /**
     * Saves all the known block stores
     */
    public void saveAll();

    /**
     * Loads all the known block stores. The implementing manager will assume a
     * save has been completed and may wipe the previous entries from memory.
     *
     * @return a list of block stores loaded because of this operation
     */
    public List<BlockStore> loadAll();

    /**
     * Runs a cleanup (on the current thread) on the BlockManager. This will remove
     * any excess objects which have not been touched from the manager by cleanly
     * unloading them.
     */
    public void cleanup();
}
