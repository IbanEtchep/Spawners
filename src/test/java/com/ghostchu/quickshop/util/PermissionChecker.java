package com.ghostchu.quickshop.util;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;

public final class PermissionChecker {

    private PermissionChecker() {
    }

    public static final class FakeBlockBreakEvent extends BlockBreakEvent {

        public FakeBlockBreakEvent(Block block, Player player) {
            super(block, player);
        }
    }
}
