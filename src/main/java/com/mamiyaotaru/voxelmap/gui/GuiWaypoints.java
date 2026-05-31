package com.mamiyaotaru.voxelmap.gui;

import com.mamiyaotaru.voxelmap.MapSettingsManager;
import com.mamiyaotaru.voxelmap.gui.overridden.GuiScreenMinimap;
import com.mamiyaotaru.voxelmap.interfaces.IVoxelMap;
import com.mamiyaotaru.voxelmap.interfaces.IWaypointManager;
import com.mamiyaotaru.voxelmap.util.CommandUtils;
import com.mamiyaotaru.voxelmap.util.GameVariableAccessShim;
import com.mamiyaotaru.voxelmap.util.I18nUtils;
import com.mamiyaotaru.voxelmap.util.Waypoint;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.GuiYesNo;
import org.lwjgl.input.Keyboard;

import java.util.Random;
import java.util.TreeSet;

@SuppressWarnings("unused")
public class GuiWaypoints extends GuiScreenMinimap implements IGuiWaypoints {
	protected final MapSettingsManager options;
	protected final IWaypointManager waypointManager;
	private final Random generator = new Random();
	private final GuiScreen parentScreen;
	private final IVoxelMap master;
	private final int EDIT = -1;
	private final int DELETE = -2;
	private final int HIGHLIGHT = -3;
	private final int TELEPORT = -4;
	private final int SHARE = -5;
	private final int NEW = -6;
	private final int OPTIONS = -7;
	private final int DONE = -200;
	protected String screenTitle = "Waypoints";
	protected GuiTextField filter;
	protected Waypoint selectedWaypoint;
	protected Waypoint highlightedWaypoint;
	protected Waypoint newWaypoint;
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
	private String tooltip;
	private boolean changedSort = false;

	public GuiWaypoints(GuiScreen parentScreen, IVoxelMap master) {
		this.master = master;
		this.parentScreen = parentScreen;
		this.options = master.getMapOptions();
		this.waypointManager = master.getWaypointManager();
		this.highlightedWaypoint = this.waypointManager.getHighlightedWaypoint();
	}

	static String setTooltip(GuiWaypoints guiWaypoints, String string) {
		return guiWaypoints.tooltip = string;
	}

	public void updateScreen() {
		this.filter.updateCursorCounter();
	}

