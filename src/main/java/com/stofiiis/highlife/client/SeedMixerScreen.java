package com.stofiiis.highlife.client;

import com.stofiiis.highlife.HighLifeMod;
import com.stofiiis.highlife.block.entity.SeedMixerBlockEntity;
import com.stofiiis.highlife.menu.SeedMixerMenu;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;

public class SeedMixerScreen extends AbstractContainerScreen<SeedMixerMenu> {
    private static final Identifier BACKGROUND_TEXTURE = Identifier.fromNamespaceAndPath(HighLifeMod.MODID, "textures/gui/seed_mixer.png");

    public SeedMixerScreen(SeedMixerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
        this.titleLabelX = 8;
        this.titleLabelY = 4;
        this.inventoryLabelX = 8;
        this.inventoryLabelY = 72;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;

        guiGraphics.blit(
                RenderPipelines.GUI_TEXTURED,
                BACKGROUND_TEXTURE,
                x,
                y,
                0.0F,
                0.0F,
                this.imageWidth,
                this.imageHeight,
                this.imageWidth,
                this.imageHeight);

        drawSlotState(guiGraphics, x + 35, y + 26, this.menu.hasSeedA(), true);
        drawSlotState(guiGraphics, x + 53, y + 26, this.menu.hasSeedB(), true);
        drawSlotState(guiGraphics, x + 35, y + 52, this.menu.hasDirt(), true);
        drawSlotState(guiGraphics, x + 53, y + 52, this.menu.hasBonemeal(), false);
        drawArrowProgress(guiGraphics, x + 79, y + 39, this.menu.getProgressScaled(24), 0xBEEA6E, 0x7DB946);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0xEDF4DF);

        Component status = switch (this.menu.getMixState()) {
            case SeedMixerBlockEntity.STATE_MIXING -> Component.translatable("screen.highlife.seed_mixer_mixing", this.menu.getRemainingSeconds());
            case SeedMixerBlockEntity.STATE_READY -> Component.translatable("screen.highlife.seed_mixer_ready");
            default -> this.getIdleStatus();
        };
        guiGraphics.drawString(this.font, status, 79, 56, 0xDCEBC7);
    }

    private Component getIdleStatus() {
        if (!this.menu.hasSeedA()) {
            return Component.translatable("screen.highlife.seed_mixer_need_seed_a");
        }
        if (!this.menu.hasSeedB()) {
            return Component.translatable("screen.highlife.seed_mixer_need_seed_b");
        }
        if (!this.menu.hasDirt()) {
            return Component.translatable("screen.highlife.seed_mixer_need_dirt");
        }
        return Component.translatable("screen.highlife.seed_mixer_starting");
    }

    @Override
    protected void renderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderTooltip(guiGraphics, mouseX, mouseY);

        int arrowX = this.leftPos + 79;
        int arrowY = this.topPos + 39;
        if (this.isHovering(arrowX - this.leftPos, arrowY - this.topPos, 24, 17, mouseX, mouseY)) {
            int percent = this.menu.isMixing() ? Mth.clamp(this.menu.getProgressScaled(100), 0, 100) : 0;
            guiGraphics.setTooltipForNextFrame(this.font, Component.translatable("screen.highlife.loading_progress", percent), mouseX, mouseY);
        }
    }

    private static void drawArrowProgress(GuiGraphics guiGraphics, int x, int y, int progressPixels, int lightColor, int darkColor) {
        int fill = Mth.clamp(progressPixels, 0, 24);
        if (fill <= 0) {
            return;
        }

        int shaftFill = Math.min(fill, 14);
        if (shaftFill > 0) {
            guiGraphics.fill(x, y + 6, x + shaftFill, y + 11, 0xFF000000 | darkColor);
            guiGraphics.fill(x, y + 7, x + shaftFill, y + 10, 0xFF000000 | lightColor);
        }

        if (fill > 14) {
            int midFill = Math.min(fill - 14, 6);
            guiGraphics.fill(x + 14, y + 4, x + 14 + midFill, y + 13, 0xFF000000 | darkColor);
            guiGraphics.fill(x + 14, y + 5, x + 14 + midFill, y + 12, 0xFF000000 | lightColor);
        }

        if (fill > 20) {
            int headFill = Math.min(fill - 20, 4);
            guiGraphics.fill(x + 20, y + 5, x + 20 + headFill, y + 12, 0xFF000000 | darkColor);
            guiGraphics.fill(x + 20, y + 6, x + 20 + headFill, y + 11, 0xFF000000 | lightColor);
        }
    }

    private static void drawSlotState(GuiGraphics guiGraphics, int x, int y, boolean filled, boolean required) {
        int color = filled ? 0xFF55C878 : required ? 0xFFE06B55 : 0xFFB89548;
        guiGraphics.fill(x - 1, y - 1, x + 17, y, color);
        guiGraphics.fill(x - 1, y + 16, x + 17, y + 17, color);
        guiGraphics.fill(x - 1, y, x, y + 16, color);
        guiGraphics.fill(x + 16, y, x + 17, y + 16, color);
    }
}
