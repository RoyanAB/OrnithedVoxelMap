package com.mamiyaotaru.voxelmap.gui;

import com.mamiyaotaru.voxelmap.RadarSettingsManager;
import com.mamiyaotaru.voxelmap.gui.overridden.GuiSlotMinimap;
import com.mamiyaotaru.voxelmap.util.*;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

class GuiSlotMobs extends GuiSlotMinimap {
    final GuiMobs parentGui;
    private final ArrayList<String> mobNames;
    private ArrayList<String> mobNamesFiltered;
    private final RadarSettingsManager options;

    public GuiSlotMobs(GuiMobs par1GuiMobs) {
        super(par1GuiMobs.options.game, par1GuiMobs.getWidth(), par1GuiMobs.getHeight(), 32, par1GuiMobs.getHeight() - 65 + 4, 18);
        this.parentGui = par1GuiMobs;
        this.options = this.parentGui.options;
        this.mobNames = new ArrayList<>();

        for (EnumMobs mob : EnumMobs.values()) {
            if (mob.isTopLevelUnit && (mob.isHostile && this.options.showHostiles || mob.isNeutral && this.options.showNeutrals)) {
                this.mobNames.add(mob.id);
            }
        }

        for (CustomMob mobx : CustomMobsManager.mobs) {
            if (mobx.isHostile && this.options.showHostiles || mobx.isNeutral && this.options.showNeutrals) {
                this.mobNames.add(mobx.id);
            }
        }

        final Collator collator = I18nUtils.getLocaleAwareCollator();
        Collections.sort(this.mobNames, new Comparator<String>() {
            public int compare(String name1, String name2) {
                name1 = GuiSlotMobs.getTranslatedName(name1);
                name2 = GuiSlotMobs.getTranslatedName(name2);
                return collator.compare(name1, name2);
            }
        });
        this.mobNamesFiltered = new ArrayList<>(this.mobNames);
    }

    private static String getTranslatedName(String name) {
        if (name.indexOf(".") == -1) {
            name = "entity.minecraft." + name.toLowerCase();
        }

        name = I18nUtils.getString(name);
        name = name.replaceAll("^entity.minecraft.", "");
        name = name.replace("_", " ");
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    protected int getSize() {
        return this.mobNamesFiltered.size();
    }

    protected boolean mouseClicked(int index, int mouseEvent, double mouseX, double mouseY) {
        this.parentGui.setSelectedMob(this.mobNamesFiltered.get(index));
        int leftEdge = this.parentGui.getWidth() / 2 - 92 - 16;
        byte padding = 3;
        int width = 215;
        if (mouseX >= leftEdge + width - 16 - padding && mouseX <= leftEdge + width + padding) {
            this.parentGui.toggleMobVisibility();
        } else if (this.doubleclick) {
            this.parentGui.toggleMobVisibility();
        }

        return true;
    }

    protected boolean isSelected(int par1) {
        return this.mobNamesFiltered.get(par1).equals(this.parentGui.selectedMobName);
    }

    protected int getContentHeight() {
        return this.getSize() * this.slotHeight;
    }

    protected void drawBackground() {
        this.parentGui.drawDefaultBackground();
    }

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
        if (mouseX >= leftEdge - padding && mouseY >= slotYPos && mouseX <= leftEdge + 215 + padding && mouseY <= slotYPos + this.slotHeight) {
            String tooltip;
            if (mouseX >= leftEdge + 215 - 16 - padding && mouseX <= leftEdge + 215 + padding) {
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
                if (mobName == this.parentGui.selectedMobName) {
                    this.parentGui.setSelectedMob(null);
                }

                iterator.remove();
            }
        }
    }
}
