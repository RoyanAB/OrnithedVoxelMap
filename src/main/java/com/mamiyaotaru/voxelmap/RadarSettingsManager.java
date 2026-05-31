package com.mamiyaotaru.voxelmap;

import com.mamiyaotaru.voxelmap.gui.overridden.EnumOptionsMinimap;
import com.mamiyaotaru.voxelmap.interfaces.ISubSettingsManager;
import com.mamiyaotaru.voxelmap.util.CustomMob;
import com.mamiyaotaru.voxelmap.util.CustomMobsManager;
import com.mamiyaotaru.voxelmap.util.EnumMobs;
import com.mamiyaotaru.voxelmap.util.I18nUtils;
import net.minecraft.client.Minecraft;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.Objects;

public class RadarSettingsManager implements ISubSettingsManager {
	public Minecraft game;
	public int radarMode = 2;
	public boolean showRadar = true;
	public boolean showHostiles = true;
	public boolean showPlayers = true;
	public boolean showNeutrals = false;
	public boolean showPlayerNames = true;
	public boolean outlines = true;
	public boolean filtering = true;
	public boolean showHelmetsPlayers = true;
	public boolean showHelmetsMobs = true;
	public boolean randomobs = true;
	public boolean showFacing = true;
	public Boolean radarAllowed = true;
	public Boolean radarPlayersAllowed = true;
	public Boolean radarMobsAllowed = true;
	float fontScale = 1.0F;
	private boolean somethingChanged;

	public RadarSettingsManager() {
		this.game = Minecraft.getMinecraft();
	}

	@Override
	public void loadSettings(File settingsFile) {
		try {
			BufferedReader in = new BufferedReader(new FileReader(settingsFile));

			String sCurrentLine;
			while ((sCurrentLine = in.readLine()) != null) {
				String[] curLine = sCurrentLine.split(":");
				switch (curLine[0]) {
					case "Radar Mode":
						this.radarMode = Math.max(1, Math.min(2, Integer.parseInt(curLine[1])));
						break;
					case "Show Radar":
						this.showRadar = Boolean.parseBoolean(curLine[1]);
						break;
					case "Show Hostiles":
						this.showHostiles = Boolean.parseBoolean(curLine[1]);
						break;
					case "Show Players":
						this.showPlayers = Boolean.parseBoolean(curLine[1]);
						break;
					case "Show Neutrals":
						this.showNeutrals = Boolean.parseBoolean(curLine[1]);
						break;
					case "Filter Mob Icons":
						this.filtering = Boolean.parseBoolean(curLine[1]);
						break;
					case "Outline Mob Icons":
						this.outlines = Boolean.parseBoolean(curLine[1]);
						break;
					case "Show Player Helmets":
						this.showHelmetsPlayers = Boolean.parseBoolean(curLine[1]);
						break;
					case "Show Mob Helmets":
						this.showHelmetsMobs = Boolean.parseBoolean(curLine[1]);
						break;
					case "Show Player Names":
						this.showPlayerNames = Boolean.parseBoolean(curLine[1]);
						break;
					case "Font Scale":
						this.fontScale = Float.parseFloat(curLine[1]);
						break;
					case "Randomobs":
						this.randomobs = Boolean.parseBoolean(curLine[1]);
						break;
					case "Show Facing":
						this.showFacing = Boolean.parseBoolean(curLine[1]);
						break;
					case "Hidden Mobs":
						this.applyHiddenMobSettings(curLine[1]);
						break;
				}
			}

			in.close();
		} catch (Exception ignored) {
		}
	}

	private void applyHiddenMobSettings(String hiddenMobs) {
		String[] mobsToHide = hiddenMobs.split(",");

		for (String s : mobsToHide) {
			boolean builtIn = false;

			for (EnumMobs mob : EnumMobs.values()) {
				if (mob.id.equals(s)) {
					mob.enabled = false;
					builtIn = true;
				}
			}

			if (!builtIn) {
				CustomMobsManager.add(s, false);
			}
		}
	}

