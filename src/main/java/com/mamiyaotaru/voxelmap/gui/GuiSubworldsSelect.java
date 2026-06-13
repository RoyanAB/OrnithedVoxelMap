package com.mamiyaotaru.voxelmap.gui;

import com.mamiyaotaru.voxelmap.gui.overridden.GuiScreenMinimap;
import com.mamiyaotaru.voxelmap.interfaces.IVoxelMap;
import com.mamiyaotaru.voxelmap.interfaces.IWaypointManager;
import com.mamiyaotaru.voxelmap.util.I18nUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.*;
import net.minecraft.client.util.RecipeBookClient;
import net.minecraft.util.MovementInputFromOptions;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Objects;

public class GuiSubworldsSelect extends GuiScreenMinimap implements GuiYesNoCallback {
	private final int thirdPersonViewOrig;
	private final GuiScreen parent;
	private final IVoxelMap master;
	private final IWaypointManager waypointManager;
	EntityPlayerSP thePlayer;
	EntityPlayerSP camera;
	private boolean multiworld = false;
	private GuiTextField newNameField;
	private boolean newWorld = false;
	private float yaw;
	private GuiButton[] selectButtons;
	private String[] worlds;

	private String title = "VoxelMap - Multiworld Selection";
	private String select = "Which world are you in?";

	public GuiSubworldsSelect(GuiScreen parent, IVoxelMap master) {
		this.mc = Minecraft.getMinecraft();
		this.parent = parent;
		this.thePlayer = this.getMinecraft().player;
		this.camera = new EntityPlayerSP(
			this.getMinecraft(), this.getMinecraft().world, Objects.requireNonNull(this.getMinecraft().getConnection()), this.thePlayer.getStatFileWriter(), new RecipeBookClient()
		);
		this.camera.movementInput = new MovementInputFromOptions(this.getMinecraft().gameSettings);
		this.camera
			.setLocationAndAngles(
				this.thePlayer.posX,
				this.thePlayer.posY - this.thePlayer.getYOffset(),
				this.thePlayer.posZ,
				this.thePlayer.rotationYaw,
				0.0F
			);
		this.yaw = this.thePlayer.rotationYaw;
		this.thirdPersonViewOrig = this.getMinecraft().gameSettings.thirdPersonView;
		this.master = master;
		this.waypointManager = master.getWaypointManager();
	}

	public void initGui() {
		ArrayList<String> knownSubworldNames = new ArrayList<>(this.waypointManager.getKnownSubworldNames());
		if (!this.multiworld && !this.waypointManager.isMultiworld()) {
			GuiYesNo gui = new GuiYesNo(
				this,
				I18nUtils.getString("worldmap.multiworld.isthismultiworld"),
				I18nUtils.getString("worldmap.multiworld.explanation"),
				I18nUtils.getString("gui.yes"),
				I18nUtils.getString("gui.no"),
				0
			);
			this.getMinecraft().displayGuiScreen(gui);
		} else {
			this.getMinecraft().gameSettings.thirdPersonView = 0;
			this.getMinecraft().setRenderViewEntity(this.camera);
		}

		this.title = I18nUtils.getString("worldmap.multiworld.title");
		this.select = I18nUtils.getString("worldmap.multiworld.select");
		this.buttonList.clear();
		int centerX = this.width / 2;
		int buttonsPerRow = this.width / 150;
		if (buttonsPerRow == 0) {
			buttonsPerRow = 1;
		}

		int buttonWidth = this.width / buttonsPerRow - 5;
		int xSpacing = (this.width - buttonsPerRow * buttonWidth) / 2;
		GuiButton cancelBtn = new GuiButton(0, centerX - 100, this.height - 30, I18nUtils.getString("gui.cancel"));
		this.buttonList.add(cancelBtn);
		final Collator collator = I18nUtils.getLocaleAwareCollator();
		knownSubworldNames.sort((name1, name2) -> -collator.compare(name1, name2));
		int numKnownSubworlds = knownSubworldNames.size();
		int completeRows = (int) Math.floor((float) (numKnownSubworlds + 1) / buttonsPerRow);
		int lastRowShiftBy = (int) (Math.ceil((float) (numKnownSubworlds + 1) / buttonsPerRow) * buttonsPerRow - (numKnownSubworlds + 1));
		this.worlds = new String[numKnownSubworlds];
		this.selectButtons = new GuiButton[numKnownSubworlds + 1];
		GuiButton[] editButtons = new GuiButton[numKnownSubworlds + 1];

		for (int t = 0; t < numKnownSubworlds; t++) {
			int shiftBy = 1;
			if (t / buttonsPerRow >= completeRows) {
				shiftBy = lastRowShiftBy + 1;
			}

			this.worlds[t] = knownSubworldNames.get(t);
			this.selectButtons[t] = new GuiButton(
				t + 1,
				(buttonsPerRow - shiftBy - t % buttonsPerRow) * buttonWidth + xSpacing,
				this.height - 60 - t / buttonsPerRow * 21,
				buttonWidth - 32,
				20,
				this.worlds[t]
			);
			editButtons[t] = new GuiButton(-(t + 1), (buttonsPerRow - shiftBy - t % buttonsPerRow) * buttonWidth + xSpacing + buttonWidth - 32, this.height - 60 - t / buttonsPerRow * 21, 30, 20, "⚒");
			this.buttonList.add(this.selectButtons[t]);
			this.buttonList.add(editButtons[t]);
		}

		int numButtons = this.selectButtons.length - 1;
		if (!this.newWorld) {
			this.selectButtons[numButtons] = new GuiButton(
				numButtons + 1,
				(buttonsPerRow - 1 - lastRowShiftBy - numButtons % buttonsPerRow) * buttonWidth + xSpacing,
				this.height - 60 - numButtons / buttonsPerRow * 21,
				buttonWidth - 2,
				20,
				"< " + I18nUtils.getString("worldmap.multiworld.newname") + " >"
			);
			this.buttonList.add(this.selectButtons[numButtons]);
		}

		this.newNameField = new GuiTextField(
			0,
			this.getFontRenderer(),
			(buttonsPerRow - 1 - lastRowShiftBy - numButtons % buttonsPerRow) * buttonWidth + xSpacing,
			this.height - 60 - numButtons / buttonsPerRow * 21 + 1,
			buttonWidth - 2,
			18
		);
	}

