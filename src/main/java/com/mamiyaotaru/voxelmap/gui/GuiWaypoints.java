package com.mamiyaotaru.voxelmap.gui;

import com.mamiyaotaru.voxelmap.MapSettingsManager;
import com.mamiyaotaru.voxelmap.gui.overridden.GuiScreenMinimap;
import com.mamiyaotaru.voxelmap.interfaces.AbstractVoxelMap;
import com.mamiyaotaru.voxelmap.interfaces.IVoxelMap;
import com.mamiyaotaru.voxelmap.interfaces.IWaypointManager;
import com.mamiyaotaru.voxelmap.util.*;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.world.dimension.DimensionType;

import java.util.Random;
import java.util.TreeSet;

public class GuiWaypoints extends GuiScreenMinimap implements IGuiWaypoints {
    protected final MapSettingsManager options;
    protected final IWaypointManager waypointManager;
    private final GuiScreen parentScreen;
    private final int EDIT = -1;
    private final int DELETE = -2;
    private final int HIGHLIGHT = -3;
    private final int TELEPORT = -4;
    private final int SHARE = -5;
    private final int NEW = -6;
    private final int OPTIONS = -7;
    protected String screenTitle = "Waypoints";
    protected GuiTextField filter;
    protected Waypoint selectedWaypoint = null;
    protected Waypoint highlightedWaypoint = null;
    protected Waypoint newWaypoint = null;
    private final IVoxelMap master;
    private GuiSlotWaypoints waypointList;
    private GuiButton buttonEdit;
    private boolean editClicked = false;
    private GuiButton buttonDelete;
    private boolean deleteClicked = false;
    private GuiButton buttonHighlight;
    private GuiButton buttonShare;
    private GuiButton buttonTeleport;
    private GuiButton buttonSortName;
    private GuiButton buttonSortCreated;
    private GuiButton buttonSortDistance;
    private GuiButton buttonSortColor;
    private boolean addClicked = false;
    private String tooltip = null;
    private final Random generator = new Random();
    private boolean changedSort = false;

    public GuiWaypoints(GuiScreen parentScreen, IVoxelMap master) {
        this.master = master;
        this.parentScreen = parentScreen;
        this.options = master.getMapOptions();
        this.waypointManager = master.getWaypointManager();
        this.highlightedWaypoint = this.waypointManager.getHighlightedWaypoint();
    }

    static String setTooltip(GuiWaypoints par0GuiWaypoints, String par1Str) {
        return par0GuiWaypoints.tooltip = par1Str;
    }

    public void tick() {
        this.filter.tick();
    }

