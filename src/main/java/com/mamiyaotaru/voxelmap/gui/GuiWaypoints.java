package com.mamiyaotaru.voxelmap.gui;

import com.mamiyaotaru.voxelmap.MapSettingsManager;
import com.mamiyaotaru.voxelmap.gui.overridden.GuiScreenMinimap;
import com.mamiyaotaru.voxelmap.interfaces.AbstractVoxelMap;
import com.mamiyaotaru.voxelmap.interfaces.IVoxelMap;
import com.mamiyaotaru.voxelmap.interfaces.IWaypointManager;
import com.mamiyaotaru.voxelmap.util.*;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.world.dimension.DimensionType;

import java.util.Random;
import java.util.TreeSet;

public class GuiWaypoints extends GuiScreenMinimap implements IGuiWaypoints {
    protected final MapSettingsManager options;
    protected final IWaypointManager waypointManager;
    private final Screen parentScreen;
    private final int EDIT = -1;
    private final int DELETE = -2;
    private final int HIGHLIGHT = -3;
    private final int TELEPORT = -4;
    private final int SHARE = -5;
    private final int NEW = -6;
    private final int OPTIONS = -7;
    protected String screenTitle = "Waypoints";
    protected TextFieldWidget filter;
    protected Waypoint selectedWaypoint = null;
    protected Waypoint highlightedWaypoint = null;
    protected Waypoint newWaypoint = null;
    private final IVoxelMap master;
    private GuiSlotWaypoints waypointList;
    private ButtonWidget buttonEdit;
    private boolean editClicked = false;
    private ButtonWidget buttonDelete;
    private boolean deleteClicked = false;
    private ButtonWidget buttonHighlight;
    private ButtonWidget buttonShare;
    private ButtonWidget buttonTeleport;
    private ButtonWidget buttonSortName;
    private ButtonWidget buttonSortCreated;
    private ButtonWidget buttonSortDistance;
    private ButtonWidget buttonSortColor;
    private boolean addClicked = false;
    private String tooltip = null;
    private final Random generator = new Random();
    private boolean changedSort = false;

