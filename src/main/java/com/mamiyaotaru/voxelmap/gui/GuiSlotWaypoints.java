package com.mamiyaotaru.voxelmap.gui;

import com.mamiyaotaru.voxelmap.gui.overridden.GuiSlotMinimap;
import com.mamiyaotaru.voxelmap.textures.Sprite;
import com.mamiyaotaru.voxelmap.textures.TextureAtlas;
import com.mamiyaotaru.voxelmap.util.*;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

import java.awt.*;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

class GuiSlotWaypoints extends GuiSlotMinimap {
    final GuiWaypoints parentGui;
    private final ArrayList<Waypoint> waypoints;
    private ArrayList<Waypoint> waypointsFiltered;
    private String filterString = "";

    public GuiSlotWaypoints(GuiWaypoints par1GuiWaypoints) {
        super(par1GuiWaypoints.options.game, par1GuiWaypoints.getWidth(), par1GuiWaypoints.getHeight(), 54, par1GuiWaypoints.getHeight() - 90 + 4, 18);
        this.parentGui = par1GuiWaypoints;
        this.waypoints = new ArrayList<>();

        for (Waypoint pt : this.parentGui.waypointManager.getWaypoints()) {
            if (pt.inWorld && pt.inDimension) {
                this.waypoints.add(pt);
            }
        }

        this.waypointsFiltered = new ArrayList<>(this.waypoints);
    }

    protected int getSize() {
        return this.waypointsFiltered.size();
    }

    protected boolean mouseClicked(int index, int mouseEvent, double mouseX, double mouseY) {
        this.parentGui.setSelectedWaypoint(this.waypointsFiltered.get(index));
        int leftEdge = this.parentGui.getWidth() / 2 - 92 - 16;
        byte padding = 3;
        int width = 215;
        if (mouseX >= leftEdge + width - 16 - padding && mouseX <= leftEdge + width + padding) {
            if (this.doubleclick) {
                this.parentGui.setHighlightedWaypoint();
            }

            this.parentGui.toggleWaypointVisibility();
        } else if (this.doubleclick) {
            this.parentGui.editWaypoint(this.parentGui.selectedWaypoint);
        }

        return true;
    }

    protected boolean isSelected(int par1) {
        return this.waypointsFiltered.get(par1).equals(this.parentGui.selectedWaypoint);
    }

    protected int getContentHeight() {
        return this.getSize() * this.slotHeight;
    }

    protected void drawBackground() {
        this.parentGui.drawDefaultBackground();
    }

    protected void drawSlot(int slotIndex, int leftEdge, int slotYPos, int topFudge, int mouseX, int mouseY, float partialTicks) {
        Waypoint waypoint = this.waypointsFiltered.get(slotIndex);
        this.parentGui.drawCenteredString(this.parentGui.getFontRenderer(), waypoint.name, this.parentGui.getWidth() / 2, slotYPos + 3, waypoint.getUnified());
        byte padding = 3;
        if (mouseX >= leftEdge - padding && mouseY >= slotYPos && mouseX <= leftEdge + 215 + padding && mouseY <= slotYPos + this.slotHeight) {
            String tooltip;
            if (mouseX >= leftEdge + 215 - 16 - padding && mouseX <= leftEdge + 215 + padding) {
                tooltip = waypoint.enabled ? I18nUtils.getString("minimap.waypoints.disable") : I18nUtils.getString("minimap.waypoints.enable");
            } else {
                tooltip = "X: " + waypoint.getX() + " Z: " + waypoint.getZ();
                if (waypoint.getY() > 0) {
                    tooltip = tooltip + " Y: " + waypoint.getY();
                }
            }

            if (mouseX >= this.left && mouseX <= this.right && mouseY >= this.top && mouseY <= this.bottom) {
                GuiWaypoints.setTooltip(this.parentGui, tooltip);
            }
        }

        GLShim.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GLUtils.img("textures/gui/container/inventory.png");
        int xOffset = waypoint.enabled ? 72 : 90;
        int yOffset = 216;
        this.parentGui.drawTexturedModalRect(leftEdge + 198, slotYPos - 2, xOffset, yOffset, 16, 16);
        if (waypoint == this.parentGui.highlightedWaypoint) {
            int x = leftEdge + 199;
            int y = slotYPos - 1;
            GLShim.glColor4f(1.0F, 0.0F, 0.0F, 1.0F);
            TextureAtlas textureAtlas = this.parentGui.waypointManager.getTextureAtlas();
            GLUtils.disp(textureAtlas.getGlTextureId());
            Sprite icon = textureAtlas.getAtlasSprite("voxelmap:images/waypoints/target.png");
            this.drawTexturedModalRect(x, y, icon, 16, 16);
        }
    }

