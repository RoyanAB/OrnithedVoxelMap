package com.mamiyaotaru.voxelmap.persistent;

import com.mamiyaotaru.voxelmap.gui.overridden.EnumOptionsMinimap;
import com.mamiyaotaru.voxelmap.gui.overridden.GuiOptionButtonMinimap;
import com.mamiyaotaru.voxelmap.gui.overridden.GuiOptionSliderMinimap;
import com.mamiyaotaru.voxelmap.gui.overridden.GuiScreenMinimap;
import com.mamiyaotaru.voxelmap.interfaces.IVoxelMap;
import com.mamiyaotaru.voxelmap.util.I18nUtils;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

public class GuiPersistentMapOptions extends GuiScreenMinimap {
	private static EnumOptionsMinimap[] relevantOptions;
	private static EnumOptionsMinimap[] relevantOptions2;
	private final GuiScreen parent;
	private final PersistentMapSettingsManager options;
	protected String screenTitle = "Worldmap Options";
	protected String cacheSettings = "Zoom/Cache Settings";
	protected String warning = "Edit at your own risk";

	public GuiPersistentMapOptions(GuiScreen parent, IVoxelMap master) {
		this.parent = parent;
		this.options = master.getPersistentMapOptions();
	}

	public void initGui() {
		relevantOptions = new EnumOptionsMinimap[]{EnumOptionsMinimap.SHOWWAYPOINTS, EnumOptionsMinimap.SHOWWAYPOINTNAMES};
		this.screenTitle = I18nUtils.getString("options.worldmap.title");
		this.cacheSettings = I18nUtils.getString("options.worldmap.cachesettings");
		this.warning = I18nUtils.getString("options.worldmap.warning");
		int var2 = 0;

		for (int t = 0; t < relevantOptions.length; t++) {
			EnumOptionsMinimap option = relevantOptions[t];
			GuiOptionButtonMinimap var7 = new GuiOptionButtonMinimap(
				option.returnEnumOrdinal(),
				this.getWidth() / 2 - 155 + var2 % 2 * 160,
				this.getHeight() / 6 + 24 * (var2 >> 1),
				option,
				this.options.getKeyText(option)
			);
			this.getButtonList().add(var7);
			var2++;
		}

		relevantOptions2 = new EnumOptionsMinimap[]{EnumOptionsMinimap.MINZOOM, EnumOptionsMinimap.MAXZOOM, EnumOptionsMinimap.CACHESIZE};
		var2 += 2;

		for (int t = 0; t < relevantOptions2.length; t++) {
			EnumOptionsMinimap option = relevantOptions2[t];
			if (option.isFloat()) {
				float sValue = this.options.getOptionFloatValue(option);
				float fValue = 0.0F;
				switch (option) {
					case MINZOOM:
						fValue = (sValue - -3.0F) / (5 - -3);
						break;
					case MAXZOOM:
						fValue = (sValue - -3.0F) / (5 - -3);
						break;
					case CACHESIZE:
						fValue = sValue / 5000.0F;
						break;
					default:
						throw new IllegalArgumentException(
							"Add code to handle EnumOptionMinimap: " + option.getName() + ". (possibly not a float value applicable to persistent map)"
						);
				}

				this.getButtonList()
					.add(
						new GuiOptionSliderMinimap(
							option.returnEnumOrdinal(),
							this.getWidth() / 2 - 155 + var2 % 2 * 160,
							this.getHeight() / 6 + 24 * (var2 >> 1),
							option,
							fValue,
							this.options
						)
					);
			} else {
				GuiOptionButtonMinimap var7 = new GuiOptionButtonMinimap(
					option.returnEnumOrdinal(),
					this.getWidth() / 2 - 155 + var2 % 2 * 160,
					this.getHeight() / 6 + 24 * (var2 >> 1),
					option,
					this.options.getKeyText(option)
				);
				this.getButtonList().add(var7);
			}

			var2++;
		}

		this.getButtonList().add(new GuiButton(200, this.getWidth() / 2 - 100, this.getHeight() / 6 + 168, I18nUtils.getString("gui.done")));

		for (Object buttonObj : this.getButtonList()) {
			if (buttonObj instanceof GuiOptionButtonMinimap) {
				GuiOptionButtonMinimap button = (GuiOptionButtonMinimap) buttonObj;
				if (button.returnEnumOptions().equals(EnumOptionsMinimap.SHOWWAYPOINTNAMES)) {
					button.enabled = this.options.showWaypoints;
				}
			}
		}
	}

	protected void actionPerformed(GuiButton par1GuiButton) {
		if (par1GuiButton.enabled) {
			if (par1GuiButton.id < 100 && par1GuiButton instanceof GuiOptionButtonMinimap) {
				this.options.setOptionValue(((GuiOptionButtonMinimap) par1GuiButton).returnEnumOptions(), 1);
				par1GuiButton.displayString = this.options.getKeyText(EnumOptionsMinimap.getEnumOptions(par1GuiButton.id));

				for (Object buttonObj : this.getButtonList()) {
					if (buttonObj instanceof GuiOptionButtonMinimap) {
						GuiOptionButtonMinimap button = (GuiOptionButtonMinimap) buttonObj;
						if (button.returnEnumOptions().equals(EnumOptionsMinimap.SHOWWAYPOINTNAMES)) {
							button.enabled = this.options.showWaypoints;
						}
					}
				}
			}

			if (par1GuiButton.id == 200) {
				this.getMinecraft().displayGuiScreen(this.parent);
			}
		}
	}

	public void drawScreen(int par1, int par2, float par3) {
		for (Object buttonObj : this.getButtonList()) {
			if (buttonObj instanceof GuiOptionSliderMinimap) {
				GuiOptionSliderMinimap slider = (GuiOptionSliderMinimap) buttonObj;
				EnumOptionsMinimap option = slider.returnEnumOptions();
				float sValue = this.options.getOptionFloatValue(option);
				float fValue = 0.0F;
				switch (option) {
					case MINZOOM:
						fValue = (sValue - -3.0F) / (5 - -3);
						break;
					case MAXZOOM:
						fValue = (sValue - -3.0F) / (5 - -3);
						break;
					case CACHESIZE:
						fValue = sValue / 5000.0F;
						break;
					default:
						throw new IllegalArgumentException(
							"Add code to handle EnumOptionMinimap: " + option.getName() + ". (possibly not a float value applicable to persistent map)"
						);
				}

				slider.sliderValue = fValue;
				slider.resetText();
			}
		}

		super.drawMap();
		this.drawDefaultBackground();
		this.drawCenteredString(this.getFontRenderer(), this.screenTitle, this.getWidth() / 2, 20, 16777215);
		this.drawCenteredString(this.getFontRenderer(), this.cacheSettings, this.getWidth() / 2, this.getHeight() / 6 + 24, 16777215);
		this.drawCenteredString(this.getFontRenderer(), this.warning, this.getWidth() / 2, this.getHeight() / 6 + 34, 16777215);
		super.drawScreen(par1, par2, par3);
	}
}
