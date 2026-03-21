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
	protected String screenTitle = "Controls";
	private final GuiScreen parentScreen;
	private final MapSettingsManager options;
	private int buttonId = -1;

	public GuiMinimapControls(GuiScreen par1GuiScreen, IVoxelMap master) {
		this.parentScreen = par1GuiScreen;
		this.options = master.getMapOptions();
	}

	private int getLeftBorder() {
		return this.getWidth() / 2 - 155;
	}

	public void initGui() {
		int var2 = this.getLeftBorder();

		for (int var3 = 0; var3 < this.options.keyBindings.length; var3++) {
			this.getButtonList()
				.add(
					new GuiOptionButtonMinimap(
						var3, var2 + var3 % 2 * 160, this.getHeight() / 6 + 24 * (var3 >> 1), 70, 20, this.options.getOptionDisplayString(var3)
					)
				);
		}

		this.getButtonList().add(new GuiButton(200, this.getWidth() / 2 - 100, this.getHeight() / 6 + 168, I18nUtils.getString("gui.done")));
		this.screenTitle = I18nUtils.getString("controls.minimap.title");
	}

	protected void actionPerformed(GuiButton par1GuiButton) {
		for (int buttonListIndex = 0; buttonListIndex < this.options.keyBindings.length; buttonListIndex++) {
			this.getButtonList().get(buttonListIndex).displayString = this.options.getOptionDisplayString(buttonListIndex);
		}

		if (par1GuiButton.id == 200) {
			this.getMinecraft().displayGuiScreen(this.parentScreen);
		} else {
			this.buttonId = par1GuiButton.id;
			par1GuiButton.displayString = "> " + this.options.getOptionDisplayString(par1GuiButton.id) + " <";
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

	public void drawScreen(int par1, int par2, float par3) {
		super.drawMap();
		this.drawDefaultBackground();
		this.drawCenteredString(this.getFontRenderer(), this.screenTitle, this.getWidth() / 2, 20, 16777215);
		int var4 = this.getLeftBorder();

		for (int var5 = 0; var5 < this.options.keyBindings.length; var5++) {
			boolean var6 = false;
			int var7 = 0;

			while (true) {
				if (var7 < this.options.keyBindings.length) {
					if (this.options.keyBindings[var5].getKeyCode() == 0
						|| (var7 == var5 || this.options.keyBindings[var5].getKeyCode() != this.options.keyBindings[var7].getKeyCode())
						&& (
						this.options.keyBindings[var5].getKeyCode() != this.options.game.gameSettings.keyBindings[var7].getKeyCode()
							|| this.options.keyBindings[var5].equals(this.options.game.gameSettings.keyBindings[var7])
					)) {
						var7++;
						continue;
					}

					var6 = true;
				}

				if (var7 >= this.options.game.gameSettings.keyBindings.length) {
					break;
				}

				if (this.options.keyBindings[var5].getKeyCode() != 0
					&& this.options.keyBindings[var5].getKeyCode() == this.options.game.gameSettings.keyBindings[var7].getKeyCode()
					&& !this.options.keyBindings[var5].equals(this.options.game.gameSettings.keyBindings[var7])) {
					var6 = true;
					break;
				}

				var7++;
			}

			if (this.buttonId == var5) {
				this.getButtonList().get(var5).displayString = "§f> §e??? §f<";
			} else if (var6) {
				this.getButtonList().get(var5).displayString = "§c" + this.options.getOptionDisplayString(var5);
			} else {
				this.getButtonList().get(var5).displayString = this.options.getOptionDisplayString(var5);
			}

			this.drawString(
				this.getFontRenderer(),
				this.options.getKeyBindingDescription(var5),
				var4 + var5 % 2 * 160 + 70 + 6,
				this.getHeight() / 6 + 24 * (var5 >> 1) + 7,
				-1
			);
		}

		this.drawCenteredString(this.getFontRenderer(), I18nUtils.getString("controls.minimap.unbind1"), this.getWidth() / 2, this.getHeight() / 6 + 115, 16777215);
		this.drawCenteredString(this.getFontRenderer(), I18nUtils.getString("controls.minimap.unbind2"), this.getWidth() / 2, this.getHeight() / 6 + 129, 16777215);
		super.drawScreen(par1, par2, par3);
	}
}
