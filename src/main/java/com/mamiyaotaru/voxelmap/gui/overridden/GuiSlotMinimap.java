package com.mamiyaotaru.voxelmap.gui.overridden;

import com.mamiyaotaru.voxelmap.util.GLShim;
import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.math.MathHelper;

public abstract class GuiSlotMinimap extends EntryListWidget {
    public boolean doubleclick = false;
    protected int slotWidth = 220;
    protected boolean centerListVertically = true;
    protected int headerPadding;
    protected long lastClicked = 0L;
    private boolean showTopBottomBG = true;
    private boolean showSlotBG = true;
    private boolean hasListHeader;

    public GuiSlotMinimap(MinecraftClient par1Minecraft, int width, int height, int y1, int y2, int slotHeight) {
        super(par1Minecraft, width, height, y1, y2, slotHeight);
        this.blitOffset = 0;
    }

    public void setDimensions(int width, int height, int y1, int y2) {
        this.width = width;
        this.height = height;
        this.top = y1;
        this.bottom = y2;
        this.left = 0;
        this.right = width;
    }

    public void setShowTopBottomBG(boolean showTopBottomBG) {
        this.showTopBottomBG = showTopBottomBG;
    }

    public void setShowSlotBG(boolean showSlotBG) {
        this.showSlotBG = showSlotBG;
    }