    public void initGui() {
        this.screenTitle = I18nUtils.getString("minimap.waypoints.title");
        this.mc.keyboardListener.enableRepeatEvents(true);
        this.waypointList = new GuiSlotWaypoints(this);
        this.addButton(this.buttonSortName = new GuiButton(2, this.getWidth() / 2 - 154, 34, 77, 20, I18nUtils.getString("minimap.waypoints.sortbyname")) {
            public void onClick(double p_onClick_1_, double p_onClick_3_) {
                GuiWaypoints.this.actionPerformed(this);
            }
        });
        this.addButton(
                this.buttonSortDistance = new GuiButton(3, this.getWidth() / 2 - 77, 34, 77, 20, I18nUtils.getString("minimap.waypoints.sortbydistance")) {
                    public void onClick(double p_onClick_1_, double p_onClick_3_) {
                        GuiWaypoints.this.actionPerformed(this);
                    }
                }
        );
        this.addButton(this.buttonSortCreated = new GuiButton(1, this.getWidth() / 2, 34, 77, 20, I18nUtils.getString("minimap.waypoints.sortbycreated")) {
            public void onClick(double p_onClick_1_, double p_onClick_3_) {
                GuiWaypoints.this.actionPerformed(this);
            }
        });
        this.addButton(this.buttonSortColor = new GuiButton(4, this.getWidth() / 2 + 77, 34, 77, 20, I18nUtils.getString("minimap.waypoints.sortbycolor")) {
            public void onClick(double p_onClick_1_, double p_onClick_3_) {
                GuiWaypoints.this.actionPerformed(this);
            }
        });
        int filterStringWidth = this.getFontRenderer().getStringWidth(I18nUtils.getString("minimap.waypoints.filter") + ":");
        this.filter = new GuiTextField(
                2, this.getFontRenderer(), this.getWidth() / 2 - 153 + filterStringWidth + 5, this.getHeight() - 80, 305 - filterStringWidth - 5, 20
        );
        this.filter.setMaxStringLength(35);
        this.filter.setFocused(true);
        this.addButton(this.buttonEdit = new GuiButton(-1, this.getWidth() / 2 - 154, this.getHeight() - 52, 74, 20, I18nUtils.getString("selectServer.edit")) {
            public void onClick(double p_onClick_1_, double p_onClick_3_) {
                GuiWaypoints.this.actionPerformed(this);
            }
        });
        this.addButton(
                this.buttonDelete = new GuiButton(-2, this.getWidth() / 2 - 76, this.getHeight() - 52, 74, 20, I18nUtils.getString("selectServer.delete")) {
                    public void onClick(double p_onClick_1_, double p_onClick_3_) {
                        GuiWaypoints.this.actionPerformed(this);
                    }
                }
        );
        this.addButton(
                this.buttonHighlight = new GuiButton(-3, this.getWidth() / 2 + 2, this.getHeight() - 52, 74, 20, I18nUtils.getString("minimap.waypoints.highlight")) {
                    public void onClick(double p_onClick_1_, double p_onClick_3_) {
                        GuiWaypoints.this.actionPerformed(this);
                    }
                }
        );
        this.addButton(
                this.buttonTeleport = new GuiButton(-4, this.getWidth() / 2 + 80, this.getHeight() - 52, 74, 20, I18nUtils.getString("minimap.waypoints.teleportto")) {
                    public void onClick(double p_onClick_1_, double p_onClick_3_) {
                        GuiWaypoints.this.actionPerformed(this);
                    }
                }
        );
        this.addButton(
                this.buttonShare = new GuiButton(-5, this.getWidth() / 2 - 154, this.getHeight() - 28, 74, 20, I18nUtils.getString("minimap.waypoints.share")) {
                    public void onClick(double p_onClick_1_, double p_onClick_3_) {
                        GuiWaypoints.this.actionPerformed(this);
                    }
                }
        );
        this.addButton(new GuiButton(-6, this.getWidth() / 2 - 76, this.getHeight() - 28, 74, 20, I18nUtils.getString("minimap.waypoints.newwaypoint")) {
            public void onClick(double p_onClick_1_, double p_onClick_3_) {
                GuiWaypoints.this.actionPerformed(this);
            }
        });
        this.addButton(new GuiButton(-7, this.getWidth() / 2 + 2, this.getHeight() - 28, 74, 20, I18nUtils.getString("menu.options")) {
            public void onClick(double p_onClick_1_, double p_onClick_3_) {
                GuiWaypoints.this.actionPerformed(this);
            }
        });
        this.addButton(new GuiButton(-200, this.getWidth() / 2 + 80, this.getHeight() - 28, 74, 20, I18nUtils.getString("gui.done")) {
            public void onClick(double p_onClick_1_, double p_onClick_3_) {
                GuiWaypoints.this.actionPerformed(this);
            }
        });
        boolean isSomethingSelected = this.selectedWaypoint != null;
        this.buttonEdit.enabled = isSomethingSelected;
        this.buttonDelete.enabled = isSomethingSelected;
        this.buttonHighlight.enabled = isSomethingSelected;
        this.buttonShare.enabled = isSomethingSelected;
        this.buttonTeleport.enabled = isSomethingSelected && this.canTeleport();
        this.sort();
    }

    private void sort() {
        int sortKey = Math.abs(this.options.sort);
        boolean ascending = this.options.sort > 0;
        this.waypointList.sortBy(sortKey, ascending);
        String arrow = ascending ? "↑" : "↓";
        if (sortKey == 2) {
            this.buttonSortName.displayString = arrow + " " + I18nUtils.getString("minimap.waypoints.sortbyname") + " " + arrow;
        } else {
            this.buttonSortName.displayString = I18nUtils.getString("minimap.waypoints.sortbyname");
        }

        if (sortKey == 3) {
            this.buttonSortDistance.displayString = arrow + " " + I18nUtils.getString("minimap.waypoints.sortbydistance") + " " + arrow;
        } else {
            this.buttonSortDistance.displayString = I18nUtils.getString("minimap.waypoints.sortbydistance");
        }

        if (sortKey == 1) {
            this.buttonSortCreated.displayString = arrow + " " + I18nUtils.getString("minimap.waypoints.sortbycreated") + " " + arrow;
        } else {
            this.buttonSortCreated.displayString = I18nUtils.getString("minimap.waypoints.sortbycreated");
        }

        if (sortKey == 4) {
            this.buttonSortColor.displayString = arrow + " " + I18nUtils.getString("minimap.waypoints.sortbycolor") + " " + arrow;
        } else {
            this.buttonSortColor.displayString = I18nUtils.getString("minimap.waypoints.sortbycolor");
        }
    }

