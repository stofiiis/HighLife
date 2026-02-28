package com.stofiiis.highlife.registry;

import com.stofiiis.highlife.HighLifeMod;
import com.stofiiis.highlife.menu.AlchemyFlaskControlMenu;
import com.stofiiis.highlife.menu.InfusionWandControlMenu;
import com.stofiiis.highlife.menu.SeedMixerMenu;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModMenus {
    private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, HighLifeMod.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<InfusionWandControlMenu>> INFUSION_WAND_CONTROL = MENUS.register(
            "infusion_wand_control",
            () -> IMenuTypeExtension.create(InfusionWandControlMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<AlchemyFlaskControlMenu>> ALCHEMY_FLASK_CONTROL = MENUS.register(
            "alchemy_flask_control",
            () -> IMenuTypeExtension.create(AlchemyFlaskControlMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<SeedMixerMenu>> SEED_MIXER = MENUS.register(
            "seed_mixer",
            () -> IMenuTypeExtension.create(SeedMixerMenu::new));

    private ModMenus() {
    }

    public static void register(IEventBus modEventBus) {
        MENUS.register(modEventBus);
    }
}
