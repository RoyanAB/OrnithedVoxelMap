package com.mamiyaotaru.voxelmap.gui;

import com.mamiyaotaru.voxelmap.gui.overridden.GuiScreenMinimap;
import com.mamiyaotaru.voxelmap.gui.overridden.IPopupGuiScreen;
import com.mamiyaotaru.voxelmap.gui.overridden.Popup;
import com.mamiyaotaru.voxelmap.gui.overridden.PopupGuiButton;
import com.mamiyaotaru.voxelmap.interfaces.IColorManager;
import com.mamiyaotaru.voxelmap.interfaces.IVoxelMap;
import com.mamiyaotaru.voxelmap.interfaces.IWaypointManager;
import com.mamiyaotaru.voxelmap.textures.Sprite;
import com.mamiyaotaru.voxelmap.textures.TextureAtlas;
import com.mamiyaotaru.voxelmap.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;

import static com.mamiyaotaru.voxelmap.util.GLShim.*;

@SuppressWarnings("unused")
public class GuiAddWaypoint extends GuiScreenMinimap implements IPopupGuiScreen {
	private final IGuiWaypoints parentGui;
	private final float red;
	private final float green;
	private final float blue;
	private final String suffix;
	private final boolean enabled;
	private final boolean editing;
	protected Dimension selectedDimension;
	protected Waypoint waypoint;
	IVoxelMap master;
	IWaypointManager waypointManager;
	IColorManager colorManager;
	private PopupGuiButton doneButton;
	private GuiSlotDimensions dimensionList;
	private String tooltip;
	private GuiTextField waypointName;
	private GuiTextField waypointX;
	private GuiTextField waypointZ;
	private GuiTextField waypointY;
	private PopupGuiButton buttonEnabled;
	private boolean choosingColor = false;
	private boolean choosingIcon = false;

	public GuiAddWaypoint(IGuiWaypoints par1GuiScreen, IVoxelMap master, Waypoint par2Waypoint, boolean editing) {
		this.master = master;
		this.waypointManager = master.getWaypointManager();
		this.colorManager = master.getColorManager();
		this.parentGui = par1GuiScreen;
		this.waypoint = par2Waypoint;
		this.red = this.waypoint.red;
		this.green = this.waypoint.green;
		this.blue = this.waypoint.blue;
		this.suffix = this.waypoint.imageSuffix;
		this.enabled = this.waypoint.enabled;
		this.editing = editing;
	}

	static String setTooltip(GuiAddWaypoint guiAddWaypoint, String string) {
		return guiAddWaypoint.tooltip = string;
	}

	public void updateScreen() {
		this.waypointName.updateCursorCounter();
		this.waypointX.updateCursorCounter();
		this.waypointY.updateCursorCounter();
		this.waypointZ.updateCursorCounter();
	}

	public void initGui() {
		Keyboard.enableRepeatEvents(true);
		this.getButtonList().clear();
		this.doneButton = new PopupGuiButton(0, this.getWidth() / 2 - 155, this.getHeight() / 6 + 168, 150, 20, I18nUtils.getString("addServer.add"), this);
		this.getButtonList().add(this.doneButton);
		this.getButtonList().add(new PopupGuiButton(1, this.getWidth() / 2 + 5, this.getHeight() / 6 + 168, 150, 20, I18nUtils.getString("gui.cancel"), this));
		this.waypointName = new GuiTextField(2, this.getFontRenderer(), this.getWidth() / 2 - 100, this.getHeight() / 6 + 13, 200, 20);
		this.waypointName.setFocused(true);
		this.waypointName.setText(this.waypoint.name);
		this.waypointX = new GuiTextField(3, this.getFontRenderer(), this.getWidth() / 2 - 100, this.getHeight() / 6 + 41 + 13, 56, 20);
		this.waypointX.setMaxStringLength(128);
		this.waypointX.setText("" + this.waypoint.getX());
		this.waypointZ = new GuiTextField(4, this.getFontRenderer(), this.getWidth() / 2 - 28, this.getHeight() / 6 + 41 + 13, 56, 20);
		this.waypointZ.setMaxStringLength(128);
		this.waypointZ.setText("" + this.waypoint.getZ());
		this.waypointY = new GuiTextField(5, this.getFontRenderer(), this.getWidth() / 2 + 44, this.getHeight() / 6 + 41 + 13, 56, 20);
		this.waypointY.setMaxStringLength(128);
		this.waypointY.setText("" + this.waypoint.getY());
		int buttonListY = this.getHeight() / 6 + 82 + 6;
		this.getButtonList()
			.add(
				this.buttonEnabled = new PopupGuiButton(
					6, this.getWidth() / 2 - 101, buttonListY, 100, 20, "Enabled: " + (this.waypoint.enabled ? "On" : "Off"), this
				)
			);
		this.getButtonList()
			.add(
				new PopupGuiButton(7, this.getWidth() / 2 - 101, buttonListY + 24, 100, 20, I18nUtils.getString("minimap.waypoints.sortbycolor") + ":     ", this)
			);
		this.getButtonList()
			.add(new PopupGuiButton(8, this.getWidth() / 2 - 101, buttonListY + 48, 100, 20, I18nUtils.getString("minimap.waypoints.sortbyicon") + ":     ", this));
		this.doneButton.enabled = !this.waypointName.getText().isEmpty();
		this.dimensionList = new GuiSlotDimensions(this);
		this.dimensionList.registerScrollButtons(9, 10);
	}

