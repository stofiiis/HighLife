package com.stofiiis.highlife.util;

import com.stofiiis.highlife.HighLifeMod;
import com.stofiiis.highlife.registry.ModItems;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class AdvancementTracker {
    private static final String TAG_USED_JOINT = HighLifeMod.MODID + ".used_joint";
    private static final String TAG_USED_PIPE = HighLifeMod.MODID + ".used_pipe";
    private static final String TAG_USED_BONG = HighLifeMod.MODID + ".used_bong";

    private AdvancementTracker() {
    }

    public static void onSmokeUsed(Player player, ItemStack usedStack) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        grant(serverPlayer, "first_smoke", "smoked_any");

        CompoundTag persistentData = serverPlayer.getPersistentData();
        if (usedStack.is(ModItems.JOINT.get())) {
            persistentData.putBoolean(TAG_USED_JOINT, true);
        } else if (usedStack.is(ModItems.PIPE.get())) {
            persistentData.putBoolean(TAG_USED_PIPE, true);
        } else if (usedStack.is(ModItems.BONG.get())) {
            persistentData.putBoolean(TAG_USED_BONG, true);
        }

        boolean smokedJoint = persistentData.getBooleanOr(TAG_USED_JOINT, false);
        boolean smokedPipe = persistentData.getBooleanOr(TAG_USED_PIPE, false);
        boolean smokedBong = persistentData.getBooleanOr(TAG_USED_BONG, false);
        if (smokedJoint && smokedPipe && smokedBong) {
            grant(serverPlayer, "smoke_trinity", "all_three_tools");
        }
    }

    public static void onPeaceApplied(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            grant(serverPlayer, "zen_walk", "peace_active");
        }
    }

    public static void onPipeLoaded(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            grant(serverPlayer, "pipe_loader", "pipe_loaded");
        }
    }

    public static void onBongLoaded(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            grant(serverPlayer, "bong_loader", "bong_loaded");
        }
    }

    public static void onBongRefilled(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            grant(serverPlayer, "bong_hydration", "bong_refilled");
        }
    }

    public static void onSeedMixCollected(Player player, boolean boosted) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        grant(serverPlayer, "first_cross", "cross_done");
        if (boosted) {
            grant(serverPlayer, "bonemeal_hacker", "boosted_cross");
        }
    }

    private static void grant(ServerPlayer player, String advancementId, String criterion) {
        if (player.level().getServer() == null) {
            return;
        }

        AdvancementHolder advancement = player.level().getServer().getAdvancements()
                .get(Identifier.fromNamespaceAndPath(HighLifeMod.MODID, advancementId));
        if (advancement != null) {
            player.getAdvancements().award(advancement, criterion);
        }
    }
}
