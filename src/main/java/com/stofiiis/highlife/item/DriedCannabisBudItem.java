package com.stofiiis.highlife.item;

import java.util.function.Consumer;

import com.stofiiis.highlife.util.StrainData;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

public class DriedCannabisBudItem extends Item {
    public DriedCannabisBudItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> tooltipAdder, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltipDisplay, tooltipAdder, flag);

        StrainData.get(stack).ifPresent(data -> {
            tooltipAdder.accept(Component.translatable("tooltip.highlife.strain", data.strainNameComponent()));
            tooltipAdder.accept(Component.translatable("tooltip.highlife.quality", data.qualityNameComponent()));
            tooltipAdder.accept(Component.translatable("tooltip.highlife.potency", data.potencyPercent()));
        });
    }
}
