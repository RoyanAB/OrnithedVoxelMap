package com.mamiyaotaru.voxelmap.gui.overridden;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;

@SuppressWarnings("unused")
public class GuiButtonText extends GuiButton {
	private final GuiTextField textField;

	private boolean editing = false;

	public GuiButtonText(int buttonId, FontRenderer fontRenderer, int x, int y, String buttonText) {
		this(buttonId, fontRenderer, x, y, 200, 20, buttonText);
	}

	public GuiButtonText(int buttonId, FontRenderer fontRenderer, int x, int y, int widthIn, int heightIn, String buttonText) {
		super(buttonId, x, y, widthIn, heightIn, buttonText);
		this.textField = new GuiTextField(buttonId, fontRenderer, x, y, widthIn, heightIn);
	}

	public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
		if (!this.editing) {
			super.drawButton(mc, mouseX, mouseY, partialTicks);
		} else {
			this.textField.drawTextBox();
		}
	}

	public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
		boolean pressed = super.mousePressed(mc, mouseX, mouseY);
		this.setEditing(pressed);
		return pressed;
	}

	private void setEditing(boolean editing) {
		this.editing = editing;
		this.textField.setFocused(editing);
	}

	public void textboxKeyTyped(char character, int keycode) {
		this.textField.textboxKeyTyped(character, keycode);
		if (character == '\r' && this.textField.isFocused()) {
			this.setEditing(false);
		}
	}

	public void updateCursorCounter() {
		this.textField.updateCursorCounter();
	}

	public String getText() {
		return this.textField.getText();
	}

	public void setText(String textIn) {
		this.textField.setText(textIn);
	}

	public boolean isFocused() {
		return this.textField.isFocused();
	}

	public void setFocused(boolean isFocusedIn) {
		this.textField.setFocused(isFocusedIn);
	}
}
