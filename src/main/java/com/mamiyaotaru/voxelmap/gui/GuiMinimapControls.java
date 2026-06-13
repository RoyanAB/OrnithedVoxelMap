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

	public void initGui() {
		int leftBorder = this.getLeftBorder();

		for (int i = 0; i < this.options.keyBindings.length; i++) {
			this.getButtonList()
				.add(
					new GuiOptionButtonMinimap(
						i, leftBorder + i % 2 * 160, this.getHeight() / 6 + 24 * (i >> 1), 70, 20, this.options.getOptionDisplayString(i)
					)
				);
		}

		this.getButtonList().add(new GuiButton(200, this.getWidth() / 2 - 100, this.getHeight() / 6 + 168, I18nUtils.getString("gui.done")));
		this.screenTitle = I18nUtils.getString("controls.minimap.title");
	}

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

	protected void keyTyped(char character, int keyCode) {
		if (this.buttonId >= 0) {
			if (keyCode == 1) {
				if (this.options.keyBindings[this.buttonId] != this.options.keyBindMenu) {
					this.options.setKeyBinding(this.options.keyBindings[this.buttonId], 0);
				}
			} else if (keyCode != 0) {
				this.options.setKeyBinding(this.options.keyBindings[this.buttonId], keyCode);
			} else if (character > 0) {
				this.options.setKeyBinding(this.options.keyBindings[this.buttonId], character + 256);
			}

			this.getButtonList().get(this.buttonId).displayString = this.options.getOptionDisplayString(this.buttonId);
			this.buttonId = -1;
			KeyBinding.resetKeyBindingArrayAndHash();
		} else {
			super.keyTyped(character, keyCode);
		}
	}

	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawMap();
		this.drawDefaultBackground();
		this.drawCenteredString(this.getFontRenderer(), this.screenTitle, this.getWidth() / 2, 20, 16777215);
		int leftBorder = this.getLeftBorder();

		for (int i = 0; i < this.options.keyBindings.length; i++) {
			boolean var6 = false;
			int var7 = 0;

			while (true) {
				if (var7 < this.options.keyBindings.length) {
					if (this.options.keyBindings[i].getKeyCode() == 0
						|| (var7 == i || this.options.keyBindings[i].getKeyCode() != this.options.keyBindings[var7].getKeyCode())
						&& (
						this.options.keyBindings[i].getKeyCode() != this.options.game.gameSettings.keyBindings[var7].getKeyCode()
							|| this.options.keyBindings[i].equals(this.options.game.gameSettings.keyBindings[var7])
					)) {
						var7++;
						continue;
					}

					var6 = true;
				}

				if (var7 >= this.options.game.gameSettings.keyBindings.length) {
					break;
				}

				if (this.options.keyBindings[i].getKeyCode() != 0
					&& this.options.keyBindings[i].getKeyCode() == this.options.game.gameSettings.keyBindings[var7].getKeyCode()
					&& !this.options.keyBindings[i].equals(this.options.game.gameSettings.keyBindings[var7])) {
					var6 = true;
					break;
				}

				var7++;
			}

			if (this.buttonId == i) {
				this.getButtonList().get(i).displayString = "§f> §e??? §f<";
			} else if (var6) {
				this.getButtonList().get(i).displayString = "§c" + this.options.getOptionDisplayString(i);
			} else {
				this.getButtonList().get(i).displayString = this.options.getOptionDisplayString(i);
			}

			this.drawString(
				this.getFontRenderer(),
				this.options.getKeyBindingDescription(i),
				leftBorder + i % 2 * 160 + 70 + 6,
				this.getHeight() / 6 + 24 * (i >> 1) + 7,
				-1
			);
		}

		this.drawCenteredString(this.getFontRenderer(), I18nUtils.getString("controls.minimap.unbind1"), this.getWidth() / 2, this.getHeight() / 6 + 115, 16777215);
		this.drawCenteredString(this.getFontRenderer(), I18nUtils.getString("controls.minimap.unbind2"), this.getWidth() / 2, this.getHeight() / 6 + 129, 16777215);
		super.drawScreen(mouseX, mouseY, partialTicks);
	}
}
