package com.stofiiis.weed.block;

import javax.annotation.Nullable;

import com.mojang.serialization.MapCodec;
import com.stofiiis.weed.block.entity.SeedMixerBlockEntity;
import com.stofiiis.weed.menu.SeedMixerMenu;
import com.stofiiis.weed.registry.ModBlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class SeedMixerBlock extends BaseEntityBlock {
    public static final MapCodec<SeedMixerBlock> CODEC = simpleCodec(SeedMixerBlock::new);

    public SeedMixerBlock(Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<SeedMixerBlock> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SeedMixerBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return level.isClientSide() ? null : createTickerHelper(blockEntityType, ModBlockEntities.SEED_MIXER.get(), SeedMixerBlockEntity::tickServer);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        return this.openMixer(level, pos, player);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        return this.openMixer(level, pos, player);
    }

    private InteractionResult openMixer(Level level, BlockPos pos, Player player) {
        if (!(level.getBlockEntity(pos) instanceof SeedMixerBlockEntity mixerBlockEntity)) {
            return InteractionResult.PASS;
        }

        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        mixerBlockEntity.tryGiveReadyResult(player);
        player.openMenu(
                new SimpleMenuProvider(
                        (containerId, inventory, ignoredPlayer) -> new SeedMixerMenu(containerId, inventory, pos),
                        Component.translatable("screen.weed.seed_mixer")),
                data -> data.writeBlockPos(pos));
        return InteractionResult.SUCCESS;
    }
}
