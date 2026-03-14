package com.mamiyaotaru.voxelmap.gui;

import com.mamiyaotaru.voxelmap.gui.overridden.GuiSlotMinimap;
import com.mamiyaotaru.voxelmap.interfaces.IDimensionManager;
import com.mamiyaotaru.voxelmap.util.DimensionContainer;
import com.mamiyaotaru.voxelmap.util.GLShim;
import com.mamiyaotaru.voxelmap.util.GLUtils;
import com.mamiyaotaru.voxelmap.util.I18nUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.text.TranslatableText;

import java.util.ArrayList;

class GuiSlotDimensions extends GuiSlotMinimap {
    final GuiAddWaypoint parentGui;
    private final IDimensionManager dimensionManager;
    private final ArrayList<GuiSlotDimensions.DimensionItem> dimensions;

    public GuiSlotDimensions(GuiAddWaypoint par1GuiWaypoints) {
        super(
                MinecraftClient.getInstance(),
                101,
                par1GuiWaypoints.getHeight(),
                par1GuiWaypoints.getHeight() / 6 + 82 + 6,
                par1GuiWaypoints.getHeight() / 6 + 164 + 3,
                18
        );
        this.parentGui = par1GuiWaypoints;
        this.setSlotWidth(88);
        this.setLeftPos(this.parentGui.getWidth() / 2);
        this.setRenderSelection(false);
        this.setShowTopBottomBG(false);
        this.setShowSlotBG(false);
        this.dimensionManager = this.parentGui.master.getDimensionManager();
        this.dimensions = new ArrayList<>();
        GuiSlotDimensions.DimensionItem first = null;

        for (DimensionContainer dim : this.dimensionManager.getDimensions()) {
            GuiSlotDimensions.DimensionItem item = new GuiSlotDimensions.DimensionItem(this.parentGui, dim);
            this.dimensions.add(item);
            if (dim.equals(this.parentGui.waypoint.dimensions.first())) {
                first = item;
            }
        }

        this.dimensions.forEach(this::addEntry);
        if (first != null) {
            this.ensureVisible(first);
        }
    }

    public void setSelected(Entry item) {
        this.setSelected((GuiSlotDimensions.DimensionItem) item);
    }

    public void setSelected(GuiSlotDimensions.DimensionItem item) {
        super.setSelected(item);
        if (this.getSelected() instanceof GuiSlotDimensions.DimensionItem) {
            NarratorManager.INSTANCE
                    .narrate(new TranslatableText("narrator.select", ((DimensionItem) this.getSelected()).dim.name).getString());
        }

        this.parentGui.setSelectedDimension(item.dim);
    }

    protected boolean isSelectedItem(int par1) {
        return this.dimensions.get(par1).dim.equals(this.parentGui.selectedDimension);
    }

    public void renderBackground() {
    }

    protected void renderHoleBackground(int par1, int par2, int par3, int par4) {
    }

    public class DimensionItem extends Entry<GuiSlotDimensions.DimensionItem> {
        private final GuiAddWaypoint parentGui;
        private final DimensionContainer dim;

        protected DimensionItem(GuiAddWaypoint waypointScreen, DimensionContainer dim) {
            this.parentGui = waypointScreen;
            this.dim = dim;
        }

        public void render(
                int slotIndex, int slotYPos, int leftEdge, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean mouseOver, float partialTicks
        ) {
            this.parentGui
                    .drawCenteredString(
                            this.parentGui.getFontRenderer(),
                            this.dim.getDisplayName(),
                            this.parentGui.getWidth() / 2 + GuiSlotDimensions.this.slotWidth / 2,
                            slotYPos + 3,
                            16777215
                    );
            byte padding = 4;
            byte iconWidth = 16;
            leftEdge = this.parentGui.getWidth() / 2;
            int width = GuiSlotDimensions.this.slotWidth;
            if (mouseX >= leftEdge + padding
                    && mouseY >= slotYPos
                    && mouseX <= leftEdge + width + padding
                    && mouseY <= slotYPos + GuiSlotDimensions.this.itemHeight) {
                String tooltip = null;
                if (!this.parentGui.popupOpen() && mouseX >= leftEdge + width - iconWidth - padding && mouseX <= leftEdge + width) {
                    tooltip = this.parentGui.waypoint.dimensions.contains(this.dim)
                            ? I18nUtils.getString("minimap.waypoints.dimension.applies")
                            : I18nUtils.getString("minimap.waypoints.dimension.notapplies");
                } else {
                    tooltip = null;
                }

                GuiAddWaypoint.setTooltip(this.parentGui, tooltip);
            }

            GLShim.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            GLUtils.img("textures/gui/container/beacon.png");
            int xOffset = this.parentGui.waypoint.dimensions.contains(this.dim) ? 91 : 113;
            int yOffset = 222;
            this.parentGui.blit(leftEdge + width - iconWidth, slotYPos - 2, xOffset, yOffset, 16, 16);
        }

        public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
            GuiSlotDimensions.this.setSelected(this);
            int leftEdge = this.parentGui.getWidth() / 2;
            byte padding = 4;
            byte iconWidth = 16;
            int width = GuiSlotDimensions.this.slotWidth;
            if (mouseX >= leftEdge + width - iconWidth - padding && mouseX <= leftEdge + width) {
                this.parentGui.toggleDimensionSelected();
            } else if (GuiSlotDimensions.this.doubleclick) {
                this.parentGui.toggleDimensionSelected();
            }

            return true;
        }
    }
}
