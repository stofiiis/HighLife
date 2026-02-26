package com.stofiiis.weed.registry;

import com.stofiiis.weed.WeedMod;
import com.stofiiis.weed.item.BongItem;
import com.stofiiis.weed.item.CannabisBudItem;
import com.stofiiis.weed.item.CannabisSeedsItem;
import com.stofiiis.weed.item.DriedCannabisBudItem;
import com.stofiiis.weed.item.JointItem;
import com.stofiiis.weed.item.PipeItem;
import com.stofiiis.weed.item.SmokeItem;

import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(WeedMod.MODID);

    public static final DeferredItem<BlockItem> DRYING_RACK = ITEMS.registerSimpleBlockItem("drying_rack", ModBlocks.DRYING_RACK);

    public static final DeferredItem<CannabisSeedsItem> CANNABIS_SEEDS = ITEMS.registerItem("cannabis_seeds",
            properties -> new CannabisSeedsItem(ModBlocks.CANNABIS_CROP.get(), properties));

    public static final DeferredItem<CannabisBudItem> CANNABIS_BUD = ITEMS.registerItem("cannabis_bud", CannabisBudItem::new);
    public static final DeferredItem<DriedCannabisBudItem> DRIED_CANNABIS_BUD = ITEMS.registerItem("dried_cannabis_bud", DriedCannabisBudItem::new);
    public static final DeferredItem<Item> ROLLING_PAPER = ITEMS.registerSimpleItem("rolling_paper");

    public static final DeferredItem<JointItem> JOINT = ITEMS.registerItem("joint",
            properties -> new JointItem(properties.stacksTo(1), 1.0F, 7200, 2600, 1800, 6));

    public static final DeferredItem<PipeItem> PIPE = ITEMS.registerItem("pipe",
            properties -> new PipeItem(properties.stacksTo(1), 1.35F, 8600, 3200, 2600, 8));

    public static final DeferredItem<BongItem> BONG = ITEMS.registerItem("bong",
            properties -> new BongItem(properties.stacksTo(1).durability(41), 1.7F, 9800, 4200, 3400, 10));

    public static final DeferredItem<SmokeItem> WEED_BROWNIE = ITEMS.registerItem("weed_brownie",
            properties -> new SmokeItem(properties.stacksTo(64).food(new FoodProperties.Builder()
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
