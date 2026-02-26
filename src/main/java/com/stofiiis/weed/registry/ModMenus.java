package com.stofiiis.weed.registry;

import com.stofiiis.weed.WeedMod;
import com.stofiiis.weed.menu.BongControlMenu;
import com.stofiiis.weed.menu.PipeControlMenu;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModMenus {
    private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, WeedMod.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<PipeControlMenu>> PIPE_CONTROL = MENUS.register(
            "pipe_control",
            () -> IMenuTypeExtension.create(PipeControlMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<BongControlMenu>> BONG_CONTROL = MENUS.register(
            "bong_control",
            () -> IMenuTypeExtension.create(BongControlMenu::new));

    private ModMenus() {
    }

    public static void register(IEventBus modEventBus) {
        MENUS.register(modEventBus);
    }
}
