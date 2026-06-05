package com.mamiyaotaru.voxelmap.gui.overridden;

import com.mamiyaotaru.voxelmap.util.GLShim;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

@SuppressWarnings("unused")
public abstract class GuiSlotMinimap {
	protected final int slotHeight;
	private final Minecraft mc;

	protected int width;
	protected int top;
	protected int bottom;
	protected int right;
	protected int left;

	protected int slotWidth = 220;
	protected int mouseX;
	protected int mouseY;
	protected boolean centerListVertically = true;
	protected int headerPadding;
	private int height;
	private int scrollUpButtonID;
	private int scrollDownButtonID;
	private float initialClickY = -2.0F;
	private float scrollMultiplier;
	private float amountScrolled;
	private int selectedElement = -1;
	private long lastClickedTouch = 0L;

	private boolean showSelectionBox = true;
	private boolean showTopBottomBG = true;
	private boolean showSlotBG = true;
	private boolean hasListHeader;
	private boolean enabled = true;

	public GuiSlotMinimap(Minecraft par1Minecraft, int width, int height, int top, int bottom, int slotHeight) {
		this.mc = par1Minecraft;
		this.width = width;
		this.height = height;
		this.top = top;
		this.bottom = bottom;
		this.slotHeight = slotHeight;
		this.left = 0;
		this.right = width;
	}

