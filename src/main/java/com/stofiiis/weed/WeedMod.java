package com.stofiiis.weed;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import com.stofiiis.weed.client.ModMenuScreens;
import com.stofiiis.weed.config.WeedConfig;
import com.stofiiis.weed.event.ModEvents;
import com.stofiiis.weed.registry.ModBlockEntities;
import com.stofiiis.weed.registry.ModBlocks;
import com.stofiiis.weed.registry.ModCreativeTabs;
import com.stofiiis.weed.registry.ModEffects;
import com.stofiiis.weed.registry.ModItems;
import com.stofiiis.weed.registry.ModMenus;
import com.stofiiis.weed.registry.ModSounds;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;

@Mod(WeedMod.MODID)
public class WeedMod {
    public static final String MODID = "weed";
    public static final Logger LOGGER = LogUtils.getLogger();

    public WeedMod(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, WeedConfig.SPEC);

        ModEffects.register(modEventBus);
        ModSounds.register(modEventBus);
        ModMenus.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModItems.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModCreativeTabs.register(modEventBus);

        if (FMLEnvironment.getDist().isClient()) {
            modEventBus.addListener(ModMenuScreens::onRegisterMenuScreens);
        }

        NeoForge.EVENT_BUS.register(ModEvents.class);
    }
}
