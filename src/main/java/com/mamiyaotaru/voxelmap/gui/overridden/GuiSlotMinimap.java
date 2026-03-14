package com.mamiyaotaru.voxelmap.gui.overridden;

import com.mamiyaotaru.voxelmap.util.GLShim;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiSlot;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.MathHelper;

public abstract class GuiSlotMinimap extends GuiSlot {
    public boolean doubleclick = false;
    protected int slotWidth = 220;
    protected boolean field_148163_i = true;
    protected int field_148160_j;
    protected long field_148167_s = 0L;
    private boolean showTopBottomBG = true;
    private boolean showSlotBG = true;
    private boolean hasListHeader;

    public GuiSlotMinimap(Minecraft par1Minecraft, int width, int height, int top, int bottom, int slotHeight) {
        super(par1Minecraft, width, height, top, bottom, slotHeight);
    }

    public void setDimensions(int width, int height, int top, int bottom) {
        this.width = width;
        this.height = height;
        this.top = top;
        this.bottom = bottom;
        this.left = 0;
        this.right = width;
    }

    public void setShowTopBottomBG(boolean showTopBottomBG) {
        this.showTopBottomBG = showTopBottomBG;
    }

    public void setShowSlotBG(boolean showSlotBG) {
        this.showSlotBG = showSlotBG;
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (this.isVisible()) {
            this.drawBackground();
            int scrollBarLeft = this.getScrollBarX();
            int scrollBarRight = scrollBarLeft + 6;
            this.bindAmountScrolled();
            GLShim.glDisable(2896);
            GLShim.glDisable(2912);
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder vertexBuffer = tessellator.getBuffer();
            if (this.showSlotBG) {
                this.mc.getTextureManager().bindTexture(Gui.OPTIONS_BACKGROUND);
                GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                float f = 32.0F;
                vertexBuffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
                vertexBuffer.pos(this.left, this.bottom, 0.0).tex(this.left / f, (this.bottom + (int) this.amountScrolled) / f).color(32, 32, 32, 255).endVertex();
                vertexBuffer.pos(this.right, this.bottom, 0.0).tex(this.right / f, (this.bottom + (int) this.amountScrolled) / f).color(32, 32, 32, 255).endVertex();
                vertexBuffer.pos(this.right, this.top, 0.0).tex(this.right / f, (this.top + (int) this.amountScrolled) / f).color(32, 32, 32, 255).endVertex();
                vertexBuffer.pos(this.left, this.top, 0.0).tex(this.left / f, (this.top + (int) this.amountScrolled) / f).color(32, 32, 32, 255).endVertex();
                tessellator.draw();
            }

            int leftEdge = this.left + this.width / 2 - this.getListWidth() / 2 + 2;
            int topOfListYPos = this.top + 4 - (int) this.amountScrolled;
            if (this.hasListHeader) {
                this.drawListHeader(leftEdge, topOfListYPos, tessellator);
            }

            this.drawSelectionBox(leftEdge, topOfListYPos, mouseX, mouseY, partialTicks);
            GLShim.glDisable(2929);
            byte topBottomFadeHeight = 4;
            if (this.showTopBottomBG) {
                this.overlayBackground(0, this.top, 255, 255);
                this.overlayBackground(this.bottom, this.height, 255, 255);
            }

            GLShim.glEnable(3042);
            OpenGlHelper.glBlendFuncSeparate(770, 771, 0, 1);
            GLShim.glDisable(3008);
            GLShim.glShadeModel(7425);
            GLShim.glDisable(3553);
            if (this.showTopBottomBG) {
                vertexBuffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
                vertexBuffer.pos(this.left, this.top + topBottomFadeHeight, 0.0).tex(0.0, 1.0).color(0, 0, 0, 0).endVertex();
                vertexBuffer.pos(this.right, this.top + topBottomFadeHeight, 0.0).tex(1.0, 1.0).color(0, 0, 0, 0).endVertex();
                vertexBuffer.pos(this.right, this.top, 0.0).tex(1.0, 0.0).color(0, 0, 0, 255).endVertex();
                vertexBuffer.pos(this.left, this.top, 0.0).tex(0.0, 0.0).color(0, 0, 0, 255).endVertex();
                tessellator.draw();
                vertexBuffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
                vertexBuffer.pos(this.left, this.bottom, 0.0).tex(0.0, 1.0).color(0, 0, 0, 255).endVertex();
                vertexBuffer.pos(this.right, this.bottom, 0.0).tex(1.0, 1.0).color(0, 0, 0, 255).endVertex();
                vertexBuffer.pos(this.right, this.bottom - topBottomFadeHeight, 0.0).tex(1.0, 0.0).color(0, 0, 0, 0).endVertex();
                vertexBuffer.pos(this.left, this.bottom - topBottomFadeHeight, 0.0).tex(0.0, 0.0).color(0, 0, 0, 0).endVertex();
                tessellator.draw();
            }

            int maxScroll = this.getMaxScroll();
            if (maxScroll > 0) {
                int k1 = (this.bottom - this.top) * (this.bottom - this.top) / this.getContentHeight();
                k1 = MathHelper.clamp(k1, 32, this.bottom - this.top - 8);
                int l1 = (int) this.amountScrolled * (this.bottom - this.top - k1) / maxScroll + this.top;
                if (l1 < this.top) {
                    l1 = this.top;
                }

                vertexBuffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
                vertexBuffer.pos(scrollBarLeft, this.bottom, 0.0).tex(0.0, 1.0).color(0, 0, 0, 255).endVertex();
                vertexBuffer.pos(scrollBarRight, this.bottom, 0.0).tex(1.0, 1.0).color(0, 0, 0, 255).endVertex();
                vertexBuffer.pos(scrollBarRight, this.top, 0.0).tex(1.0, 0.0).color(0, 0, 0, 255).endVertex();
                vertexBuffer.pos(scrollBarLeft, this.top, 0.0).tex(0.0, 0.0).color(0, 0, 0, 255).endVertex();
                tessellator.draw();
                vertexBuffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
                vertexBuffer.pos(scrollBarLeft, l1 + k1, 0.0).tex(0.0, 1.0).color(128, 128, 128, 255).endVertex();
                vertexBuffer.pos(scrollBarRight, l1 + k1, 0.0).tex(1.0, 1.0).color(128, 128, 128, 255).endVertex();
                vertexBuffer.pos(scrollBarRight, l1, 0.0).tex(1.0, 0.0).color(128, 128, 128, 255).endVertex();
                vertexBuffer.pos(scrollBarLeft, l1, 0.0).tex(0.0, 0.0).color(128, 128, 128, 255).endVertex();
                tessellator.draw();
                vertexBuffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
                vertexBuffer.pos(scrollBarLeft, l1 + k1 - 1, 0.0).tex(0.0, 1.0).color(192, 192, 192, 255).endVertex();
                vertexBuffer.pos(scrollBarRight - 1, l1 + k1 - 1, 0.0).tex(1.0, 1.0).color(192, 192, 192, 255).endVertex();
                vertexBuffer.pos(scrollBarRight - 1, l1, 0.0).tex(1.0, 0.0).color(192, 192, 192, 255).endVertex();
                vertexBuffer.pos(scrollBarLeft, l1, 0.0).tex(0.0, 0.0).color(192, 192, 192, 255).endVertex();
                tessellator.draw();
            }

            this.renderDecorations(mouseX, mouseY);
            GLShim.glEnable(3553);
            GLShim.glShadeModel(7424);
            GLShim.glEnable(3008);
            GLShim.glDisable(3042);
        }
    }

    public int getListWidth() {
        return this.slotWidth;
    }

    public void setSlotWidth(int slotWidth) {
        this.slotWidth = slotWidth;
    }

    protected int getScrollBarX() {
        return this.slotWidth >= 220 ? this.width / 2 + 124 : this.right - 6;
    }

    public void setSlotXBoundsFromLeft(int left) {
        this.left = left;
        this.right = left + this.width;
    }

    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        this.doubleclick = System.currentTimeMillis() - this.field_148167_s < 250L;
        this.field_148167_s = System.currentTimeMillis();
        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }
}