	public void setDimensions(int width, int height, int top, int bottom) {
		this.width = width;
		this.height = height;
		this.top = top;
		this.bottom = bottom;
		this.left = 0;
		this.right = width;
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

	protected void setHasListHeader(boolean hasListHeader, int headerPadding) {
		this.hasListHeader = hasListHeader;
		this.headerPadding = headerPadding;
		if (!hasListHeader) {
			this.headerPadding = 0;
		}
	}

	protected abstract int getSize();

	protected abstract void elementClicked(int slotIndex, boolean isSelected, int mouseX, int mouseY);

	protected abstract boolean isSelected(int slotIndex);

	protected int getContentHeight() {
		return this.getSize() * this.slotHeight + this.headerPadding;
	}

	protected abstract void drawBackground();

	protected void updateItemPos(int entryID, int insideLeft, int yPos, float partialTicks) {
	}

	protected abstract void drawSlot(int slotIndex, int leftEdge, int slotYPos, int topFudge, int mouseX, int mouseY, float partialTicks);

	protected void drawListHeader(int insideLeft, int insideTop, Tessellator tessellatorIn) {
	}

	protected void clickedHeader(int mouseX, int mouseY) {
	}

	protected void renderDecorations(int mouseXIn, int mouseYIn) {
	}

	public int getSlotIndexFromScreenCoords(int x, int y) {
		int slotLeft = this.left + this.width / 2 - this.getListWidth() / 2;
		int slotRight = this.left + this.width / 2 + this.getListWidth() / 2;
		int yInSlotList = y - this.top - this.headerPadding + (int) this.amountScrolled - 4;
		int slotIndex = yInSlotList / this.slotHeight;
		return x < this.getScrollBarX() && x >= slotLeft && x <= slotRight && slotIndex >= 0 && yInSlotList >= 0 && slotIndex < this.getSize() ? slotIndex : -1;
	}

	public void registerScrollButtons(int scrollUpButtonID, int scrollDownButtonID) {
		this.scrollUpButtonID = scrollUpButtonID;
		this.scrollDownButtonID = scrollDownButtonID;
	}

	protected void bindAmountScrolled() {
		this.amountScrolled = MathHelper.clamp(this.amountScrolled, 0.0F, this.getMaxScroll());
	}

	public int getMaxScroll() {
		return Math.max(0, this.getContentHeight() - (this.bottom - this.top - 4));
	}

	public int getAmountScrolled() {
		return (int) this.amountScrolled;
	}

	public boolean isMouseYWithinSlotBounds(int mouseY) {
		return mouseY >= this.top && mouseY <= this.bottom;
	}

	public boolean isMouseXYWithinSlotBounds(int mouseX, int mouseY) {
		return mouseX > this.left && mouseX < this.right && mouseY >= this.top && mouseY <= this.bottom;
	}

	public void scrollBy(int scrollBy) {
		this.amountScrolled += scrollBy;
		this.bindAmountScrolled();
		this.initialClickY = -2.0F;
	}

	public void actionPerformed(GuiButton button) {
		if (button.enabled) {
			if (button.id == this.scrollUpButtonID) {
				this.amountScrolled = this.amountScrolled - this.slotHeight * 2F / 3F;
				this.initialClickY = -2.0F;
				this.bindAmountScrolled();
			} else if (button.id == this.scrollDownButtonID) {
				this.amountScrolled = this.amountScrolled + this.slotHeight * 2F / 3F;
				this.initialClickY = -2.0F;
				this.bindAmountScrolled();
			}
		}
	}

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

	public void handleMouseInput() {
		if (this.isMouseYWithinSlotBounds(this.mouseY)) {
			if (Mouse.getEventButton() == 0 && Mouse.getEventButtonState() && this.mouseY >= this.top && this.mouseY <= this.bottom) {
				int leftEdge = this.left + this.width / 2 - this.getListWidth() / 2;
				int rightEdge = this.left + this.width / 2 + this.getListWidth() / 2;
				int mouseYInList = this.mouseY - this.top - this.headerPadding + (int) this.amountScrolled - 4;
				int slotIndex = mouseYInList / this.slotHeight;
				if (slotIndex < this.getSize() && this.mouseX >= leftEdge && this.mouseX <= rightEdge && slotIndex >= 0 && mouseYInList >= 0) {
					this.elementClicked(slotIndex, false, this.mouseX, this.mouseY);
					this.selectedElement = slotIndex;
				} else if (this.mouseX >= leftEdge && this.mouseX <= rightEdge && mouseYInList < 0) {
					this.clickedHeader(this.mouseX - leftEdge, this.mouseY - this.top + (int) this.amountScrolled - 4);
				}
			}

			if (!Mouse.isButtonDown(0) || !this.getEnabled()) {
				this.initialClickY = -1.0F;
			} else if (this.initialClickY == -1.0F) {
				boolean flag1 = true;
				if (this.mouseY >= this.top && this.mouseY <= this.bottom) {
					int leftEdge = this.left + this.width / 2 - this.getListWidth() / 2;
					int rightEdge = this.left + this.width / 2 + this.getListWidth() / 2;
					int mouseYInList = this.mouseY - this.top - this.headerPadding + (int) this.amountScrolled - 4;
					int slotIndex = mouseYInList / this.slotHeight;
					if (slotIndex < this.getSize() && this.mouseX >= leftEdge && this.mouseX <= rightEdge && slotIndex >= 0 && mouseYInList >= 0) {
						this.selectedElement = slotIndex;
						long lastClicked = Minecraft.getSystemTime();
					} else if (this.mouseX >= leftEdge && this.mouseX <= rightEdge && mouseYInList < 0) {
						this.clickedHeader(this.mouseX - leftEdge, this.mouseY - this.top + (int) this.amountScrolled - 4);
						flag1 = false;
					}

					int scrollBarLeft = this.getScrollBarX();
					int scrollBarRight = scrollBarLeft + 6;
					if (this.mouseX >= scrollBarLeft && this.mouseX <= scrollBarRight) {
						this.scrollMultiplier = -1.0F;
						int k1 = this.getMaxScroll();
						if (k1 < 1) {
							k1 = 1;
						}

						int l1 = (int) ((float) ((this.bottom - this.top) * (this.bottom - this.top)) / this.getContentHeight());
						l1 = MathHelper.clamp(l1, 32, this.bottom - this.top - 8);
						this.scrollMultiplier = this.scrollMultiplier / ((float) (this.bottom - this.top - l1) / k1);
					} else {
						this.scrollMultiplier = 1.0F;
					}

					if (flag1) {
						this.initialClickY = this.mouseY;
					} else {
						this.initialClickY = -2.0F;
					}
				} else {
					this.initialClickY = -2.0F;
				}
			} else if (this.initialClickY >= 0.0F) {
				this.amountScrolled = this.amountScrolled - (this.mouseY - this.initialClickY) * this.scrollMultiplier;
				this.initialClickY = this.mouseY;
			}

			int mouseRoll = Mouse.getEventDWheel();
			if (mouseRoll != 0) {
				if (mouseRoll > 0)
					mouseRoll = -1;
				else
					mouseRoll = 1;

				this.amountScrolled = this.amountScrolled + mouseRoll * this.slotHeight / 2F;
			}
		}
	}

	public boolean mouseClicked(int mouseX, int mouseY, int mouseEvent) {
		if (this.isMouseYWithinSlotBounds(mouseY)) {
			int i = this.getSlotIndexFromScreenCoords(mouseX, mouseY);
			if (i >= 0) {
				int j = this.left + this.width / 2 - this.getListWidth() / 2 + 2;
				int k = this.top + 4 - this.getAmountScrolled() + i * this.slotHeight + this.headerPadding;
				int l = mouseX - j;
				int i1 = mouseY - k;
				boolean flag = (i == this.selectedElement && Minecraft.getSystemTime() - this.lastClickedTouch < 250L);
				if (flag) {
					this.setEnabled(false);
					this.elementClicked(i, flag, this.mouseX, this.mouseY);
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

	public boolean getEnabled() {
		return this.enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public int getListWidth() {
		return this.slotWidth;
	}

	public void setSlotWidth(int slotWidth) {
		this.slotWidth = slotWidth;
	}

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

	protected int getScrollBarX() {
		return this.right - 6;
	}

	protected void overlayBackground(int startY, int endY, int startAlpha, int endAlpha) {
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder vertexBuffer = tessellator.getBuffer();
		this.mc.getTextureManager().bindTexture(Gui.OPTIONS_BACKGROUND);
		GLShim.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		vertexBuffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
		vertexBuffer.pos(this.left, endY, 0.0).tex(0.0, endY / 32.0F).color(64, 64, 64, endAlpha).endVertex();
		vertexBuffer.pos(this.left + this.width, endY, 0.0)
			.tex(this.width / 32.0F, endY / 32.0F)
			.color(64, 64, 64, endAlpha)
			.endVertex();
		vertexBuffer.pos(this.left + this.width, startY, 0.0)
			.tex(this.width / 32.0F, startY / 32.0F)
			.color(64, 64, 64, startAlpha)
			.endVertex();
		vertexBuffer.pos(this.left, startY, 0.0).tex(0.0, startY / 32.0F).color(64, 64, 64, startAlpha).endVertex();
		tessellator.draw();
	}

	public void setSlotXBoundsFromLeft(int left) {
		this.left = left;
		this.right = left + this.width;
	}

	public int getSlotHeight() {
		return this.slotHeight;
	}
}