    protected void actionPerformed(GuiButton par1GuiButton) {
        if (par1GuiButton.enabled) {
            if (par1GuiButton.id > 0) {
                this.options.setSort(par1GuiButton.id);
                this.changedSort = true;
                this.sort();
            }

            if (par1GuiButton.id == -1) {
                this.editWaypoint(this.selectedWaypoint);
            }

            if (par1GuiButton.id == -2) {
                String var2 = this.selectedWaypoint.name;
                if (var2 != null) {
                    this.deleteClicked = true;
                    String title = I18nUtils.getString("minimap.waypoints.deleteconfirm");
                    String explanation = "'" + var2 + "' " + I18nUtils.getString("selectServer.deleteWarning");
                    String affirm = I18nUtils.getString("selectServer.deleteButton");
                    String deny = I18nUtils.getString("gui.cancel");
                    GuiYesNo var8 = new GuiYesNo(this, title, explanation, affirm, deny, this.waypointManager.getWaypoints().indexOf(this.selectedWaypoint));
                    this.getMinecraft().displayGuiScreen(var8);
                }
            }

            if (par1GuiButton.id == -3) {
                this.setHighlightedWaypoint();
            }

            if (par1GuiButton.id == -5) {
                CommandUtils.sendWaypoint(this.selectedWaypoint);
            }

            if (par1GuiButton.id == -4) {
                boolean mp = !this.mc.isIntegratedServerRunning();
                int y = this.selectedWaypoint.getY() > 0 ? this.selectedWaypoint.getY() : (this.options.game.player.dimension != DimensionType.NETHER ? 128 : 64);
                this.options
                        .game
                        .player
                        .sendChatMessage(
                                "/tp " + this.options.game.player.getName().getString() + " " + this.selectedWaypoint.getX() + " " + y + " " + this.selectedWaypoint.getZ()
                        );
                if (mp) {
                    this.options.game.player.sendChatMessage("/tppos " + this.selectedWaypoint.getX() + " " + y + " " + this.selectedWaypoint.getZ());
                }

                this.getMinecraft().displayGuiScreen(null);
            }

            if (par1GuiButton.id == -6) {
                this.addWaypoint();
            }

            if (par1GuiButton.id == -7) {
                this.getMinecraft().displayGuiScreen(new GuiWaypointsOptions(this, this.options));
            }

            if (par1GuiButton.id == -200) {
                this.getMinecraft().displayGuiScreen(this.parentScreen);
            }
        }
    }

    public boolean keyPressed(int keysm, int scancode, int b) {
        if (this.filter.keyPressed(keysm, scancode, b)) {
            this.waypointList.updateFilter(this.filter.getText().toLowerCase());
        }

        return super.keyPressed(keysm, scancode, b);
    }

