package com.stofiiis.highlife.menu;

import com.stofiiis.highlife.item.AlchemyFlaskItem;
import com.stofiiis.highlife.registry.ModItems;
import com.stofiiis.highlife.registry.ModMenus;
import com.stofiiis.highlife.util.StrainData;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class AlchemyFlaskControlMenu extends AbstractContainerMenu {
    public static final int MODE_IDLE = 0;
    public static final int MODE_PACKING = 1;
    public static final int MODE_REFILLING = 2;

    private static final int SLOT_BUD = 0;
    private static final int SLOT_WATER = 1;
    private static final int INV_START = 2;
    private static final int INV_END = 37;
    private static final int PROCESS_TIME_TICKS = 64;

    private final Player player;
    private final InteractionHand hand;
    private final SimpleContainer inputContainer = new SimpleContainer(2);

    private int loadedState;
    private int waterPercent;
    private int processMode;
    private int progress;
    private int progressMax = PROCESS_TIME_TICKS;

    public AlchemyFlaskControlMenu(int containerId, Inventory inventory, RegistryFriendlyByteBuf data) {
        this(containerId, inventory, decodeHand(data));
    }

    public AlchemyFlaskControlMenu(int containerId, Inventory inventory, InteractionHand hand) {
        super(ModMenus.ALCHEMY_FLASK_CONTROL.get(), containerId);
        this.player = inventory.player;
        this.hand = hand;

        this.addSlot(new Slot(this.inputContainer, SLOT_BUD, 44, 26) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(ModItems.DRIED_MYSTIC_HERB.get());
            }
        });
        this.addSlot(new Slot(this.inputContainer, SLOT_WATER, 44, 52) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(Items.WATER_BUCKET);
            }
        });

        this.addPlayerInventorySlots(inventory);
        this.refreshStateFromItem();

        this.addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return AlchemyFlaskControlMenu.this.loadedState;
            }

            @Override
            public void set(int value) {
                AlchemyFlaskControlMenu.this.loadedState = value;
            }
        });
        this.addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return AlchemyFlaskControlMenu.this.waterPercent;
            }

            @Override
            public void set(int value) {
                AlchemyFlaskControlMenu.this.waterPercent = value;
            }
        });
        this.addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return AlchemyFlaskControlMenu.this.processMode;
            }

            @Override
            public void set(int value) {
                AlchemyFlaskControlMenu.this.processMode = value;
            }
        });
        this.addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return AlchemyFlaskControlMenu.this.progress;
            }

            @Override
            public void set(int value) {
                AlchemyFlaskControlMenu.this.progress = value;
            }
        });
        this.addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return AlchemyFlaskControlMenu.this.progressMax;
            }

            @Override
            public void set(int value) {
                AlchemyFlaskControlMenu.this.progressMax = Math.max(1, value);
            }
        });
    }

    @Override
    public void broadcastChanges() {
        if (!this.player.level().isClientSide()) {
            this.tickProcessing();
            this.refreshStateFromItem();
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

        if (index == SLOT_BUD || index == SLOT_WATER) {
            if (!this.moveItemStackTo(sourceStack, INV_START, INV_END + 1, true)) {
                return ItemStack.EMPTY;
            }
        } else if (sourceStack.is(ModItems.DRIED_MYSTIC_HERB.get())) {
            if (!this.moveItemStackTo(sourceStack, SLOT_BUD, SLOT_BUD + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (sourceStack.is(Items.WATER_BUCKET)) {
            if (!this.moveItemStackTo(sourceStack, SLOT_WATER, SLOT_WATER + 1, false)) {
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
        return player.isAlive() && this.getControlledStack().getItem() instanceof AlchemyFlaskItem;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (player.level().isClientSide()) {
            return;
        }

        for (int slot = 0; slot < this.inputContainer.getContainerSize(); slot++) {
            ItemStack remaining = this.inputContainer.removeItemNoUpdate(slot);
            if (!remaining.isEmpty()) {
                player.getInventory().placeItemBackInInventory(remaining);
            }
        }
    }

    public boolean isLoaded() {
        return this.loadedState > 0;
    }

    public int getWaterPercent() {
        return this.waterPercent;
    }

    public boolean isProcessing() {
        return this.processMode != MODE_IDLE;
    }

    public int getProcessMode() {
        return this.processMode;
    }

    public int getProgressScaled(int pixels) {
        if (this.progressMax <= 0 || this.progress <= 0) {
            return 0;
        }
        return Math.min(pixels, this.progress * pixels / this.progressMax);
    }

    private void tickProcessing() {
        this.progressMax = PROCESS_TIME_TICKS;

        if (this.processMode == MODE_PACKING && !this.canPackBowl()) {
            this.resetProcess();
        } else if (this.processMode == MODE_REFILLING && !this.canRefillWater()) {
            this.resetProcess();
        }

        if (this.processMode == MODE_IDLE) {
            if (this.canPackBowl()) {
                this.processMode = MODE_PACKING;
            } else if (this.canRefillWater()) {
                this.processMode = MODE_REFILLING;
            }
        }

        if (this.processMode == MODE_IDLE) {
            return;
        }

        this.progress++;
        if (this.progress < this.progressMax) {
            return;
        }

        if (this.processMode == MODE_PACKING) {
            this.finishPacking();
        } else if (this.processMode == MODE_REFILLING) {
            this.finishRefill();
        }
        this.resetProcess();
    }

    private boolean canPackBowl() {
        ItemStack stack = this.getControlledStack();
        if (!(stack.getItem() instanceof AlchemyFlaskItem) || AlchemyFlaskItem.isLoaded(stack)) {
            return false;
        }
        return this.inputContainer.getItem(SLOT_BUD).is(ModItems.DRIED_MYSTIC_HERB.get());
    }

    private boolean canRefillWater() {
        ItemStack stack = this.getControlledStack();
        if (!(stack.getItem() instanceof AlchemyFlaskItem) || AlchemyFlaskItem.getWaterRatio(stack) >= 0.999F) {
            return false;
        }
        return this.inputContainer.getItem(SLOT_WATER).is(Items.WATER_BUCKET);
    }

    private void finishPacking() {
        ItemStack stack = this.getControlledStack();
        ItemStack budStack = this.inputContainer.getItem(SLOT_BUD);
        if (budStack.isEmpty()) {
            return;
        }

        StrainData strainData = StrainData.getOrCreate(budStack, this.player.getRandom());
        if (!this.player.getAbilities().instabuild) {
            budStack.shrink(1);
            if (budStack.isEmpty()) {
                this.inputContainer.setItem(SLOT_BUD, ItemStack.EMPTY);
            }
        }
        AlchemyFlaskItem.setLoadedFromMenu(stack, strainData, this.player);
        this.inputContainer.setChanged();
    }

    private void finishRefill() {
        ItemStack stack = this.getControlledStack();
        ItemStack waterStack = this.inputContainer.getItem(SLOT_WATER);
        if (waterStack.isEmpty()) {
            return;
        }

        if (!this.player.getAbilities().instabuild) {
            waterStack.shrink(1);
            ItemStack emptyBucket = new ItemStack(Items.BUCKET);
            if (waterStack.isEmpty()) {
                this.inputContainer.setItem(SLOT_WATER, emptyBucket);
            } else if (!this.player.getInventory().add(emptyBucket)) {
                this.player.drop(emptyBucket, false);
            }
        }

        AlchemyFlaskItem.setWaterFullFromMenu(stack, this.player);
        this.inputContainer.setChanged();
    }

    private void refreshStateFromItem() {
        ItemStack stack = this.getControlledStack();
        this.loadedState = AlchemyFlaskItem.isLoaded(stack) ? 1 : 0;
        this.waterPercent = Math.round(AlchemyFlaskItem.getWaterRatio(stack) * 100.0F);
    }

    private void resetProcess() {
        this.processMode = MODE_IDLE;
        this.progress = 0;
    }

    private ItemStack getControlledStack() {
        return this.player.getItemInHand(this.hand);
    }

    private static InteractionHand decodeHand(RegistryFriendlyByteBuf data) {
        int ord = data.readVarInt();
        return ord == 1 ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
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