    public void render(int mouseX, int mouseY, float partialTicks) {
        this.renderBackground();
        int scrollBarLeft = this.getScrollbarPosition();
        int scrollBarRight = scrollBarLeft + 6;
        this.setScrollAmount(this.getScrollAmount());
        GLShim.glDisable(2896);
        GLShim.glDisable(2912);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder vertexBuffer = tessellator.getBuffer();
        if (this.showSlotBG) {
            this.minecraft.getTextureManager().bindTexture(Screen.BACKGROUND_LOCATION);
            GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            float f = 32.0F;
            vertexBuffer.begin(7, VertexFormats.POSITION_TEXTURE_COLOR);
            vertexBuffer.vertex(this.left, this.bottom, 0.0).texture(this.left / f, (this.bottom + (int) this.getScrollAmount()) / f).color(32, 32, 32, 255).next();
            vertexBuffer.vertex(this.right, this.bottom, 0.0)
                    .texture(this.right / f, (this.bottom + (int) this.getScrollAmount()) / f)
                    .color(32, 32, 32, 255)
                    .next();
            vertexBuffer.vertex(this.right, this.top, 0.0).texture(this.right / f, (this.top + (int) this.getScrollAmount()) / f).color(32, 32, 32, 255).next();
            vertexBuffer.vertex(this.left, this.top, 0.0).texture(this.left / f, (this.top + (int) this.getScrollAmount()) / f).color(32, 32, 32, 255).next();
            tessellator.draw();
        }

        int leftEdge = this.left + this.width / 2 - this.getRowWidth() / 2 + 2;
        int topOfListYPos = this.top + 4 - (int) this.getScrollAmount();
        if (this.hasListHeader) {
            this.renderHeader(leftEdge, topOfListYPos, tessellator);
        }

        this.renderList(leftEdge, topOfListYPos, mouseX, mouseY, partialTicks);
        GLShim.glDisable(2929);
        byte topBottomFadeHeight = 4;
        if (this.showTopBottomBG) {
            this.renderHoleBackground(0, this.top, 255, 255);
            this.renderHoleBackground(this.bottom, this.height, 255, 255);
        }

        GLShim.glEnable(3042);
        GLX.glBlendFuncSeparate(770, 771, 0, 1);
        GLShim.glDisable(3008);
        GLShim.glShadeModel(7425);
        GLShim.glDisable(3553);
        if (this.showTopBottomBG) {
            vertexBuffer.begin(7, VertexFormats.POSITION_TEXTURE_COLOR);
            vertexBuffer.vertex(this.left, this.top + topBottomFadeHeight, 0.0).texture(0.0, 1.0).color(0, 0, 0, 0).next();
            vertexBuffer.vertex(this.right, this.top + topBottomFadeHeight, 0.0).texture(1.0, 1.0).color(0, 0, 0, 0).next();
            vertexBuffer.vertex(this.right, this.top, 0.0).texture(1.0, 0.0).color(0, 0, 0, 255).next();
            vertexBuffer.vertex(this.left, this.top, 0.0).texture(0.0, 0.0).color(0, 0, 0, 255).next();
            tessellator.draw();
            vertexBuffer.begin(7, VertexFormats.POSITION_TEXTURE_COLOR);
            vertexBuffer.vertex(this.left, this.bottom, 0.0).texture(0.0, 1.0).color(0, 0, 0, 255).next();
            vertexBuffer.vertex(this.right, this.bottom, 0.0).texture(1.0, 1.0).color(0, 0, 0, 255).next();
            vertexBuffer.vertex(this.right, this.bottom - topBottomFadeHeight, 0.0).texture(1.0, 0.0).color(0, 0, 0, 0).next();
            vertexBuffer.vertex(this.left, this.bottom - topBottomFadeHeight, 0.0).texture(0.0, 0.0).color(0, 0, 0, 0).next();
            tessellator.draw();
        }

        int maxScroll = this.getMaxScroll();
        if (maxScroll > 0) {
            int k1 = (this.bottom - this.top) * (this.bottom - this.top) / this.getMaxPosition();
            k1 = MathHelper.clamp(k1, 32, this.bottom - this.top - 8);
            int l1 = (int) this.getScrollAmount() * (this.bottom - this.top - k1) / maxScroll + this.top;
            if (l1 < this.top) {
                l1 = this.top;
            }

            vertexBuffer.begin(7, VertexFormats.POSITION_TEXTURE_COLOR);
            vertexBuffer.vertex(scrollBarLeft, this.bottom, 0.0).texture(0.0, 1.0).color(0, 0, 0, 255).next();
            vertexBuffer.vertex(scrollBarRight, this.bottom, 0.0).texture(1.0, 1.0).color(0, 0, 0, 255).next();
            vertexBuffer.vertex(scrollBarRight, this.top, 0.0).texture(1.0, 0.0).color(0, 0, 0, 255).next();
            vertexBuffer.vertex(scrollBarLeft, this.top, 0.0).texture(0.0, 0.0).color(0, 0, 0, 255).next();
            tessellator.draw();
            vertexBuffer.begin(7, VertexFormats.POSITION_TEXTURE_COLOR);
            vertexBuffer.vertex(scrollBarLeft, l1 + k1, 0.0).texture(0.0, 1.0).color(128, 128, 128, 255).next();
            vertexBuffer.vertex(scrollBarRight, l1 + k1, 0.0).texture(1.0, 1.0).color(128, 128, 128, 255).next();
            vertexBuffer.vertex(scrollBarRight, l1, 0.0).texture(1.0, 0.0).color(128, 128, 128, 255).next();
            vertexBuffer.vertex(scrollBarLeft, l1, 0.0).texture(0.0, 0.0).color(128, 128, 128, 255).next();
            tessellator.draw();
            vertexBuffer.begin(7, VertexFormats.POSITION_TEXTURE_COLOR);
            vertexBuffer.vertex(scrollBarLeft, l1 + k1 - 1, 0.0).texture(0.0, 1.0).color(192, 192, 192, 255).next();
            vertexBuffer.vertex(scrollBarRight - 1, l1 + k1 - 1, 0.0).texture(1.0, 1.0).color(192, 192, 192, 255).next();
            vertexBuffer.vertex(scrollBarRight - 1, l1, 0.0).texture(1.0, 0.0).color(192, 192, 192, 255).next();
            vertexBuffer.vertex(scrollBarLeft, l1, 0.0).texture(0.0, 0.0).color(192, 192, 192, 255).next();
            tessellator.draw();
        }

        this.renderDecorations(mouseX, mouseY);
        GLShim.glEnable(3553);
        GLShim.glShadeModel(7424);
        GLShim.glEnable(3008);
        GLShim.glDisable(3042);
    }

    private int getMaxScroll() {
        return Math.max(0, this.getMaxPosition() - (this.bottom - this.top - 4));
    }

    public int getRowWidth() {
        return this.slotWidth;
    }

    public void setSlotWidth(int slotWidth) {
        this.slotWidth = slotWidth;
    }

    protected int getScrollbarPosition() {
        return this.slotWidth >= 220 ? this.width / 2 + 124 : this.right - 6;
    }

    public void setLeftPos(int x1) {
        this.left = x1;
        this.right = x1 + this.width;
    }

    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        this.doubleclick = System.currentTimeMillis() - this.lastClicked < 250L;
        this.lastClicked = System.currentTimeMillis();
        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }
}
