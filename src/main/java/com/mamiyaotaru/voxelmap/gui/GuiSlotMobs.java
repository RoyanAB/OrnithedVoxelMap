package com.mamiyaotaru.voxelmap.gui;

import com.mamiyaotaru.voxelmap.RadarSettingsManager;
import com.mamiyaotaru.voxelmap.gui.overridden.GuiSlotMinimap;
import com.mamiyaotaru.voxelmap.util.*;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.text.TranslatableText;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

class GuiSlotMobs extends GuiSlotMinimap {
    final GuiMobs parentGui;
    private final ArrayList<GuiSlotMobs.MobItem> mobs;
    private ArrayList<GuiSlotMobs.MobItem> mobsFiltered;
    private final RadarSettingsManager options;

    public GuiSlotMobs(GuiMobs par1GuiMobs) {
        super(par1GuiMobs.options.game, par1GuiMobs.getWidth(), par1GuiMobs.getHeight(), 32, par1GuiMobs.getHeight() - 65 + 4, 18);
        this.parentGui = par1GuiMobs;
        this.options = this.parentGui.options;
        this.mobs = new ArrayList<>();

        for (EnumMobs mob : EnumMobs.values()) {
            if (mob.isTopLevelUnit && (mob.isHostile && this.options.showHostiles || mob.isNeutral && this.options.showNeutrals)) {
                this.mobs.add(new GuiSlotMobs.MobItem(this.parentGui, mob.id));
            }
        }

        for (CustomMob mobx : CustomMobsManager.mobs) {
            if (mobx.isHostile && this.options.showHostiles || mobx.isNeutral && this.options.showNeutrals) {
                this.mobs.add(new GuiSlotMobs.MobItem(this.parentGui, mobx.id));
            }
        }

        final Collator collator = I18nUtils.getLocaleAwareCollator();
        Collections.sort(this.mobs, new Comparator<GuiSlotMobs.MobItem>() {
            public int compare(GuiSlotMobs.MobItem mob1, GuiSlotMobs.MobItem mob2) {
                return collator.compare(mob1.name, mob2.name);
            }
        });
        this.mobsFiltered = new ArrayList<>(this.mobs);
        this.mobsFiltered.forEach(this::addEntry);
    }

    private static String getTranslatedName(String name) {
        if (name.indexOf(".") == -1) {
            name = "entity.minecraft." + name.toLowerCase();
        }

        name = I18nUtils.getString(name);
        name = name.replaceAll("^entity.minecraft.", "");
        name = name.replace("_", " ");
        name = name.substring(0, 1).toUpperCase() + name.substring(1);
        return TextUtils.scrubCodes(name);
    }

    public void setSelected(Entry item) {
        this.setSelected(item);
    }

    public void setSelected(GuiSlotMobs.MobItem item) {
        super.setSelected(item);
        if (this.getSelected() instanceof GuiSlotMobs.MobItem) {
            NarratorManager.INSTANCE.narrate(new TranslatableText("narrator.select", ((MobItem) this.getSelected()).name).getString());
        }

        this.parentGui.setSelectedMob(item.id);
    }

    protected boolean isSelectedItem(int par1) {
        return this.mobsFiltered.get(par1).id.equals(this.parentGui.selectedMobId);
    }

    protected int getMaxPosition() {
        return this.getItemCount() * this.itemHeight;
    }

    public void renderBackground() {
        this.parentGui.renderBackground();
    }

    protected void updateFilter(String filterString) {
        this.clearEntries();
        this.mobsFiltered = new ArrayList<>(this.mobs);
        Iterator<GuiSlotMobs.MobItem> iterator = this.mobsFiltered.iterator();

        while (iterator.hasNext()) {
            String mobName = iterator.next().name;
            if (!mobName.toLowerCase().contains(filterString)) {
                if (mobName == this.parentGui.selectedMobId) {
                    this.parentGui.setSelectedMob(null);
                }

                iterator.remove();
            }
        }

        this.mobsFiltered.forEach(this::addEntry);
    }

    public class MobItem extends Entry<GuiSlotMobs.MobItem> {
        private final GuiMobs parentGui;
        private final String id;
        private final String name;

        protected MobItem(GuiMobs mobsScreen, String id) {
            this.parentGui = mobsScreen;
            this.id = id;
            this.name = GuiSlotMobs.getTranslatedName(id);
        }

        public void render(
                int slotIndex, int slotYPos, int leftEdge, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean mouseOver, float partialTicks
        ) {
            boolean isHostile = false;
            boolean isNeutral = false;
            boolean isEnabled = true;
            EnumMobs mob = EnumMobs.getMobByName(this.id);
            if (mob != null) {
                isHostile = mob.isHostile;
                isNeutral = mob.isNeutral;
                isEnabled = mob.enabled;
            } else {
                CustomMob customMob = CustomMobsManager.getCustomMobByType(this.id);
                if (customMob != null) {
                    isHostile = customMob.isHostile;
                    isNeutral = customMob.isNeutral;
                    isEnabled = customMob.enabled;
                }
            }

            int red = isHostile ? 255 : 0;
            int green = isNeutral ? 255 : 0;
            int color = -16777216 + (red << 16) + (green << 8);
            this.parentGui.drawCenteredString(this.parentGui.getFontRenderer(), this.name, this.parentGui.getWidth() / 2, slotYPos + 3, color);
            byte padding = 3;
            if (mouseX >= leftEdge - padding && mouseY >= slotYPos && mouseX <= leftEdge + 215 + padding && mouseY <= slotYPos + GuiSlotMobs.this.itemHeight) {
                String tooltip;
                if (mouseX >= leftEdge + 215 - 16 - padding && mouseX <= leftEdge + 215 + padding) {
                    tooltip = isEnabled ? I18nUtils.getString("options.minimap.mobs.disable") : I18nUtils.getString("options.minimap.mobs.enable");
                } else {
                    tooltip = isEnabled ? I18nUtils.getString("options.minimap.mobs.enabled") : I18nUtils.getString("options.minimap.mobs.disabled");
                }

                GuiMobs.setTooltip(this.parentGui, tooltip);
            }

            GLShim.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            GLUtils.img("textures/mob_effect/" + (isEnabled ? "night_vision.png" : "blindness.png"));
            GuiMobs.blit(leftEdge + 198, slotYPos - 2, GuiSlotMobs.this.blitOffset, 0.0F, 0.0F, 18, 18, 18, 18);
        }

        public boolean mouseClicked(double mouseX, double mouseY, int mouseEvent) {
            GuiSlotMobs.this.setSelected(this);
            int leftEdge = this.parentGui.getWidth() / 2 - 92 - 16;
            byte padding = 3;
            int width = 215;
            if (mouseX >= leftEdge + width - 16 - padding && mouseX <= leftEdge + width + padding) {
                this.parentGui.toggleMobVisibility();
            } else if (GuiSlotMobs.this.doubleclick) {
                this.parentGui.toggleMobVisibility();
            }

            return true;
        }
    }
}
