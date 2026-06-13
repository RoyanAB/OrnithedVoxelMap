package com.mamiyaotaru.voxelmap.gui.overridden;

import com.mamiyaotaru.voxelmap.util.GLShim;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiSlot;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;

public abstract class GuiSlotMinimap extends GuiSlot {
	protected int slotWidth = 220;

	private long lastClickedTouch = 0L;
	private boolean showSelectionBox = true;
	private boolean showTopBottomBG = true;
	private boolean showSlotBG = true;

	public GuiSlotMinimap(Minecraft minecraft, int width, int height, int top, int bottom, int slotHeight) {
		super(minecraft, width, height, top, bottom, slotHeight);
	}

	public void setShowSelectionBox(boolean showSelectionBox) {
		this.showSelectionBox = showSelectionBox;
	}

	public void setShowTopBottomBG(boolean showTopBottomBG) {
		this.showTopBottomBG = showTopBottomBG;
	}

	public void setShowSlotBG(boolean showSlotBG) {
		this.showSlotBG = showSlotBG;
	}

	public void setSlotWidth(int slotWidth) {
		this.slotWidth = slotWidth;
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.mouseX = mouseX;
		this.mouseY = mouseY;
		this.drawBackground();
		int scrollBarLeft = this.getScrollBarX();
		int scrollBarRight = scrollBarLeft + 6;
		this.bindAmountScrolled();
		GLShim.glDisable(GLShim.GL11_GL_LIGHTING);
		GLShim.glDisable(GLShim.GL11_GL_FOG);
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder vertexBuffer = tessellator.getBuffer();
		if (this.showSlotBG) {
			this.mc.getTextureManager().bindTexture(Gui.OPTIONS_BACKGROUND);
			GLShim.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			float f = 32.0F;
			vertexBuffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
			vertexBuffer.pos(this.left, this.bottom, 0.0)
				.tex(this.left / f, (this.bottom + (int) this.amountScrolled) / f)
				.color(32, 32, 32, 255)
				.endVertex();
			vertexBuffer.pos(this.right, this.bottom, 0.0)
				.tex(this.right / f, (this.bottom + (int) this.amountScrolled) / f)
				.color(32, 32, 32, 255)
				.endVertex();
			vertexBuffer.pos(this.right, this.top, 0.0)
				.tex(this.right / f, (this.top + (int) this.amountScrolled) / f)
				.color(32, 32, 32, 255)
				.endVertex();
			vertexBuffer.pos(this.left, this.top, 0.0)
				.tex(this.left / f, (this.top + (int) this.amountScrolled) / f)
				.color(32, 32, 32, 255)
				.endVertex();
			tessellator.draw();
		}

		int leftEdge = this.left + this.width / 2 - this.getListWidth() / 2 + 2;
		int topOfListYPos = this.top + 4 - (int) this.amountScrolled;
		if (this.hasListHeader) {
			this.drawListHeader(leftEdge, topOfListYPos, tessellator);
		}

		this.drawSelectionBox(leftEdge, topOfListYPos, mouseX, mouseY, partialTicks);
		GLShim.glDisable(GLShim.GL11_GL_DEPTH_TEST);
		byte topBottomFadeHeight = 4;
		if (this.showTopBottomBG) {
			this.overlayBackground(0, this.top, 255, 255);
			this.overlayBackground(this.bottom, this.height, 255, 255);
		}

		GLShim.glEnable(GLShim.GL11_GL_BLEND);
		OpenGlHelper.glBlendFunc(770, 771, 0, 1);
		GLShim.glDisable(GLShim.GL11_GL_ALPHA_TEST);
		GLShim.glShadeModel(GLShim.GL11_GL_SMOOTH);
		GLShim.glDisable(GLShim.GL11_GL_TEXTURE_2D);
		if (this.showTopBottomBG) {
			vertexBuffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
			vertexBuffer.pos(this.left, this.top + topBottomFadeHeight, 0.0).tex(0.0, 1.0).color(0, 0, 0, 0).endVertex();
			vertexBuffer.pos(this.right, this.top + topBottomFadeHeight, 0.0).tex(1.0, 1.0).color(0, 0, 0, 0).endVertex();
			vertexBuffer.pos(this.right, this.top, 0.0).tex(1.0, 0.0).color(0, 0, 0, 255).endVertex();
			vertexBuffer.pos(this.left, this.top, 0.0).tex(0.0, 0.0).color(0, 0, 0, 255).endVertex();
			tessellator.draw();
			vertexBuffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
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

			vertexBuffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
			vertexBuffer.pos(scrollBarLeft, this.bottom, 0.0).tex(0.0, 1.0).color(0, 0, 0, 255).endVertex();
			vertexBuffer.pos(scrollBarRight, this.bottom, 0.0).tex(1.0, 1.0).color(0, 0, 0, 255).endVertex();
			vertexBuffer.pos(scrollBarRight, this.top, 0.0).tex(1.0, 0.0).color(0, 0, 0, 255).endVertex();
			vertexBuffer.pos(scrollBarLeft, this.top, 0.0).tex(0.0, 0.0).color(0, 0, 0, 255).endVertex();
			tessellator.draw();
			vertexBuffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
			vertexBuffer.pos(scrollBarLeft, l1 + k1, 0.0).tex(0.0, 1.0).color(128, 128, 128, 255).endVertex();
			vertexBuffer.pos(scrollBarRight, l1 + k1, 0.0).tex(1.0, 1.0).color(128, 128, 128, 255).endVertex();
			vertexBuffer.pos(scrollBarRight, l1, 0.0).tex(1.0, 0.0).color(128, 128, 128, 255).endVertex();
			vertexBuffer.pos(scrollBarLeft, l1, 0.0).tex(0.0, 0.0).color(128, 128, 128, 255).endVertex();
			tessellator.draw();
			vertexBuffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
			vertexBuffer.pos(scrollBarLeft, l1 + k1 - 1, 0.0).tex(0.0, 1.0).color(192, 192, 192, 255).endVertex();
			vertexBuffer.pos(scrollBarRight - 1, l1 + k1 - 1, 0.0).tex(1.0, 1.0).color(192, 192, 192, 255).endVertex();
			vertexBuffer.pos(scrollBarRight - 1, l1, 0.0).tex(1.0, 0.0).color(192, 192, 192, 255).endVertex();
			vertexBuffer.pos(scrollBarLeft, l1, 0.0).tex(0.0, 0.0).color(192, 192, 192, 255).endVertex();
			tessellator.draw();
		}

		this.renderDecorations(mouseX, mouseY);
		GLShim.glEnable(GLShim.GL11_GL_TEXTURE_2D);
		GLShim.glShadeModel(GLShim.GL11_GL_FLAT);
		GLShim.glEnable(GLShim.GL11_GL_ALPHA_TEST);
		GLShim.glDisable(GLShim.GL11_GL_BLEND);
	}

