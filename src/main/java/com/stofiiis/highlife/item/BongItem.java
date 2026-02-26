package com.stofiiis.highlife.item;

import com.stofiiis.highlife.config.HighLifeConfig;
import com.stofiiis.highlife.menu.BongControlMenu;
import com.stofiiis.highlife.registry.ModSounds;
import com.stofiiis.highlife.util.AdvancementTracker;
import com.stofiiis.highlife.util.StrainData;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import java.util.function.Consumer;

public class BongItem extends SmokeItem {
    private static final String TAG_LOADED = "bong_loaded";
    private static final String TAG_LOADED_STRAIN = "bong_loaded_strain";
    private static final String TAG_LOADED_QUALITY = "bong_loaded_quality";

    public BongItem(Properties properties, float potency, int relaxedDuration, int cottonmouthDuration, int fogDuration, int toleranceGain) {
        super(properties, potency, relaxedDuration, cottonmouthDuration, fogDuration, toleranceGain);
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.NONE;
    }

    @Override
    protected SoundEvent getSmokeSound(ItemStack stack, Player player) {
        return ModSounds.BONG_PUFF.get();
    }

    @Override
    protected StrainData resolveStrainForUse(ItemStack stack, Player player) {
        return getLoadedStrain(stack).orElseGet(() -> super.resolveStrainForUse(stack, player));
    }

    @Override
    protected boolean consumesItemOnUse() {
        return false;
    }

    @Override
    protected ItemStack afterSmokeUse(ItemStack stack, Level level, Player player) {
        if (!player.getAbilities().instabuild) {
            setLoaded(stack, false);
            clearLoadedStrain(stack);
            int maxUsableDamage = getMaxUsableDamage(stack);
            int nextDamage = Math.min(maxUsableDamage, stack.getDamageValue() + 1);
            stack.setDamageValue(nextDamage);

            if (nextDamage >= maxUsableDamage) {
                player.displayClientMessage(Component.translatable("message.highlife.bong_empty"), true);
            }
        }
        return stack;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (player.isShiftKeyDown()) {
            if (!level.isClientSide()) {
                player.openMenu(
                        new SimpleMenuProvider((containerId, inventory, ignoredPlayer) -> new BongControlMenu(containerId, inventory, hand),
                                Component.translatable("screen.highlife.bong_control")),
                        data -> data.writeVarInt(hand == InteractionHand.OFF_HAND ? 1 : 0));
            }
            return InteractionResult.SUCCESS;
        }

        ItemStack stack = player.getItemInHand(hand);
        if (!player.getAbilities().instabuild && isEmpty(stack)) {
            if (!level.isClientSide()) {
                player.displayClientMessage(Component.translatable("message.highlife.bong_need_refill"), true);
            }
            return InteractionResult.FAIL;
        }

        if (!isLoaded(stack)) {
            if (!level.isClientSide()) {
                player.displayClientMessage(Component.translatable("message.highlife.bong_open_menu"), true);
            }
            return InteractionResult.FAIL;
        }

        return super.use(level, player, hand);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }

        BlockPos clickedPos = context.getClickedPos();
        BlockState clickedState = level.getBlockState(clickedPos);
        boolean isWaterSource = clickedState.getFluidState().is(Fluids.WATER) && clickedState.getFluidState().isSource();
        boolean isWaterCauldron = clickedState.is(Blocks.WATER_CAULDRON);
        if (!isWaterSource && !isWaterCauldron) {
            return super.useOn(context);
        }

        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        ItemStack stack = context.getItemInHand();
        if (stack.getDamageValue() == 0) {
            player.displayClientMessage(Component.translatable("message.highlife.bong_already_full"), true);
            return InteractionResult.SUCCESS;
        }

        stack.setDamageValue(0);

        if (isWaterCauldron && !player.getAbilities().instabuild) {
            LayeredCauldronBlock.lowerFillLevel(clickedState, level, clickedPos);
        }

