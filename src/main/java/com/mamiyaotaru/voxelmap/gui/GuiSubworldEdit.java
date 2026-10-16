package com.mamiyaotaru.voxelmap.gui;

import com.mamiyaotaru.voxelmap.gui.overridden.GuiScreenMinimap;
import com.mamiyaotaru.voxelmap.interfaces.IVoxelMap;
import com.mamiyaotaru.voxelmap.interfaces.IWaypointManager;
import com.mamiyaotaru.voxelmap.util.I18nUtils;
import net.minecraft.client.gui.*;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;

public class GuiSubworldEdit extends GuiScreenMinimap implements GuiYesNoCallback {
	private final GuiScreen parent;
	private final IWaypointManager waypointManager;
	private final ArrayList<String> knownSubworldNames;
	private final String originalSubworldName;

	private GuiTextField subworldNameField;
	private String currentSubworldName = "";
	private boolean deleteClicked = false;

	public GuiSubworldEdit(GuiScreen parent, IVoxelMap master, String subworldName) {
		this.parent = parent;
		this.waypointManager = master.getWaypointManager();
		this.originalSubworldName = subworldName;
		this.knownSubworldNames = new ArrayList<>(this.waypointManager.getKnownSubworldNames());
	}

	@Override
	public void updateScreen() {
		this.subworldNameField.updateCursorCounter();
	}

	@Override
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

	@Override
	protected void actionPerformed(GuiButton button) {
		if (button.enabled) {
			if (button.id == 0) {
				if (!this.currentSubworldName.equals(this.originalSubworldName)) {
					this.waypointManager.changeSubworldName(this.originalSubworldName, this.currentSubworldName);
				}

				this.getMinecraft().displayGuiScreen(this.parent);
			} else if (button.id == 1) {
				this.getMinecraft().displayGuiScreen(this.parent);
			} else if (button.id == 7) {
				this.deleteClicked = true;
				String confirmTitle = I18nUtils.getString("worldmap.subworld.deleteconfirm");
				String confirmMessage = "'" + this.originalSubworldName + "' " + I18nUtils.getString("selectServer.deleteWarning");
				String deleteButtonText = I18nUtils.getString("selectServer.deleteButton");
				String cancelButtonText = I18nUtils.getString("gui.cancel");
				GuiYesNo confirmDialog = new GuiYesNo(this, confirmTitle, confirmMessage, deleteButtonText, cancelButtonText, 0);
				this.getMinecraft().displayGuiScreen(confirmDialog);
			}
		}
	}

	@Override
	public void confirmClicked(boolean confirmed, int id) {
		if (this.deleteClicked) {
			this.deleteClicked = false;
			if (confirmed) {
				this.waypointManager.deleteSubworld(this.originalSubworldName);
			}

			this.getMinecraft().displayGuiScreen(this.parent);
		}
	}

	@Override
	protected void keyTyped(char character, int keyCode) {
		super.keyTyped(character, keyCode);
		this.subworldNameField.textboxKeyTyped(character, keyCode);
		boolean acceptable = this.isNameAcceptable();
		if (character == '\r' && acceptable) {
			this.actionPerformed(this.getButtonList().get(0));
		}

		this.getButtonList().get(0).enabled = acceptable;
		this.getButtonList().get(2).enabled = this.originalSubworldName.equals(this.subworldNameField.getText());
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		this.subworldNameField.mouseClicked(mouseX, mouseY, mouseButton);
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawMap();
		this.drawDefaultBackground();
		this.drawCenteredString(this.getFontRenderer(), I18nUtils.getString("worldmap.subworld.edit"), this.getWidth() / 2, 20, 16777215);
		this.drawString(this.getFontRenderer(), I18nUtils.getString("worldmap.subworld.name"), this.getWidth() / 2 - 100, this.getHeight() / 6, 10526880);
		this.subworldNameField.drawTextBox();
		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	private boolean isNameAcceptable() {
		this.currentSubworldName = this.subworldNameField.getText();
		boolean acceptable = !this.currentSubworldName.isEmpty();
		return acceptable && (this.currentSubworldName.equals(this.originalSubworldName) || !this.knownSubworldNames.contains(this.currentSubworldName));
	}
}