	@Override
	public void onGuiClosed() {
		Keyboard.enableRepeatEvents(false);
	}

	protected void actionPerformed(GuiButton button) {
		if (button.enabled) {
			if (button.id == 6) {
				this.waypoint.enabled = !this.waypoint.enabled;
			}

			if (button.id == 7) {
				this.choosingColor = true;
			}

			if (button.id == 8) {
				this.choosingIcon = true;
			}

			if (button.id == 1) {
				this.waypoint.red = this.red;
				this.waypoint.green = this.green;
				this.waypoint.blue = this.blue;
				this.waypoint.imageSuffix = this.suffix;
				this.waypoint.enabled = this.enabled;
				if (this.parentGui != null) {
					this.parentGui.confirmClicked(false, 0);
				} else {
					this.getMinecraft().displayGuiScreen(null);
				}
			} else if (button.id == 0) {
				this.waypoint.name = this.waypointName.getText();
				this.waypoint.setX(Integer.parseInt(this.waypointX.getText()));
				this.waypoint.setZ(Integer.parseInt(this.waypointZ.getText()));
				this.waypoint.setY(Integer.parseInt(this.waypointY.getText()));
				if (this.parentGui != null) {
					this.parentGui.confirmClicked(true, 0);
				} else {
					if (this.editing) {
						this.waypointManager.saveWaypoints();
					} else {
						this.waypointManager.addWaypoint(this.waypoint);
					}

					this.getMinecraft().displayGuiScreen(null);
				}
			}
		}
	}

