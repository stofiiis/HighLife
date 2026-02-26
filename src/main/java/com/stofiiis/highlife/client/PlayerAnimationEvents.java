package com.stofiiis.highlife.client;

import com.mojang.math.Axis;
import com.stofiiis.highlife.HighLifeMod;
import com.stofiiis.highlife.registry.ModEffects;
import com.stofiiis.highlife.registry.ModItems;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;

@EventBusSubscriber(modid = HighLifeMod.MODID, value = Dist.CLIENT)
public final class PlayerAnimationEvents {
    private PlayerAnimationEvents() {
    }

    @SubscribeEvent
    public static void onRenderPlayerPre(RenderPlayerEvent.Pre<?> event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }

        AvatarRenderState state = event.getRenderState();
        Entity renderedEntity = minecraft.level.getEntity(state.id);
        if (!(renderedEntity instanceof Player player)) {
            return;
        }

        boolean peaceActive = player.hasEffect(ModEffects.PEACE);
        if (state.isUsingItem) {
            ItemStack usedStack = state.useItemHand == net.minecraft.world.InteractionHand.MAIN_HAND
                    ? state.getUseItemStackForArm(state.mainArm)
                    : state.getUseItemStackForArm(state.mainArm.getOpposite());
            if (isSmokeItem(usedStack)) {
                applySmokingAnimation(event, state);
                return;
            }
        }

        if (peaceActive) {
            applyPeaceAnimation(event, state);
        }
    }

    private static void applySmokingAnimation(RenderPlayerEvent.Pre<?> event, AvatarRenderState state) {
        float age = state.ageInTicks + event.getPartialTick();
        float useTicks = state.ticksUsingItem;
        float inhaleProgress = Mth.clamp(useTicks / 8.0F, 0.0F, 1.0F);
        float sway = Mth.sin(age * 0.35F) * (1.2F + inhaleProgress * 1.3F);
        float breathe = Mth.sin(age * 0.5F) * 0.012F;

        event.getPoseStack().translate(0.0D, breathe, 0.0D);
        event.getPoseStack().mulPose(Axis.YP.rotationDegrees(sway));
        event.getPoseStack().mulPose(Axis.ZP.rotationDegrees(sway * 0.35F));

        state.attackTime = 0.0F;
    }

    private static void applyPeaceAnimation(RenderPlayerEvent.Pre<?> event, AvatarRenderState state) {
        float age = state.ageInTicks + event.getPartialTick();
        float slowWave = Mth.sin(age * 0.16F);
        float sideWave = Mth.sin(age * 0.08F + 1.2F);
        float breathe = 0.015F * (0.5F + 0.5F * Mth.sin(age * 0.23F));

        event.getPoseStack().translate(0.0D, breathe, 0.0D);
        event.getPoseStack().mulPose(Axis.YP.rotationDegrees(slowWave * 2.0F));
        event.getPoseStack().mulPose(Axis.ZP.rotationDegrees(sideWave * 1.1F));

        // Keep arms out of attack posture while Peace is active.
        state.attackTime = 0.0F;
        state.rightArmPose = net.minecraft.client.model.HumanoidModel.ArmPose.EMPTY;
        state.leftArmPose = net.minecraft.client.model.HumanoidModel.ArmPose.EMPTY;
    }

    private static boolean isSmokeItem(ItemStack stack) {
        return stack.is(ModItems.JOINT.get()) || stack.is(ModItems.PIPE.get()) || stack.is(ModItems.BONG.get());
    }
}
