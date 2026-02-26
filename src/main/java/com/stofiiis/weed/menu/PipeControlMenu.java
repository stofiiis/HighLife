package com.stofiiis.weed.menu;

import com.stofiiis.weed.item.PipeItem;
import com.stofiiis.weed.registry.ModItems;
import com.stofiiis.weed.registry.ModMenus;
import com.stofiiis.weed.util.StrainData;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class PipeControlMenu extends AbstractContainerMenu {
    private static final int SLOT_INPUT = 0;
    private static final int INV_START = 1;
    private static final int INV_END = 36;
    private static final int LOAD_TIME_TICKS = 48;

    private final Player player;
    private final InteractionHand hand;
    private final SimpleContainer inputContainer = new SimpleContainer(1);

    private int loadedState;
    private int cooldownPercent;
    private int progress;
    private int progressMax = LOAD_TIME_TICKS;

    public PipeControlMenu(int containerId, Inventory inventory, RegistryFriendlyByteBuf data) {
        this(containerId, inventory, decodeHand(data));
    }

    public PipeControlMenu(int containerId, Inventory inventory, InteractionHand hand) {
        super(ModMenus.PIPE_CONTROL.get(), containerId);
        this.player = inventory.player;
        this.hand = hand;

        this.addSlot(new Slot(this.inputContainer, SLOT_INPUT, 64, 35) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(ModItems.DRIED_CANNABIS_BUD.get());
            }
        });

        this.addPlayerInventorySlots(inventory);
        this.refreshStateFromItem();

        this.addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return PipeControlMenu.this.loadedState;
            }

            @Override
            public void set(int value) {
                PipeControlMenu.this.loadedState = value;
            }
        });
        this.addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return PipeControlMenu.this.cooldownPercent;
            }

            @Override
            public void set(int value) {
                PipeControlMenu.this.cooldownPercent = value;
            }
        });
        this.addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return PipeControlMenu.this.progress;
            }

            @Override
            public void set(int value) {
                PipeControlMenu.this.progress = value;
            }
        });
        this.addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return PipeControlMenu.this.progressMax;
            }

            @Override
            public void set(int value) {
                PipeControlMenu.this.progressMax = Math.max(1, value);
            }
        });
    }

    @Override
    public void broadcastChanges() {
        if (!this.player.level().isClientSide()) {
            this.tickLoading();
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

        if (index == SLOT_INPUT) {
            if (!this.moveItemStackTo(sourceStack, INV_START, INV_END + 1, true)) {
                return ItemStack.EMPTY;
            }
        } else if (sourceStack.is(ModItems.DRIED_CANNABIS_BUD.get())) {
            if (!this.moveItemStackTo(sourceStack, SLOT_INPUT, SLOT_INPUT + 1, false)) {
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
        return player.isAlive() && this.getControlledStack().getItem() instanceof PipeItem;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (player.level().isClientSide()) {
            return;
        }

        ItemStack remainingInput = this.inputContainer.removeItemNoUpdate(SLOT_INPUT);
        if (!remainingInput.isEmpty()) {
            player.getInventory().placeItemBackInInventory(remainingInput);
        }
    }

    public int getCooldownPercent() {
        return this.cooldownPercent;
    }

    public boolean isLoaded() {
        return this.loadedState > 0;
    }

    public boolean isProcessing() {
        return this.progress > 0;
    }

    public int getProgressScaled(int pixels) {
        if (this.progressMax <= 0 || this.progress <= 0) {
            return 0;
        }
        return Math.min(pixels, this.progress * pixels / this.progressMax);
    }

    private void tickLoading() {
        this.progressMax = LOAD_TIME_TICKS;

        if (!this.canLoad()) {
            this.progress = 0;
            return;
        }

        this.progress++;
        if (this.progress < this.progressMax) {
            return;
        }

        ItemStack inputBud = this.inputContainer.getItem(SLOT_INPUT);
        if (inputBud.isEmpty()) {
            this.progress = 0;
            return;
        }

        StrainData strainData = StrainData.getOrCreate(inputBud, this.player.getRandom());
        if (!this.player.getAbilities().instabuild) {
            inputBud.shrink(1);
            if (inputBud.isEmpty()) {
                this.inputContainer.setItem(SLOT_INPUT, ItemStack.EMPTY);
            }
        }

        PipeItem.setLoadedFromMenu(this.getControlledStack(), strainData, this.player);
        this.inputContainer.setChanged();
        this.progress = 0;
    }

    private boolean canLoad() {
        ItemStack stack = this.getControlledStack();
        if (!(stack.getItem() instanceof PipeItem)) {
            return false;
        }
        if (PipeItem.isLoaded(stack)) {
            return false;
        }
        if (Math.round(PipeItem.getReloadCooldownPercent(this.player, stack, 0.0F) * 100.0F) > 0) {
            return false;
        }
        return this.inputContainer.getItem(SLOT_INPUT).is(ModItems.DRIED_CANNABIS_BUD.get());
    }

    private void refreshStateFromItem() {
        ItemStack stack = this.getControlledStack();
        this.loadedState = PipeItem.isLoaded(stack) ? 1 : 0;
        this.cooldownPercent = Math.round(PipeItem.getReloadCooldownPercent(this.player, stack, 0.0F) * 100.0F);
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
