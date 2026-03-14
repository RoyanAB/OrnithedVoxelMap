package com.mamiyaotaru.voxelmap.gui.overridden;

import com.mamiyaotaru.voxelmap.util.GLShim;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.OpenGlHelper;

public class PopupGuiButton extends GuiButton {
	IPopupGuiScreen parentScreen;

	public PopupGuiButton(int buttonId, int x, int y, String buttonText, IPopupGuiScreen parentScreen) {
		this(buttonId, x, y, 200, 20, buttonText, parentScreen);
	}

	public PopupGuiButton(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText, IPopupGuiScreen parentScreen) {
		super(buttonId, x, y, widthIn, heightIn, buttonText);
		this.parentScreen = parentScreen;
	}

	public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
		if (this.visible) {
			FontRenderer fontrenderer = mc.fontRendererObj;
			mc.getTextureManager().bindTexture(BUTTON_TEXTURES);
			GLShim.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			this.hovered = !this.parentScreen.overPopup(mouseX, mouseY)
				&& mouseX >= this.xPosition
				&& mouseY >= this.yPosition
				&& mouseX < this.xPosition + this.width
				&& mouseY < this.yPosition + this.height;
			int i = this.getHoverState(this.hovered);
			GLShim.glEnable(3042);
			OpenGlHelper.glBlendFunc(770, 771, 1, 0);
			GLShim.glBlendFunc(770, 771);
			this.drawTexturedModalRect(this.xPosition, this.yPosition, 0, 46 + i * 20, this.width / 2, this.height);
			this.drawTexturedModalRect(
				this.xPosition + this.width / 2,
				this.yPosition,
				200 - this.width / 2,
				46 + i * 20,
				this.width / 2,
				this.height
			);
			this.mouseDragged(mc, mouseX, mouseY);
			int j = 14737632;
			if (!this.enabled) {
				j = 10526880;
			} else if (this.hovered) {
				j = 16777120;
			}

			this.drawCenteredString(
				fontrenderer, this.displayString, this.xPosition + this.width / 2, this.yPosition + (this.height - 8) / 2, j
			);
		}
	}
}
