package com.stofiiis.weed.item;

import java.util.function.Consumer;

import com.stofiiis.weed.registry.ModItems;
import com.stofiiis.weed.util.SeedCategoryData;
import com.stofiiis.weed.util.StrainData;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

public class SeedMixerItem extends Item {
    private static final int MIX_TIME_NORMAL_TICKS = 20 * 60;
    private static final int MIX_TIME_BONEMEAL_TICKS = 20 * 30;

    private static final String TAG_ACTIVE = "seed_mixer_active";
    private static final String TAG_FINISH_TICK = "seed_mixer_finish_tick";
    private static final String TAG_RESULT_STRAIN = "seed_mixer_result_strain";
    private static final String TAG_RESULT_CATEGORY = "seed_mixer_result_category";

    public SeedMixerItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack mixerStack = player.getItemInHand(hand);
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        if (isMixing(mixerStack)) {
            return this.handleActiveMix(level, player, mixerStack);
        }
        return this.tryStartMix(level, player, mixerStack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> tooltipAdder, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltipDisplay, tooltipAdder, flag);
        tooltipAdder.accept(Component.translatable("tooltip.weed.seed_mixer_usage"));
        tooltipAdder.accept(Component.translatable("tooltip.weed.seed_mixer_requirements"));
        if (isMixing(stack)) {
            tooltipAdder.accept(Component.translatable("tooltip.weed.seed_mixer_active"));
        }
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return isMixing(stack);
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return 13;
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return 0x63C27B;
    }

    private InteractionResult handleActiveMix(Level level, Player player, ItemStack mixerStack) {
        long finishTick = getCustomLong(mixerStack, TAG_FINISH_TICK, 0L);
        long now = level.getGameTime();
        if (now < finishTick) {
            int remainingSeconds = (int) Math.max(1L, (finishTick - now + 19L) / 20L);
            player.displayClientMessage(Component.translatable("message.weed.seed_mixer_progress", remainingSeconds), true);
            return InteractionResult.SUCCESS;
        }

        StrainData.Strain strain = StrainData.Strain.byName(getCustomString(mixerStack, TAG_RESULT_STRAIN, StrainData.Strain.OG_KUSH.getSerializedName()));
        SeedCategoryData.SeedCategory category = SeedCategoryData.SeedCategory.byName(
                getCustomString(mixerStack, TAG_RESULT_CATEGORY, SeedCategoryData.SeedCategory.COMMON.getSerializedName()));

        ItemStack resultSeed = new ItemStack(ModItems.CANNABIS_SEEDS.get());
        SeedCategoryData.set(resultSeed, category);
        StrainData.set(resultSeed, SeedCategoryData.stackableSeedData(category, strain));

        if (!player.addItem(resultSeed)) {
            player.drop(resultSeed, false);
        }
        clearMixState(mixerStack);

        level.playSound(null, player.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.85F, 1.05F);
        player.displayClientMessage(Component.translatable("message.weed.seed_mixer_ready"), true);
        return InteractionResult.SUCCESS;
    }

    private InteractionResult tryStartMix(Level level, Player player, ItemStack mixerStack) {
        Inventory inventory = player.getInventory();

        ParentSeed firstParent = null;
        ParentSeed secondParent = null;
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (!stack.is(ModItems.CANNABIS_SEEDS.get()) || stack.isEmpty()) {
                continue;
            }

            ParentSeed candidate = ParentSeed.fromStack(slot, stack, player);
            if (firstParent == null) {
                firstParent = candidate;
                if (stack.getCount() >= 2) {
                    secondParent = candidate;
                    break;
                }
            } else {
                secondParent = candidate;
                break;
            }
        }

        int dirtSlot = findSlot(inventory, Items.DIRT);
        int bonemealSlot = findSlot(inventory, Items.BONE_MEAL);

        if (firstParent == null || secondParent == null || dirtSlot < 0) {
            player.displayClientMessage(Component.translatable("message.weed.seed_mixer_need_items"), true);
            return InteractionResult.FAIL;
        }

        boolean hasBonemeal = bonemealSlot >= 0;
        int duration = hasBonemeal ? MIX_TIME_BONEMEAL_TICKS : MIX_TIME_NORMAL_TICKS;
        StrainData.Strain childStrain = SeedCategoryData.breedStrain(firstParent.strain, secondParent.strain, player.getRandom());
        SeedCategoryData.SeedCategory childCategory = SeedCategoryData.breedCategory(
                firstParent.category,
                secondParent.category,
                firstParent.strain == secondParent.strain,
                player.getRandom());

        if (!player.getAbilities().instabuild) {
            consumeFromSlot(inventory, firstParent.slot, 1);
            consumeFromSlot(inventory, secondParent.slot, 1);
            consumeFromSlot(inventory, dirtSlot, 1);
            if (hasBonemeal) {
                consumeFromSlot(inventory, bonemealSlot, 1);
            }
            inventory.setChanged();
        }

        setCustomBoolean(mixerStack, TAG_ACTIVE, true);
        setCustomLong(mixerStack, TAG_FINISH_TICK, level.getGameTime() + duration);
        setCustomString(mixerStack, TAG_RESULT_STRAIN, childStrain.getSerializedName());
        setCustomString(mixerStack, TAG_RESULT_CATEGORY, childCategory.getSerializedName());

        level.playSound(null, player.blockPosition(), SoundEvents.BREWING_STAND_BREW, SoundSource.PLAYERS, 0.75F, hasBonemeal ? 1.08F : 0.96F);
        player.displayClientMessage(Component.translatable("message.weed.seed_mixer_started", duration / 20), true);
        return InteractionResult.SUCCESS;
    }

    private static int findSlot(Inventory inventory, Item item) {
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.is(item) && !stack.isEmpty()) {
                return slot;
            }
        }
        return -1;
    }

    private static void consumeFromSlot(Inventory inventory, int slot, int count) {
        if (slot < 0 || slot >= inventory.getContainerSize()) {
            return;
        }
        ItemStack stack = inventory.getItem(slot);
        if (!stack.isEmpty()) {
            stack.shrink(count);
        }
    }

    private static boolean isMixing(ItemStack stack) {
        return getCustomBoolean(stack, TAG_ACTIVE, false);
    }

    private static void clearMixState(ItemStack stack) {
        setCustomBoolean(stack, TAG_ACTIVE, false);
        clearCustomValue(stack, TAG_FINISH_TICK);
        clearCustomValue(stack, TAG_RESULT_STRAIN);
        clearCustomValue(stack, TAG_RESULT_CATEGORY);
    }

    private static boolean getCustomBoolean(ItemStack stack, String key, boolean defaultValue) {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        return customData.copyTag().getBooleanOr(key, defaultValue);
    }

    private static void setCustomBoolean(ItemStack stack, String key, boolean value) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            if (value) {
                tag.putBoolean(key, true);
            } else {
                tag.remove(key);
            }
        });
    }

    private static long getCustomLong(ItemStack stack, String key, long defaultValue) {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        return customData.copyTag().getLongOr(key, defaultValue);
    }

    private static void setCustomLong(ItemStack stack, String key, long value) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putLong(key, value));
    }

    private static String getCustomString(ItemStack stack, String key, String defaultValue) {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        return customData.copyTag().getStringOr(key, defaultValue);
    }

    private static void setCustomString(ItemStack stack, String key, String value) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putString(key, value));
    }

    private static void clearCustomValue(ItemStack stack, String key) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.remove(key));
    }

    private record ParentSeed(int slot, SeedCategoryData.SeedCategory category, StrainData.Strain strain) {
        private static ParentSeed fromStack(int slot, ItemStack stack, Player player) {
            SeedCategoryData.SeedCategory category = SeedCategoryData.get(stack)
                    .orElseGet(() -> StrainData.get(stack)
                            .map(data -> SeedCategoryData.fromQuality(data.quality()))
                            .orElseGet(() -> SeedCategoryData.randomWildDrop(player.getRandom())));
            StrainData.Strain strain = StrainData.get(stack)
                    .map(StrainData::strain)
                    .orElseGet(() -> SeedCategoryData.randomStrain(player.getRandom()));
            return new ParentSeed(slot, category, strain);
        }
    }
}
