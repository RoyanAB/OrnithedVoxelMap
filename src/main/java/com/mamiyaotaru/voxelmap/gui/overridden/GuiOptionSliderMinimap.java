package com.mamiyaotaru.voxelmap.gui.overridden;

import com.mamiyaotaru.voxelmap.interfaces.ISettingsManager;
import com.mamiyaotaru.voxelmap.util.GLShim;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.math.MathHelper;

public class GuiOptionSliderMinimap extends GuiButton {
    public float sliderValue;
    public boolean dragging;
    private final ISettingsManager options;
    private EnumOptionsMinimap option = null;

    public GuiOptionSliderMinimap(int buttonId, int x, int y, EnumOptionsMinimap optionIn, float sliderValue, ISettingsManager options) {
        super(buttonId, x, y, 150, 20, "");
        this.options = options;
        this.option = optionIn;
        this.sliderValue = sliderValue;
        this.displayString = this.options.getKeyText(optionIn);
    }

    protected int getHoverState(boolean mouseOver) {
        return 0;
    }

    protected void renderBg(Minecraft minecraft, int mouseX, int mouseY) {
        if (this.visible) {
            if (this.dragging) {
                this.sliderValue = (float) (mouseX - (this.x + 4)) / (this.width - 8);
                this.sliderValue = MathHelper.clamp(this.sliderValue, 0.0F, 1.0F);
            }

            if (this.dragging) {
                this.options.setOptionFloatValue(this.option, this.sliderValue);
                this.displayString = this.options.getKeyText(this.option);
            }

            minecraft.getTextureManager().bindTexture(BUTTON_TEXTURES);
            GLShim.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            this.drawTexturedModalRect(this.x + (int) ((double) this.sliderValue * (this.width - 8)), this.y, 0, 66, 4, 20);
            this.drawTexturedModalRect(this.x + (int) ((double) this.sliderValue * (this.width - 8)) + 4, this.y, 196, 66, 4, 20);
        }
    }

    public final void onClick(double mouseX, double mouseY) {
        this.sliderValue = (float) ((mouseX - (this.x + 4)) / (this.width - 8));
        this.sliderValue = MathHelper.clamp(this.sliderValue, 0.0F, 1.0F);
        this.options.setOptionFloatValue(this.option, this.sliderValue);
        this.displayString = this.options.getKeyText(this.option);
        this.dragging = true;
    }

    public void onRelease(double mouseX, double mouseY) {
        this.dragging = false;
    }

    public void resetText() {
        this.displayString = this.options.getKeyText(this.option);
    }

    public EnumOptionsMinimap returnEnumOptions() {
        return this.option;
    }
}
