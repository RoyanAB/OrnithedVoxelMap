package com.mamiyaotaru.voxelmap.gui.overridden;

import com.mamiyaotaru.voxelmap.util.GLShim;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;

@SuppressWarnings("unused")
public class Popup {
	public int clickedX;
	public int clickedY;
	public int clickedDirectX;
	public int clickedDirectY;

	Minecraft mc;
	FontRenderer fontRendererObj;

	int x;
	int y;

	Popup.PopupEntry[] entries;

	int w;
	int h;

	boolean shouldClose = false;
	PopupGuiScreen parentGui;
	int padding = 6;

	public Popup(int x, int y, int directX, int directY, ArrayList<Popup.PopupEntry> entries, PopupGuiScreen parentGui) {
		this.mc = Minecraft.getMinecraft();
		this.fontRendererObj = this.mc.fontRenderer;
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

		for (PopupEntry entry : this.entries) {
			int entryWidth = this.fontRendererObj.getStringWidth(entry.name);
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

	public boolean clickedMe(int mouseX, int mouseY) {
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
		GLShim.glDisable(GLShim.GL11_GL_DEPTH_TEST);
		this.mc.getTextureManager().bindTexture(Gui.OPTIONS_BACKGROUND);
		GLShim.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

		float spriteSize = 32.0F;
		vertexBuffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
		vertexBuffer.pos(this.x, this.y + this.h, 0.0).tex(this.x / spriteSize, this.y / spriteSize).color(64, 64, 64, 255).endVertex();
		vertexBuffer.pos(this.x + this.w, this.y + this.h, 0.0)
			.tex((this.x + this.w) / spriteSize, this.y / spriteSize)
			.color(64, 64, 64, 255)
			.endVertex();
		vertexBuffer.pos(this.x + this.w, this.y, 0.0)
			.tex((this.x + this.w) / spriteSize, (this.y + this.h) / spriteSize)
			.color(64, 64, 64, 255)
			.endVertex();
		vertexBuffer.pos(this.x, this.y, 0.0).tex(this.x / spriteSize, (this.y + this.h) / spriteSize).color(64, 64, 64, 255).endVertex();
		tessellator.draw();
		GLShim.glEnable(GLShim.GL11_GL_BLEND);
		GLShim.glBlendFunc(GLShim.GL11_GL_SRC_ALPHA, GLShim.GL11_GL_ONE_MINUS_SRC_ALPHA);
		GLShim.glDisable(GLShim.GL11_GL_ALPHA_TEST);
		GLShim.glShadeModel(GLShim.GL11_GL_SMOOTH);
		GLShim.glDisable(GLShim.GL11_GL_TEXTURE_2D);

		byte fadeWidth = 4;
		vertexBuffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
		vertexBuffer.pos(this.x, this.y + fadeWidth, 0.0).color(0, 0, 0, 0).endVertex();
		vertexBuffer.pos(this.x + this.w, this.y + fadeWidth, 0.0).color(0, 0, 0, 0).endVertex();
		vertexBuffer.pos(this.x + this.w, this.y, 0.0).color(0, 0, 0, 255).endVertex();
		vertexBuffer.pos(this.x, this.y, 0.0).color(0, 0, 0, 255).endVertex();
		tessellator.draw();
		vertexBuffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
		vertexBuffer.pos(this.x, this.y + this.h, 0.0).color(0, 0, 0, 255).endVertex();
		vertexBuffer.pos(this.x + this.w, this.y + this.h, 0.0).color(0, 0, 0, 255).endVertex();
		vertexBuffer.pos(this.x + this.w, this.y + this.h - fadeWidth, 0.0).color(0, 0, 0, 0).endVertex();
		vertexBuffer.pos(this.x, this.y + this.h - fadeWidth, 0.0).color(0, 0, 0, 0).endVertex();
		tessellator.draw();
		vertexBuffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
		vertexBuffer.pos(this.x, this.y, 0.0).color(0, 0, 0, 255).endVertex();
		vertexBuffer.pos(this.x, this.y + this.h, 0.0).color(0, 0, 0, 255).endVertex();
		vertexBuffer.pos(this.x + fadeWidth, this.y + this.h, 0.0).color(0, 0, 0, 0).endVertex();
		vertexBuffer.pos(this.x + fadeWidth, this.y, 0.0).color(0, 0, 0, 0).endVertex();
		tessellator.draw();
		vertexBuffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
		vertexBuffer.pos(this.x + this.w - fadeWidth, this.y, 0.0).color(0, 0, 0, 0).endVertex();
		vertexBuffer.pos(this.x + this.w - fadeWidth, this.y + this.h, 0.0).color(0, 0, 0, 0).endVertex();
		vertexBuffer.pos(this.x + this.w, this.y + this.h, 0.0).color(0, 0, 0, 255).endVertex();
		vertexBuffer.pos(this.x + this.w, this.y, 0.0).color(0, 0, 0, 255).endVertex();
		tessellator.draw();

		GLShim.glEnable(GLShim.GL11_GL_TEXTURE_2D);
		GLShim.glShadeModel(GLShim.GL11_GL_FLAT);
		GLShim.glEnable(GLShim.GL11_GL_ALPHA_TEST);
		GLShim.glDisable(GLShim.GL11_GL_BLEND);

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