    public GuiWaypoints(Screen parentScreen, IVoxelMap master) {
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

    public void init() {
        this.screenTitle = I18nUtils.getString("minimap.waypoints.title");
        this.getMinecraft().keyboard.enableRepeatEvents(true);
        this.waypointList = new GuiSlotWaypoints(this);
        this.addButton(this.buttonSortName = new ButtonWidget(this.getWidth() / 2 - 154, 34, 77, 20, I18nUtils.getString("minimap.waypoints.sortbyname"), null) {
            public void onPress() {
                GuiWaypoints.this.actionPerformed(this, 2);
            }
        });
        this.addButton(
                this.buttonSortDistance = new ButtonWidget(this.getWidth() / 2 - 77, 34, 77, 20, I18nUtils.getString("minimap.waypoints.sortbydistance"), null) {
                    public void onPress() {
                        GuiWaypoints.this.actionPerformed(this, 3);
                    }
                }
        );
        this.addButton(this.buttonSortCreated = new ButtonWidget(this.getWidth() / 2, 34, 77, 20, I18nUtils.getString("minimap.waypoints.sortbycreated"), null) {
            public void onPress() {
                GuiWaypoints.this.actionPerformed(this, 1);
            }
        });
        this.addButton(
                this.buttonSortColor = new ButtonWidget(this.getWidth() / 2 + 77, 34, 77, 20, I18nUtils.getString("minimap.waypoints.sortbycolor"), null) {
                    public void onPress() {
                        GuiWaypoints.this.actionPerformed(this, 4);
                    }
                }
        );
        int filterStringWidth = this.getFontRenderer().getStringWidth(I18nUtils.getString("minimap.waypoints.filter") + ":");
        this.filter = new TextFieldWidget(
                this.getFontRenderer(), this.getWidth() / 2 - 153 + filterStringWidth + 5, this.getHeight() - 80, 305 - filterStringWidth - 5, 20, ""
        );
        this.filter.setMaxLength(35);
        this.addButton(this.filter);
        this.addButton(
                this.buttonEdit = new ButtonWidget(this.getWidth() / 2 - 154, this.getHeight() - 52, 74, 20, I18nUtils.getString("selectServer.edit"), null) {
                    public void onPress() {
                        GuiWaypoints.this.actionPerformed(this, -1);
                    }
                }
        );
        this.addButton(
                this.buttonDelete = new ButtonWidget(this.getWidth() / 2 - 76, this.getHeight() - 52, 74, 20, I18nUtils.getString("selectServer.delete"), null) {
                    public void onPress() {
                        GuiWaypoints.this.actionPerformed(this, -2);
                    }
                }
        );
        this.addButton(
                this.buttonHighlight = new ButtonWidget(
                        this.getWidth() / 2 + 2, this.getHeight() - 52, 74, 20, I18nUtils.getString("minimap.waypoints.highlight"), null
                ) {
                    public void onPress() {
                        GuiWaypoints.this.actionPerformed(this, -3);
                    }
                }
        );
        this.addButton(
                this.buttonTeleport = new ButtonWidget(
                        this.getWidth() / 2 + 80, this.getHeight() - 52, 74, 20, I18nUtils.getString("minimap.waypoints.teleportto"), null
                ) {
                    public void onPress() {
                        GuiWaypoints.this.actionPerformed(this, -4);
                    }
                }
        );
        this.addButton(
                this.buttonShare = new ButtonWidget(this.getWidth() / 2 - 154, this.getHeight() - 28, 74, 20, I18nUtils.getString("minimap.waypoints.share"), null) {
                    public void onPress() {
                        GuiWaypoints.this.actionPerformed(this, -5);
                    }
                }
        );
        this.addButton(new ButtonWidget(this.getWidth() / 2 - 76, this.getHeight() - 28, 74, 20, I18nUtils.getString("minimap.waypoints.newwaypoint"), null) {
            public void onPress() {
                GuiWaypoints.this.actionPerformed(this, -6);
            }
        });
        this.addButton(new ButtonWidget(this.getWidth() / 2 + 2, this.getHeight() - 28, 74, 20, I18nUtils.getString("menu.options"), null) {
            public void onPress() {
                GuiWaypoints.this.actionPerformed(this, -7);
            }
        });
        this.addButton(new ButtonWidget(this.getWidth() / 2 + 80, this.getHeight() - 28, 74, 20, I18nUtils.getString("gui.done"), null) {
            public void onPress() {
                GuiWaypoints.this.actionPerformed(this, -200);
            }
        });
        this.setFocused(this.filter);
        this.filter.method_1876(true);
        boolean isSomethingSelected = this.selectedWaypoint != null;
        this.buttonEdit.active = isSomethingSelected;
        this.buttonDelete.active = isSomethingSelected;
        this.buttonHighlight.active = isSomethingSelected;
        this.buttonShare.active = isSomethingSelected;
        this.buttonTeleport.active = isSomethingSelected && this.canTeleport();
        this.sort();
    }

    private void sort() {
        int sortKey = Math.abs(this.options.sort);
        boolean ascending = this.options.sort > 0;
        this.waypointList.sortBy(sortKey, ascending);
        String arrow = ascending ? "↑" : "↓";
        if (sortKey == 2) {
            this.buttonSortName.setMessage(arrow + " " + I18nUtils.getString("minimap.waypoints.sortbyname") + " " + arrow);
        } else {
            this.buttonSortName.setMessage(I18nUtils.getString("minimap.waypoints.sortbyname"));
        }

        if (sortKey == 3) {
            this.buttonSortDistance.setMessage(arrow + " " + I18nUtils.getString("minimap.waypoints.sortbydistance") + " " + arrow);
        } else {
            this.buttonSortDistance.setMessage(I18nUtils.getString("minimap.waypoints.sortbydistance"));
        }

        if (sortKey == 1) {
            this.buttonSortCreated.setMessage(arrow + " " + I18nUtils.getString("minimap.waypoints.sortbycreated") + " " + arrow);
        } else {
            this.buttonSortCreated.setMessage(I18nUtils.getString("minimap.waypoints.sortbycreated"));
        }

        if (sortKey == 4) {
            this.buttonSortColor.setMessage(arrow + " " + I18nUtils.getString("minimap.waypoints.sortbycolor") + " " + arrow);
        } else {
            this.buttonSortColor.setMessage(I18nUtils.getString("minimap.waypoints.sortbycolor"));
        }
    }

    protected void actionPerformed(ButtonWidget par1GuiButton, int id) {
        if (par1GuiButton.active) {
            if (id > 0) {
                this.options.setSort(id);
                this.changedSort = true;
                this.sort();
            }

            if (id == -1) {
                this.editWaypoint(this.selectedWaypoint);
            }

            if (id == -2) {
                String var2x = this.selectedWaypoint.name;
                if (var2x != null) {
                    this.deleteClicked = true;
                    String title = I18nUtils.getString("minimap.waypoints.deleteconfirm");
                    TranslatableText explanation = new TranslatableText("selectServer.deleteWarning", var2x);
                    String affirm = I18nUtils.getString("selectServer.deleteButton");
                    String deny = I18nUtils.getString("gui.cancel");
                    ConfirmScreen var8 = new ConfirmScreen(this, new LiteralText(title), explanation, affirm, deny);
                    this.getMinecraft().openScreen(var8);
                }
            }

            if (id == -3) {
                this.setHighlightedWaypoint();
            }

            if (id == -5) {
                CommandUtils.sendWaypoint(this.selectedWaypoint);
            }

            if (id == -4) {
                boolean mp = !this.minecraft.isIntegratedServerRunning();
                int y = this.selectedWaypoint.getY() > 0
                        ? this.selectedWaypoint.getY()
                        : (this.options.game.player.dimension != DimensionType.THE_NETHER ? 128 : 64);
                this.options
                        .game
                        .player
                        .sendChatMessage(
                                "/tp " + this.options.game.player.getName().getString() + " " + this.selectedWaypoint.getX() + " " + y + " " + this.selectedWaypoint.getZ()
                        );
                if (mp) {
                    this.options.game.player.sendChatMessage("/tppos " + this.selectedWaypoint.getX() + " " + y + " " + this.selectedWaypoint.getZ());
                }

                this.getMinecraft().openScreen(null);
            }

            if (id == -6) {
                this.addWaypoint();
            }

            if (id == -7) {
                this.getMinecraft().openScreen(new GuiWaypointsOptions(this, this.options));
            }

            if (id == -200) {
                this.getMinecraft().openScreen(this.parentScreen);
            }
        }
    }

    public boolean keyPressed(int keysm, int scancode, int b) {
        boolean OK = super.keyPressed(keysm, scancode, b);
        if (this.filter.isFocused()) {
            this.waypointList.updateFilter(this.filter.getText().toLowerCase());
        }

        return OK;
    }

    public boolean charTyped(char character, int keycode) {
        boolean OK = super.charTyped(character, keycode);
        if (this.filter.isFocused()) {
            this.waypointList.updateFilter(this.filter.getText().toLowerCase());
        }

        return OK;
    }

    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        this.waypointList.mouseClicked(mouseX, mouseY, mouseButton);
        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    public boolean mouseReleased(double mouseX, double mouseY, int mouseButton) {
        this.waypointList.mouseReleased(mouseX, mouseY, mouseButton);
        return super.mouseReleased(mouseX, mouseY, mouseButton);
    }

    public boolean mouseDragged(double mouseX, double mouseY, int mouseEvent, double deltaX, double deltaY) {
        return this.waypointList.mouseDragged(mouseX, mouseY, mouseEvent, deltaX, deltaY);
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        return this.waypointList.mouseScrolled(mouseX, mouseY, amount);
    }

    @Override
    public boolean isEditing() {
        return this.editClicked;
    }

    public void accept(boolean par1) {
        if (this.deleteClicked) {
            this.deleteClicked = false;
            if (par1) {
                this.waypointManager.deleteWaypoint(this.selectedWaypoint);
                this.selectedWaypoint = null;
            }

            this.getMinecraft().openScreen(this);
        }

        if (this.editClicked) {
            this.editClicked = false;
            if (par1) {
                this.waypointManager.saveWaypoints();
            }

            this.getMinecraft().openScreen(this);
        }

        if (this.addClicked) {
            this.addClicked = false;
            if (par1) {
                this.waypointManager.addWaypoint(this.newWaypoint);
                this.setSelectedWaypoint(this.newWaypoint);
            }

            this.getMinecraft().openScreen(this);
        }
    }

    protected void setSelectedWaypoint(Waypoint waypoint) {
        this.selectedWaypoint = waypoint;
        boolean isSomethingSelected = this.selectedWaypoint != null;
        this.buttonEdit.active = isSomethingSelected;
        this.buttonDelete.active = isSomethingSelected;
        this.buttonHighlight.active = isSomethingSelected;
        this.buttonHighlight
                .setMessage(
                        I18nUtils.getString(
                                isSomethingSelected && this.selectedWaypoint == this.highlightedWaypoint ? "minimap.waypoints.removehighlight" : "minimap.waypoints.highlight"
                        )
                );
        this.buttonShare.active = isSomethingSelected;
        this.buttonTeleport.active = isSomethingSelected && this.canTeleport();
    }

    protected void setHighlightedWaypoint() {
        this.waypointManager.setHighlightedWaypoint(this.selectedWaypoint, true);
        this.highlightedWaypoint = this.waypointManager.getHighlightedWaypoint();
        boolean isSomethingSelected = this.selectedWaypoint != null;
        this.buttonHighlight
                .setMessage(
                        I18nUtils.getString(
                                isSomethingSelected && this.selectedWaypoint == this.highlightedWaypoint ? "minimap.waypoints.removehighlight" : "minimap.waypoints.highlight"
                        )
                );
    }

    protected void editWaypoint(Waypoint waypoint) {
        this.editClicked = true;
        this.getMinecraft().openScreen(new GuiAddWaypoint(this, this.master, waypoint, true));
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
        dimensions.add(AbstractVoxelMap.getInstance().getDimensionManager().getDimensionContainerByDimension(this.getMinecraft().world.dimension));
        this.newWaypoint = new Waypoint(
                "",
                this.options.game.player.dimension != DimensionType.THE_NETHER ? GameVariableAccessShim.xCoord() : GameVariableAccessShim.xCoord() * 8,
                this.options.game.player.dimension != DimensionType.THE_NETHER ? GameVariableAccessShim.zCoord() : GameVariableAccessShim.zCoord() * 8,
                GameVariableAccessShim.yCoord(),
                true,
                r,
                g,
                b,
                "",
                this.master.getWaypointManager().getCurrentSubworldDescriptor(false),
                dimensions
        );
        this.getMinecraft().openScreen(new GuiAddWaypoint(this, this.master, this.newWaypoint, false));
    }

    protected void toggleWaypointVisibility() {
        this.selectedWaypoint.enabled = !this.selectedWaypoint.enabled;
        this.waypointManager.saveWaypoints();
    }

    public void render(int mouseX, int mouseY, float partialTicks) {
        super.drawMap();
        this.tooltip = null;
        this.waypointList.render(mouseX, mouseY, partialTicks);
        this.drawCenteredString(this.getFontRenderer(), this.screenTitle, this.getWidth() / 2, 20, 16777215);
        super.render(mouseX, mouseY, partialTicks);
        this.drawString(this.getFontRenderer(), I18nUtils.getString("minimap.waypoints.filter") + ":", this.getWidth() / 2 - 153, this.getHeight() - 75, 10526880);
        this.filter.render(mouseX, mouseY, partialTicks);
        if (this.tooltip != null) {
            this.renderTooltip(this.tooltip, mouseX, mouseY);
        }
    }

    public void renderTooltip(String par1Str, int par2, int par3) {
        if (par1Str != null) {
            int var4 = par2 + 12;
            int var5 = par3 - 12;
            int var6 = this.getFontRenderer().getStringWidth(par1Str);
            this.fillGradient(var4 - 3, var5 - 3, var4 + var6 + 3, var5 + 8 + 3, -1073741824, -1073741824);
            this.getFontRenderer().drawWithShadow(par1Str, var4, var5, -1);
        }
    }

    public boolean canTeleport() {
        boolean allowed = false;
        boolean singlePlayer = this.options.game.isIntegratedServerRunning();
        if (singlePlayer) {
            try {
                allowed = this.getMinecraft().getServer().getPlayerManager().isOperator(this.getMinecraft().player.getGameProfile());
            } catch (Exception e) {
                allowed = this.getMinecraft().getServer().getWorld(DimensionType.OVERWORLD).getLevelProperties().areCommandsAllowed();
            }
        } else {
            allowed = true;
        }

        return allowed;
    }

    @Override
    public void removed() {
        this.minecraft.keyboard.enableRepeatEvents(false);
        if (this.changedSort) {
            super.removed();
        }
    }
}
