package com.mamiyaotaru.voxelmap.gui;

import com.mamiyaotaru.voxelmap.RadarSettingsManager;
import com.mamiyaotaru.voxelmap.gui.overridden.GuiScreenMinimap;
import com.mamiyaotaru.voxelmap.util.CustomMob;
import com.mamiyaotaru.voxelmap.util.CustomMobsManager;
import com.mamiyaotaru.voxelmap.util.EnumMobs;
import com.mamiyaotaru.voxelmap.util.I18nUtils;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import org.lwjgl.input.Keyboard;

public class GuiMobs extends GuiScreenMinimap {
	protected final RadarSettingsManager options;
	private final GuiScreen parentScreen;
	protected GuiTextField filter;
	protected String selectedMobName;
	private GuiSlotMobs mobsList;
	private GuiButton buttonEnable;
	private GuiButton buttonDisable;

	protected String screenTitle = "Select Mobs";

	public GuiMobs(GuiScreen parentScreen, RadarSettingsManager options) {
		this.parentScreen = parentScreen;
		this.options = options;
	}

	public void updateScreen() {
		this.filter.updateCursorCounter();
	}

	public void initGui() {
		this.screenTitle = I18nUtils.getString("options.minimap.mobs.title");
		Keyboard.enableRepeatEvents(true);
		this.mobsList = new GuiSlotMobs(this);
		this.mobsList.registerScrollButtons(7, 8);
		int filterStringWidth = this.getFontRenderer().getStringWidth(I18nUtils.getString("minimap.waypoints.filter") + ":");
		this.filter = new GuiTextField(
			2, this.getFontRenderer(), this.getWidth() / 2 - 153 + filterStringWidth + 5, this.getHeight() - 56, 305 - filterStringWidth - 5, 20
		);
		this.filter.setMaxStringLength(35);
		this.filter.setFocused(true);
		this.getButtonList()
			.add(
				this.buttonEnable = new GuiButton(-1, this.getWidth() / 2 - 154, this.getHeight() - 28, 100, 20, I18nUtils.getString("options.minimap.mobs.enable"))
			);
		this.getButtonList()
			.add(
				this.buttonDisable = new GuiButton(
					-2, this.getWidth() / 2 - 50, this.getHeight() - 28, 100, 20, I18nUtils.getString("options.minimap.mobs.disable")
				)
			);
		this.getButtonList().add(new GuiButton(-200, this.getWidth() / 2 + 4 + 50, this.getHeight() - 28, 100, 20, I18nUtils.getString("gui.done")));
		boolean isSomethingSelected = this.selectedMobName != null;
		this.buttonEnable.enabled = isSomethingSelected;
		this.buttonDisable.enabled = isSomethingSelected;
	}

	protected void actionPerformed(GuiButton button) {
		if (button.enabled) {
			if (button.id == -1) {
				this.setMobEnabled(this.selectedMobName, true);
			}

			if (button.id == -2) {
				this.setMobEnabled(this.selectedMobName, false);
			}

			if (button.id == -200) {
				this.getMinecraft().displayGuiScreen(this.parentScreen);
			}
		}
	}

	protected void keyTyped(char character, int keycode) {
		super.keyTyped(character, keycode);
		if (this.filter.textboxKeyTyped(character, keycode)) {
			this.mobsList.updateFilter(this.filter.getText().toLowerCase());
		}
	}

	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		this.filter.mouseClicked(mouseX, mouseY, mouseButton);
	}

	protected void setSelectedMob(String mob) {
		this.selectedMobName = mob;
	}

	private boolean isMobEnabled(String selectedMobName) {
		EnumMobs mob = EnumMobs.getMobByName(selectedMobName);
		if (mob != null) {
			return mob.enabled;
		}

		CustomMob customMob = CustomMobsManager.getCustomMobByType(selectedMobName);
		return customMob != null && customMob.enabled;
	}

	private void setMobEnabled(String selectedMobName, boolean enabled) {
		for (EnumMobs mob : EnumMobs.values()) {
			if (mob.id.equals(selectedMobName)) {
				mob.enabled = enabled;
			}
		}

		for (CustomMob mob : CustomMobsManager.mobs) {
			if (mob.id.equals(selectedMobName)) {
				mob.enabled = enabled;
			}
		}
	}

	protected void toggleMobVisibility() {
		EnumMobs mob = EnumMobs.getMobByName(this.selectedMobName);
		if (mob != null) {
			this.setMobEnabled(this.selectedMobName, !mob.enabled);
		} else {
			CustomMob customMob = CustomMobsManager.getCustomMobByType(this.selectedMobName);
			if (customMob != null) {
				this.setMobEnabled(this.selectedMobName, !customMob.enabled);
			}
		}
	}

	public void handleMouseInput() {
		super.handleMouseInput();
		if (this.mobsList != null) {
			this.mobsList.handleMouseInput();
		}
	}

	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawMap();
		this.tooltip = null;
		this.mobsList.drawScreen(mouseX, mouseY, partialTicks);
		this.drawCenteredString(this.getFontRenderer(), this.screenTitle, this.getWidth() / 2, 20, 16777215);
		boolean isSomethingSelected = this.selectedMobName != null;
		this.buttonEnable.enabled = isSomethingSelected && !this.isMobEnabled(this.selectedMobName);
		this.buttonDisable.enabled = isSomethingSelected && this.isMobEnabled(this.selectedMobName);
		super.drawScreen(mouseX, mouseY, partialTicks);
		this.drawString(
			this.getFontRenderer(), I18nUtils.getString("minimap.waypoints.filter") + ":", this.getWidth() / 2 - 153, this.getHeight() - 51, 10526880
		);
		this.filter.drawTextBox();
		if (this.tooltip != null) {
			this.drawTooltip(this.tooltip, mouseX, mouseY);
		}
	}

	protected void drawTooltip(String tooltip, int mouseX, int mouseY) {
		if (tooltip != null) {
			int drawX = mouseX + 12;
			int drawY = mouseY - 12;
			int textWidth = this.getFontRenderer().getStringWidth(tooltip);
			this.drawGradientRect(drawX - 3, drawY - 3, drawX + textWidth + 3, drawY + 8 + 3, -1073741824, -1073741824);
			this.getFontRenderer().drawStringWithShadow(tooltip, drawX, drawY, -1);
		}
	}

	@Override
	public void onGuiClosed() {
		Keyboard.enableRepeatEvents(false);
		super.onGuiClosed();
	}
}
