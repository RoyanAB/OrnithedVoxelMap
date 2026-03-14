package com.mamiyaotaru.voxelmap.gui.overridden;

import com.mamiyaotaru.voxelmap.util.GLShim;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;

import java.util.ArrayList;

public class Popup {
    public int clickedX;
    public int clickedY;
    public int clickedDirectX;
    public int clickedDirectY;
    MinecraftClient client;
    TextRenderer fontRendererObj;
    int x;
    int y;
    Popup.PopupEntry[] entries;
    int w;
    int h;
    boolean shouldClose = false;
    PopupGuiScreen parentGui;
    int padding = 6;

    public Popup(int x, int y, int directX, int directY, ArrayList<Popup.PopupEntry> entries, PopupGuiScreen parentGui) {
        this.client = MinecraftClient.getInstance();
        this.fontRendererObj = this.client.textRenderer;
        this.parentGui = parentGui;
        this.clickedX = x;
        this.clickedY = y;
        this.clickedDirectX = directX;
        this.clickedDirectY = directY;
        this.x = x - 1;
        this.y = y - 1;
        this.entries = new Popup.PopupEntry[entries.size()];
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
        this.client.getTextureManager().bindTexture(Screen.BACKGROUND_LOCATION);
        GLShim.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        float var6 = 32.0F;
        vertexBuffer.begin(7, VertexFormats.POSITION_TEXTURE_COLOR);
        vertexBuffer.vertex(this.x, this.y + this.h, 0.0).texture(this.x / var6, this.y / var6).color(64, 64, 64, 255).next();
        vertexBuffer.vertex(this.x + this.w, this.y + this.h, 0.0).texture((this.x + this.w) / var6, this.y / var6).color(64, 64, 64, 255).next();
        vertexBuffer.vertex(this.x + this.w, this.y, 0.0).texture((this.x + this.w) / var6, (this.y + this.h) / var6).color(64, 64, 64, 255).next();
        vertexBuffer.vertex(this.x, this.y, 0.0).texture(this.x / var6, (this.y + this.h) / var6).color(64, 64, 64, 255).next();
        tessellator.draw();
        GLShim.glEnable(3042);
        GLShim.glBlendFunc(770, 771);
        GLShim.glDisable(3008);
        GLShim.glShadeModel(7425);
        GLShim.glDisable(3553);
        byte fadeWidth = 4;
        vertexBuffer.begin(7, VertexFormats.POSITION_COLOR);
        vertexBuffer.vertex(this.x, this.y + 4, 0.0).color(0, 0, 0, 0).next();
        vertexBuffer.vertex(this.x + this.w, this.y + 4, 0.0).color(0, 0, 0, 0).next();
        vertexBuffer.vertex(this.x + this.w, this.y, 0.0).color(0, 0, 0, 255).next();
        vertexBuffer.vertex(this.x, this.y, 0.0).color(0, 0, 0, 255).next();
        tessellator.draw();
        vertexBuffer.begin(7, VertexFormats.POSITION_COLOR);
        vertexBuffer.vertex(this.x, this.y + this.h, 0.0).color(0, 0, 0, 255).next();
        vertexBuffer.vertex(this.x + this.w, this.y + this.h, 0.0).color(0, 0, 0, 255).next();
        vertexBuffer.vertex(this.x + this.w, this.y + this.h - 4, 0.0).color(0, 0, 0, 0).next();
        vertexBuffer.vertex(this.x, this.y + this.h - 4, 0.0).color(0, 0, 0, 0).next();
        tessellator.draw();
        vertexBuffer.begin(7, VertexFormats.POSITION_COLOR);
        vertexBuffer.vertex(this.x, this.y, 0.0).color(0, 0, 0, 255).next();
        vertexBuffer.vertex(this.x, this.y + this.h, 0.0).color(0, 0, 0, 255).next();
        vertexBuffer.vertex(this.x + 4, this.y + this.h, 0.0).color(0, 0, 0, 0).next();
        vertexBuffer.vertex(this.x + 4, this.y, 0.0).color(0, 0, 0, 0).next();
        tessellator.draw();
        vertexBuffer.begin(7, VertexFormats.POSITION_COLOR);
        vertexBuffer.vertex(this.x + this.w - 4, this.y, 0.0).color(0, 0, 0, 0).next();
        vertexBuffer.vertex(this.x + this.w - 4, this.y + this.h, 0.0).color(0, 0, 0, 0).next();
        vertexBuffer.vertex(this.x + this.w, this.y + this.h, 0.0).color(0, 0, 0, 255).next();
        vertexBuffer.vertex(this.x + this.w, this.y, 0.0).color(0, 0, 0, 255).next();
        tessellator.draw();
        GLShim.glEnable(3553);
        GLShim.glShadeModel(7424);
        GLShim.glEnable(3008);
        GLShim.glDisable(3042);

        for (int t = 0; t < this.entries.length; t++) {
            int color = !this.entries[t].enabled
                    ? 10526880
                    : (mouseX >= this.x && mouseX <= this.x + this.w && mouseY >= this.y + t * 20 && mouseY <= this.y + (t + 1) * 20 ? 16777120 : 14737632);
            this.fontRendererObj.drawWithShadow(this.entries[t].name, this.x + this.padding, this.y + this.padding + t * 20, color);
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