	protected void keyTyped(char character, int keycode) {
		if (keycode == 1) {
			this.waypoint.red = this.red;
			this.waypoint.green = this.green;
			this.waypoint.blue = this.blue;
			this.waypoint.imageSuffix = this.suffix;
			this.waypoint.enabled = this.enabled;
		}

		super.keyTyped(character, keycode);
		if (!this.popupOpen()) {
			this.waypointName.textboxKeyTyped(character, keycode);
			this.waypointX.textboxKeyTyped(character, keycode);
			this.waypointZ.textboxKeyTyped(character, keycode);
			this.waypointY.textboxKeyTyped(character, keycode);
			if (character == '\t') {
				if (this.waypointName.isFocused()) {
					this.waypointName.setFocused(false);
					this.waypointX.setFocused(true);
					this.waypointZ.setFocused(false);
					this.waypointY.setFocused(false);
				} else if (this.waypointX.isFocused()) {
					this.waypointName.setFocused(false);
					this.waypointX.setFocused(false);
					this.waypointZ.setFocused(true);
					this.waypointY.setFocused(false);
				} else if (this.waypointZ.isFocused()) {
					this.waypointName.setFocused(false);
					this.waypointX.setFocused(false);
					this.waypointZ.setFocused(false);
					this.waypointY.setFocused(true);
				} else if (this.waypointY.isFocused()) {
					this.waypointName.setFocused(true);
					this.waypointX.setFocused(false);
					this.waypointZ.setFocused(false);
					this.waypointY.setFocused(false);
				}
			}

			if (character == '\r') {
				this.actionPerformed(this.doneButton);
			}

			boolean acceptable = !this.waypointName.getText().isEmpty();

			try {
				Integer.parseInt(this.waypointX.getText());
				Integer.parseInt(this.waypointZ.getText());
				Integer.parseInt(this.waypointY.getText());
			} catch (NumberFormatException e) {
				acceptable = false;
			}

			this.doneButton.enabled = acceptable;
		}
	}

	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
		if (!this.popupOpen()) {
			super.mouseClicked(mouseX, mouseY, mouseButton);
			this.waypointName.mouseClicked(mouseX, mouseY, mouseButton);
			this.waypointX.mouseClicked(mouseX, mouseY, mouseButton);
			this.waypointZ.mouseClicked(mouseX, mouseY, mouseButton);
			this.waypointY.mouseClicked(mouseX, mouseY, mouseButton);
		} else if (this.choosingColor) {
			if (mouseX >= this.getWidth() / 2 - 128
				&& mouseX < this.getWidth() / 2 + 128
				&& mouseY >= this.getHeight() / 2 - 128
				&& mouseY < this.getHeight() / 2 + 128) {
				int color = this.colorManager.getColorPicker().getRGB(mouseX - (this.getWidth() / 2 - 128), mouseY - (this.getHeight() / 2 - 128));
				this.waypoint.red = (color >> 16 & 0xFF) / 255.0F;
				this.waypoint.green = (color >> 8 & 0xFF) / 255.0F;
				this.waypoint.blue = (color >> 0 & 0xFF) / 255.0F;
				this.choosingColor = false;
			}
		} else if (this.choosingIcon) {
			ScaledResolution scRes = new ScaledResolution(this.mc);
			float scScale = scRes.getScaleFactor();
			TextureAtlas chooser = this.waypointManager.getTextureAtlasChooser();
			float scale = scScale / 2.0F;
			float displayWidthFloat = chooser.getWidth() / scale;
			float displayHeightFloat = chooser.getHeight() / scale;
			if (displayWidthFloat > scRes.getScaledWidth()) {
				float adj = displayWidthFloat / scRes.getScaledWidth();
				scale *= adj;
				displayWidthFloat /= adj;
				displayHeightFloat /= adj;
			}

			if (displayHeightFloat > scRes.getScaledHeight()) {
				float adj = displayHeightFloat / scRes.getScaledHeight();
				scale *= adj;
				displayWidthFloat /= adj;
				displayHeightFloat /= adj;
			}

			int displayWidth = (int) displayWidthFloat;
			int displayHeight = (int) displayHeightFloat;
			if (mouseX >= this.getWidth() / 2 - displayWidth / 2
				&& mouseX < this.getWidth() / 2 + displayWidth / 2
				&& mouseY >= this.getHeight() / 2 - displayHeight / 2
				&& mouseY < this.getHeight() / 2 + displayHeight / 2) {
				float x = (mouseX - (this.getWidth() / 2 - displayWidth / 2)) * scale;
				float y = (mouseY - (this.getHeight() / 2 - displayHeight / 2)) * scale;
				Sprite icon = chooser.getIconAt(x, y);
				if (icon != chooser.getMissingImage()) {
					this.waypoint.imageSuffix = icon.getIconName().replace("voxelmap:images/waypoints/waypoint", "").replace(".png", "");
					this.choosingIcon = false;
				}
			}
		}
	}

	@Override
	public boolean overPopup(int x, int y) {
		return this.choosingColor || this.choosingIcon;
	}

	@Override
	public boolean popupOpen() {
		return this.choosingColor || this.choosingIcon;
	}

	@Override
	public void popupAction(Popup popup, int action) {
	}

	public void handleMouseInput() {
		super.handleMouseInput();
		if (!this.popupOpen() && this.dimensionList != null) {
			this.dimensionList.handleMouseInput();
		}
	}

	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawMap();
		ScaledResolution scRes = new ScaledResolution(this.mc);
		float scScale = scRes.getScaleFactor();
		this.tooltip = null;
		this.buttonEnabled.displayString = I18nUtils.getString("minimap.waypoints.enabled")
			+ " "
			+ (this.waypoint.enabled ? I18nUtils.getString("options.on") : I18nUtils.getString("options.off"));
		if (!this.choosingColor && !this.choosingIcon) {
			this.drawDefaultBackground();
		}

		this.dimensionList.drawScreen(mouseX, mouseY, partialTicks);
		this.drawCenteredString(
			this.getFontRenderer(),
			(this.parentGui == null || !this.parentGui.isEditing()) && !this.editing
				? I18nUtils.getString("minimap.waypoints.new")
				: I18nUtils.getString("minimap.waypoints.edit"),
			this.getWidth() / 2,
			20,
			16777215
		);
		this.drawString(this.getFontRenderer(), I18nUtils.getString("minimap.waypoints.name"), this.getWidth() / 2 - 100, this.getHeight() / 6, 10526880);
		this.drawString(this.getFontRenderer(), I18nUtils.getString("X"), this.getWidth() / 2 - 100, this.getHeight() / 6 + 41, 10526880);
		this.drawString(this.getFontRenderer(), I18nUtils.getString("Z"), this.getWidth() / 2 - 28, this.getHeight() / 6 + 41, 10526880);
		this.drawString(this.getFontRenderer(), I18nUtils.getString("Y"), this.getWidth() / 2 + 44, this.getHeight() / 6 + 41, 10526880);
		this.waypointName.drawTextBox();
		this.waypointX.drawTextBox();
		this.waypointZ.drawTextBox();
		this.waypointY.drawTextBox();
		int buttonListY = this.getHeight() / 6 + 82 + 6;
		super.drawScreen(mouseX, mouseY, partialTicks);
		GLShim.glColor4f(this.waypoint.red, this.waypoint.green, this.waypoint.blue, 1.0F);
		GLUtils.disp(-1);
		this.drawTexturedModalRect(this.getWidth() / 2 - 25, buttonListY + 24 + 5, 0, 0, 16, 10);
		TextureAtlas chooser = this.waypointManager.getTextureAtlasChooser();
		GLUtils.disp(chooser.getGlTextureId());
		GLShim.glTexParameteri(GLShim.GL11_GL_TEXTURE_2D, GLShim.GL11_GL_TEXTURE_MIN_FILTER, GLShim.GL11_GL_LINEAR);
		Sprite icon = chooser.getAtlasSprite("voxelmap:images/waypoints/waypoint" + this.waypoint.imageSuffix + ".png");
		this.drawTexturedModalRect(this.getWidth() / 2 - 25, buttonListY + 48 + 2, icon, 16.0F, 16.0F);
		if (this.choosingColor || this.choosingIcon) {
			this.drawDefaultBackground();
		}

		if (this.choosingColor) {
			GLShim.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			GLUtils.img(new ResourceLocation("voxelmap", "images/colorpicker.png"));
			GLShim.glTexParameteri(GLShim.GL11_GL_TEXTURE_2D, GLShim.GL11_GL_TEXTURE_MIN_FILTER, GLShim.GL11_GL_NEAREST);
			this.drawTexturedModalRect(this.getWidth() / 2 - 128, this.getHeight() / 2 - 128, 0, 0, 256, 256);
		}

		if (this.choosingIcon) {
			float scale = scScale / 2.0F;
			float displayWidthFloat = chooser.getWidth() / scale;
			float displayHeightFloat = chooser.getHeight() / scale;
			if (displayWidthFloat > scRes.getScaledWidth()) {
				float adj = displayWidthFloat / scRes.getScaledWidth();
				displayWidthFloat /= adj;
				displayHeightFloat /= adj;
			}

			if (displayHeightFloat > scRes.getScaledHeight()) {
				float adj = displayHeightFloat / scRes.getScaledHeight();
				displayWidthFloat /= adj;
				displayHeightFloat /= adj;
			}

			int displayWidth = (int) displayWidthFloat;
			int displayHeight = (int) displayHeightFloat;
			GLUtils.disp(-1);
			GLShim.glTexParameteri(GLShim.GL11_GL_TEXTURE_2D, GLShim.GL11_GL_TEXTURE_MIN_FILTER, GLShim.GL11_GL_NEAREST);
			GLShim.glColor4f(0.0F, 0.0F, 0.0F, 1.0F);
			this.drawTexturedModalRect(this.getWidth() / 2 - displayWidth / 2 - 1, this.getHeight() / 2 - displayHeight / 2 - 1, 0, 0, displayWidth + 2, displayHeight + 2);
			GLShim.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			this.drawTexturedModalRect(this.getWidth() / 2 - displayWidth / 2, this.getHeight() / 2 - displayHeight / 2, 0, 0, displayWidth, displayHeight);
			GLShim.glColor4f(this.waypoint.red, this.waypoint.green, this.waypoint.blue, 1.0F);
			GLShim.glEnable(GLShim.GL11_GL_BLEND);
			GLUtils.disp(chooser.getGlTextureId());
			GLShim.glTexParameteri(GLShim.GL11_GL_TEXTURE_2D, GLShim.GL11_GL_TEXTURE_MIN_FILTER, GLShim.GL11_GL_LINEAR);
			drawScaledCustomSizeModalRect(
				this.getWidth() / 2 - displayWidth / 2,
				this.getHeight() / 2 - displayHeight / 2,
				0.0F,
				0.0F,
				chooser.getWidth(),
				chooser.getHeight(),
				displayWidth,
				displayHeight,
				chooser.getImageWidth(),
				chooser.getImageHeight()
			);
			if (mouseX >= this.getWidth() / 2 - displayWidth / 2
				&& mouseX <= this.getWidth() / 2 + displayWidth / 2
				&& mouseY >= this.getHeight() / 2 - displayHeight / 2
				&& mouseY <= this.getHeight() / 2 + displayHeight / 2) {
				float x = (mouseX - (this.getWidth() / 2 - displayWidth / 2)) * scale;
				float y = (mouseY - (this.getHeight() / 2 - displayHeight / 2)) * scale;
				icon = chooser.getIconAt(x, y);
				if (icon != chooser.getMissingImage()) {
					this.tooltip = icon.getIconName().replace("voxelmap:images/waypoints/waypoint", "").replace(".png", "");
				}
			}

			GLShim.glDisable(GLShim.GL11_GL_BLEND);
			GLShim.glTexParameteri(GLShim.GL11_GL_TEXTURE_2D, GLShim.GL11_GL_TEXTURE_MIN_FILTER, GLShim.GL11_GL_NEAREST);
		}

		this.drawTooltip(this.tooltip, mouseX, mouseY);
	}

	public void setSelectedDimension(Dimension dimension) {
		this.selectedDimension = dimension;
	}

	public void toggleDimensionSelected() {
		if (this.waypoint.dimensions.size() > 1
			&& this.waypoint.dimensions.contains(this.selectedDimension.ID)
			&& this.selectedDimension.ID != Minecraft.getMinecraft().player.dimension) {
			this.waypoint.dimensions.remove(this.selectedDimension.ID);
		} else
			this.waypoint.dimensions.add(this.selectedDimension.ID);
	}

	protected void drawTooltip(String par1Str, int mouseX, int mouseY) {
		if (par1Str != null && !par1Str.isEmpty()) {
			int var4 = mouseX + 12;
			int var5 = mouseY - 12;
			int var6 = this.getFontRenderer().getStringWidth(par1Str);
			this.drawGradientRect(var4 - 3, var5 - 3, var4 + var6 + 3, var5 + 8 + 3, -1073741824, -1073741824);
			this.getFontRenderer().drawStringWithShadow(par1Str, var4, var5, -1);
		}
	}

	public void drawTexturedModalRect(Sprite icon, float x, float y) {
		float width = icon.getIconWidth() / 2.0F;
		float height = icon.getIconHeight() / 2.0F;
		this.drawTexturedModalRect(x, y, icon, width, height);
	}

	public void drawTexturedModalRect(float xCoord, float yCoord, Sprite icon, float widthIn, float heightIn) {
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder vertexbuffer = tessellator.getBuffer();
		vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX);
		vertexbuffer.pos(xCoord + 0.0F, yCoord + heightIn, this.zLevel).tex(icon.getMinU(), icon.getMaxV()).endVertex();
		vertexbuffer.pos(xCoord + widthIn, yCoord + heightIn, this.zLevel).tex(icon.getMaxU(), icon.getMaxV()).endVertex();
		vertexbuffer.pos(xCoord + widthIn, yCoord + 0.0F, this.zLevel).tex(icon.getMaxU(), icon.getMinV()).endVertex();
		vertexbuffer.pos(xCoord + 0.0F, yCoord + 0.0F, this.zLevel).tex(icon.getMinU(), icon.getMinV()).endVertex();
		tessellator.draw();
	}
}