	public void initGui() {
		this.screenTitle = I18nUtils.getString("minimap.waypoints.title");
		Keyboard.enableRepeatEvents(true);
		this.waypointList = new GuiSlotWaypoints(this);
		this.waypointList.registerScrollButtons(7, 8);
		this.getButtonList()
			.add(this.buttonSortName = new GuiButton(2, this.getWidth() / 2 - 154, 34, 77, 20, I18nUtils.getString("minimap.waypoints.sortbyname")));
		this.getButtonList()
			.add(this.buttonSortDistance = new GuiButton(3, this.getWidth() / 2 - 77, 34, 77, 20, I18nUtils.getString("minimap.waypoints.sortbydistance")));
		this.getButtonList()
			.add(this.buttonSortCreated = new GuiButton(1, this.getWidth() / 2, 34, 77, 20, I18nUtils.getString("minimap.waypoints.sortbycreated")));
		this.getButtonList()
			.add(this.buttonSortColor = new GuiButton(4, this.getWidth() / 2 + 77, 34, 77, 20, I18nUtils.getString("minimap.waypoints.sortbycolor")));
		int filterStringWidth = this.getFontRenderer().getStringWidth(I18nUtils.getString("minimap.waypoints.filter") + ":");
		this.filter = new GuiTextField(
			2, this.getFontRenderer(), this.getWidth() / 2 - 153 + filterStringWidth + 5, this.getHeight() - 80, 305 - filterStringWidth - 5, 20
		);
		this.filter.setMaxStringLength(35);
		this.filter.setFocused(true);
		this.getButtonList()
			.add(this.buttonEdit = new GuiButton(EDIT, this.getWidth() / 2 - 154, this.getHeight() - 52, 74, 20, I18nUtils.getString("selectServer.edit")));
		this.getButtonList()
			.add(this.buttonDelete = new GuiButton(DELETE, this.getWidth() / 2 - 76, this.getHeight() - 52, 74, 20, I18nUtils.getString("selectServer.delete")));
		this.getButtonList()
			.add(
				this.buttonHighlight = new GuiButton(HIGHLIGHT, this.getWidth() / 2 + 2, this.getHeight() - 52, 74, 20, I18nUtils.getString("minimap.waypoints.highlight"))
			);
		this.getButtonList()
			.add(
				this.buttonTeleport = new GuiButton(
					TELEPORT, this.getWidth() / 2 + 80, this.getHeight() - 52, 74, 20, I18nUtils.getString("minimap.waypoints.teleportto")
				)
			);
		this.getButtonList()
			.add(this.buttonShare = new GuiButton(SHARE, this.getWidth() / 2 - 154, this.getHeight() - 28, 74, 20, I18nUtils.getString("minimap.waypoints.share")));
		this.getButtonList()
			.add(new GuiButton(NEW, this.getWidth() / 2 - 76, this.getHeight() - 28, 74, 20, I18nUtils.getString("minimap.waypoints.newwaypoint")));
		this.getButtonList().add(new GuiButton(OPTIONS, this.getWidth() / 2 + 2, this.getHeight() - 28, 74, 20, I18nUtils.getString("menu.options")));
		this.getButtonList().add(new GuiButton(DONE, this.getWidth() / 2 + 80, this.getHeight() - 28, 74, 20, I18nUtils.getString("gui.done")));
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

	protected void actionPerformed(GuiButton guiButton) {
		if (guiButton.enabled) {
			if (guiButton.id > 0) {
				this.options.setSort(guiButton.id);
				this.changedSort = true;
				this.sort();
			}

			if (guiButton.id == EDIT) {
				this.editWaypoint(this.selectedWaypoint);
			}

			if (guiButton.id == DELETE) {
				String name = this.selectedWaypoint.name;
				if (name != null) {
					this.deleteClicked = true;
					String title = I18nUtils.getString("minimap.waypoints.deleteconfirm");
					String explanation = "'" + name + "' " + I18nUtils.getString("selectServer.deleteWarning");
					String affirm = I18nUtils.getString("selectServer.deleteButton");
					String deny = I18nUtils.getString("gui.cancel");
					GuiYesNo gui = new GuiYesNo(this, title, explanation, affirm, deny, this.waypointManager.getWaypoints().indexOf(this.selectedWaypoint));
					this.getMinecraft().displayGuiScreen(gui);
				}
			}

			if (guiButton.id == HIGHLIGHT) {
				this.setHighlightedWaypoint();
			}

			if (guiButton.id == SHARE) {
				CommandUtils.sendWaypoint(this.selectedWaypoint);
			}

			if (guiButton.id == TELEPORT) {
				boolean mp = !this.mc.isIntegratedServerRunning();
				int y = this.selectedWaypoint.getY() > 0 ? this.selectedWaypoint.getY() : (this.options.game.player.dimension != -1 ? 128 : 64);
				this.options
					.game
					.player
					.sendChatMessage(
						"/tp " + this.options.game.player.getName() + " " + this.selectedWaypoint.getX() + " " + y + " " + this.selectedWaypoint.getZ()
					);
				if (mp) {
					this.options.game.player.sendChatMessage("/tppos " + this.selectedWaypoint.getX() + " " + y + " " + this.selectedWaypoint.getZ());
				}

				this.getMinecraft().displayGuiScreen(null);
			}

			if (guiButton.id == NEW) {
				this.addWaypoint();
			}

			if (guiButton.id == OPTIONS) {
				this.getMinecraft().displayGuiScreen(new GuiWaypointsOptions(this, this.options));
			}

			if (guiButton.id == DONE) {
				this.getMinecraft().displayGuiScreen(this.parentScreen);
			}
		}
	}

	@Override
	protected void keyTyped(char character, int keycode) {
		super.keyTyped(character, keycode);
		if (this.filter.textboxKeyTyped(character, keycode)) {
			this.waypointList.updateFilter(this.filter.getText().toLowerCase());
		}
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		this.filter.mouseClicked(mouseX, mouseY, mouseButton);
		this.waypointList.mouseClicked(mouseX, mouseY, mouseButton);
	}

	@Override
	protected void mouseReleased(int mouseX, int mouseY, int state) {
		super.mouseReleased(mouseX, mouseY, state);
		this.waypointList.mouseReleased(mouseX, mouseY, state);
	}

	@Override
	public boolean isEditing() {
		return this.editClicked;
	}

	public void confirmClicked(boolean par1, int par2) {
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
		if (this.waypointManager.getWaypoints().isEmpty()) {
			r = 0.0F;
			g = 1.0F;
			b = 0.0F;
		} else {
			r = this.generator.nextFloat();
			g = this.generator.nextFloat();
			b = this.generator.nextFloat();
		}

		TreeSet<Integer> dimensions = new TreeSet<>();
		dimensions.add(this.options.game.player.dimension);
		this.newWaypoint = new Waypoint(
			"",
			this.options.game.player.dimension != -1 ? GameVariableAccessShim.xCoord() : GameVariableAccessShim.xCoord() * 8,
			this.options.game.player.dimension != -1 ? GameVariableAccessShim.zCoord() : GameVariableAccessShim.zCoord() * 8,
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

	public void handleMouseInput() {
		super.handleMouseInput();
		if (this.waypointList != null) {
			this.waypointList.handleMouseInput();
		}
	}

	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawMap();
		this.tooltip = null;
		this.waypointList.drawScreen(mouseX, mouseY, partialTicks);
		this.drawCenteredString(this.getFontRenderer(), this.screenTitle, this.getWidth() / 2, 20, 16777215);
		super.drawScreen(mouseX, mouseY, partialTicks);
		this.drawString(
			this.getFontRenderer(), I18nUtils.getString("minimap.waypoints.filter") + ":", this.getWidth() / 2 - 153, this.getHeight() - 75, 10526880
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

	public boolean canTeleport() {
		boolean allowed;
		boolean singlePlayer = this.options.game.isIntegratedServerRunning();
		if (singlePlayer) {
			try {
				allowed = this.mc.getIntegratedServer().getPlayerList().canSendCommands(this.mc.player.getGameProfile());
			} catch (Exception e) {
				allowed = this.mc.getIntegratedServer().worlds[0].getWorldInfo().areCommandsAllowed();
			}
		} else {
			allowed = true;
		}

		return allowed;
	}

	@Override
	public void onGuiClosed() {
		Keyboard.enableRepeatEvents(false);
		if (this.changedSort) {
			super.onGuiClosed();
		}
	}
}
