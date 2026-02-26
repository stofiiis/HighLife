package com.stofiiis.weed.client;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.neoforged.fml.common.asm.enumextension.EnumProxy;
import net.neoforged.neoforge.client.IArmPoseTransformer;

public final class ModArmPoses {
    public static final EnumProxy<HumanoidModel.ArmPose> JOINT_PUFF = new EnumProxy<>(
            HumanoidModel.ArmPose.class,
            false,
            true,
            (IArmPoseTransformer) (model, state, arm) -> applySmokePose(model, state, arm, 0.95F, 0.32F));

    public static final EnumProxy<HumanoidModel.ArmPose> PIPE_PUFF = new EnumProxy<>(
            HumanoidModel.ArmPose.class,
            false,
            true,
            (IArmPoseTransformer) (model, state, arm) -> applySmokePose(model, state, arm, 1.18F, 0.38F));

    public static final EnumProxy<HumanoidModel.ArmPose> BONG_PUFF = new EnumProxy<>(
            HumanoidModel.ArmPose.class,
            false,
            true,
            (IArmPoseTransformer) (model, state, arm) -> applySmokePose(model, state, arm, 1.35F, 0.46F));

    private ModArmPoses() {
    }

    private static void applySmokePose(HumanoidModel<?> model, HumanoidRenderState state, HumanoidArm arm, float pullStrength, float torsoLean) {
        ModelPart activeArm = model.getArm(arm);
        ModelPart offArm = model.getArm(arm.getOpposite());

        float useTicks = Mth.clamp(state.ticksUsingItem(arm), 0.0F, 999.0F);
        float inhale = Mth.clamp(useTicks / 8.0F, 0.0F, 1.0F);
        float age = state.ageInTicks;
        float pulse = Mth.sin(age * 0.45F) * 0.06F;
        float shoulderSway = Mth.sin(age * 0.20F) * 0.05F;
        float side = arm == HumanoidArm.RIGHT ? -1.0F : 1.0F;

        activeArm.xRot = -0.95F - inhale * (0.85F * pullStrength) + pulse;
        activeArm.yRot = side * (0.30F + inhale * 0.35F + shoulderSway);
        activeArm.zRot = side * (0.10F + inhale * 0.25F) + pulse * side;
        activeArm.x = side * (5.0F - inhale * 0.8F);
        activeArm.y = 2.2F + Mth.sin(age * 0.33F) * 0.12F;

        offArm.xRot = -0.35F - inhale * 0.12F + pulse * 0.5F;
        offArm.yRot = side * -0.22F;
        offArm.zRot = side * 0.18F;

        model.body.yRot = side * (torsoLean + inhale * 0.10F + shoulderSway * 0.45F);
        model.head.xRot += -0.05F * inhale;
        model.head.yRot += side * 0.04F * inhale;
    }
}
