package com.stofiiis.highlife.block.entity;

import com.stofiiis.highlife.block.DryingRackBlock;
import com.stofiiis.highlife.config.HighLifeConfig;
import com.stofiiis.highlife.registry.ModBlockEntities;
import com.stofiiis.highlife.registry.ModItems;
import com.stofiiis.highlife.util.StrainData;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class DryingRackBlockEntity extends BlockEntity {
    public static final int DEFAULT_DRY_TIME_TOTAL = 1200;

    private ItemStack storedItem = ItemStack.EMPTY;
    private int dryTime;

    public DryingRackBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(ModBlockEntities.DRYING_RACK.get(), blockPos, blockState);
    }

    public boolean isEmpty() {
        return this.storedItem.isEmpty();
    }

    public ItemStack getStoredItem() {
        return this.storedItem;
    }

    public boolean canInsert(ItemStack stack) {
        return this.storedItem.isEmpty() && stack.is(ModItems.MYSTIC_HERB_BUNDLE.get());
    }

    public boolean isDryingComplete() {
        return this.storedItem.is(ModItems.DRIED_MYSTIC_HERB.get());
    }

    public boolean isDrying() {
        return this.storedItem.is(ModItems.MYSTIC_HERB_BUNDLE.get());
    }

    public int getDryTimeTotal() {
        return HighLifeConfig.getDryTimeTicks();
    }

    public int getRemainingTicks() {
        if (!isDrying()) {
            return 0;
        }
        return Math.max(0, this.getDryTimeTotal() - this.dryTime);
    }

    public int getProgressPercent() {
        if (this.storedItem.isEmpty()) {
            return 0;
        }
        if (isDryingComplete()) {
            return 100;
        }
        int dryTimeTotal = this.getDryTimeTotal();
        return Math.min(99, Math.max(0, (int) ((this.dryTime * 100.0D) / dryTimeTotal)));
    }

    public void insertOne(ItemStack stack) {
        this.storedItem = stack.copyWithCount(1);
        this.dryTime = 0;
        this.markDirtyAndSync();
    }

    public ItemStack extract() {
        if (this.storedItem.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack extracted = this.storedItem.copy();
        this.storedItem = ItemStack.EMPTY;
        this.dryTime = 0;
        this.markDirtyAndSync();
        return extracted;
    }

    public static void tickServer(Level level, BlockPos pos, BlockState state, DryingRackBlockEntity blockEntity) {
        int dryTimeTotal = HighLifeConfig.getDryTimeTicks();

        if (blockEntity.storedItem.isEmpty()) {
            if (blockEntity.dryTime != 0) {
                blockEntity.dryTime = 0;
                blockEntity.markDirtyAndSync();
            }
            return;
        }

        if (!blockEntity.storedItem.is(ModItems.MYSTIC_HERB_BUNDLE.get())) {
            return;
        }

        blockEntity.dryTime++;
        if (blockEntity.dryTime >= dryTimeTotal) {
            ItemStack driedStack = new ItemStack(ModItems.DRIED_MYSTIC_HERB.get(), blockEntity.storedItem.getCount());
            StrainData source = StrainData.get(blockEntity.storedItem).orElseGet(() -> StrainData.random(level.getRandom()));
            StrainData.set(driedStack, source.withQualityOffset(0.04F));
            blockEntity.storedItem = driedStack;
            blockEntity.dryTime = 0;
            blockEntity.markDirtyAndSync();
            playCompletionFeedback(level, pos);
        } else if (blockEntity.dryTime % 40 == 0) {
            if (HighLifeConfig.isDryingRackParticlesEnabled() && level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.WHITE_SMOKE, pos.getX() + 0.5D, pos.getY() + 0.94D, pos.getZ() + 0.5D, 2, 0.12D, 0.04D, 0.12D, 0.003D);
            }
            blockEntity.setChanged();
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        this.syncOccupiedProperty(false);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("dry_time", this.dryTime);
        if (!this.storedItem.isEmpty()) {
            output.store("stored_item", ItemStack.CODEC, this.storedItem);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.dryTime = input.getIntOr("dry_time", 0);
        this.storedItem = input.read("stored_item", ItemStack.CODEC).orElse(ItemStack.EMPTY);
    }

    private void markDirtyAndSync() {
        this.setChanged();
        this.syncOccupiedProperty(true);
    }

    private static void playCompletionFeedback(Level level, BlockPos pos) {
        level.playSound(null, pos, SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, 0.75F, 1.0F);
        if (HighLifeConfig.isDryingRackParticlesEnabled() && level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, pos.getX() + 0.5D, pos.getY() + 1.05D, pos.getZ() + 0.5D, 10, 0.2D, 0.08D, 0.2D, 0.006D);
        }
    }

    private void syncOccupiedProperty(boolean notify) {
        if (this.level == null || this.level.isClientSide()) {
            return;
        }

        BlockState current = this.level.getBlockState(this.worldPosition);
        if (!current.hasProperty(DryingRackBlock.OCCUPIED)) {
            if (notify) {
                this.level.sendBlockUpdated(this.worldPosition, current, current, 3);
            }
            return;
        }

        BlockState updated = current.setValue(DryingRackBlock.OCCUPIED, !this.storedItem.isEmpty());
        if (!updated.equals(current)) {
            this.level.setBlock(this.worldPosition, updated, notify ? 3 : 2);
        } else if (notify) {
            this.level.sendBlockUpdated(this.worldPosition, current, current, 3);
        }
    }
}
