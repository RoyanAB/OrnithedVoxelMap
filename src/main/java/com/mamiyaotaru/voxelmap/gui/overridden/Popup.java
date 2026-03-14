package com.mamiyaotaru.voxelmap.gui.overridden;

import com.mamiyaotaru.voxelmap.util.GLShim;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

import java.util.ArrayList;

public class Popup {
    public int clickedX;
    public int clickedY;
    public int clickedDirectX;
    public int clickedDirectY;
    Minecraft mc;
    FontRenderer fontRendererObj;
    int x;
    int y;
    PopupEntry[] entries;
    int w;
    int h;
    boolean shouldClose = false;
    PopupGuiScreen parentGui;
    int padding = 6;

    public Popup(int x, int y, int directX, int directY, ArrayList<PopupEntry> entries, PopupGuiScreen parentGui) {
        this.mc = Minecraft.getInstance();
        this.fontRendererObj = this.mc.fontRenderer;
        this.parentGui = parentGui;
        this.clickedX = x;
        this.clickedY = y;
        this.clickedDirectX = directX;
        this.clickedDirectY = directY;
        this.x = x - 1;
        this.y = y - 1;
        this.entries = new PopupEntry[entries.size()];
        entries.toArray(this.entries);
        this.w = 0;
        this.h = this.entries.length * 20;

        for (int t = 0; t < this.entries.length; t++) {
            int entryWidth = this.fontRendererObj.getStringWidth(this.entries[t].name);
            if (entryWidth > this.w) {
                this.w = entryWidth;
            }
        }

        this.w = this.w + this.padding * 2;
        if (x + this.w > parentGui.width) {
            this.x = x - this.w + 2;
        }

        if (y + this.h > parentGui.height) {
            this.y = y - this.h + 2;
        }
    }

    public boolean clickedMe(double mouseX, double mouseY) {
        boolean clicked = mouseX > this.x && mouseX < this.x + this.w && mouseY > this.y && mouseY < this.y + this.h;
        if (clicked) {
            for (int t = 0; t < this.entries.length; t++) {
                if (this.entries[t].enabled) {
                    boolean entryClicked = mouseX >= this.x && mouseX <= this.x + this.w && mouseY >= this.y + t * 20 && mouseY <= this.y + (t + 1) * 20;
                    if (entryClicked) {
                        this.shouldClose = this.entries[t].causesClose;
                        this.parentGui.popupAction(this, this.entries[t].action);
                    }
                }
            }
        }

        return clicked;
    }

    public boolean overMe(int x, int y) {
        return x > this.x && x < this.x + this.w && y > this.y && y < this.y + this.h;
    }

    public boolean shouldClose() {
        return this.shouldClose;
    }

    public void drawPopup(int mouseX, int mouseY) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder vertexBuffer = tessellator.getBuffer();
        GLShim.glDisable(2929);
        this.mc.getTextureManager().bindTexture(Gui.OPTIONS_BACKGROUND);
        GLShim.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        float var6 = 32.0F;
        vertexBuffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
        vertexBuffer.pos(this.x, this.y + this.h, 0.0).tex(this.x / var6, this.y / var6).color(64, 64, 64, 255).endVertex();
        vertexBuffer.pos(this.x + this.w, this.y + this.h, 0.0).tex((this.x + this.w) / var6, this.y / var6).color(64, 64, 64, 255).endVertex();
        vertexBuffer.pos(this.x + this.w, this.y, 0.0).tex((this.x + this.w) / var6, (this.y + this.h) / var6).color(64, 64, 64, 255).endVertex();
        vertexBuffer.pos(this.x, this.y, 0.0).tex(this.x / var6, (this.y + this.h) / var6).color(64, 64, 64, 255).endVertex();
        tessellator.draw();
        GLShim.glEnable(3042);
        GLShim.glBlendFunc(770, 771);
        GLShim.glDisable(3008);
        GLShim.glShadeModel(7425);
        GLShim.glDisable(3553);
        byte fadeWidth = 4;
        vertexBuffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        vertexBuffer.pos(this.x, this.y + fadeWidth, 0.0).color(0, 0, 0, 0).endVertex();
        vertexBuffer.pos(this.x + this.w, this.y + fadeWidth, 0.0).color(0, 0, 0, 0).endVertex();
        vertexBuffer.pos(this.x + this.w, this.y, 0.0).color(0, 0, 0, 255).endVertex();
        vertexBuffer.pos(this.x, this.y, 0.0).color(0, 0, 0, 255).endVertex();
        tessellator.draw();
        vertexBuffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        vertexBuffer.pos(this.x, this.y + this.h, 0.0).color(0, 0, 0, 255).endVertex();
        vertexBuffer.pos(this.x + this.w, this.y + this.h, 0.0).color(0, 0, 0, 255).endVertex();
        vertexBuffer.pos(this.x + this.w, this.y + this.h - fadeWidth, 0.0).color(0, 0, 0, 0).endVertex();
        vertexBuffer.pos(this.x, this.y + this.h - fadeWidth, 0.0).color(0, 0, 0, 0).endVertex();
        tessellator.draw();
        vertexBuffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        vertexBuffer.pos(this.x, this.y, 0.0).color(0, 0, 0, 255).endVertex();
        vertexBuffer.pos(this.x, this.y + this.h, 0.0).color(0, 0, 0, 255).endVertex();
        vertexBuffer.pos(this.x + fadeWidth, this.y + this.h, 0.0).color(0, 0, 0, 0).endVertex();
        vertexBuffer.pos(this.x + fadeWidth, this.y, 0.0).color(0, 0, 0, 0).endVertex();
        tessellator.draw();
        vertexBuffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        vertexBuffer.pos(this.x + this.w - fadeWidth, this.y, 0.0).color(0, 0, 0, 0).endVertex();
        vertexBuffer.pos(this.x + this.w - fadeWidth, this.y + this.h, 0.0).color(0, 0, 0, 0).endVertex();
        vertexBuffer.pos(this.x + this.w, this.y + this.h, 0.0).color(0, 0, 0, 255).endVertex();
        vertexBuffer.pos(this.x + this.w, this.y, 0.0).color(0, 0, 0, 255).endVertex();
        tessellator.draw();
        GLShim.glEnable(3553);
        GLShim.glShadeModel(7424);
        GLShim.glEnable(3008);
        GLShim.glDisable(3042);

        for (int t = 0; t < this.entries.length; t++) {
            int color = !this.entries[t].enabled
                    ? 10526880
                    : (mouseX >= this.x && mouseX <= this.x + this.w && mouseY >= this.y + t * 20 && mouseY <= this.y + (t + 1) * 20 ? 16777120 : 14737632);
            this.fontRendererObj.drawStringWithShadow(this.entries[t].name, this.x + this.padding, this.y + this.padding + t * 20, color);
        }
    }

    public static class PopupEntry {
        public String name;
        public int action;
        boolean causesClose;
        boolean enabled;

        public PopupEntry(String name, int action, boolean causesClose, boolean enabled) {
            this.name = name;
            this.action = action;
            this.causesClose = causesClose;
            this.enabled = enabled;
        }
    }
}
