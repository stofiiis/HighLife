package com.stofiiis.highlife.item;

import java.util.function.Consumer;

import com.stofiiis.highlife.block.CannabisCropBlock;
import com.stofiiis.highlife.util.CropGeneticsData;
import com.stofiiis.highlife.util.SeedCategoryData;
import com.stofiiis.highlife.util.StrainData;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;

public class CannabisSeedsItem extends BlockItem {
    public CannabisSeedsItem(Block block, Item.Properties properties) {
        super(block, properties);
    }

    @Override
    public Component getName(ItemStack stack) {
        var strainData = StrainData.get(stack);
        if (strainData.isPresent()) {
            return Component.translatable("item.highlife.cannabis_seeds.strained", strainData.get().strainNameComponent());
        }
        return super.getName(stack);
    }

    @Override
    public InteractionResult place(BlockPlaceContext context) {
        StrainData seedData = null;
        if (context.getLevel() instanceof ServerLevel serverLevel) {
            ItemStack usedStack = context.getItemInHand();
            var seedCategory = SeedCategoryData.get(usedStack);
            var storedStrain = StrainData.get(usedStack).map(StrainData::strain);

            if (seedCategory.isPresent()) {
                StrainData.Strain strain = storedStrain.orElseGet(() -> SeedCategoryData.randomStrain(serverLevel.getRandom()));
                seedData = SeedCategoryData.rollPlantData(seedCategory.get(), strain, serverLevel.getRandom());
            } else {
                seedData = StrainData.get(usedStack).orElseGet(() -> StrainData.random(serverLevel.getRandom()));
            }
        }

        InteractionResult result = super.place(context);
        if (!result.consumesAction()) {
            return result;
        }

        if (context.getLevel() instanceof ServerLevel serverLevel
                && serverLevel.getBlockState(context.getClickedPos()).getBlock() instanceof CannabisCropBlock) {
            StrainData usedSeedData = seedData != null ? seedData : StrainData.random(serverLevel.getRandom());
            CropGeneticsData.get(serverLevel).put(context.getClickedPos(), usedSeedData.mutate(serverLevel.getRandom(), 0.04F));
        }

        return result;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> tooltipAdder, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltipDisplay, tooltipAdder, flag);

        StrainData.get(stack).ifPresent(data -> tooltipAdder.accept(Component.translatable("tooltip.highlife.strain", data.strainNameComponent())));

        var seedCategory = SeedCategoryData.get(stack);
        if (seedCategory.isPresent()) {
            seedCategory.ifPresent(category -> tooltipAdder.accept(Component.translatable("tooltip.highlife.seed_category", category.displayNameComponent())));
        }
    }
}
