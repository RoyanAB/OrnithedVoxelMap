package com.mamiyaotaru.voxelmap.gui.overridden;

import net.minecraft.client.gui.widget.ButtonWidget;

public class PopupGuiButton extends ButtonWidget {
    IPopupGuiScreen parentScreen;

    public PopupGuiButton(int buttonId, int x, int y, String buttonText, PressAction onPress, IPopupGuiScreen parentScreen) {
        this(x, y, 200, 20, buttonText, onPress, parentScreen);
    }

    public PopupGuiButton(int x, int y, int widthIn, int heightIn, String buttonText, PressAction pressAction, IPopupGuiScreen parentScreen) {
        super(x, y, widthIn, heightIn, buttonText, pressAction);
        this.parentScreen = parentScreen;
    }

    public void render(int mouseX, int mouseY, float partialTicks) {
        boolean canHover = !this.parentScreen.overPopup(mouseX, mouseY);
        if (!canHover) {
            mouseX = 0;
            mouseY = 0;
        }

        super.render(mouseX, mouseY, partialTicks);
    }
}
