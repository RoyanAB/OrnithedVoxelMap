package com.mamiyaotaru.voxelmap.gui;

import com.mamiyaotaru.voxelmap.RadarSettingsManager;
import com.mamiyaotaru.voxelmap.gui.overridden.GuiSlotMinimap;
import com.mamiyaotaru.voxelmap.util.*;
import org.lwjgl.input.Mouse;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Objects;

@SuppressWarnings("unused")
public class GuiSlotMobs extends GuiSlotMinimap {
	final GuiMobs parentGui;
	private final ArrayList<String> mobNames;
	private final RadarSettingsManager options;
	private ArrayList<String> mobNamesFiltered;

	public GuiSlotMobs(GuiMobs guiMobs) {
		super(guiMobs.options.game, guiMobs.getWidth(), guiMobs.getHeight(), 32, guiMobs.getHeight() - 65 + 4, 18);
		this.parentGui = guiMobs;
		this.options = this.parentGui.options;
		this.mobNames = new ArrayList<>();

		for (EnumMobs mob : EnumMobs.values()) {
			if (mob.isTopLevelUnit && (mob.isHostile && this.options.showHostiles || mob.isNeutral && this.options.showNeutrals)) {
				this.mobNames.add(mob.id);
			}
		}

		for (CustomMob mob : CustomMobsManager.mobs) {
			if (mob.isHostile && this.options.showHostiles || mob.isNeutral && this.options.showNeutrals) {
				this.mobNames.add(mob.id);
			}
		}

		final Collator collator = I18nUtils.getLocaleAwareCollator();
		this.mobNames.sort((name1, name2) -> {
			name1 = GuiSlotMobs.getTranslatedName(name1);
			name2 = GuiSlotMobs.getTranslatedName(name2);
			return collator.compare(name1, name2);
		});
		this.mobNamesFiltered = new ArrayList<>(this.mobNames);
	}

	private static String getTranslatedName(String name) {
		name = I18nUtils.getString("entity." + name + ".name");
		return name.replaceAll("^entity.", "").replaceAll(".name$", "");
	}

	@Override
	protected int getSize() {
		return this.mobNamesFiltered.size();
	}

	@Override
	protected void elementClicked(int slotIndex, boolean isSelected, int mouseX, int mouseY) {
		this.parentGui.setSelectedMob(this.mobNamesFiltered.get(slotIndex));
		int leftEdge = this.parentGui.getWidth() / 2 - 92 - 16;
		byte padding = 3;
		int width = 215;
		if (this.mouseX >= leftEdge + width - 16 - padding && this.mouseX <= leftEdge + width + padding) {
			this.parentGui.toggleMobVisibility();
		} else if (isSelected) {
			Mouse.next();
			this.parentGui.toggleMobVisibility();
		}
	}

	@Override
	protected boolean isSelected(int slotIndex) {
		return this.mobNamesFiltered.get(slotIndex).equals(this.parentGui.selectedMobName);
	}

	@Override
	protected int getContentHeight() {
		return this.getSize() * this.slotHeight;
	}

	@Override
	protected void drawBackground() {
		this.parentGui.drawDefaultBackground();
	}

	@Override
	protected void drawSlot(int slotIndex, int leftEdge, int slotYPos, int topFudgeorHeightIn, int mouseX, int mouseY, float partialTicks) {
		String name = this.mobNamesFiltered.get(slotIndex);
		boolean isHostile = false;
		boolean isNeutral = false;
		boolean isEnabled = true;
		EnumMobs mob = EnumMobs.getMobByName(name);
		if (mob != null) {
			isHostile = mob.isHostile;
			isNeutral = mob.isNeutral;
			isEnabled = mob.enabled;
		} else {
			CustomMob customMob = CustomMobsManager.getCustomMobByType(name);
			if (customMob != null) {
				isHostile = customMob.isHostile;
				isNeutral = customMob.isNeutral;
				isEnabled = customMob.enabled;
			}
		}

		int red = isHostile ? 255 : 0;
		int green = isNeutral ? 255 : 0;
		int color = -16777216 + (red << 16) + (green << 8);
		this.parentGui.drawCenteredString(this.parentGui.getFontRenderer(), getTranslatedName(name), this.parentGui.getWidth() / 2, slotYPos + 3, color);
		byte padding = 3;
		if (this.mouseX >= leftEdge - padding && this.mouseY >= slotYPos && this.mouseX <= leftEdge + 215 + padding && this.mouseY <= slotYPos + this.slotHeight) {
			String tooltip;
			if (this.mouseX >= leftEdge + 215 - 16 - padding && this.mouseX <= leftEdge + 215 + padding) {
				tooltip = isEnabled ? I18nUtils.getString("options.minimap.mobs.disable") : I18nUtils.getString("options.minimap.mobs.enable");
			} else {
				tooltip = isEnabled ? I18nUtils.getString("options.minimap.mobs.enabled") : I18nUtils.getString("options.minimap.mobs.disabled");
			}

			GuiMobs.setTooltip(this.parentGui, tooltip);
		}

		GLShim.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GLUtils.img("textures/gui/container/inventory.png");
		int xOffset = isEnabled ? 72 : 90;
		int yOffset = 216;
		this.parentGui.drawTexturedModalRect(leftEdge + 198, slotYPos - 2, xOffset, yOffset, 16, 16);
	}

	protected void updateFilter(String filterString) {
		this.mobNamesFiltered = new ArrayList<>(this.mobNames);
		Iterator<String> iterator = this.mobNamesFiltered.iterator();

		while (iterator.hasNext()) {
			String mobName = iterator.next();
			if (!getTranslatedName(mobName).toLowerCase().contains(filterString)) {
				if (Objects.equals(mobName, this.parentGui.selectedMobName)) {
					this.parentGui.setSelectedMob(null);
				}

				iterator.remove();
			}
		}
	}
}
