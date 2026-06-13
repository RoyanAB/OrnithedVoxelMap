package com.mamiyaotaru.voxelmap.gui;

import com.mamiyaotaru.voxelmap.MapSettingsManager;
import com.mamiyaotaru.voxelmap.gui.overridden.EnumOptionsMinimap;
import com.mamiyaotaru.voxelmap.gui.overridden.GuiOptionButtonMinimap;
import com.mamiyaotaru.voxelmap.gui.overridden.GuiScreenMinimap;
import com.mamiyaotaru.voxelmap.interfaces.IVoxelMap;
import com.mamiyaotaru.voxelmap.persistent.GuiPersistentMapOptions;
import com.mamiyaotaru.voxelmap.util.I18nUtils;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import java.util.Objects;

public class GuiMinimapOptions extends GuiScreenMinimap {
	EnumOptionsMinimap[] relevantOptions = new EnumOptionsMinimap[]{
		EnumOptionsMinimap.COORDS,
		EnumOptionsMinimap.HIDE,
		EnumOptionsMinimap.LOCATION,
		EnumOptionsMinimap.SIZE,
		EnumOptionsMinimap.SQUARE,
		EnumOptionsMinimap.ROTATES,
		EnumOptionsMinimap.BEACONS,
		EnumOptionsMinimap.CAVEMODE
	};

	private final GuiScreen parent;
	private final IVoxelMap master;
	private final MapSettingsManager options;

	protected String screenTitle = "Minimap Options";

	public GuiMinimapOptions(GuiScreen parent, IVoxelMap master) {
		this.parent = parent;
		this.master = master;
		this.options = master.getMapOptions();
	}

	public void initGui() {
		int optionIndex = 0;
		this.screenTitle = I18nUtils.getString("options.minimap.title");

		for (EnumOptionsMinimap option : relevantOptions) {
			GuiOptionButtonMinimap button = new GuiOptionButtonMinimap(
				option.returnEnumOrdinal(),
				this.getWidth() / 2 - 155 + optionIndex % 2 * 160,
				this.getHeight() / 6 + 24 * (optionIndex >> 1),
				option,
				this.options.getKeyText(option)
			);
			this.getButtonList().add(button);
			if (option.equals(EnumOptionsMinimap.CAVEMODE)) {
				button.enabled = this.options.cavesAllowed;
			}

			optionIndex++;
		}

		GuiOptionButtonMinimap radarOptionsButton = new GuiOptionButtonMinimap(
			101, this.getWidth() / 2 - 155, this.getHeight() / 6 + 120 - 6, 150, 20, I18nUtils.getString("options.minimap.radar")
		);
		radarOptionsButton.enabled = this.master.getRadarOptions().radarAllowed
			|| this.master.getRadarOptions().radarMobsAllowed
			|| this.master.getRadarOptions().radarPlayersAllowed;
		this.getButtonList().add(radarOptionsButton);
		this.getButtonList()
			.add(new GuiButton(103, this.getWidth() / 2 + 5, this.getHeight() / 6 + 120 - 6, 150, 20, I18nUtils.getString("options.minimap.detailsperformance")));
		this.getButtonList().add(new GuiButton(102, this.getWidth() / 2 - 155, this.getHeight() / 6 + 144 - 6, 150, 20, I18nUtils.getString("options.controls")));
		this.getButtonList()
			.add(new GuiButton(100, this.getWidth() / 2 + 5, this.getHeight() / 6 + 144 - 6, 150, 20, I18nUtils.getString("options.minimap.worldmap")));
		this.getButtonList().add(new GuiButton(200, this.getWidth() / 2 - 100, this.getHeight() / 6 + 168, I18nUtils.getString("gui.done")));
	}

	protected void actionPerformed(GuiButton button) {
		if (button.enabled) {
			if (button.id < 100 && button instanceof GuiOptionButtonMinimap) {
				this.options.setOptionValue(((GuiOptionButtonMinimap) button).returnEnumOptions());
				button.displayString = this.options.getKeyText(Objects.requireNonNull(EnumOptionsMinimap.getEnumOptions(button.id)));
				if (((GuiOptionButtonMinimap) button).returnEnumOptions() == EnumOptionsMinimap.OLDNORTH) {
					this.master.getWaypointManager().setOldNorth(this.options.oldNorth);
				}
			}

			if (button.id == 103) {
				this.getMinecraft().displayGuiScreen(new GuiMinimapPerformance(this, this.master));
			}

			if (button.id == 102) {
				this.getMinecraft().displayGuiScreen(new GuiMinimapControls(this, this.master));
			}

			if (button.id == 101) {
				this.getMinecraft().displayGuiScreen(new GuiRadarOptions(this, this.master));
			}

			if (button.id == 100) {
				this.getMinecraft().displayGuiScreen(new GuiPersistentMapOptions(this, this.master));
			}

			if (button.id == 200) {
				this.getMinecraft().displayGuiScreen(this.parent);
			}
		}
	}

	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawMap();
		this.drawDefaultBackground();
		this.drawCenteredString(this.getFontRenderer(), this.screenTitle, this.getWidth() / 2, 20, 16777215);
		super.drawScreen(mouseX, mouseY, partialTicks);
	}
}
