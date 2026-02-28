package com.stofiiis.highlife.item;

import com.stofiiis.highlife.registry.ModSounds;

import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class HerbRollItem extends InfusionItem {
    private static final String TAG_PUFFS_LEFT = "roll_casts_left";
    private static final int MAX_PUFFS = 3;

    public HerbRollItem(Properties properties, float potency, int serenityDuration, int thirstDuration, int hazeDuration, int toleranceGain) {
        super(properties, potency, serenityDuration, thirstDuration, hazeDuration, toleranceGain);
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.NONE;
    }

    @Override
    protected SoundEvent getInfusionSound(ItemStack stack, Player player) {
        return ModSounds.ROLL_CAST.get();
    }

    @Override
    protected boolean consumesItemOnUse() {
        return false;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!player.getAbilities().instabuild && getPuffsLeft(stack) <= 0) {
            if (!level.isClientSide()) {
                stack.shrink(1);
            }
            return InteractionResult.FAIL;
        }
        return super.use(level, player, hand);
    }

    @Override
    protected ItemStack afterInfusionUse(ItemStack stack, Level level, Player player) {
        if (player.getAbilities().instabuild) {
            return stack;
        }

        int remaining = getPuffsLeft(stack) - 1;
        if (remaining <= 0) {
            stack.shrink(1);
            player.displayClientMessage(Component.translatable("message.highlife.herb_roll_finished"), true);
            return stack;
        }

        setPuffsLeft(stack, remaining);
        return stack;
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        float ratio = getPuffsLeft(stack) / (float) MAX_PUFFS;
        return Math.max(1, Math.round(13.0F * ratio));
    }

    @Override
    public int getBarColor(ItemStack stack) {
        float ratio = getPuffsLeft(stack) / (float) MAX_PUFFS;
        int red = Mth.floor(255.0F * (1.0F - ratio));
        int green = Mth.floor(180.0F * ratio) + 40;
        return (red << 16) | (green << 8) | 0x2A;
    }

    public static int getPuffsLeft(ItemStack stack) {
        int value = getCustomInt(stack, TAG_PUFFS_LEFT, MAX_PUFFS);
        return Mth.clamp(value, 0, MAX_PUFFS);
    }

    public static int getMaxPuffs() {
        return MAX_PUFFS;
    }

    private static void setPuffsLeft(ItemStack stack, int value) {
        setCustomInt(stack, TAG_PUFFS_LEFT, Mth.clamp(value, 0, MAX_PUFFS), MAX_PUFFS);
    }
}
