package com.mamiyaotaru.voxelmap.gui;

import com.mamiyaotaru.voxelmap.MapSettingsManager;
import com.mamiyaotaru.voxelmap.gui.overridden.EnumOptionsMinimap;
import com.mamiyaotaru.voxelmap.gui.overridden.GuiOptionButtonMinimap;
import com.mamiyaotaru.voxelmap.gui.overridden.GuiOptionSliderMinimap;
import com.mamiyaotaru.voxelmap.gui.overridden.GuiScreenMinimap;
import com.mamiyaotaru.voxelmap.util.I18nUtils;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

public class GuiWaypointsOptions extends GuiScreenMinimap {
	private static final EnumOptionsMinimap[] relevantOptions = new EnumOptionsMinimap[]{EnumOptionsMinimap.WAYPOINTDISTANCE, EnumOptionsMinimap.DEATHPOINTS};
	private final GuiScreen parent;
	private final MapSettingsManager options;

	protected String screenTitle = "Waypoint Options";

	public GuiWaypointsOptions(GuiScreen parent, MapSettingsManager options) {
		this.parent = parent;
		this.options = options;
	}

	@Override
	public void initGui() {
		int buttonIndex = 0;
		this.screenTitle = I18nUtils.getString("options.minimap.waypoints.title");

		for (EnumOptionsMinimap option : relevantOptions) {
			if (option.isFloat()) {
				float distance = this.options.getOptionFloatValue(option);
				if (distance < 0.0F) {
					distance = 10001.0F;
				}

				distance = (distance - 50.0F) / 9951.0F;
				this.getButtonList()
					.add(
						new GuiOptionSliderMinimap(
							option.returnEnumOrdinal(),
							this.getWidth() / 2 - 155 + buttonIndex % 2 * 160,
							this.getHeight() / 6 + 24 * (buttonIndex >> 1),
							option,
							distance,
							this.options
						)
					);
			} else {
				GuiOptionButtonMinimap button = new GuiOptionButtonMinimap(
					option.returnEnumOrdinal(),
					this.getWidth() / 2 - 155 + buttonIndex % 2 * 160,
					this.getHeight() / 6 + 24 * (buttonIndex >> 1),
					option,
					this.options.getKeyText(option)
				);
				this.getButtonList().add(button);
			}

			buttonIndex++;
		}

		this.getButtonList().add(new GuiButton(200, this.getWidth() / 2 - 100, this.getHeight() / 6 + 168, I18nUtils.getString("gui.done")));
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		if (button.enabled) {
			if (button.id < 100 && button instanceof GuiOptionButtonMinimap) {
				this.options.setOptionValue(((GuiOptionButtonMinimap) button).returnEnumOptions(), 1);
				button.displayString = this.options.getKeyText(EnumOptionsMinimap.getEnumOptions(button.id));
			}

			if (button.id == 200) {
				this.getMinecraft().displayGuiScreen(this.parent);
			}
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawMap();
		this.drawDefaultBackground();
		this.drawCenteredString(this.fontRenderer, this.screenTitle, this.getWidth() / 2, 20, 16777215);
		super.drawScreen(mouseX, mouseY, partialTicks);
	}
}
