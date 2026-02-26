package com.stofiiis.highlife;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import com.stofiiis.highlife.client.ModMenuScreens;
import com.stofiiis.highlife.config.HighLifeConfig;
import com.stofiiis.highlife.event.ModEvents;
import com.stofiiis.highlife.registry.ModBlockEntities;
import com.stofiiis.highlife.registry.ModBlocks;
import com.stofiiis.highlife.registry.ModCreativeTabs;
import com.stofiiis.highlife.registry.ModEffects;
import com.stofiiis.highlife.registry.ModItems;
import com.stofiiis.highlife.registry.ModMenus;
import com.stofiiis.highlife.registry.ModSounds;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;

@Mod(HighLifeMod.MODID)
public class HighLifeMod {
    public static final String MODID = "highlife";
    public static final Logger LOGGER = LogUtils.getLogger();

    public HighLifeMod(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, HighLifeConfig.SPEC);

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
