package com.stofiiis.weed.block.entity;

import com.stofiiis.weed.registry.ModBlockEntities;
import com.stofiiis.weed.registry.ModItems;
import com.stofiiis.weed.util.SeedCategoryData;
import com.stofiiis.weed.util.StrainData;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class SeedMixerBlockEntity extends BlockEntity implements Container {
    public static final int SLOT_SEED_A = 0;
    public static final int SLOT_SEED_B = 1;
    public static final int SLOT_DIRT = 2;
    public static final int SLOT_BONEMEAL = 3;

    public static final int STATE_IDLE = 0;
    public static final int STATE_MIXING = 1;
    public static final int STATE_READY = 2;

    public static final int MIX_TIME_NORMAL_TICKS = 20 * 60;
    public static final int MIX_TIME_BONEMEAL_TICKS = 20 * 30;

    private final NonNullList<ItemStack> items = NonNullList.withSize(4, ItemStack.EMPTY);
    private ItemStack readyResult = ItemStack.EMPTY;
    private ItemStack pendingResult = ItemStack.EMPTY;
    private int progress;
    private int progressMax;
    private boolean mixing;

    public SeedMixerBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(ModBlockEntities.SEED_MIXER.get(), blockPos, blockState);
    }

    public int getMixState() {
        if (!this.readyResult.isEmpty()) {
            return STATE_READY;
        }
        return this.mixing ? STATE_MIXING : STATE_IDLE;
    }

    public int getProgress() {
        return this.progress;
    }

    public int getProgressMax() {
        return this.progressMax <= 0 ? MIX_TIME_NORMAL_TICKS : this.progressMax;
    }

    public boolean tryGiveReadyResult(Player player) {
        if (this.readyResult.isEmpty()) {
            return false;
        }

        ItemStack extracted = this.readyResult.copy();
        this.readyResult = ItemStack.EMPTY;
        if (!player.addItem(extracted)) {
            player.drop(extracted, false);
        }

        this.markDirtyAndSync();
        player.level().playSound(null, this.worldPosition, SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.9F, 1.0F);
        player.displayClientMessage(Component.translatable("message.weed.seed_mixer_ready"), true);
        return true;
    }

    public void dropAllContents() {
        if (this.level == null || this.level.isClientSide()) {
            return;
        }

        for (ItemStack stack : this.items) {
            if (!stack.isEmpty()) {
                Block.popResource(this.level, this.worldPosition, stack.copy());
            }
        }
        if (!this.readyResult.isEmpty()) {
            Block.popResource(this.level, this.worldPosition, this.readyResult.copy());
        }
        if (!this.pendingResult.isEmpty()) {
            Block.popResource(this.level, this.worldPosition, this.pendingResult.copy());
        }
        this.clearContent();
        this.readyResult = ItemStack.EMPTY;
        this.pendingResult = ItemStack.EMPTY;
        this.progress = 0;
        this.progressMax = 0;
        this.mixing = false;
        this.markDirtyAndSync();
    }

    public static void tickServer(Level level, BlockPos pos, BlockState state, SeedMixerBlockEntity blockEntity) {
        if (blockEntity.mixing) {
            blockEntity.progress++;
            if (blockEntity.progress >= blockEntity.getProgressMax()) {
                blockEntity.progress = 0;
                blockEntity.progressMax = 0;
                blockEntity.mixing = false;
                blockEntity.readyResult = blockEntity.pendingResult.copy();
                blockEntity.pendingResult = ItemStack.EMPTY;
                blockEntity.markDirtyAndSync();
                level.playSound(null, pos, SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, 0.75F, 1.0F);
            } else if (blockEntity.progress % 20 == 0) {
                blockEntity.setChanged();
            }
            return;
        }

        if (!blockEntity.readyResult.isEmpty()) {
            return;
        }

        if (blockEntity.canStartMix()) {
            blockEntity.startMix(level);
        }
    }

    private boolean canStartMix() {
        return this.items.get(SLOT_SEED_A).is(ModItems.CANNABIS_SEEDS.get())
                && this.items.get(SLOT_SEED_B).is(ModItems.CANNABIS_SEEDS.get())
                && this.items.get(SLOT_DIRT).is(Items.DIRT);
    }

    private void startMix(Level level) {
        ItemStack seedA = this.items.get(SLOT_SEED_A);
        ItemStack seedB = this.items.get(SLOT_SEED_B);

        SeedCategoryData.SeedCategory categoryA = resolveSeedCategory(seedA, level);
        SeedCategoryData.SeedCategory categoryB = resolveSeedCategory(seedB, level);
        StrainData.Strain strainA = resolveSeedStrain(seedA, level);
        StrainData.Strain strainB = resolveSeedStrain(seedB, level);

        StrainData.Strain childStrain = SeedCategoryData.breedStrain(strainA, strainB, level.getRandom());
        SeedCategoryData.SeedCategory childCategory = SeedCategoryData.breedCategory(
                categoryA,
                categoryB,
                strainA == strainB,
                level.getRandom());

        ItemStack resultSeed = new ItemStack(ModItems.CANNABIS_SEEDS.get());
        SeedCategoryData.set(resultSeed, childCategory);
        StrainData.set(resultSeed, SeedCategoryData.stackableSeedData(childCategory, childStrain));
        this.pendingResult = resultSeed;

        this.items.get(SLOT_SEED_A).shrink(1);
        this.items.get(SLOT_SEED_B).shrink(1);
        this.items.get(SLOT_DIRT).shrink(1);

        boolean boosted = this.items.get(SLOT_BONEMEAL).is(Items.BONE_MEAL);
        if (boosted) {
            this.items.get(SLOT_BONEMEAL).shrink(1);
        }

        this.progress = 0;
        this.progressMax = boosted ? MIX_TIME_BONEMEAL_TICKS : MIX_TIME_NORMAL_TICKS;
        this.mixing = true;
        this.markDirtyAndSync();
        level.playSound(null, this.worldPosition, SoundEvents.GRINDSTONE_USE, SoundSource.BLOCKS, 0.55F, boosted ? 1.05F : 0.95F);
    }

    private static SeedCategoryData.SeedCategory resolveSeedCategory(ItemStack seedStack, Level level) {
        return SeedCategoryData.get(seedStack)
                .orElseGet(() -> StrainData.get(seedStack)
                        .map(data -> SeedCategoryData.fromQuality(data.quality()))
                        .orElseGet(() -> SeedCategoryData.randomWildDrop(level.getRandom())));
    }

    private static StrainData.Strain resolveSeedStrain(ItemStack seedStack, Level level) {
        return StrainData.get(seedStack)
                .map(StrainData::strain)
                .orElseGet(() -> SeedCategoryData.randomStrain(level.getRandom()));
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, this.items);
        output.putBoolean("mixing", this.mixing);
        output.putInt("progress", this.progress);
        output.putInt("progress_max", this.progressMax);
        if (!this.readyResult.isEmpty()) {
            output.store("ready_result", ItemStack.CODEC, this.readyResult);
        }
        if (!this.pendingResult.isEmpty()) {
            output.store("pending_result", ItemStack.CODEC, this.pendingResult);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.clearContent();
        ContainerHelper.loadAllItems(input, this.items);
        this.mixing = input.getBooleanOr("mixing", false);
        this.progress = input.getIntOr("progress", 0);
        this.progressMax = input.getIntOr("progress_max", 0);
        this.readyResult = input.read("ready_result", ItemStack.CODEC).orElse(ItemStack.EMPTY);
        this.pendingResult = input.read("pending_result", ItemStack.CODEC).orElse(ItemStack.EMPTY);
    }

    @Override
    public int getContainerSize() {
        return this.items.size();
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : this.items) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return this.readyResult.isEmpty() && this.pendingResult.isEmpty();
    }

    @Override
    public ItemStack getItem(int slot) {
        return this.items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack removed = ContainerHelper.removeItem(this.items, slot, amount);
        if (!removed.isEmpty()) {
            this.markDirtyAndSync();
        }
        return removed;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        ItemStack removed = ContainerHelper.takeItem(this.items, slot);
        if (!removed.isEmpty()) {
            this.markDirtyAndSync();
        }
        return removed;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        this.items.set(slot, stack);
        if (stack.getCount() > this.getMaxStackSize(stack)) {
            stack.setCount(this.getMaxStackSize(stack));
        }
        this.markDirtyAndSync();
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return switch (slot) {
            case SLOT_SEED_A, SLOT_SEED_B -> stack.is(ModItems.CANNABIS_SEEDS.get());
            case SLOT_DIRT -> stack.is(Items.DIRT);
            case SLOT_BONEMEAL -> stack.is(Items.BONE_MEAL);
            default -> false;
        };
    }

    @Override
    public void clearContent() {
        for (int i = 0; i < this.items.size(); i++) {
            this.items.set(i, ItemStack.EMPTY);
        }
    }

    @Override
    public void setChanged() {
        super.setChanged();
    }

    private void markDirtyAndSync() {
        this.setChanged();
        if (this.level != null && !this.level.isClientSide()) {
            BlockState state = this.level.getBlockState(this.worldPosition);
            this.level.sendBlockUpdated(this.worldPosition, state, state, 3);
        }
    }
}
