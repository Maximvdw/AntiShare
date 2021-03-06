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

package com.turt2live.antishare.object;

import com.turt2live.antishare.object.attribute.Facing;
import com.turt2live.antishare.object.attribute.TrackedState;

/**
 * An AntiShare Block.
 *
 * @author turt2live
 */
public interface ABlock extends Rejectable {

    /**
     * Represents the various chest states
     */
    public static enum ChestType {
        /**
         * A single standard chest
         */
        NORMAL,
        /**
         * A single trapped chest
         */
        TRAPPED,
        /**
         * Two standard chests merged into one large standard chest
         */
        DOUBLE_NORMAL,
        /**
         * Two trapped chests merged into one large trapped chest
         */
        DOUBLE_TRAPPED,
        /**
         * An ender chest
         */
        ENDER,
        /**
         * A locked chest (April Fool's Joke)
         *
         * @deprecated No longer used in newer versions
         */
        @Deprecated
        LOCKED,
        /**
         * Not a chest
         */
        NONE
    }

    /**
     * Determines if this block is considered a container.
     *
     * @return true if this block is a container, false otherwise
     */
    public boolean isContainer();

    /**
     * Determines if this block is attached to the supplied block.
     * If this block cannot be attached to blocks, this will
     * return false.
     *
     * @param block the block to check, if null: false is returned
     *
     * @return whether or not this block is attached to the specified block
     */
    public boolean isAttached(ABlock block);

    /**
     * Gets the facing direction of this block. May be null if not applicable
     *
     * @return the facing direction, or null
     */
    public Facing getFacingDirection();

    /**
     * Gets the applicable chest type for this block
     *
     * @return the applicable chest type, returning {@link ABlock.ChestType#NONE} for 'not a chest'
     */
    public ChestType getChestType();

    /**
     * Gets the other part of this chest if {@link #getChestType()} returns
     * a double chest (or similar) value. If this is not a chest, or not a
     * double chest, this returns null.
     *
     * @return the partner chest, or null
     */
    public ABlock getOtherChest();

    /**
     * Gets the block's location
     *
     * @return the block location
     */
    public ASLocation getLocation();

    /**
     * Gets the world of this block
     *
     * @return the world
     */
    public AWorld getWorld();

    /**
     * Gets the block relative to this block. For example, if this
     * is passed {@link com.turt2live.antishare.object.attribute.Facing#NORTH}, the
     * block immediately NORTH of this block will be returned.
     *
     * @param relative the direction to get the relative block from, cannot be null
     *
     * @return the relative block
     */
    public ABlock getRelative(Facing relative);

    /**
     * Determines if a player can place this block. This should be
     * strictly a lookup of permissions without validating with any
     * engine components or through the rejection lists.
     * <p/>
     * This uses the tri-state enum {@link com.turt2live.antishare.object.attribute.TrackedState}
     * to represent various states, as outlined below.
     * <p/>
     * {@link com.turt2live.antishare.object.attribute.TrackedState#NOT_PRESENT} - Neither allow or deny permission found<br/>
     * {@link com.turt2live.antishare.object.attribute.TrackedState#INCLUDED} - Allow permission found<br/>
     * {@link com.turt2live.antishare.object.attribute.TrackedState#NEGATED} - Deny permission found
     *
     * @param player the player, cannot be null
     *
     * @return the appropriate tracking state as defined
     */
    public TrackedState canPlace(APlayer player);

    /**
     * Determines if a player can break this block. This should be
     * strictly a lookup of permissions without validating with any
     * engine components or through the rejection lists.
     * <p/>
     * This uses the tri-state enum {@link com.turt2live.antishare.object.attribute.TrackedState}
     * to represent various states, as outlined below.
     * <p/>
     * {@link com.turt2live.antishare.object.attribute.TrackedState#NOT_PRESENT} - Neither allow or deny permission found<br/>
     * {@link com.turt2live.antishare.object.attribute.TrackedState#INCLUDED} - Allow permission found<br/>
     * {@link com.turt2live.antishare.object.attribute.TrackedState#NEGATED} - Deny permission found
     *
     * @param player the player, cannot be null
     *
     * @return the appropriate tracking state as defined
     */
    public TrackedState canBreak(APlayer player);

    /**
     * Determines if a player can interact with this block. This should be
     * strictly a lookup of permissions without validating with any
     * engine components or through the rejection lists.
     * <p/>
     * This uses the tri-state enum {@link com.turt2live.antishare.object.attribute.TrackedState}
     * to represent various states, as outlined below.
     * <p/>
     * {@link com.turt2live.antishare.object.attribute.TrackedState#NOT_PRESENT} - Neither allow or deny permission found<br/>
     * {@link com.turt2live.antishare.object.attribute.TrackedState#INCLUDED} - Allow permission found<br/>
     * {@link com.turt2live.antishare.object.attribute.TrackedState#NEGATED} - Deny permission found
     *
     * @param player the player, cannot be null
     *
     * @return the appropriate tracking state as defined
     */
    public TrackedState canInteract(APlayer player);
}