    public boolean charTyped(char character, int keycode) {
        if (this.filter.charTyped(character, keycode)) {
            this.waypointList.updateFilter(this.filter.getText().toLowerCase());
        }

        return super.charTyped(character, keycode);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        this.filter.mouseClicked(mouseX, mouseY, mouseButton);
        this.waypointList.mouseClicked(mouseX, mouseY, mouseButton);
        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    public boolean mouseReleased(double mouseX, double mouseY, int mouseButton) {
        this.filter.mouseReleased(mouseX, mouseY, mouseButton);
        this.waypointList.mouseReleased(mouseX, mouseY, mouseButton);
        return super.mouseReleased(mouseX, mouseY, mouseButton);
    }

    public boolean mouseDragged(double mouseX, double mouseY, int mouseEvent, double deltaX, double deltaY) {
        return this.waypointList.mouseDragged(mouseX, mouseY, mouseEvent, deltaX, deltaY);
    }

    public boolean mouseScrolled(double amount) {
        return this.waypointList.mouseScrolled(amount);
    }

    @Override
    public boolean isEditing() {
        return this.editClicked;
    }

    public void confirmResult(boolean par1, int par2) {
        if (this.deleteClicked) {
            this.deleteClicked = false;
            if (par1) {
                this.waypointManager.deleteWaypoint(this.selectedWaypoint);
                this.selectedWaypoint = null;
            }

            this.getMinecraft().displayGuiScreen(this);
        }

        if (this.editClicked) {
            this.editClicked = false;
            if (par1) {
                this.waypointManager.saveWaypoints();
            }

            this.getMinecraft().displayGuiScreen(this);
        }

        if (this.addClicked) {
            this.addClicked = false;
            if (par1) {
                this.waypointManager.addWaypoint(this.newWaypoint);
                this.setSelectedWaypoint(this.newWaypoint);
            }

            this.getMinecraft().displayGuiScreen(this);
        }
    }

    protected void setSelectedWaypoint(Waypoint waypoint) {
        this.selectedWaypoint = waypoint;
        boolean isSomethingSelected = this.selectedWaypoint != null;
        this.buttonEdit.enabled = isSomethingSelected;
        this.buttonDelete.enabled = isSomethingSelected;
        this.buttonHighlight.enabled = isSomethingSelected;
        this.buttonHighlight.displayString = I18nUtils.getString(
                isSomethingSelected && this.selectedWaypoint == this.highlightedWaypoint ? "minimap.waypoints.removehighlight" : "minimap.waypoints.highlight"
        );
        this.buttonShare.enabled = isSomethingSelected;
        this.buttonTeleport.enabled = isSomethingSelected && this.canTeleport();
    }

    protected void setHighlightedWaypoint() {
        this.waypointManager.setHighlightedWaypoint(this.selectedWaypoint, true);
        this.highlightedWaypoint = this.waypointManager.getHighlightedWaypoint();
        boolean isSomethingSelected = this.selectedWaypoint != null;
        this.buttonHighlight.displayString = I18nUtils.getString(
                isSomethingSelected && this.selectedWaypoint == this.highlightedWaypoint ? "minimap.waypoints.removehighlight" : "minimap.waypoints.highlight"
        );
    }

    protected void editWaypoint(Waypoint waypoint) {
        this.editClicked = true;
        this.getMinecraft().displayGuiScreen(new GuiAddWaypoint(this, this.master, waypoint, true));
    }

    protected void addWaypoint() {
        this.addClicked = true;
        float r;
        float g;
        float b;
        if (this.waypointManager.getWaypoints().size() == 0) {
            r = 0.0F;
            g = 1.0F;
            b = 0.0F;
        } else {
            r = this.generator.nextFloat();
            g = this.generator.nextFloat();
            b = this.generator.nextFloat();
        }

        TreeSet<DimensionContainer> dimensions = new TreeSet<>();
        dimensions.add(AbstractVoxelMap.getInstance().getDimensionManager().getDimensionContainerByDimension(this.mc.world.dimension));
        this.newWaypoint = new Waypoint(
                "",
                this.options.game.player.dimension != DimensionType.NETHER ? GameVariableAccessShim.xCoord() : GameVariableAccessShim.xCoord() * 8,
                this.options.game.player.dimension != DimensionType.NETHER ? GameVariableAccessShim.zCoord() : GameVariableAccessShim.zCoord() * 8,
                GameVariableAccessShim.yCoord(),
                true,
                r,
                g,
                b,
                "",
                this.master.getWaypointManager().getCurrentSubworldDescriptor(false),
                dimensions
        );
        this.getMinecraft().displayGuiScreen(new GuiAddWaypoint(this, this.master, this.newWaypoint, false));
    }

    protected void toggleWaypointVisibility() {
        this.selectedWaypoint.enabled = !this.selectedWaypoint.enabled;
        this.waypointManager.saveWaypoints();
    }

    public void render(int mouseX, int mouseY, float partialTicks) {
        super.drawMap();
        this.tooltip = null;
        this.waypointList.drawScreen(mouseX, mouseY, partialTicks);
        this.drawCenteredString(this.getFontRenderer(), this.screenTitle, this.getWidth() / 2, 20, 16777215);
        super.render(mouseX, mouseY, partialTicks);
        this.drawString(this.getFontRenderer(), I18nUtils.getString("minimap.waypoints.filter") + ":", this.getWidth() / 2 - 153, this.getHeight() - 75, 10526880);
        this.filter.drawTextField(mouseX, mouseY, partialTicks);
        if (this.tooltip != null) {
            this.drawTooltip(this.tooltip, mouseX, mouseY);
        }
    }

    protected void drawTooltip(String par1Str, int par2, int par3) {
        if (par1Str != null) {
            int var4 = par2 + 12;
            int var5 = par3 - 12;
            int var6 = this.getFontRenderer().getStringWidth(par1Str);
            this.drawGradientRect(var4 - 3, var5 - 3, var4 + var6 + 3, var5 + 8 + 3, -1073741824, -1073741824);
            this.getFontRenderer().drawStringWithShadow(par1Str, var4, var5, -1);
        }
    }

    public boolean canTeleport() {
        boolean allowed = false;
        boolean singlePlayer = this.options.game.isIntegratedServerRunning();
        if (singlePlayer) {
            try {
                allowed = this.mc.getIntegratedServer().getPlayerList().canSendCommands(this.mc.player.getGameProfile());
            } catch (Exception e) {
                allowed = this.mc.getIntegratedServer().getWorld(DimensionType.OVERWORLD).getWorldInfo().areCommandsAllowed();
            }
        } else {
            allowed = true;
        }

        return allowed;
    }

    @Override
    public void onGuiClosed() {
        this.mc.keyboardListener.enableRepeatEvents(false);
        if (this.changedSort) {
            super.onGuiClosed();
        }
    }
}
