package com.stofiiis.weed.event;

import com.stofiiis.weed.block.entity.DryingRackBlockEntity;
import com.stofiiis.weed.command.WeedDebugCommand;
import com.stofiiis.weed.config.WeedConfig;
import com.stofiiis.weed.registry.ModBlocks;
import com.stofiiis.weed.registry.ModEffects;
import com.stofiiis.weed.registry.ModItems;
import com.stofiiis.weed.util.CropGeneticsData;
import com.stofiiis.weed.util.SeedCategoryData;
import com.stofiiis.weed.util.StrainData;
import com.stofiiis.weed.util.ToleranceData;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

public final class ModEvents {
    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        WeedDebugCommand.register(event.getDispatcher());
    }

    @SubscribeEvent
    public static void onGrassBroken(BlockEvent.BreakEvent event) {
        if (!(event.getLevel() instanceof Level level) || level.isClientSide()) {
            return;
        }

        if (event.getState().is(ModBlocks.DRYING_RACK.get())
                && level.getBlockEntity(event.getPos()) instanceof DryingRackBlockEntity dryingRackBlockEntity
                && !dryingRackBlockEntity.isEmpty()) {
            Block.popResource(level, event.getPos(), dryingRackBlockEntity.extract());
        }

        Player player = event.getPlayer();
        if (player != null && player.isCreative()) {
            return;
        }

        Block block = event.getState().getBlock();
        boolean isGrassLike = block == Blocks.SHORT_GRASS || block == Blocks.TALL_GRASS || block == Blocks.FERN || block == Blocks.LARGE_FERN;
        if (isGrassLike && WeedConfig.areSeedDropsEnabled() && level.getRandom().nextFloat() < WeedConfig.getSeedDropChance()) {
            ItemStack seedDrop = new ItemStack(ModItems.CANNABIS_SEEDS.get());
            SeedCategoryData.set(seedDrop, SeedCategoryData.randomWildDrop(level.getRandom()));
            Block.popResource(level, event.getPos(), seedDrop);
        }
    }

    @SubscribeEvent
    public static void onCropDrops(BlockDropsEvent event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel) || !event.getState().is(ModBlocks.CANNABIS_CROP.get())) {
            return;
        }

        StrainData source = CropGeneticsData.get(serverLevel)
                .remove(event.getPos())
                .orElseGet(() -> StrainData.random(serverLevel.getRandom()));

        for (ItemEntity drop : event.getDrops()) {
            ItemStack droppedStack = drop.getItem();
            if (droppedStack.is(ModItems.CANNABIS_BUD.get())) {
                StrainData budData = source.mutate(serverLevel.getRandom(), 0.08F).withQualityOffset(0.02F);
                StrainData.set(droppedStack, budData);
            } else if (droppedStack.is(ModItems.CANNABIS_SEEDS.get())) {
                StrainData seedData = source.mutate(serverLevel.getRandom(), 0.16F);
                SeedCategoryData.set(droppedStack, SeedCategoryData.fromQuality(seedData.quality()));
                StrainData.clear(droppedStack);
            }
        }
    }

    @SubscribeEvent
    public static void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
        ItemStack crafted = event.getCrafting();
        if (!crafted.is(ModItems.JOINT.get())) {
            return;
        }

        StrainData fromBud = null;
        for (int slot = 0; slot < event.getInventory().getContainerSize(); slot++) {
            ItemStack ingredient = event.getInventory().getItem(slot);
            if (ingredient.is(ModItems.DRIED_CANNABIS_BUD.get())) {
                fromBud = StrainData.getOrCreate(ingredient, event.getEntity().getRandom());
                break;
            }
        }

        if (fromBud != null) {
            StrainData.set(crafted, fromBud.mutate(event.getEntity().getRandom(), 0.03F));
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) {
            return;
        }

        int intervalTicks = WeedConfig.getToleranceDecayIntervalTicks();
        if (player.tickCount % intervalTicks == 0) {
            int decayAmount = WeedConfig.getToleranceDecayAmount();
            if (decayAmount > 0) {
                ToleranceData.decay(player, decayAmount);
            }
        }

        if (player.hasEffect(ModEffects.PEACE) && player.tickCount % 20 == 0) {
            for (Mob mob : player.level().getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(32.0D), mob -> mob.getTarget() == player)) {
                mob.setTarget(null);
            }
        }

        if (player.tickCount % 80 == 0) {
            ensureInventoryStrains(player);
        }
    }

    @SubscribeEvent
    public static void onMobChangeTarget(LivingChangeTargetEvent event) {
        if (!(event.getEntity() instanceof Mob)) {
            return;
        }

        if (event.getNewAboutToBeSetTarget() instanceof Player player && player.hasEffect(ModEffects.PEACE)) {
            event.setNewAboutToBeSetTarget(null);
        }
    }

    @SubscribeEvent
    public static void onAttackEntity(AttackEntityEvent event) {
        Player player = event.getEntity();
        if (player.hasEffect(ModEffects.PEACE)) {
            event.setCanceled(true);
            if (!player.level().isClientSide()) {
                player.displayClientMessage(Component.translatable("message.weed.peace_no_attack"), true);
            }
        }
    }

    @SubscribeEvent
    public static void onLivingIncomingDamage(LivingIncomingDamageEvent event) {
        Entity attacker = event.getSource().getEntity();
        if (attacker instanceof Player player && player.hasEffect(ModEffects.PEACE)) {
            event.setCanceled(true);
        }
    }

    private static void ensureInventoryStrains(Player player) {
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (stack.isEmpty()) {
                continue;
            }

            if (stack.is(ModItems.CANNABIS_SEEDS.get())) {
                migrateSeedToCategory(stack, player);
                continue;
            }

            if (!isStrainTrackedItem(stack.getItem()) || StrainData.get(stack).isPresent()) {
                continue;
            }
            StrainData.set(stack, StrainData.random(player.getRandom()));
        }
    }

    private static void migrateSeedToCategory(ItemStack seedStack, Player player) {
        if (SeedCategoryData.get(seedStack).isPresent()) {
            if (StrainData.get(seedStack).isPresent()) {
                StrainData.clear(seedStack);
            }
            return;
        }

        StrainData.get(seedStack).ifPresentOrElse(data -> {
            SeedCategoryData.set(seedStack, SeedCategoryData.fromQuality(data.quality()));
            StrainData.clear(seedStack);
        }, () -> SeedCategoryData.set(seedStack, SeedCategoryData.randomWildDrop(player.getRandom())));
    }

    private static boolean isStrainTrackedItem(Item item) {
        return item == ModItems.CANNABIS_BUD.get()
                || item == ModItems.DRIED_CANNABIS_BUD.get()
                || item == ModItems.JOINT.get();
    }
}
