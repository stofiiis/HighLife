package com.stofiiis.weed.util;

import com.stofiiis.weed.WeedMod;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

public final class ToleranceData {
    private static final String TOLERANCE_TAG = WeedMod.MODID + ".tolerance";

    private ToleranceData() {
    }

    public static int get(Player player) {
        return Mth.clamp(player.getPersistentData().getInt(TOLERANCE_TAG).orElse(0), 0, 100);
    }

    public static void add(Player player, int amount) {
        set(player, get(player) + amount);
    }

    public static void set(Player player, int value) {
        CompoundTag tag = player.getPersistentData();
        tag.putInt(TOLERANCE_TAG, Mth.clamp(value, 0, 100));
    }

    public static void decay(Player player, int amount) {
        if (amount <= 0) {
            return;
        }
        add(player, -amount);
    }
}
