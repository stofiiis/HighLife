package com.stofiiis.highlife.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.stofiiis.highlife.block.entity.DryingRackBlockEntity;
import com.stofiiis.highlife.item.AlchemyFlaskItem;
import com.stofiiis.highlife.item.InfusionWandItem;
import com.stofiiis.highlife.registry.ModEffects;
import com.stofiiis.highlife.registry.ModItems;
import com.stofiiis.highlife.util.StrainData;
import com.stofiiis.highlife.util.ToleranceData;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;

public final class HighLifeDebugCommand {
    private HighLifeDebugCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("highlife")
                .then(Commands.literal("debug")
                        .executes(context -> debugPlayer(context.getSource()))
                        .then(Commands.literal("rack")
                                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                        .executes(context -> debugRack(context.getSource(), BlockPosArgument.getLoadedBlockPos(context, "pos")))))));
    }

    private static int debugPlayer(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        int tolerance = ToleranceData.get(player);

        MobEffectInstance serenity = player.getEffect(ModEffects.RELAXED);
        MobEffectInstance thirst = player.getEffect(ModEffects.COTTONMOUTH);
        MobEffectInstance haze = player.getEffect(ModEffects.FOG);
        MobEffectInstance peace = player.getEffect(ModEffects.PEACE);

        ItemStack mainHand = player.getMainHandItem();
        final String alchemy_flaskInfo;
        if (mainHand.is(ModItems.ALCHEMY_FLASK.get())) {
            int maxCharge = Math.max(1, mainHand.getMaxDamage() - 1);
            int used = Math.min(maxCharge, mainHand.getDamageValue());
            int left = maxCharge - used;
            String loadedStrain = AlchemyFlaskItem.getLoadedStrain(mainHand).map(data -> data.strain().getSerializedName()).orElse("none");
            alchemy_flaskInfo = " | alchemy_flask_water=" + left + "/" + maxCharge + " | alchemy_flask_loaded_strain=" + loadedStrain;
        } else if (mainHand.is(ModItems.INFUSION_WAND.get())) {
            String loadedStrain = InfusionWandItem.getLoadedStrain(mainHand).map(data -> data.strain().getSerializedName()).orElse("none");
            alchemy_flaskInfo = " | infusion_wand_loaded=" + InfusionWandItem.isLoaded(mainHand) + " | infusion_wand_loaded_strain=" + loadedStrain;
        } else {
            alchemy_flaskInfo = "";
        }

        String heldStrain = StrainData.get(mainHand)
                .map(data -> data.strain().getSerializedName() + "@" + String.format("%.2f", data.quality()))
                .orElse("none");

        source.sendSuccess(
                () -> Component.literal("highlife debug | tolerance=" + tolerance
                        + " | serenity=" + formatEffect(serenity)
                        + " | thirst=" + formatEffect(thirst)
                        + " | haze=" + formatEffect(haze)
                        + " | peace=" + formatEffect(peace)
                        + " | held_strain=" + heldStrain
                        + alchemy_flaskInfo),
                false);
        return 1;
    }

    private static int debugRack(CommandSourceStack source, BlockPos pos) {
        if (!(source.getLevel().getBlockEntity(pos) instanceof DryingRackBlockEntity rack)) {
            source.sendFailure(Component.literal("No drying rack at " + pos.getX() + " " + pos.getY() + " " + pos.getZ()));
            return 0;
        }

        ItemStack stored = rack.getStoredItem();
        String storedText = stored.isEmpty() ? "empty" : stored.getHoverName().getString() + " x" + stored.getCount();
        int remainingSeconds = Math.max(0, rack.getRemainingTicks() / 20);

        source.sendSuccess(
                () -> Component.literal("rack " + pos.getX() + " " + pos.getY() + " " + pos.getZ()
                        + " | item=" + storedText
                        + " | progress=" + rack.getProgressPercent() + "%"
                        + " | remaining=" + remainingSeconds + "s"),
                false);
        return 1;
    }

    private static String formatEffect(MobEffectInstance effect) {
        if (effect == null) {
            return "none";
        }
        return effect.getDuration() + "t@" + effect.getAmplifier();
    }
}
