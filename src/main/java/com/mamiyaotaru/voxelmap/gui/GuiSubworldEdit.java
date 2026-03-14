package com.mamiyaotaru.voxelmap.gui;

import com.mamiyaotaru.voxelmap.gui.overridden.GuiScreenMinimap;
import com.mamiyaotaru.voxelmap.interfaces.IVoxelMap;
import com.mamiyaotaru.voxelmap.interfaces.IWaypointManager;
import com.mamiyaotaru.voxelmap.util.I18nUtils;
import net.minecraft.client.gui.*;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.ArrayList;

public class GuiSubworldEdit extends GuiScreenMinimap implements GuiYesNoCallback {
	private final GuiScreen parent;
	private final IWaypointManager waypointManager;
	private final ArrayList<String> knownSubworldNames;
	private String originalSubworldName = "";
	private String currentSubworldName = "";
	private GuiTextField subworldNameField;
	private boolean deleteClicked = false;

	public GuiSubworldEdit(GuiScreen parent, IVoxelMap master, String subworldName) {
		this.parent = parent;
		this.waypointManager = master.getWaypointManager();
		this.originalSubworldName = subworldName;
		this.knownSubworldNames = new ArrayList<>(this.waypointManager.getKnownSubworldNames());
	}

	public void updateScreen() {
		this.subworldNameField.updateCursorCounter();
	}

	public void initGui() {
		Keyboard.enableRepeatEvents(true);
		this.getButtonList().clear();
		this.getButtonList().add(new GuiButton(0, this.getWidth() / 2 - 155, this.getHeight() / 6 + 168, 150, 20, I18nUtils.getString("gui.done")));
		this.getButtonList().add(new GuiButton(1, this.getWidth() / 2 + 5, this.getHeight() / 6 + 168, 150, 20, I18nUtils.getString("gui.cancel")));
		this.subworldNameField = new GuiTextField(2, this.getFontRenderer(), this.getWidth() / 2 - 100, this.getHeight() / 6 + 13, 200, 20);
		this.subworldNameField.setFocused(true);
		this.subworldNameField.setText(this.originalSubworldName);
		int buttonListY = this.getHeight() / 6 + 82 + 6;
		this.getButtonList().add(new GuiButton(7, this.getWidth() / 2 - 50, buttonListY + 24, 100, 20, I18nUtils.getString("selectServer.delete")));
		this.getButtonList().get(0).enabled = this.isNameAcceptable();
		this.getButtonList().get(2).enabled = this.originalSubworldName.equals(this.subworldNameField.getText());
	}

	@Override
	public void onGuiClosed() {
		Keyboard.enableRepeatEvents(false);
	}

	protected void actionPerformed(GuiButton par1GuiButton) {
		if (par1GuiButton.enabled) {
			if (par1GuiButton.id == 0) {
				if (!this.currentSubworldName.equals(this.originalSubworldName)) {
					this.waypointManager.changeSubworldName(this.originalSubworldName, this.currentSubworldName);
				}

				this.getMinecraft().displayGuiScreen(this.parent);
			} else if (par1GuiButton.id == 1) {
				this.getMinecraft().displayGuiScreen(this.parent);
			} else if (par1GuiButton.id == 7) {
				this.deleteClicked = true;
				String var4 = I18nUtils.getString("worldmap.subworld.deleteconfirm");
				String var5 = "'" + this.originalSubworldName + "' " + I18nUtils.getString("selectServer.deleteWarning");
				String var6 = I18nUtils.getString("selectServer.deleteButton");
				String var7 = I18nUtils.getString("gui.cancel");
				GuiYesNo var8 = new GuiYesNo(this, var4, var5, var6, var7, 0);
				this.getMinecraft().displayGuiScreen(var8);
			}
		}
	}

	public void confirmClicked(boolean par1, int par2) {
		if (this.deleteClicked) {
			this.deleteClicked = false;
			if (par1) {
				this.waypointManager.deleteSubworld(this.originalSubworldName);
			}

			this.getMinecraft().displayGuiScreen(this.parent);
		}
	}

	protected void keyTyped(char character, int keycode) {
		super.keyTyped(character, keycode);
		this.subworldNameField.textboxKeyTyped(character, keycode);
		boolean acceptable = this.isNameAcceptable();
		if (character == '\r' && acceptable) {
			this.actionPerformed(this.getButtonList().get(0));
		}

		this.getButtonList().get(0).enabled = acceptable;
		this.getButtonList().get(2).enabled = this.originalSubworldName.equals(this.subworldNameField.getText());
	}

	protected void mouseClicked(int mouseX, int mouseY, int par3) {
		super.mouseClicked(mouseX, mouseY, par3);
		this.subworldNameField.mouseClicked(mouseX, mouseY, par3);
	}

	public void drawScreen(int mouseX, int mouseY, float par3) {
		super.drawMap();
		this.drawDefaultBackground();
		this.drawCenteredString(this.getFontRenderer(), I18nUtils.getString("worldmap.subworld.edit"), this.getWidth() / 2, 20, 16777215);
		this.drawString(this.getFontRenderer(), I18nUtils.getString("worldmap.subworld.name"), this.getWidth() / 2 - 100, this.getHeight() / 6, 10526880);
		this.subworldNameField.drawTextBox();
		super.drawScreen(mouseX, mouseY, par3);
	}

	private boolean isNameAcceptable() {
		boolean acceptable = true;
		this.currentSubworldName = this.subworldNameField.getText();
		acceptable = acceptable && this.currentSubworldName.length() > 0;
		return acceptable && (this.currentSubworldName.equals(this.originalSubworldName) || !this.knownSubworldNames.contains(this.currentSubworldName));
	}
}
