package com.mamiyaotaru.voxelmap.gui.overridden;

import com.mamiyaotaru.voxelmap.util.GLShim;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.OpenGlHelper;

public class PopupGuiButton extends GuiButton {
	IPopupGuiScreen parentScreen;

	public PopupGuiButton(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText, IPopupGuiScreen parentScreen) {
		super(buttonId, x, y, widthIn, heightIn, buttonText);
		this.parentScreen = parentScreen;
	}

	@Override
	public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
		if (this.visible) {
			FontRenderer fontrenderer = mc.fontRenderer;
			mc.getTextureManager().bindTexture(BUTTON_TEXTURES);
			GLShim.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			this.hovered = !this.parentScreen.overPopup(mouseX, mouseY)
				&& mouseX >= this.x
				&& mouseY >= this.y
				&& mouseX < this.x + this.width
				&& mouseY < this.y + this.height;
			int i = this.getHoverState(this.hovered);
			GLShim.glEnable(GLShim.GL11_GL_BLEND);
			OpenGlHelper.glBlendFunc(770, 771, 1, 0);
			GLShim.glBlendFunc(GLShim.GL11_GL_SRC_ALPHA, GLShim.GL11_GL_ONE_MINUS_SRC_ALPHA);
			this.drawTexturedModalRect(this.x, this.y, 0, 46 + i * 20, this.width / 2, this.height);
			this.drawTexturedModalRect(
				this.x + this.width / 2,
				this.y,
				200 - this.width / 2,
				46 + i * 20,
				this.width / 2,
				this.height
			);

			this.mouseDragged(mc, mouseX, mouseY);

			int color = 14737632;
			if (!this.enabled) {
				color = 10526880;
			} else if (this.hovered) {
				color = 16777120;
			}

			this.drawCenteredString(
				fontrenderer, this.displayString, this.x + this.width / 2, this.y + (this.height - 8) / 2, color
			);
		}
	}
}
