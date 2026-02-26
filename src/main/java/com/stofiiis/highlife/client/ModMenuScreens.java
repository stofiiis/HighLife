package com.stofiiis.highlife.client;

import com.stofiiis.highlife.registry.ModMenus;

import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

public final class ModMenuScreens {
    private ModMenuScreens() {
    }

    public static void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.PIPE_CONTROL.get(), PipeControlScreen::new);
        event.register(ModMenus.BONG_CONTROL.get(), BongControlScreen::new);
        event.register(ModMenus.SEED_MIXER.get(), SeedMixerScreen::new);
    }
}
