package com.mamiyaotaru.voxelmap.gui;

import com.mamiyaotaru.voxelmap.RadarSettingsManager;
import com.mamiyaotaru.voxelmap.gui.overridden.EnumOptionsMinimap;
import com.mamiyaotaru.voxelmap.gui.overridden.GuiOptionButtonMinimap;
import com.mamiyaotaru.voxelmap.gui.overridden.GuiScreenMinimap;
import com.mamiyaotaru.voxelmap.interfaces.IVoxelMap;
import com.mamiyaotaru.voxelmap.util.I18nUtils;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import java.util.Objects;

public class GuiRadarOptions extends GuiScreenMinimap {
	private static final EnumOptionsMinimap[] relevantOptionsFull = new EnumOptionsMinimap[]{
		EnumOptionsMinimap.SHOWRADAR,
		EnumOptionsMinimap.RADARMODE,
		EnumOptionsMinimap.SHOWHOSTILES,
		EnumOptionsMinimap.SHOWNEUTRALS,
		EnumOptionsMinimap.SHOWPLAYERS,
		EnumOptionsMinimap.SHOWPLAYERNAMES,
		EnumOptionsMinimap.SHOWPLAYERHELMETS,
		EnumOptionsMinimap.SHOWMOBHELMETS,
		EnumOptionsMinimap.RADARFILTERING,
		EnumOptionsMinimap.RADAROUTLINES
	};
	private static final EnumOptionsMinimap[] relevantOptionsSimple = new EnumOptionsMinimap[]{
		EnumOptionsMinimap.SHOWRADAR,
		EnumOptionsMinimap.RADARMODE,
		EnumOptionsMinimap.SHOWHOSTILES,
		EnumOptionsMinimap.SHOWNEUTRALS,
		EnumOptionsMinimap.SHOWPLAYERS,
		EnumOptionsMinimap.SHOWFACING
	};
	private static EnumOptionsMinimap[] relevantOptions;
	private final GuiScreen parent;
	private final RadarSettingsManager options;
	protected String screenTitle = "Radar Options";

	public GuiRadarOptions(GuiScreen parent, IVoxelMap master) {
		this.parent = parent;
		this.options = master.getRadarOptions();
	}

	public void initGui() {
		this.getButtonList().clear();
		int var2 = 0;
		this.screenTitle = I18nUtils.getString("options.minimap.radar.title");
		if (this.options.radarMode == 2) {
			relevantOptions = relevantOptionsFull;
		} else {
			relevantOptions = relevantOptionsSimple;
		}

		for (EnumOptionsMinimap option : relevantOptions) {
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

		for (Object buttonObj : this.getButtonList()) {
			if (buttonObj instanceof GuiOptionButtonMinimap) {
				GuiOptionButtonMinimap button = (GuiOptionButtonMinimap) buttonObj;
				if (!button.returnEnumOptions().equals(EnumOptionsMinimap.SHOWRADAR)) {
					button.enabled = this.options.showRadar;
				}

				if (button.returnEnumOptions().equals(EnumOptionsMinimap.SHOWPLAYERS)) {
					button.enabled = button.enabled && (this.options.radarAllowed || this.options.radarPlayersAllowed);
				} else if (button.returnEnumOptions().equals(EnumOptionsMinimap.SHOWNEUTRALS) || button.returnEnumOptions().equals(EnumOptionsMinimap.SHOWHOSTILES)
				) {
					button.enabled = button.enabled && (this.options.radarAllowed || this.options.radarMobsAllowed);
				} else if (!button.returnEnumOptions().equals(EnumOptionsMinimap.SHOWPLAYERHELMETS)
					&& !button.returnEnumOptions().equals(EnumOptionsMinimap.SHOWPLAYERNAMES)) {
					if (button.returnEnumOptions().equals(EnumOptionsMinimap.SHOWMOBHELMETS)) {
						button.enabled = button.enabled
							&& (this.options.showNeutrals || this.options.showHostiles)
							&& (this.options.radarAllowed || this.options.radarMobsAllowed);
					}
				} else {
					button.enabled = button.enabled && this.options.showPlayers && (this.options.radarAllowed || this.options.radarPlayersAllowed);
				}
			}
		}

		if (this.options.radarMode == 2) {
			this.getButtonList()
				.add(
					new GuiButton(101, this.getWidth() / 2 - 155, this.getHeight() / 6 + 144 - 6, 150, 20, I18nUtils.getString("options.minimap.radar.selectmobs"))
				);
		}

		this.getButtonList().add(new GuiButton(200, this.getWidth() / 2 - 100, this.getHeight() / 6 + 168, I18nUtils.getString("gui.done")));
	}

	protected void actionPerformed(GuiButton buttonClicked) {
		if (buttonClicked.enabled) {
			if (buttonClicked.id < 100 && buttonClicked instanceof GuiOptionButtonMinimap) {
				this.options.setOptionValue(((GuiOptionButtonMinimap) buttonClicked).returnEnumOptions(), 1);
				if (((GuiOptionButtonMinimap) buttonClicked).returnEnumOptions().equals(EnumOptionsMinimap.RADARMODE)) {
					this.initGui();
					return;
				}

				buttonClicked.displayString = this.options.getKeyText(Objects.requireNonNull(EnumOptionsMinimap.getEnumOptions(buttonClicked.id)));

				for (GuiButton buttonObj : this.getButtonList()) {
					if (buttonObj instanceof GuiOptionButtonMinimap) {
						GuiOptionButtonMinimap button = (GuiOptionButtonMinimap) buttonObj;
						if (!button.returnEnumOptions().equals(EnumOptionsMinimap.SHOWRADAR)) {
							button.enabled = this.options.showRadar;
						}

						if (button.returnEnumOptions().equals(EnumOptionsMinimap.SHOWPLAYERS)) {
							button.enabled = button.enabled && (this.options.radarAllowed || this.options.radarPlayersAllowed);
						} else if (button.returnEnumOptions().equals(EnumOptionsMinimap.SHOWNEUTRALS)
							|| button.returnEnumOptions().equals(EnumOptionsMinimap.SHOWHOSTILES)) {
							button.enabled = button.enabled && (this.options.radarAllowed || this.options.radarMobsAllowed);
						} else if (button.returnEnumOptions().equals(EnumOptionsMinimap.SHOWPLAYERHELMETS)
							|| button.returnEnumOptions().equals(EnumOptionsMinimap.SHOWPLAYERNAMES)) {
							button.enabled = button.enabled
								&& this.options.showPlayers
								&& (this.options.radarAllowed || this.options.radarPlayersAllowed);
						} else if (button.returnEnumOptions().equals(EnumOptionsMinimap.SHOWMOBHELMETS)) {
							button.enabled = button.enabled
								&& (this.options.showNeutrals || this.options.showHostiles)
								&& (this.options.radarAllowed || this.options.radarMobsAllowed);
						}
					} else if (buttonObj instanceof GuiButton) {
						GuiButton buttonx = buttonObj;
						if (buttonx.id == 101) {
							buttonx.enabled = this.options.showRadar;
						}
					}
				}
			}

			if (buttonClicked.id == 101) {
				this.getMinecraft().displayGuiScreen(new GuiMobs(this, this.options));
			}

			if (buttonClicked.id == 200) {
				this.getMinecraft().displayGuiScreen(this.parent);
			}
		}
	}

	public void drawScreen(int par1, int par2, float par3) {
		super.drawMap();
		this.drawDefaultBackground();
		this.drawCenteredString(this.getFontRenderer(), this.screenTitle, this.getWidth() / 2, 20, 16777215);
		super.drawScreen(par1, par2, par3);
	}
}
