package com.mamiyaotaru.voxelmap.gui.overridden;

import com.mamiyaotaru.voxelmap.interfaces.ISettingsManager;
import com.mamiyaotaru.voxelmap.util.GLShim;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

public class GuiOptionSliderMinimap extends GuiButton {
	public float sliderValue;
	public boolean dragging = false;
	private final ISettingsManager options;
	private EnumOptionsMinimap enumOptions = null;

	public GuiOptionSliderMinimap(int buttonId, int x, int y, EnumOptionsMinimap optionIn, float sliderValue, ISettingsManager options) {
		super(buttonId, x, y, 150, 20, "");
		this.options = options;
		this.enumOptions = optionIn;
		this.sliderValue = sliderValue;
		this.displayString = this.options.getKeyText(optionIn);
	}

	public int getHoverState(boolean mouseOver) {
		return 0;
	}

	protected void mouseDragged(Minecraft mc, int mouseX, int mouseY) {
		if (this.visible) {
			if (this.dragging) {
				this.sliderValue = (float) (mouseX - (this.x + 4)) / (this.width - 8);
				if (this.sliderValue < 0.0F) {
					this.sliderValue = 0.0F;
				}

				if (this.sliderValue > 1.0F) {
					this.sliderValue = 1.0F;
				}

				this.options.setOptionFloatValue(this.enumOptions, this.sliderValue);
				this.displayString = this.options.getKeyText(this.enumOptions);
			}

			GLShim.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			this.drawTexturedModalRect(this.x + (int) (this.sliderValue * (this.width - 8)), this.y, 0, 66, 4, 20);
			this.drawTexturedModalRect(this.x + (int) (this.sliderValue * (this.width - 8)) + 4, this.y, 196, 66, 4, 20);
		}
	}

	public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
		if (super.mousePressed(mc, mouseX, mouseY)) {
			this.sliderValue = (float) (mouseX - (this.x + 4)) / (this.width - 8);
			if (this.sliderValue < 0.0F) {
				this.sliderValue = 0.0F;
			}

			if (this.sliderValue > 1.0F) {
				this.sliderValue = 1.0F;
			}

			this.options.setOptionFloatValue(this.enumOptions, this.sliderValue);
			this.displayString = this.options.getKeyText(this.enumOptions);
			this.dragging = true;
			return true;
		} else {
			return false;
		}
	}

	public void mouseReleased(int mouseX, int mouseY) {
		this.dragging = false;
	}

	public void resetText() {
		this.displayString = this.options.getKeyText(this.enumOptions);
	}

	public EnumOptionsMinimap returnEnumOptions() {
		return this.enumOptions;
	}
}
