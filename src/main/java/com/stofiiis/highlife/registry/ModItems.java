package com.stofiiis.highlife.registry;

import com.stofiiis.highlife.HighLifeMod;
import com.stofiiis.highlife.item.AlchemyFlaskItem;
import com.stofiiis.highlife.item.MysticHerbBundleItem;
import com.stofiiis.highlife.item.MysticHerbSeedsItem;
import com.stofiiis.highlife.item.DriedMysticHerbItem;
import com.stofiiis.highlife.item.HerbRollItem;
import com.stofiiis.highlife.item.InfusionWandItem;
import com.stofiiis.highlife.item.InfusionItem;

import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(HighLifeMod.MODID);

    public static final DeferredItem<BlockItem> DRYING_RACK = ITEMS.registerSimpleBlockItem("drying_rack", ModBlocks.DRYING_RACK);
    public static final DeferredItem<BlockItem> SEED_MIXER = ITEMS.registerSimpleBlockItem("seed_mixer", ModBlocks.SEED_MIXER);

    public static final DeferredItem<MysticHerbSeedsItem> MYSTIC_HERB_SEEDS = ITEMS.registerItem("mystic_herb_seeds",
            properties -> new MysticHerbSeedsItem(ModBlocks.MYSTIC_HERB_CROP.get(), properties));

    public static final DeferredItem<MysticHerbBundleItem> MYSTIC_HERB_BUNDLE = ITEMS.registerItem("mystic_herb_bundle", MysticHerbBundleItem::new);
    public static final DeferredItem<DriedMysticHerbItem> DRIED_MYSTIC_HERB = ITEMS.registerItem("dried_mystic_herb", DriedMysticHerbItem::new);
    public static final DeferredItem<Item> ROLLING_PAPER = ITEMS.registerSimpleItem("rolling_paper");
    public static final DeferredItem<HerbRollItem> HERB_ROLL = ITEMS.registerItem("herb_roll",
            properties -> new HerbRollItem(properties.stacksTo(1), 1.0F, 7200, 2600, 1800, 6));

    public static final DeferredItem<InfusionWandItem> INFUSION_WAND = ITEMS.registerItem("infusion_wand",
            properties -> new InfusionWandItem(properties.stacksTo(1), 1.35F, 8600, 3200, 2600, 8));

    public static final DeferredItem<AlchemyFlaskItem> ALCHEMY_FLASK = ITEMS.registerItem("alchemy_flask",
            properties -> new AlchemyFlaskItem(properties.stacksTo(1).durability(41), 1.7F, 9800, 4200, 3400, 10));

    public static final DeferredItem<InfusionItem> HERB_COOKIE = ITEMS.registerItem("herb_cookie",
            properties -> new InfusionItem(properties.stacksTo(64).food(new FoodProperties.Builder()
                    .alwaysEdible()
                    .nutrition(6)
                    .saturationModifier(0.6F)
                    .build()), 0.75F, 5200, 1600, 1200, 2, false, false));

    private ModItems() {
    }

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}
