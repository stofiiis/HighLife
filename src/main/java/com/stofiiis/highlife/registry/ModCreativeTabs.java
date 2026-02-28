package com.stofiiis.highlife.registry;

import com.stofiiis.highlife.HighLifeMod;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModCreativeTabs {
    private static final DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, HighLifeMod.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> HIGHLIFE_TAB = CREATIVE_TABS.register("highlife_tab",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.highlife"))
                    .withTabsBefore(CreativeModeTabs.FOOD_AND_DRINKS)
                    .icon(() -> ModItems.HERB_ROLL.get().getDefaultInstance())
                    .displayItems((params, output) -> {
                        output.accept(ModItems.MYSTIC_HERB_SEEDS.get());
                        output.accept(ModItems.MYSTIC_HERB_BUNDLE.get());
                        output.accept(ModItems.DRIED_MYSTIC_HERB.get());
                        output.accept(ModItems.SEED_MIXER.get());
                        output.accept(ModItems.ROLLING_PAPER.get());
                        output.accept(ModItems.HERB_ROLL.get());
                        output.accept(ModItems.INFUSION_WAND.get());
                        output.accept(ModItems.ALCHEMY_FLASK.get());
                        output.accept(ModItems.HERB_COOKIE.get());
                        output.accept(ModItems.DRYING_RACK.get());
                    })
                    .build());

    private ModCreativeTabs() {
    }

    public static void register(IEventBus modEventBus) {
        CREATIVE_TABS.register(modEventBus);
    }
}
