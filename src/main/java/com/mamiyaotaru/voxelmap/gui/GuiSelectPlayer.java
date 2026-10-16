package com.mamiyaotaru.voxelmap.gui;

import com.mamiyaotaru.voxelmap.gui.overridden.GuiScreenMinimap;
import com.mamiyaotaru.voxelmap.interfaces.IVoxelMap;
import com.mamiyaotaru.voxelmap.util.I18nUtils;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.GuiYesNoCallback;
import org.lwjgl.input.Keyboard;

public class GuiSelectPlayer extends GuiScreenMinimap implements GuiYesNoCallback {
	private final GuiScreen parentScreen;
	private final boolean sharingWaypoint;
	private final String locInfo;

	protected String screenTitle = "Players";
	private GuiButtonRowListPlayers playerList;
	protected boolean allClicked = false;
	protected GuiTextField message;
	protected GuiTextField filter;
	private String tooltip;

	public GuiSelectPlayer(GuiScreen parentScreen, IVoxelMap master, String locInfo, boolean sharingWaypoint) {
		this.parentScreen = parentScreen;
		this.locInfo = locInfo;
		this.sharingWaypoint = sharingWaypoint;
	}

	@Override
	public void updateScreen() {
		this.message.updateCursorCounter();
		this.filter.updateCursorCounter();
	}

	@Override
	public void initGui() {
		this.screenTitle = I18nUtils.getString(this.sharingWaypoint ? "minimap.waypointshare.title" : "minimap.waypointshare.titlecoordinate");
		Keyboard.enableRepeatEvents(true);
		this.playerList = new GuiButtonRowListPlayers(this);
		this.playerList.registerScrollButtons(7, 8);
		int messageStringWidth = this.getFontRenderer().getStringWidth(I18nUtils.getString("minimap.waypointshare.sharemessage") + ":");
		this.message = new GuiTextField(1, this.getFontRenderer(), this.getWidth() / 2 - 153 + messageStringWidth + 5, 34, 305 - messageStringWidth - 5, 20);
		int maxMessageLength = 78;
		this.message.setMaxStringLength(maxMessageLength);
		int filterStringWidth = this.getFontRenderer().getStringWidth(I18nUtils.getString("minimap.waypoints.filter") + ":");
		this.filter = new GuiTextField(
			2, this.getFontRenderer(), this.getWidth() / 2 - 153 + filterStringWidth + 5, this.getHeight() - 55, 305 - filterStringWidth - 5, 20
		);
		this.filter.setMaxStringLength(35);
		this.filter.setFocused(true);
		this.buttonList.add(new GuiButton(-200, this.width / 2 - 100, this.height - 27, I18nUtils.getString("gui.cancel")));
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		if (button.enabled && button.id == -200) {
			this.getMinecraft().displayGuiScreen(this.parentScreen);
		}
	}

	@Override
	protected void keyTyped(char character, int keyCode) {
		super.keyTyped(character, keyCode);
		this.message.textboxKeyTyped(character, keyCode);
		if (this.filter.textboxKeyTyped(character, keyCode)) {
			this.playerList.updateFilter(this.filter.getText().toLowerCase());
		}
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		this.message.mouseClicked(mouseX, mouseY, mouseButton);
		this.filter.mouseClicked(mouseX, mouseY, mouseButton);
		this.playerList.mouseClicked(mouseX, mouseY, mouseButton);
	}

	@Override
	public void confirmClicked(boolean confirmed, int id) {
		if (this.allClicked) {
			this.allClicked = false;
			if (confirmed) {
				String combined = this.message.getText() + " " + this.locInfo;
				if (combined.length() > 100) {
					this.mc.player.sendChatMessage(this.message.getText());
					this.mc.player.sendChatMessage(this.locInfo);
				} else {
					this.mc.player.sendChatMessage(combined);
				}

				this.getMinecraft().displayGuiScreen(this.parentScreen);
			} else {
				this.getMinecraft().displayGuiScreen(this);
			}
		}
	}

	protected void sendMessageToPlayer(String name) {
		String combined = "/msg " + name + " " + this.message.getText() + " " + this.locInfo;
		if (combined.length() > 100) {
			this.mc.player.sendChatMessage("/msg " + name + " " + this.message.getText());
			this.mc.player.sendChatMessage("/msg " + name + " " + this.locInfo);
		} else {
			this.mc.player.sendChatMessage(combined);
		}

		this.getMinecraft().displayGuiScreen(this.parentScreen);
	}

	@Override
	public void handleMouseInput() {
		super.handleMouseInput();
		if (this.playerList != null) {
			this.playerList.handleMouseInput();
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawMap();
		this.tooltip = null;
		this.playerList.drawScreen(mouseX, mouseY, partialTicks);
		this.drawCenteredString(this.getFontRenderer(), this.screenTitle, this.getWidth() / 2, 20, 16777215);
		super.drawScreen(mouseX, mouseY, partialTicks);
		this.drawString(this.getFontRenderer(), I18nUtils.getString("minimap.waypointshare.sharemessage") + ":", this.getWidth() / 2 - 153, 39, 10526880);
		this.message.drawTextBox();
		this.drawCenteredString(this.getFontRenderer(), I18nUtils.getString("minimap.waypointshare.sharewith"), this.getWidth() / 2, 75, 16777215);
		this.drawString(this.getFontRenderer(), I18nUtils.getString("minimap.waypoints.filter") + ":", this.getWidth() / 2 - 153, this.getHeight() - 50, 10526880);
		this.filter.drawTextBox();
		if (this.tooltip != null) {
			this.drawTooltip(this.tooltip, mouseX, mouseY);
		}
	}

	protected void drawTooltip(String tooltipText, int mouseX, int mouseY) {
		if (tooltipText != null) {
			int tooltipX = mouseX + 12;
			int tooltipY = mouseY - 12;
			int tooltipWidth = this.getFontRenderer().getStringWidth(tooltipText);
			this.drawGradientRect(tooltipX - 3, tooltipY - 3, tooltipX + tooltipWidth + 3, tooltipY + 8 + 3, -1073741824, -1073741824);
			this.getFontRenderer().drawStringWithShadow(tooltipText, tooltipX, tooltipY, -1);
		}
	}

	static String setTooltip(GuiSelectPlayer guiSelectPlayer, String tooltipText) {
		return guiSelectPlayer.tooltip = tooltipText;
	}

	@Override
	public void onGuiClosed() {
		Keyboard.enableRepeatEvents(false);
	}
}
