package com.stofiiis.highlife.menu;

import com.stofiiis.highlife.block.entity.SeedMixerBlockEntity;
import com.stofiiis.highlife.registry.ModBlocks;
import com.stofiiis.highlife.registry.ModItems;
import com.stofiiis.highlife.registry.ModMenus;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class SeedMixerMenu extends AbstractContainerMenu {
    private static final int SLOT_SEED_A = 0;
    private static final int SLOT_SEED_B = 1;
    private static final int SLOT_DIRT = 2;
    private static final int SLOT_BONEMEAL = 3;
    private static final int INV_START = 4;
    private static final int INV_END = 39;

    private final Level level;
    private final BlockPos blockPos;
    private final SeedMixerBlockEntity blockEntity;
    private final Container container;
    private final Player player;

    private int mixState;
    private int progress;
    private int progressMax;

    public SeedMixerMenu(int containerId, Inventory inventory, RegistryFriendlyByteBuf data) {
        this(containerId, inventory, data.readBlockPos());
    }

    public SeedMixerMenu(int containerId, Inventory inventory, BlockPos blockPos) {
        super(ModMenus.SEED_MIXER.get(), containerId);
        this.player = inventory.player;
        this.level = inventory.player.level();
        this.blockPos = blockPos;

        if (this.level.getBlockEntity(blockPos) instanceof SeedMixerBlockEntity mixerBlockEntity) {
            this.blockEntity = mixerBlockEntity;
            this.container = mixerBlockEntity;
            this.mixState = mixerBlockEntity.getMixState();
            this.progress = mixerBlockEntity.getProgress();
            this.progressMax = mixerBlockEntity.getProgressMax();
        } else {
            this.blockEntity = null;
            this.container = new SimpleContainer(4);
            this.mixState = SeedMixerBlockEntity.STATE_IDLE;
            this.progress = 0;
            this.progressMax = SeedMixerBlockEntity.MIX_TIME_NORMAL_TICKS;
        }

        this.addSlot(new Slot(this.container, SLOT_SEED_A, 35, 26) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(ModItems.MYSTIC_HERB_SEEDS.get());
            }
        });
        this.addSlot(new Slot(this.container, SLOT_SEED_B, 53, 26) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(ModItems.MYSTIC_HERB_SEEDS.get());
            }
        });
        this.addSlot(new Slot(this.container, SLOT_DIRT, 35, 52) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(Items.DIRT);
            }
        });
        this.addSlot(new Slot(this.container, SLOT_BONEMEAL, 53, 52) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(Items.BONE_MEAL);
            }
        });

        this.addPlayerInventorySlots(inventory);

        this.addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return SeedMixerMenu.this.blockEntity != null ? SeedMixerMenu.this.blockEntity.getMixState() : SeedMixerMenu.this.mixState;
            }

            @Override
            public void set(int value) {
                SeedMixerMenu.this.mixState = value;
            }
        });
        this.addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return SeedMixerMenu.this.blockEntity != null ? SeedMixerMenu.this.blockEntity.getProgress() : SeedMixerMenu.this.progress;
            }

            @Override
            public void set(int value) {
                SeedMixerMenu.this.progress = value;
            }
        });
        this.addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return SeedMixerMenu.this.blockEntity != null ? SeedMixerMenu.this.blockEntity.getProgressMax() : SeedMixerMenu.this.progressMax;
            }

            @Override
            public void set(int value) {
                SeedMixerMenu.this.progressMax = Math.max(1, value);
            }
        });
    }

    @Override
    public void broadcastChanges() {
        if (!this.level.isClientSide() && this.blockEntity != null && this.blockEntity.getMixState() == SeedMixerBlockEntity.STATE_READY) {
            this.blockEntity.tryGiveReadyResult(this.player);
        }
        super.broadcastChanges();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack sourceStack = slot.getItem();
        ItemStack copy = sourceStack.copy();

        if (index >= SLOT_SEED_A && index <= SLOT_BONEMEAL) {
            if (!this.moveItemStackTo(sourceStack, INV_START, INV_END + 1, true)) {
                return ItemStack.EMPTY;
            }
        } else if (sourceStack.is(ModItems.MYSTIC_HERB_SEEDS.get())) {
            if (!this.moveItemStackTo(sourceStack, SLOT_SEED_A, SLOT_DIRT, false)) {
                return ItemStack.EMPTY;
            }
        } else if (sourceStack.is(Items.DIRT)) {
            if (!this.moveItemStackTo(sourceStack, SLOT_DIRT, SLOT_DIRT + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (sourceStack.is(Items.BONE_MEAL)) {
            if (!this.moveItemStackTo(sourceStack, SLOT_BONEMEAL, SLOT_BONEMEAL + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (index >= INV_START && index < INV_START + 27) {
            if (!this.moveItemStackTo(sourceStack, INV_START + 27, INV_END + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (index >= INV_START + 27 && index <= INV_END) {
            if (!this.moveItemStackTo(sourceStack, INV_START, INV_START + 27, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            return ItemStack.EMPTY;
        }

        if (sourceStack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        return copy;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.blockEntity != null
                && Container.stillValidBlockEntity(this.blockEntity, player)
                && this.level.getBlockState(this.blockPos).is(ModBlocks.SEED_MIXER.get());
    }

    public int getMixState() {
        return this.mixState;
    }

    public boolean isMixing() {
        return this.mixState == SeedMixerBlockEntity.STATE_MIXING;
    }

    public boolean isReady() {
        return this.mixState == SeedMixerBlockEntity.STATE_READY;
    }

    public int getProgressScaled(int pixels) {
        if (this.progressMax <= 0 || this.progress <= 0) {
            return 0;
        }
        return Math.min(pixels, this.progress * pixels / this.progressMax);
    }

    public int getRemainingSeconds() {
        if (!this.isMixing()) {
            return 0;
        }
        return Math.max(1, (this.progressMax - this.progress + 19) / 20);
    }

    private void addPlayerInventorySlots(Inventory inventory) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                this.addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, 84 + row * 18));
            }
        }

        for (int column = 0; column < 9; column++) {
            this.addSlot(new Slot(inventory, column, 8 + column * 18, 142));
        }
    }
}
