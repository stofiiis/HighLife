package com.stofiiis.weed.client;

import com.stofiiis.weed.registry.ModMenus;

import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

public final class ModMenuScreens {
    private ModMenuScreens() {
    }

    public static void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.PIPE_CONTROL.get(), PipeControlScreen::new);
        event.register(ModMenus.BONG_CONTROL.get(), BongControlScreen::new);
    }
}