	@Override
	public void saveAll(PrintWriter out) {
		out.println("Radar Mode:" + this.radarMode);
		out.println("Show Radar:" + this.showRadar);
		out.println("Show Hostiles:" + this.showHostiles);
		out.println("Show Players:" + this.showPlayers);
		out.println("Show Neutrals:" + this.showNeutrals);
		out.println("Filter Mob Icons:" + this.filtering);
		out.println("Outline Mob Icons:" + this.outlines);
		out.println("Show Player Helmets:" + this.showHelmetsPlayers);
		out.println("Show Mob Helmets:" + this.showHelmetsMobs);
		out.println("Show Player Names:" + this.showPlayerNames);
		out.println("Font Scale:" + this.fontScale);
		out.println("Randomobs:" + this.randomobs);
		out.println("Show Facing:" + this.showFacing);
		out.print("Hidden Mobs:");

		for (EnumMobs mob : EnumMobs.values()) {
			if (mob.isTopLevelUnit && !mob.enabled) {
				out.print(mob.id + ",");
			}
		}

		for (CustomMob mobx : CustomMobsManager.mobs) {
			if (!mobx.enabled) {
				out.print(mobx.id + ",");
			}
		}

		out.println();
	}

	@Override
	public String getKeyText(EnumOptionsMinimap par1EnumOptions) {
		String s = I18nUtils.getString(par1EnumOptions.getName()) + ": ";
		if (par1EnumOptions.isBoolean()) {
			boolean flag = this.getOptionBooleanValue(par1EnumOptions);
			return flag ? s + I18nUtils.getString("options.on") : s + I18nUtils.getString("options.off");
		} else {
			return s;
		}
	}

	public boolean getOptionBooleanValue(EnumOptionsMinimap par1EnumOptions) {
		switch (par1EnumOptions) {
			case SHOWRADAR:
				return this.showRadar;
			case SHOWHOSTILES:
				return this.showHostiles;
			case SHOWPLAYERS:
				return this.showPlayers;
			case SHOWNEUTRALS:
				return this.showNeutrals;
			case SHOWPLAYERHELMETS:
				return this.showHelmetsPlayers;
			case SHOWMOBHELMETS:
				return this.showHelmetsMobs;
			case SHOWPLAYERNAMES:
				return this.showPlayerNames;
			case RADAROUTLINES:
				return this.outlines;
			case RADARFILTERING:
				return this.filtering;
			case RANDOMOBS:
				return this.randomobs;
			case SHOWFACING:
				return this.showFacing;
			default:
				throw new IllegalArgumentException("Add code to handle EnumOptionMinimap: " + par1EnumOptions.getName() + ". (possibly not a boolean)");
		}
	}

	public String getOptionListValue(EnumOptionsMinimap par1EnumOptions) {
		if (Objects.requireNonNull(par1EnumOptions) == EnumOptionsMinimap.RADARMODE) {
			if (this.radarMode == 2) {
				return I18nUtils.getString("options.minimap.radar.radarmode.full");
			}

			return I18nUtils.getString("options.minimap.radar.radarmode.simple");
		}
		throw new IllegalArgumentException(
			"Add code to handle EnumOptionMinimap: " + par1EnumOptions.getName() + ". (possibly not a list value applicable to minimap)"
		);
	}

	@Override
	public void setOptionFloatValue(EnumOptionsMinimap idFloat, float sliderValue) {
	}

	public void setOptionValue(EnumOptionsMinimap par1EnumOptions, int i) {
		switch (par1EnumOptions) {
			case SHOWRADAR:
				this.showRadar = !this.showRadar;
				break;
			case SHOWHOSTILES:
				this.showHostiles = !this.showHostiles;
				break;
			case SHOWPLAYERS:
				this.showPlayers = !this.showPlayers;
				break;
			case SHOWNEUTRALS:
				this.showNeutrals = !this.showNeutrals;
				break;
			case SHOWPLAYERHELMETS:
				this.showHelmetsPlayers = !this.showHelmetsPlayers;
				break;
			case SHOWMOBHELMETS:
				this.showHelmetsMobs = !this.showHelmetsMobs;
				break;
			case SHOWPLAYERNAMES:
				this.showPlayerNames = !this.showPlayerNames;
				break;
			case RADAROUTLINES:
				this.outlines = !this.outlines;
				break;
			case RADARFILTERING:
				this.filtering = !this.filtering;
				break;
			case RANDOMOBS:
				this.randomobs = !this.randomobs;
				break;
			case SHOWFACING:
				this.showFacing = !this.showFacing;
				break;
			case RADARMODE:
				if (this.radarMode == 2) {
					this.radarMode = 1;
				} else {
					this.radarMode = 2;
				}
				break;
			default:
				throw new IllegalArgumentException("Add code to handle EnumOptionMinimap: " + par1EnumOptions.getName());
		}

		this.somethingChanged = true;
	}

	public boolean isChanged() {
		if (this.somethingChanged) {
			this.somethingChanged = false;
			return true;
		} else {
			return false;
		}
	}
}
