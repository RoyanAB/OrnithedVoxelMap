package com.mamiyaotaru.voxelmap.gui.overridden;

import net.minecraft.client.gui.widget.ButtonWidget;

public class GuiOptionButtonMinimap extends ButtonWidget {
    private final EnumOptionsMinimap enumOptions;

    public GuiOptionButtonMinimap(int buttonId, int x, int y, String buttonText, PressAction press) {
        this(x, y, null, buttonText, press);
    }

    public GuiOptionButtonMinimap(int x, int y, EnumOptionsMinimap par4EnumOptions, String buttonText, PressAction press) {
        super(x, y, 150, 20, buttonText, press);
        this.enumOptions = par4EnumOptions;
    }

    public EnumOptionsMinimap returnEnumOptions() {
        return this.enumOptions;
    }
}