	protected void actionPerformed(GuiButton button) {
		if (button.enabled) {
			this.newWorld = false;
			if (button.id == this.worlds.length + 1) {
				this.newWorld = true;
				this.buttonList.remove(this.selectButtons[this.worlds.length]);
			} else if (button.id == 0) {
				this.getMinecraft().displayGuiScreen(null);
			} else if (button.id > 0) {
				this.worldSelected(this.worlds[button.id - 1]);
			} else {
				this.editWorld(this.worlds[-button.id - 1]);
			}
		}
	}

	public void confirmClicked(boolean par1, int par2) {
		if (!par1) {
			this.getMinecraft().displayGuiScreen(this.parent);
		} else {
			this.multiworld = true;
			this.getMinecraft().displayGuiScreen(this);
		}
	}

	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		if (this.newWorld) {
			this.newNameField.mouseClicked(mouseX, mouseY, mouseButton);
		}
	}

	protected void keyTyped(char character, int keyCode) {
		super.keyTyped(character, keyCode);
		if (this.newNameField.isFocused()) {
			this.newNameField.textboxKeyTyped(character, keyCode);
			if (keyCode == 28) {
				String newName = this.newNameField.getText();
				if (newName != null && !newName.isEmpty()) {
					this.worldSelected(this.addMultiworld(newName));
				}
			}
		}
	}

	public void updateScreen() {
		this.newNameField.updateCursorCounter();
		super.updateScreen();
	}

	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		int titleStringWidth = this.getFontRenderer().getStringWidth(this.title);
		titleStringWidth = Math.max(titleStringWidth, this.getFontRenderer().getStringWidth(this.select));
		drawRect(this.width / 2 - titleStringWidth / 2 - 5, 0, this.width / 2 + titleStringWidth / 2 + 5, 27, -1073741824);
		this.drawCenteredString(this.getFontRenderer(), this.title, this.width / 2, 5, 16777215);
		this.drawCenteredString(this.getFontRenderer(), this.select, this.width / 2, 15, 16711680);
		this.camera.prevRotationPitch = this.camera.rotationPitch = 0.0F;
		this.camera.prevRotationYaw = this.camera.rotationYaw = this.yaw;
		float orbitRadius = 0.475F;
		this.camera.lastTickPosY = this.camera.prevPosY = this.camera.posY = this.thePlayer.posY;
		this.camera.lastTickPosX = this.camera.prevPosX = this.camera.posX = this.thePlayer.posX
			- orbitRadius * Math.sin(this.yaw / 180.0 * Math.PI);
		this.camera.lastTickPosZ = this.camera.prevPosZ = this.camera.posZ = this.thePlayer.posZ
			+ orbitRadius * Math.cos(this.yaw / 180.0 * Math.PI);
		float rotationSpeed = 1.0F;
		this.yaw = (float) (this.yaw + rotationSpeed * (1.0 + 0.7F * Math.cos((this.yaw + 45.0F) / 45.0 * Math.PI)));
		if (this.newWorld) {
			this.newNameField.drawTextBox();
		}

		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	@Override
	public void onGuiClosed() {
		super.onGuiClosed();
		this.getMinecraft().gameSettings.thirdPersonView = this.thirdPersonViewOrig;
		this.getMinecraft().setRenderViewEntity(this.thePlayer);
	}

	private void worldSelected(String selectedSubworldName) {
		this.waypointManager.setSubworldName(selectedSubworldName, false);
		this.getMinecraft().displayGuiScreen(this.parent);
	}

	private void editWorld(String subworldNameToEdit) {
		this.getMinecraft().displayGuiScreen(new GuiSubworldEdit(this, this.master, subworldNameToEdit));
	}

	private String addMultiworld(String newSubworldName) {
		return newSubworldName;
	}
}
