package com.turt2live.antishare.bukkit.groups;

import com.turt2live.antishare.ASGameMode;
import com.turt2live.antishare.bukkit.*;
import com.turt2live.antishare.bukkit.lists.BukkitBlockList;
import com.turt2live.antishare.configuration.Configuration;
import com.turt2live.antishare.configuration.groups.MainGroup;
import com.turt2live.antishare.engine.BlockTypeList;
import com.turt2live.antishare.engine.RejectionList;

import java.util.ArrayList;

/**
 * Main group
 *
 * @author turt2live
 */
public class BukkitMainGroup extends MainGroup {

    /**
     * Creates a new main group from the configuration and manager supplied
     *
     * @param configuration the configuration to be used, cannot be null
     */
    public BukkitMainGroup(Configuration configuration) {
        super(configuration);
    }

    @Override
    public BlockTypeList getTrackedList(ASGameMode gameMode) {
        if (gameMode == null) throw new IllegalArgumentException("gamemode cannot be null");
        MaterialProvider provider = AntiShare.getInstance().getMaterialProvider();
        BukkitBlockList list = new BukkitBlockList(provider);
        list.populateBlocks(super.configuration.getStringList("blocks." + gameMode.name().toLowerCase(), new ArrayList<String>()));
        return list;
    }

    @Override
    public RejectionList getRejectionList(RejectionList.ListType type) {
        if (type == null) throw new IllegalArgumentException("list type cannot be null");
        String configKey = BukkitUtils.getStringName(type);
        MaterialProvider provider = AntiShare.getInstance().getMaterialProvider();
        BukkitBlockList list = new BukkitBlockList(provider, type);
        list.populateBlocks(super.configuration.getStringList("lists." + configKey, new ArrayList<String>()));
        return list;
    }
}