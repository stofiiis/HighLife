package com.stofiiis.highlife.item;

import com.stofiiis.highlife.menu.InfusionWandControlMenu;
import com.stofiiis.highlife.registry.ModSounds;
import com.stofiiis.highlife.util.AdvancementTracker;
import com.stofiiis.highlife.util.StrainData;

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

public class InfusionWandItem extends InfusionItem {
    private static final String TAG_LOADED = "infusion_wand_loaded";
    private static final String TAG_LOADED_STRAIN = "infusion_wand_loaded_strain";
    private static final String TAG_LOADED_QUALITY = "infusion_wand_loaded_quality";
    private static final int RELOAD_DELAY_TICKS = 12;

    public InfusionWandItem(Properties properties, float potency, int serenityDuration, int thirstDuration, int hazeDuration, int toleranceGain) {
        super(properties, potency, serenityDuration, thirstDuration, hazeDuration, toleranceGain);
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.NONE;
    }

    @Override
    protected SoundEvent getInfusionSound(ItemStack stack, Player player) {
        return ModSounds.WAND_CHARGE.get();
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
                        new SimpleMenuProvider((containerId, inventory, ignoredPlayer) -> new InfusionWandControlMenu(containerId, inventory, hand),
                                Component.translatable("screen.highlife.infusion_wand_control")),
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
                player.displayClientMessage(Component.translatable("screen.highlife.infusion_wand_cooldown", cooldown), true);
            } else {
                player.displayClientMessage(Component.translatable("message.highlife.infusion_wand_open_menu"), true);
            }
        }
        return InteractionResult.FAIL;
    }

    @Override
    protected ItemStack afterInfusionUse(ItemStack stack, Level level, Player player) {
        if (!player.getAbilities().instabuild) {
            setLoaded(stack, false);
            clearLoadedStrain(stack);
            player.getCooldowns().addCooldown(stack, RELOAD_DELAY_TICKS);
            player.displayClientMessage(Component.translatable("message.highlife.infusion_wand_empty"), true);
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
            tooltipAdder.accept(Component.translatable("tooltip.highlife.loaded_status", Component.translatable("hud.highlife.empty")));
            return;
        }

        tooltipAdder.accept(Component.translatable("tooltip.highlife.loaded_status", Component.translatable("hud.highlife.loaded")));
        getLoadedStrain(stack).ifPresent(data -> tooltipAdder.accept(Component.translatable("tooltip.highlife.loaded_strain", data.strainNameComponent())));
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
        player.displayClientMessage(Component.translatable("message.highlife.infusion_wand_loaded"), true);
        AdvancementTracker.onWandLoaded(player);
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