        level.playSound(null, clickedPos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 0.8F, 1.0F);
        if (HighLifeConfig.isSmokeParticlesEnabled() && level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.SPLASH, clickedPos.getX() + 0.5D, clickedPos.getY() + 1.0D, clickedPos.getZ() + 0.5D, 8, 0.25D, 0.2D, 0.25D, 0.02D);
        }
        player.displayClientMessage(Component.translatable("message.highlife.bong_refilled"), true);
        AdvancementTracker.onBongRefilled(player);
        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        int maxUsableDamage = getMaxUsableDamage(stack);
        int used = Math.min(maxUsableDamage, stack.getDamageValue());
        float ratio = (maxUsableDamage - used) / (float) maxUsableDamage;
        return Math.max(1, Math.round(13.0F * ratio));
    }

    @Override
    public int getBarColor(ItemStack stack) {
        if (isEmpty(stack)) {
            return 0xFF2020;
        }
        return isLoaded(stack) ? 0x00FF88 : 0xFF9A00;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> tooltipAdder, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltipDisplay, tooltipAdder, flag);

        int maxUsableDamage = getMaxUsableDamage(stack);
        int waterLeft = maxUsableDamage - Math.min(maxUsableDamage, stack.getDamageValue());
        tooltipAdder.accept(Component.translatable("tooltip.highlife.bong_water", waterLeft, maxUsableDamage));

        Component loadedState = isLoaded(stack) ? Component.translatable("hud.highlife.loaded") : Component.translatable("hud.highlife.empty");
        tooltipAdder.accept(Component.translatable("tooltip.highlife.loaded_status", loadedState));

        getLoadedStrain(stack).ifPresent(data -> tooltipAdder.accept(Component.translatable("tooltip.highlife.loaded_strain", data.strainNameComponent())));
    }

    public static int getMaxUsableDamage(ItemStack stack) {
        return Math.max(1, stack.getMaxDamage() - 1);
    }

    public static boolean isEmpty(ItemStack stack) {
        return stack.getDamageValue() >= getMaxUsableDamage(stack);
    }

    public static boolean isLoaded(ItemStack stack) {
        return getCustomBoolean(stack, TAG_LOADED, false);
    }

    public static float getWaterRatio(ItemStack stack) {
        int maxUsableDamage = getMaxUsableDamage(stack);
        int used = Math.min(maxUsableDamage, stack.getDamageValue());
        return Mth.clamp((maxUsableDamage - used) / (float) maxUsableDamage, 0.0F, 1.0F);
    }

    public static java.util.Optional<StrainData> getLoadedStrain(ItemStack stack) {
        return StrainData.get(stack, TAG_LOADED_STRAIN, TAG_LOADED_QUALITY);
    }

    public static void setLoadedFromMenu(ItemStack stack, StrainData loadedStrain, Player player) {
        setLoaded(stack, true);
        setLoadedStrain(stack, loadedStrain);
        player.level().playSound(null, player.blockPosition(), SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.PLAYERS, 0.7F, 1.0F);
        player.displayClientMessage(Component.translatable("message.highlife.bong_loaded"), true);
        AdvancementTracker.onBongLoaded(player);
    }

    public static void setWaterFullFromMenu(ItemStack stack, Player player) {
        stack.setDamageValue(0);
        player.level().playSound(null, player.blockPosition(), SoundEvents.BOTTLE_FILL, SoundSource.PLAYERS, 0.85F, 1.0F);
        player.displayClientMessage(Component.translatable("message.highlife.bong_refilled"), true);
        AdvancementTracker.onBongRefilled(player);
    }

    private static void setLoaded(ItemStack stack, boolean loaded) {
        setCustomBoolean(stack, TAG_LOADED, loaded);
    }

    private static void setLoadedStrain(ItemStack stack, StrainData strainData) {
        StrainData.set(stack, TAG_LOADED_STRAIN, TAG_LOADED_QUALITY, strainData);
    }

    private static void clearLoadedStrain(ItemStack stack) {
        StrainData.clear(stack, TAG_LOADED_STRAIN, TAG_LOADED_QUALITY);
    }
}
