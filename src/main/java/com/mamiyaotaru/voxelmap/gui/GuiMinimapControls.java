package com.mamiyaotaru.voxelmap.gui;

import com.mamiyaotaru.voxelmap.MapSettingsManager;
import com.mamiyaotaru.voxelmap.gui.overridden.GuiOptionButtonMinimap;
import com.mamiyaotaru.voxelmap.gui.overridden.GuiScreenMinimap;
import com.mamiyaotaru.voxelmap.interfaces.IVoxelMap;
import com.mamiyaotaru.voxelmap.util.I18nUtils;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.settings.KeyBinding;

public class GuiMinimapControls extends GuiScreenMinimap {
	private final GuiScreen parentScreen;
	private final MapSettingsManager options;

	private int buttonId = -1;

	protected String screenTitle = "Controls";

	public GuiMinimapControls(GuiScreen guiScreen, IVoxelMap master) {
		this.parentScreen = guiScreen;
		this.options = master.getMapOptions();
	}

	private int getLeftBorder() {
		return this.getWidth() / 2 - 155;
	}

	@Override
	public void initGui() {
		int leftBorder = this.getLeftBorder();

		for (int keyIndex = 0; keyIndex < this.options.keyBindings.length; keyIndex++) {
			this.getButtonList()
				.add(
					new GuiOptionButtonMinimap(
						keyIndex, leftBorder + keyIndex % 2 * 160, this.getHeight() / 6 + 24 * (keyIndex >> 1), 70, 20, this.options.getOptionDisplayString(keyIndex)
					)
				);
		}

		this.getButtonList().add(new GuiButton(200, this.getWidth() / 2 - 100, this.getHeight() / 6 + 168, I18nUtils.getString("gui.done")));
		this.screenTitle = I18nUtils.getString("controls.minimap.title");
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		for (int buttonListIndex = 0; buttonListIndex < this.options.keyBindings.length; buttonListIndex++) {
			this.getButtonList().get(buttonListIndex).displayString = this.options.getOptionDisplayString(buttonListIndex);
		}

		if (button.id == 200) {
			this.getMinecraft().displayGuiScreen(this.parentScreen);
		} else {
			this.buttonId = button.id;
			button.displayString = "> " + this.options.getOptionDisplayString(button.id) + " <";
		}
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
		if (this.buttonId >= 0) {
			this.options.setKeyBinding(this.options.keyBindings[this.buttonId], -100 + mouseButton);
			this.getButtonList().get(this.buttonId).displayString = this.options.getOptionDisplayString(this.buttonId);
			this.buttonId = -1;
			KeyBinding.resetKeyBindingArrayAndHash();
		} else {
			super.mouseClicked(mouseX, mouseY, mouseButton);
		}
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) {
		if (this.buttonId >= 0) {
			if (keyCode == 1) {
				if (this.options.keyBindings[this.buttonId] != this.options.keyBindMenu) {
					this.options.setKeyBinding(this.options.keyBindings[this.buttonId], 0);
				}
			} else if (keyCode != 0) {
				this.options.setKeyBinding(this.options.keyBindings[this.buttonId], keyCode);
			} else if (typedChar > 0) {
				this.options.setKeyBinding(this.options.keyBindings[this.buttonId], typedChar + 256);
			}

			this.getButtonList().get(this.buttonId).displayString = this.options.getOptionDisplayString(this.buttonId);
			this.buttonId = -1;
			KeyBinding.resetKeyBindingArrayAndHash();
		} else {
			super.keyTyped(typedChar, keyCode);
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawMap();
		this.drawDefaultBackground();
		this.drawCenteredString(this.getFontRenderer(), this.screenTitle, this.getWidth() / 2, 20, 16777215);
		int leftBorder = this.getLeftBorder();

		for (int keyIndex = 0; keyIndex < this.options.keyBindings.length; keyIndex++) {
			boolean hasConflict = false;
			int checkIndex = 0;

			while (true) {
				if (checkIndex < this.options.keyBindings.length) {
					if (this.options.keyBindings[keyIndex].getKeyCode() == 0
						|| (checkIndex == keyIndex || this.options.keyBindings[keyIndex].getKeyCode() != this.options.keyBindings[checkIndex].getKeyCode())
						&& (
						this.options.keyBindings[keyIndex].getKeyCode() != this.options.game.gameSettings.keyBindings[checkIndex].getKeyCode()
							|| this.options.keyBindings[keyIndex].equals(this.options.game.gameSettings.keyBindings[checkIndex])
					)) {
						checkIndex++;
						continue;
					}

					hasConflict = true;
				}

				if (checkIndex >= this.options.game.gameSettings.keyBindings.length) {
					break;
				}

				if (this.options.keyBindings[keyIndex].getKeyCode() != 0
					&& this.options.keyBindings[keyIndex].getKeyCode() == this.options.game.gameSettings.keyBindings[checkIndex].getKeyCode()
					&& !this.options.keyBindings[keyIndex].equals(this.options.game.gameSettings.keyBindings[checkIndex])) {
					hasConflict = true;
					break;
				}

				checkIndex++;
			}

			if (this.buttonId == keyIndex) {
				this.getButtonList().get(keyIndex).displayString = "§f> §e??? §f<";
			} else if (hasConflict) {
				this.getButtonList().get(keyIndex).displayString = "§c" + this.options.getOptionDisplayString(keyIndex);
			} else {
				this.getButtonList().get(keyIndex).displayString = this.options.getOptionDisplayString(keyIndex);
			}

			this.drawString(
				this.getFontRenderer(),
				this.options.getKeyBindingDescription(keyIndex),
				leftBorder + keyIndex % 2 * 160 + 70 + 6,
				this.getHeight() / 6 + 24 * (keyIndex >> 1) + 7,
				-1
			);
		}

		this.drawCenteredString(
			this.getFontRenderer(), I18nUtils.getString("controls.minimap.unbind1"), this.getWidth() / 2, this.getHeight() / 6 + 115, 16777215
		);
		this.drawCenteredString(
			this.getFontRenderer(), I18nUtils.getString("controls.minimap.unbind2"), this.getWidth() / 2, this.getHeight() / 6 + 129, 16777215
		);
		super.drawScreen(mouseX, mouseY, partialTicks);
	}
}
