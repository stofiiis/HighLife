package com.stofiiis.weed.item;

import com.stofiiis.weed.menu.PipeControlMenu;
import com.stofiiis.weed.registry.ModSounds;
import com.stofiiis.weed.util.AdvancementTracker;
import com.stofiiis.weed.util.StrainData;

import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import java.util.function.Consumer;

public class PipeItem extends SmokeItem {
    private static final String TAG_LOADED = "pipe_loaded";
    private static final String TAG_LOADED_STRAIN = "pipe_loaded_strain";
    private static final String TAG_LOADED_QUALITY = "pipe_loaded_quality";
    private static final int RELOAD_DELAY_TICKS = 12;

    public PipeItem(Properties properties, float potency, int relaxedDuration, int cottonmouthDuration, int fogDuration, int toleranceGain) {
        super(properties, potency, relaxedDuration, cottonmouthDuration, fogDuration, toleranceGain);
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.NONE;
    }

    @Override
    protected SoundEvent getSmokeSound(ItemStack stack, Player player) {
        return ModSounds.PIPE_PUFF.get();
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
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (player.isShiftKeyDown()) {
            if (!level.isClientSide()) {
                player.openMenu(
                        new SimpleMenuProvider((containerId, inventory, ignoredPlayer) -> new PipeControlMenu(containerId, inventory, hand),
                                Component.translatable("screen.weed.pipe_control")),
                        data -> data.writeVarInt(hand == InteractionHand.OFF_HAND ? 1 : 0));
            }
            return InteractionResult.SUCCESS;
        }

        ItemStack stack = player.getItemInHand(hand);
        if (isLoaded(stack)) {
            return super.use(level, player, hand);
        }

        if (!level.isClientSide()) {
            if (player.getCooldowns().isOnCooldown(stack)) {
                int cooldown = Math.round(player.getCooldowns().getCooldownPercent(stack, 0.0F) * 100.0F);
                player.displayClientMessage(Component.translatable("screen.weed.pipe_cooldown", cooldown), true);
            } else {
                player.displayClientMessage(Component.translatable("message.weed.pipe_open_menu"), true);
            }
        }
        return InteractionResult.FAIL;
    }

    @Override
    protected ItemStack afterSmokeUse(ItemStack stack, Level level, Player player) {
        if (!player.getAbilities().instabuild) {
            setLoaded(stack, false);
            clearLoadedStrain(stack);
            player.getCooldowns().addCooldown(stack, RELOAD_DELAY_TICKS);
            player.displayClientMessage(Component.translatable("message.weed.pipe_empty"), true);
        }
        return stack;
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return isLoaded(stack) ? 13 : 1;
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return isLoaded(stack) ? 0x49C76D : 0xB83838;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> tooltipAdder, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltipDisplay, tooltipAdder, flag);

        if (!isLoaded(stack)) {
            tooltipAdder.accept(Component.translatable("tooltip.weed.loaded_status", Component.translatable("hud.weed.empty")));
            return;
        }

        tooltipAdder.accept(Component.translatable("tooltip.weed.loaded_status", Component.translatable("hud.weed.loaded")));
        getLoadedStrain(stack).ifPresent(data -> tooltipAdder.accept(Component.translatable("tooltip.weed.loaded_strain", data.strainNameComponent())));
    }

    public static boolean isLoaded(ItemStack stack) {
        return getCustomBoolean(stack, TAG_LOADED, false);
    }

    public static float getReloadCooldownPercent(Player player, ItemStack stack, float partialTick) {
        return player.getCooldowns().getCooldownPercent(stack, partialTick);
    }

    public static java.util.Optional<StrainData> getLoadedStrain(ItemStack stack) {
        return StrainData.get(stack, TAG_LOADED_STRAIN, TAG_LOADED_QUALITY);
    }

    public static void setLoadedFromMenu(ItemStack stack, StrainData strainData, Player player) {
        setLoaded(stack, true);
        setLoadedStrain(stack, strainData);
        player.level().playSound(null, player.blockPosition(), SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.PLAYERS, 0.7F, 1.0F);
        player.displayClientMessage(Component.translatable("message.weed.pipe_loaded"), true);
        AdvancementTracker.onPipeLoaded(player);
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
