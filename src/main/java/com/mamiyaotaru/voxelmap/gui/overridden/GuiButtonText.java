package com.mamiyaotaru.voxelmap.gui.overridden;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;

public class GuiButtonText extends GuiButton {
    private boolean editing = false;
    private final GuiTextField textField;

    public GuiButtonText(int buttonId, FontRenderer fontRenderer, int x, int y, String buttonText) {
        this(buttonId, fontRenderer, x, y, 200, 20, buttonText);
    }

    public GuiButtonText(int buttonId, FontRenderer fontRenderer, int x, int y, int widthIn, int heightIn, String buttonText) {
        super(buttonId, x, y, widthIn, heightIn, buttonText);
        this.textField = new GuiTextField(buttonId, fontRenderer, x + 1, y + 1, widthIn - 2, heightIn - 2);
    }

    public void render(int mouseX, int mouseY, float partialTicks) {
        if (!this.editing) {
            super.render(mouseX, mouseY, partialTicks);
        } else {
            this.textField.drawTextField(mouseX, mouseY, partialTicks);
        }
    }

    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        boolean pressed = super.mouseClicked(mouseX, mouseY, mouseButton);
        this.setEditing(pressed);
        return pressed;
    }

    private void setEditing(boolean editing) {
        this.editing = editing;
        this.textField.setFocused(editing);
    }

    public boolean keyPressed(int keysm, int scancode, int b) {
        boolean ok = false;
        if ((keysm == 257 || keysm == 335) && this.textField.isFocused()) {
            this.setEditing(false);
        } else {
            ok = this.textField.keyPressed(keysm, scancode, b);
        }

        return ok;
    }

    public boolean charTyped(char character, int keycode) {
        boolean ok = false;
        if (character == '\r' && this.textField.isFocused()) {
            this.setEditing(false);
        } else {
            ok = this.textField.charTyped(character, keycode);
        }

        return ok;
    }

    public void tick() {
        this.textField.tick();
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
