package com.stofiiis.weed.client;

import com.stofiiis.weed.WeedMod;
import com.stofiiis.weed.menu.PipeControlMenu;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;

public class PipeControlScreen extends AbstractContainerScreen<PipeControlMenu> {
    private static final int ARROW_X = 79;
    private static final int ARROW_Y = 39;
    private static final Identifier BACKGROUND_TEXTURE = Identifier.fromNamespaceAndPath(WeedMod.MODID, "textures/gui/pipe_control.png");

    public PipeControlScreen(PipeControlMenu menu, Inventory playerInventory, Component title) {
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
        drawArrowProgress(guiGraphics, x + ARROW_X, y + ARROW_Y, this.menu.getProgressScaled(24), 0x76EA95, 0x30B657);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0xEAF6EC);

        Component bowlState = this.menu.isLoaded() ? Component.translatable("hud.weed.loaded") : Component.translatable("hud.weed.empty");
        guiGraphics.drawString(this.font, Component.translatable("screen.weed.pipe_loaded", bowlState), 8, 20, 0xD7E0D7);
        guiGraphics.drawString(this.font, Component.translatable("screen.weed.pipe_cooldown", this.menu.getCooldownPercent()), 8, 32, 0xD7E0D7);

        Component processText = this.menu.isProcessing()
                ? Component.translatable("screen.weed.loading")
                : Component.translatable("screen.weed.insert_dried_bud");
        guiGraphics.drawString(this.font, processText, 79, 56, 0xC9E8CF);
    }

    @Override
    protected void renderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderTooltip(guiGraphics, mouseX, mouseY);

        int arrowX = this.leftPos + ARROW_X;
        int arrowY = this.topPos + ARROW_Y;
        if (this.isHovering(arrowX - this.leftPos, arrowY - this.topPos, 24, 17, mouseX, mouseY)) {
            int percent = this.menu.isProcessing() ? Mth.clamp(this.menu.getProgressScaled(100), 0, 100) : 0;
            guiGraphics.setTooltipForNextFrame(this.font, Component.translatable("screen.weed.loading_progress", percent), mouseX, mouseY);
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
}
