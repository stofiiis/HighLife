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
    private static final String TAG_USED_HERB_ROLL = HighLifeMod.MODID + ".used_herb_roll";
    private static final String TAG_USED_INFUSION_WAND = HighLifeMod.MODID + ".used_infusion_wand";
    private static final String TAG_USED_ALCHEMY_FLASK = HighLifeMod.MODID + ".used_alchemy_flask";

    private AdvancementTracker() {
    }

    public static void onInfusionUsed(Player player, ItemStack usedStack) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        grant(serverPlayer, "first_infusion", "infusion_used");

        CompoundTag persistentData = serverPlayer.getPersistentData();
        if (usedStack.is(ModItems.HERB_ROLL.get())) {
            persistentData.putBoolean(TAG_USED_HERB_ROLL, true);
        } else if (usedStack.is(ModItems.INFUSION_WAND.get())) {
            persistentData.putBoolean(TAG_USED_INFUSION_WAND, true);
        } else if (usedStack.is(ModItems.ALCHEMY_FLASK.get())) {
            persistentData.putBoolean(TAG_USED_ALCHEMY_FLASK, true);
        }

        boolean usedHerbRoll = persistentData.getBooleanOr(TAG_USED_HERB_ROLL, false);
        boolean usedInfusionWand = persistentData.getBooleanOr(TAG_USED_INFUSION_WAND, false);
        boolean usedAlchemyFlask = persistentData.getBooleanOr(TAG_USED_ALCHEMY_FLASK, false);
        if (usedHerbRoll && usedInfusionWand && usedAlchemyFlask) {
            grant(serverPlayer, "tool_trinity", "all_three_tools");
        }
    }

    public static void onPeaceApplied(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            grant(serverPlayer, "zen_walk", "peace_active");
        }
    }

    public static void onWandLoaded(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            grant(serverPlayer, "wand_loader", "infusion_wand_loaded");
        }
    }

    public static void onFlaskLoaded(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            grant(serverPlayer, "flask_loader", "alchemy_flask_loaded");
        }
    }

    public static void onFlaskRefilled(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            grant(serverPlayer, "flask_hydration", "alchemy_flask_refilled");
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