	@Override
	public int getListWidth() {
		return this.slotWidth;
	}

	@Override
	protected void drawSelectionBox(int leftEdge, int topOfListYPos, int mouseX, int mouseY, float partialTicks) {
		int numberOfSlots = this.getSize();
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder vertexBuffer = tessellator.getBuffer();

		for (int slotIndexIterator = 0; slotIndexIterator < numberOfSlots; slotIndexIterator++) {
			int slotYPos = topOfListYPos + slotIndexIterator * this.slotHeight + this.headerPadding;
			int topFudge = this.showTopBottomBG ? this.slotHeight - 4 : 0;
			int bottomFudge = this.showTopBottomBG ? 0 : this.slotHeight - 4;
			if (slotYPos + bottomFudge <= this.bottom && slotYPos + topFudge >= this.top) {
				if (this.showSelectionBox && this.isSelected(slotIndexIterator)) {
					int i1 = this.left + (this.width / 2 - this.getListWidth() / 2);
					int j1 = this.left + this.width / 2 + this.getListWidth() / 2;
					GLShim.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
					GLShim.glDisable(GLShim.GL11_GL_TEXTURE_2D);
					vertexBuffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
					vertexBuffer.pos(i1, slotYPos + topFudge + 2, 0.0).tex(0.0, 1.0).color(128, 128, 128, 255).endVertex();
					vertexBuffer.pos(j1, slotYPos + topFudge + 2, 0.0).tex(1.0, 1.0).color(128, 128, 128, 255).endVertex();
					vertexBuffer.pos(j1, slotYPos - 2, 0.0).tex(1.0, 0.0).color(128, 128, 128, 255).endVertex();
					vertexBuffer.pos(i1, slotYPos - 2, 0.0).tex(0.0, 0.0).color(128, 128, 128, 255).endVertex();
					vertexBuffer.pos(i1 + 1, slotYPos + topFudge + 1, 0.0).tex(0.0, 1.0).color(0, 0, 0, 255).endVertex();
					vertexBuffer.pos(j1 - 1, slotYPos + topFudge + 1, 0.0).tex(1.0, 1.0).color(0, 0, 0, 255).endVertex();
					vertexBuffer.pos(j1 - 1, slotYPos - 1, 0.0).tex(1.0, 0.0).color(0, 0, 0, 255).endVertex();
					vertexBuffer.pos(i1 + 1, slotYPos - 1, 0.0).tex(0.0, 0.0).color(0, 0, 0, 255).endVertex();
					tessellator.draw();
					GLShim.glEnable(GLShim.GL11_GL_TEXTURE_2D);
				}

				this.drawSlot(slotIndexIterator, leftEdge, slotYPos, topFudge, mouseX, mouseY, partialTicks);
			}
		}
	}

	public boolean mouseClicked(int mouseX, int mouseY, int mouseEvent) {
		if (this.isMouseYWithinSlotBounds(mouseY)) {
			int i = this.getSlotIndexFromScreenCoords(mouseX, mouseY);
			if (i >= 0) {
				if (i == this.selectedElement && Minecraft.getSystemTime() - this.lastClickedTouch < 250L) {
					this.setEnabled(false);
					this.elementClicked(i, true, this.mouseX, this.mouseY);
					return true;
				}

				this.lastClickedTouch = Minecraft.getSystemTime();
			}
		}

		return false;
	}

	public boolean mouseReleased(int x, int y, int mouseEvent) {
		this.setEnabled(true);
		return false;
	}

	@Override
	protected int getScrollBarX() {
		return this.right - 6;
	}
}
