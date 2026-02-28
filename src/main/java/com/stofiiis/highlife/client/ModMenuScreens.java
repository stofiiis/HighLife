package com.stofiiis.highlife.client;

import com.stofiiis.highlife.registry.ModMenus;

import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

public final class ModMenuScreens {
    private ModMenuScreens() {
    }

    public static void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.INFUSION_WAND_CONTROL.get(), InfusionWandControlScreen::new);
        event.register(ModMenus.ALCHEMY_FLASK_CONTROL.get(), AlchemyFlaskControlScreen::new);
        event.register(ModMenus.SEED_MIXER.get(), SeedMixerScreen::new);
    }
}
