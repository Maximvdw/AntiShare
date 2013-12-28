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
package com.turt2live.antishare.inventory;

/**
 * Class for temporary inventories
 *
 * @author turt2live
 */
public class TemporaryASInventory {

    private ASInventory lastInventory;
    private ASInventory tempInventory;

    /**
     * Creates a new temporary inventory
     *
     * @param last the last used inventory
     * @param temp the temporary inventory
     */
    public TemporaryASInventory(ASInventory last, ASInventory temp) {
        this.lastInventory = last;
        this.tempInventory = temp;
    }

    /**
     * Gets the last used inventory
     *
     * @return the last used inventory
     */
    public ASInventory getLastInventory() {
        return lastInventory;
    }

    /**
     * Gets the temporary inventory
     *
     * @return the temporary inventory
     */
    public ASInventory getTempInventory() {
        return tempInventory;
    }

}
