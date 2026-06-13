package com.mamiyaotaru.voxelmap.gui;

import com.mamiyaotaru.voxelmap.VoxelConstants;
import com.mamiyaotaru.voxelmap.gui.overridden.GuiSlotMinimap;
import com.mamiyaotaru.voxelmap.interfaces.IDimensionManager;
import com.mamiyaotaru.voxelmap.util.Dimension;
import com.mamiyaotaru.voxelmap.util.GLShim;
import com.mamiyaotaru.voxelmap.util.GLUtils;
import com.mamiyaotaru.voxelmap.util.I18nUtils;
import net.minecraft.client.Minecraft;
import org.lwjgl.input.Mouse;

public class GuiSlotDimensions extends GuiSlotMinimap {
	final GuiAddWaypoint parentGui;
	private final IDimensionManager dimensionManager;

	public GuiSlotDimensions(GuiAddWaypoint guiAddWaypoint) {
		super(Minecraft.getMinecraft(), 101, guiAddWaypoint.getHeight(), guiAddWaypoint.getHeight() / 6 + 82 + 6, guiAddWaypoint.getHeight() / 6 + 164 + 3, 18);

		this.parentGui = guiAddWaypoint;
		this.setSlotWidth(88);
		this.setSlotXBoundsFromLeft(this.parentGui.getWidth() / 2);
		this.setShowSelectionBox(false);
		this.setShowTopBottomBG(false);
		this.setShowSlotBG(false);
		this.dimensionManager = this.parentGui.master.getDimensionManager();
		this.scrollBy(this.dimensionManager.getDimensions().indexOf(this.dimensionManager.getDimensionByID(this.parentGui.waypoint.dimensions.first())) * this.slotHeight);
	}

	@Override
	protected int getSize() {
		return this.dimensionManager.getDimensions().size();
	}

	@Override
	protected void elementClicked(int slotIndex, boolean doubleClicked, int x, int y) {
		this.parentGui.setSelectedDimension(this.dimensionManager.getDimensions().get(slotIndex));
		int leftEdge = this.parentGui.getWidth() / 2;
		byte padding = 4;
		byte iconWidth = 16;
		int width = this.slotWidth;
		if (this.mouseX >= leftEdge + width - iconWidth - padding && this.mouseX <= leftEdge + width) {
			this.parentGui.toggleDimensionSelected();
		} else {
			if (doubleClicked) {
				Mouse.next();
				this.parentGui.toggleDimensionSelected();
				return;
			}

			VoxelConstants.getLogger().info("mousex: {}, leftEdge: {}, width: {}, iw: {}, pad: {}, le: {}, re: {}", this.mouseX, leftEdge, width, iconWidth, padding, leftEdge + width - iconWidth - padding, leftEdge + width);
		}
	}

	@Override
	protected boolean isSelected(int slotIndex) {
		return this.dimensionManager.getDimensions().get(slotIndex).equals(this.parentGui.selectedDimension);
	}

	@Override
	protected void drawBackground() {
	}

	@Override
	protected void overlayBackground(int startY, int endY, int startAlpha, int endAlpha) {
	}

	@Override
	protected void drawSlot(int slotIndex, int leftEdge, int slotYPos, int topFudge, int mouseX, int mouseY, float partialTicks) {
		Dimension dim = this.dimensionManager.getDimensions().get(slotIndex);

		this.parentGui.drawCenteredString(this.parentGui.getFontRenderer(), dim.name, this.parentGui.getWidth() / 2 + this.slotWidth / 2, slotYPos + 3, 16777215);
		byte padding = 4;
		byte iconWidth = 16;
		leftEdge = this.parentGui.getWidth() / 2;
		int width = this.slotWidth;
		if (this.mouseX >= leftEdge + padding
			&& this.mouseY >= slotYPos
			&& this.mouseX <= leftEdge + width + padding
			&& this.mouseY <= slotYPos + this.slotHeight) {
			String tooltip = null;
			if (!this.parentGui.popupOpen() && this.mouseX >= leftEdge + width - iconWidth - padding && this.mouseX <= leftEdge + width) {
				tooltip = this.parentGui.waypoint.dimensions.contains(dim.ID)
					? I18nUtils.getString("minimap.waypoints.dimension.applies")
					: I18nUtils.getString("minimap.waypoints.dimension.notapplies");
			}

			GuiAddWaypoint.setTooltip(this.parentGui, tooltip);
		}

		GLShim.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GLUtils.img("textures/gui/container/beacon.png");
		int xOffset = this.parentGui.waypoint.dimensions.contains(dim.ID) ? 91 : 113;
		int yOffset = 222;
		this.parentGui.drawTexturedModalRect(leftEdge + width - iconWidth, slotYPos - 2, xOffset, yOffset, 16, 16);
	}
}
