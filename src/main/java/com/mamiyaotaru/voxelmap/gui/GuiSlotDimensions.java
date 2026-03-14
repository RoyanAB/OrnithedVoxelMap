package com.mamiyaotaru.voxelmap.gui;

import com.mamiyaotaru.voxelmap.gui.overridden.GuiSlotMinimap;
import com.mamiyaotaru.voxelmap.interfaces.IDimensionManager;
import com.mamiyaotaru.voxelmap.util.DimensionContainer;
import com.mamiyaotaru.voxelmap.util.GLShim;
import com.mamiyaotaru.voxelmap.util.GLUtils;
import com.mamiyaotaru.voxelmap.util.I18nUtils;
import net.minecraft.client.Minecraft;

class GuiSlotDimensions extends GuiSlotMinimap {
    final GuiAddWaypoint parentGui;
    private final IDimensionManager dimensionManager;

    public GuiSlotDimensions(GuiAddWaypoint par1GuiWaypoints) {
        super(
                Minecraft.getInstance(), 101, par1GuiWaypoints.getHeight(), par1GuiWaypoints.getHeight() / 6 + 82 + 6, par1GuiWaypoints.getHeight() / 6 + 164 + 3, 18
        );
        this.parentGui = par1GuiWaypoints;
        this.setSlotWidth(88);
        this.setSlotXBoundsFromLeft(this.parentGui.getWidth() / 2);
        this.setShowSelectionBox(false);
        this.setShowTopBottomBG(false);
        this.setShowSlotBG(false);
        this.dimensionManager = this.parentGui.master.getDimensionManager();
        this.scrollBy(this.dimensionManager.getDimensions().indexOf(this.parentGui.waypoint.dimensions.first()) * this.slotHeight);
    }

    protected int getSize() {
        return this.dimensionManager.getDimensions().size();
    }

    protected boolean mouseClicked(int slotIndex, int mouseEvent, double mouseX, double mouseY) {
        this.parentGui.setSelectedDimension(this.dimensionManager.getDimensions().get(slotIndex));
        int leftEdge = this.parentGui.getWidth() / 2;
        byte padding = 4;
        byte iconWidth = 16;
        int width = this.slotWidth;
        if (mouseX >= leftEdge + width - iconWidth - padding && mouseX <= leftEdge + width) {
            this.parentGui.toggleDimensionSelected();
        } else if (this.doubleclick) {
            this.parentGui.toggleDimensionSelected();
        }

        return true;
    }

    protected boolean isSelected(int par1) {
        return this.dimensionManager.getDimensions().get(par1).equals(this.parentGui.selectedDimension);
    }

    protected void drawBackground() {
    }

    protected void overlayBackground(int par1, int par2, int par3, int par4) {
    }

    protected void drawSlot(int slotIndex, int leftEdge, int slotYPos, int topFudge, int mouseX, int mouseY, float partialTicks) {
        DimensionContainer dim = this.dimensionManager.getDimensions().get(slotIndex);
        String name = dim.name;
        if (name.equals("notLoaded") || name.equals("failedToLoad")) {
            name = "dimension " + dim.id + "(" + Minecraft.getInstance().world.getChunkProvider().getClass().getSimpleName() + ")";
        }

        this.parentGui
                .drawCenteredString(this.parentGui.getFontRenderer(), dim.getDisplayName(), this.parentGui.getWidth() / 2 + this.slotWidth / 2, slotYPos + 3, 16777215);
        byte padding = 4;
        byte iconWidth = 16;
        leftEdge = this.parentGui.getWidth() / 2;
        int width = this.slotWidth;
        if (mouseX >= leftEdge + padding && mouseY >= slotYPos && mouseX <= leftEdge + width + padding && mouseY <= slotYPos + this.slotHeight) {
            String tooltip = null;
            if (!this.parentGui.popupOpen() && mouseX >= leftEdge + width - iconWidth - padding && mouseX <= leftEdge + width) {
                tooltip = this.parentGui.waypoint.dimensions.contains(dim)
                        ? I18nUtils.getString("minimap.waypoints.dimension.applies")
                        : I18nUtils.getString("minimap.waypoints.dimension.notapplies");
            } else {
                tooltip = null;
            }

            GuiAddWaypoint.setTooltip(this.parentGui, tooltip);
        }

        GLShim.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GLUtils.img("textures/gui/container/beacon.png");
        int xOffset = this.parentGui.waypoint.dimensions.contains(dim) ? 91 : 113;
        int yOffset = 222;
        this.parentGui.drawTexturedModalRect(leftEdge + width - iconWidth, slotYPos - 2, xOffset, yOffset, 16, 16);
    }
}
