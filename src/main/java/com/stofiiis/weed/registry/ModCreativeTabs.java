package com.stofiiis.weed.registry;

import com.stofiiis.weed.WeedMod;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModCreativeTabs {
    private static final DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, WeedMod.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> WEED_TAB = CREATIVE_TABS.register("weed_tab",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.weed"))
                    .withTabsBefore(CreativeModeTabs.FOOD_AND_DRINKS)
                    .icon(() -> ModItems.JOINT.get().getDefaultInstance())
                    .displayItems((params, output) -> {
                        output.accept(ModItems.CANNABIS_SEEDS.get());
                        output.accept(ModItems.CANNABIS_BUD.get());
                        output.accept(ModItems.DRIED_CANNABIS_BUD.get());
                        output.accept(ModItems.ROLLING_PAPER.get());
                        output.accept(ModItems.JOINT.get());
                        output.accept(ModItems.PIPE.get());
                        output.accept(ModItems.BONG.get());
                        output.accept(ModItems.WEED_BROWNIE.get());
                        output.accept(ModItems.DRYING_RACK.get());
                    })
                    .build());

    private ModCreativeTabs() {
    }

    public static void register(IEventBus modEventBus) {
        CREATIVE_TABS.register(modEventBus);
    }
}