    public void drawTexturedModalRect(int xCoord, int yCoord, Sprite textureSprite, int widthIn, int heightIn) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder vertexbuffer = tessellator.getBuffer();
        vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX);
        vertexbuffer.pos(xCoord, yCoord + heightIn, 1.0).tex(textureSprite.getMinU(), textureSprite.getMaxV()).endVertex();
        vertexbuffer.pos(xCoord + widthIn, yCoord + heightIn, 1.0).tex(textureSprite.getMaxU(), textureSprite.getMaxV()).endVertex();
        vertexbuffer.pos(xCoord + widthIn, yCoord, 1.0).tex(textureSprite.getMaxU(), textureSprite.getMinV()).endVertex();
        vertexbuffer.pos(xCoord, yCoord, 1.0).tex(textureSprite.getMinU(), textureSprite.getMinV()).endVertex();
        tessellator.draw();
    }

    protected void sortBy(int sortKey, boolean ascending) {
        final int order = ascending ? 1 : -1;
        if (sortKey == 1) {
            final ArrayList<Waypoint> masterWaypointsList = this.parentGui.waypointManager.getWaypoints();
            Collections.sort(this.waypoints, new Comparator<Waypoint>() {
                public int compare(Waypoint waypoint1, Waypoint waypoint2) {
                    return Double.compare(masterWaypointsList.indexOf(waypoint1), masterWaypointsList.indexOf(waypoint2)) * order;
                }
            });
        } else if (sortKey == 3) {
            if (ascending) {
                Collections.sort(this.waypoints);
            } else {
                Collections.sort(this.waypoints, Collections.reverseOrder());
            }
        } else if (sortKey == 2) {
            final Collator collator = I18nUtils.getLocaleAwareCollator();
            Collections.sort(this.waypoints, new Comparator<Waypoint>() {
                public int compare(Waypoint waypoint1, Waypoint waypoint2) {
                    return collator.compare(waypoint1.name, waypoint2.name) * order;
                }
            });
        } else if (sortKey == 4) {
            Collections.sort(this.waypoints, new Comparator<Waypoint>() {
                public int compare(Waypoint waypoint1, Waypoint waypoint2) {
                    float hue1 = Color.RGBtoHSB((int) (waypoint1.red * 255.0F), (int) (waypoint1.green * 255.0F), (int) (waypoint1.blue * 255.0F), null)[0];
                    float hue2 = Color.RGBtoHSB((int) (waypoint2.red * 255.0F), (int) (waypoint2.green * 255.0F), (int) (waypoint2.blue * 255.0F), null)[0];
                    return Double.compare(hue1, hue2) * order;
                }
            });
        }

        this.updateFilter(this.filterString);
    }

    protected void updateFilter(String filterString) {
        this.filterString = filterString;
        this.waypointsFiltered = new ArrayList<>(this.waypoints);
        Iterator<Waypoint> iterator = this.waypointsFiltered.iterator();

        while (iterator.hasNext()) {
            Waypoint waypoint = iterator.next();
            if (!TextUtils.scrubCodes(waypoint.name).toLowerCase().contains(filterString)) {
                if (waypoint == this.parentGui.selectedWaypoint) {
                    this.parentGui.setSelectedWaypoint(null);
                }

                iterator.remove();
            }
        }
    }
}
