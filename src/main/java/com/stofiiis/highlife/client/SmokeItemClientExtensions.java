package com.stofiiis.highlife.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.stofiiis.highlife.HighLifeMod;
import com.stofiiis.highlife.registry.ModItems;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.fml.common.asm.enumextension.EnumProxy;

@EventBusSubscriber(modid = HighLifeMod.MODID, value = Dist.CLIENT)
public final class SmokeItemClientExtensions {
    private SmokeItemClientExtensions() {
    }

    @SubscribeEvent
    public static void onRegisterClientExtensions(RegisterClientExtensionsEvent event) {
        event.registerItem(new SmokingExtension(ModArmPoses.JOINT_PUFF, 1.0F, 0.95F), ModItems.JOINT.get());
        event.registerItem(new SmokingExtension(ModArmPoses.PIPE_PUFF, 1.10F, 1.10F), ModItems.PIPE.get());
        event.registerItem(new SmokingExtension(ModArmPoses.BONG_PUFF, 1.22F, 1.20F), ModItems.BONG.get());
    }

    private static final class SmokingExtension implements IClientItemExtensions {
        private final EnumProxy<HumanoidModel.ArmPose> poseProxy;
        private final float firstPersonPull;
        private final float firstPersonTwist;

        private SmokingExtension(EnumProxy<HumanoidModel.ArmPose> poseProxy, float firstPersonPull, float firstPersonTwist) {
            this.poseProxy = poseProxy;
            this.firstPersonPull = firstPersonPull;
            this.firstPersonTwist = firstPersonTwist;
        }

        @Override
        public HumanoidModel.ArmPose getArmPose(LivingEntity entityLiving, InteractionHand hand, ItemStack itemStack) {
            if (entityLiving.getUsedItemHand() == hand && entityLiving.getUseItemRemainingTicks() > 0) {
                return this.poseProxy.getValue();
            }
            return null;
        }

        @Override
        public boolean applyForgeHandTransform(
                PoseStack poseStack,
                LocalPlayer player,
                HumanoidArm arm,
                ItemStack itemInHand,
                float partialTick,
                float equipProcess,
                float swingProcess) {
            if (!player.isUsingItem()) {
                return false;
            }

            InteractionHand usedHand = player.getUsedItemHand();
            HumanoidArm usedArm = usedHand == InteractionHand.MAIN_HAND ? player.getMainArm() : player.getMainArm().getOpposite();
            if (usedArm != arm || player.getItemInHand(usedHand) != itemInHand) {
                return false;
            }

            float totalUseTicks = itemInHand.getUseDuration(player) - (player.getUseItemRemainingTicks() - partialTick);
            float inhale = Mth.clamp(totalUseTicks / 8.0F, 0.0F, 1.0F);
            float flutter = Mth.sin((player.tickCount + partialTick) * 0.55F) * 1.5F;
            float breathe = Mth.sin((player.tickCount + partialTick) * 0.18F) * 0.015F;
            float side = arm == HumanoidArm.RIGHT ? 1.0F : -1.0F;

            poseStack.translate(side * (0.08F - 0.18F * inhale * this.firstPersonPull), -0.16F + 0.09F * inhale + breathe, -0.26F - 0.34F * inhale);
            poseStack.mulPose(Axis.YP.rotationDegrees(side * (12.0F + inhale * 36.0F * this.firstPersonTwist + flutter)));
            poseStack.mulPose(Axis.XP.rotationDegrees(-20.0F - inhale * 44.0F * this.firstPersonPull));
            poseStack.mulPose(Axis.ZP.rotationDegrees(side * (7.0F + inhale * 14.0F)));
            return true;
        }
    }
}
