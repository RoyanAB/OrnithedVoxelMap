package com.mamiyaotaru.voxelmap.persistent;

import com.mamiyaotaru.voxelmap.MapSettingsManager;
import com.mamiyaotaru.voxelmap.gui.*;
import com.mamiyaotaru.voxelmap.gui.overridden.Popup;
import com.mamiyaotaru.voxelmap.gui.overridden.PopupGuiButton;
import com.mamiyaotaru.voxelmap.gui.overridden.PopupGuiScreen;
import com.mamiyaotaru.voxelmap.interfaces.AbstractMapData;
import com.mamiyaotaru.voxelmap.interfaces.IPersistentMap;
import com.mamiyaotaru.voxelmap.interfaces.IVoxelMap;
import com.mamiyaotaru.voxelmap.interfaces.IWaypointManager;
import com.mamiyaotaru.voxelmap.textures.Sprite;
import com.mamiyaotaru.voxelmap.textures.TextureAtlas;
import com.mamiyaotaru.voxelmap.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.gui.*;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.ThreadDownloadImageData;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;
import java.util.TreeSet;

import static com.mamiyaotaru.voxelmap.util.GLShim.*;

@SuppressWarnings("unused")
public class GuiPersistentMap extends PopupGuiScreen implements IGuiWaypoints {
	private static int playerGLID = 0;
	private static boolean gotSkin = false;
	private static int skinTries = 0;
	private final GuiScreen parent;
	private final MapSettingsManager mapOptions;
	private final PersistentMapSettingsManager options;
	private final Object closedLock = new Object();
	private final int NEW = 0;
	private final int HIGHLIGHTPOINT = 1;
	private final int SHAREPOINT = 2;
	private final int TPTOPOINT = 3;
	private final int EDIT = 4;
	private final int DELETE = 5;
	private final int HIGHLIGHTWP = 6;
	private final int SHAREWP = 7;
	private final int TPTOWP = 8;
	private final Minecraft mc;
	private final Random generator = new Random();
	private final IVoxelMap master;
	private final IPersistentMap persistentMap;
	private final IWaypointManager waypointManager;
	private final BiomeMapData biomeMapData = new BiomeMapData(760, 360);
	private final KeyBinding keyBindForward = new KeyBinding("key.forward.fake", 17, "key.categories.movement");
	private final KeyBinding keyBindLeft = new KeyBinding("key.left.fake", 30, "key.categories.movement");
	private final KeyBinding keyBindBack = new KeyBinding("key.back.fake", 31, "key.categories.movement");
	private final KeyBinding keyBindRight = new KeyBinding("key.right.fake", 32, "key.categories.movement");
	private final KeyBinding keyBindSprint = new KeyBinding("key.sprint.fake", 29, "key.categories.movement");
	public boolean editClicked = false;
	public boolean deleteClicked = false;
	public boolean addClicked = false;
	protected String screenTitle = "World Map";
	protected String worldNameDisplay = "";
	protected int worldNameDisplayLength = 0;
	protected int maxWorldNameDisplayLength = 0;
	protected int mouseX;
	protected int mouseY;
	int centerX = 0;
	int centerY = 0;
	float mapCenterX = 0.0F;
	float mapCenterZ = 0.0F;
	float deltaX = 0.0F;
	float deltaY = 0.0F;
	float deltaXonRelease = 0.0F;
	float deltaYonRelease = 0.0F;
	long timeOfRelease = 0L;
	boolean mouseCursorShown = true;
	long timeAtLastTick = 0L;
	long timeOfLastKBInput = 0L;
	long timeOfLastMouseInput = 0L;
	float timeConstant = 350.0F;
	int lastMouseX = 0;
	int lastMouseY = 0;
	boolean leftMouseButtonDown = false;
	float zoom;
	float zoomStart;
	float zoomGoal;
	long timeOfZoom = 0L;
	int zoomDirectX = 0;
	int zoomDirectY = 0;
	BackgroundImageInfo backGroundImageInfo = null;
	int sideMargin = 10;
	int buttonCount = 5;
	int buttonSeparation = 4;
	int buttonWidth = 66;
	Waypoint newWaypoint;
	Waypoint selectedWaypoint;
	private String subworldName = "";
	private PopupGuiButton buttonMultiworld;
	private int top;
	private int bottom;
	private boolean oldNorth = false;
	private boolean lastStill = false;
	private boolean editingCoordinates = false;
	private boolean lastEditingCoordinates = false;
	private GuiTextField coordinates;
	private ScaledResolution scRes;
	private float scScale = 2.0F;
	private float guiToMap = 2.0F;
	private float mapToGui = 0.5F;
	private float mouseDirectToMap = 1.0F;
	private float guiToDirectMouse = 2.0F;
	private boolean closed = false;
	private CachedRegion[] regions = new CachedRegion[0];
	private float mapPixelsX = 0.0F;
	private float mapPixelsY = 0.0F;
	private int forwardCode = 0;
	private int leftCode = 0;
	private int backCode = 0;
	private int rightCode = 0;
	private int sprintCode = 0;

	public GuiPersistentMap(GuiScreen parent, IVoxelMap master) {
		this.mc = Minecraft.getMinecraft();
		this.parent = parent;
		this.master = master;
		this.waypointManager = master.getWaypointManager();
		this.mapOptions = master.getMapOptions();
		this.persistentMap = master.getPersistentMap();
		this.options = master.getPersistentMapOptions();
		this.zoom = this.options.zoom;
		this.zoomStart = this.options.zoom;
		this.zoomGoal = this.options.zoom;
		this.persistentMap.setLightMapArray(master.getMap().getLightmapArray());
		if (!gotSkin && skinTries < 5) {
			this.getSkin();
		}

		this.allowUserInput = true;
		this.forwardCode = this.mc.gameSettings.keyBindForward.getKeyCode();
		this.leftCode = this.mc.gameSettings.keyBindLeft.getKeyCode();
		this.backCode = this.mc.gameSettings.keyBindBack.getKeyCode();
		this.rightCode = this.mc.gameSettings.keyBindRight.getKeyCode();
		this.sprintCode = this.mc.gameSettings.keyBindSprint.getKeyCode();
	}

	private void getSkin() {
		ResourceLocation skinLocation = this.mc.player.getLocationSkin();
		ThreadDownloadImageData imageData = null;

		try {
			if (skinLocation != DefaultPlayerSkin.getDefaultSkin(this.mc.player.getUniqueID())) {
				imageData = AbstractClientPlayer.getDownloadImageSkin(skinLocation, TextFormatting.getTextWithoutFormattingCodes(this.mc.player.getName()));
			}
		} catch (Exception ignored) {
		}

		if (imageData != null) {
			gotSkin = true;
			GLUtils.disp(imageData.getGlTextureId());
		} else {
			skinTries++;
			GLUtils.img(skinLocation);
		}

		BufferedImage skinImage = ImageUtils.createBufferedImageFromCurrentGLImage();
		boolean showHat = this.mc.player.isWearing(EnumPlayerModelParts.HAT);
		if (showHat) {
			skinImage = ImageUtils.addImages(ImageUtils.loadImage(skinImage, 8, 8, 8, 8), ImageUtils.loadImage(skinImage, 40, 8, 8, 8), 0.0F, 0.0F, 8, 8);
		} else {
			skinImage = ImageUtils.loadImage(skinImage, 8, 8, 8, 8);
		}

		float scale = skinImage.getWidth() / 8.0F;
		skinImage = ImageUtils.fillOutline(ImageUtils.pad(ImageUtils.scaleImage(skinImage, 2.0F / scale)), true);
		if (playerGLID != 0) {
			GLUtils.glah(playerGLID);
		}

		playerGLID = GLUtils.tex(skinImage);
	}

	public void initGui() {
		this.oldNorth = this.mapOptions.oldNorth;
		this.centerAt(this.options.mapX, this.options.mapZ);
		Keyboard.enableRepeatEvents(true);
		if (this.getMinecraft().currentScreen == this) {
			this.closed = false;
		}

		this.screenTitle = I18nUtils.getString("worldmap.title");
		this.buildWorldName();
		this.leftMouseButtonDown = false;
		this.sideMargin = 10;
		this.buttonCount = 5;
		this.buttonSeparation = 4;
		this.buttonWidth = (this.width - this.sideMargin * 2 - this.buttonSeparation * (this.buttonCount - 1)) / this.buttonCount;
		this.getButtonList()
			.add(
				new PopupGuiButton(
					100,
					this.sideMargin,
					this.getHeight() - 28,
					this.buttonWidth,
					20,
					I18nUtils.getString("options.minimap.waypoints"),
					this
				)
			);
		if (!this.getMinecraft().isIntegratedServerRunning() && !this.master.getWaypointManager().receivedAutoSubworldName()) {
			this.getButtonList()
				.add(
					this.buttonMultiworld = new PopupGuiButton(
						104,
						this.sideMargin + (this.buttonWidth + this.buttonSeparation),
						this.getHeight() - 28,
						this.buttonWidth,
						20,
						I18nUtils.getString("options.worldmap.multiworld"),
						this
					)
				);
		}

		this.getButtonList()
			.add(
				new PopupGuiButton(
					103,
					this.sideMargin + 3 * (this.buttonWidth + this.buttonSeparation),
					this.getHeight() - 28,
					this.buttonWidth,
					20,
					I18nUtils.getString("menu.options"),
					this
				)
			);
		this.getButtonList()
			.add(
				new PopupGuiButton(
					200,
					this.sideMargin + 4 * (this.buttonWidth + this.buttonSeparation),
					this.getHeight() - 28,
					this.buttonWidth,
					20,
					I18nUtils.getString("gui.done"),
					this
				)
			);
		this.coordinates = new GuiTextField(1, this.getFontRenderer(), this.sideMargin, 10, 140, 20);
		this.top = 32;
		this.bottom = this.getHeight() - 32;
		this.centerX = this.getWidth() / 2;
		this.centerY = (this.bottom - this.top) / 2;
		this.scRes = new ScaledResolution(this.mc);
		this.scScale = this.scRes.getScaleFactor();
		this.mapPixelsX = this.mc.displayWidth;
		this.mapPixelsY = this.mc.displayHeight - (int) (64.0F * this.scScale);
		this.lastStill = false;
		this.timeAtLastTick = System.currentTimeMillis();
		this.keyBindForward.setKeyCode(this.forwardCode);
		this.keyBindLeft.setKeyCode(this.leftCode);
		this.keyBindBack.setKeyCode(this.backCode);
		this.keyBindRight.setKeyCode(this.rightCode);
		this.keyBindSprint.setKeyCode(this.sprintCode);
		this.mc.gameSettings.keyBindForward.setKeyCode(0);
		this.mc.gameSettings.keyBindLeft.setKeyCode(0);
		this.mc.gameSettings.keyBindBack.setKeyCode(0);
		this.mc.gameSettings.keyBindRight.setKeyCode(0);
		this.mc.gameSettings.keyBindSprint.setKeyCode(0);
		KeyBinding.resetKeyBindingArrayAndHash();
	}

	private void centerAt(int x, int z) {
		if (this.oldNorth) {
			this.mapCenterX = -z;
			this.mapCenterZ = x;
		} else {
			this.mapCenterX = x;
			this.mapCenterZ = z;
		}
	}

	private void buildWorldName() {
		String worldName = "";
		if (this.mc.isIntegratedServerRunning()) {
			worldName = Objects.requireNonNull(this.mc.getIntegratedServer()).getWorldName();
			if (worldName == null || worldName.isEmpty()) {
				worldName = "Singleplayer World";
			}
		} else {
			ServerData serverData = this.mc.getCurrentServerData();
			if (serverData != null) {
				worldName = serverData.serverName;
			}

			if (worldName == null || worldName.isEmpty()) {
				worldName = "Multiplayer Server";
			}
		}

		StringBuilder worldNameBuilder = new StringBuilder("§r").append(worldName);
		String subworldName = this.master.getWaypointManager().getCurrentSubworldDescriptor(true);
		this.subworldName = subworldName;
		if ((subworldName == null || subworldName.isEmpty()) && this.master.getWaypointManager().isMultiworld()) {
			subworldName = "???";
		}

		if (subworldName != null && !subworldName.isEmpty()) {
			worldNameBuilder.append(" - ").append(subworldName);
		}

		this.worldNameDisplay = worldNameBuilder.toString();
		this.worldNameDisplayLength = this.getFontRenderer().getStringWidth(this.worldNameDisplay);

		for (this.maxWorldNameDisplayLength = this.getWidth() / 2 - this.getFontRenderer().getStringWidth(this.screenTitle) / 2 - this.sideMargin * 2;
			 this.worldNameDisplayLength > this.maxWorldNameDisplayLength && worldName.length() > 5;
			 this.worldNameDisplayLength = this.getFontRenderer().getStringWidth(this.worldNameDisplay)
		) {
			worldName = worldName.substring(0, worldName.length() - 1);
			worldNameBuilder = new StringBuilder(worldName);
			worldNameBuilder.append("...");
			if (subworldName != null && !subworldName.isEmpty()) {
				worldNameBuilder.append(" - ").append(subworldName);
			}

			this.worldNameDisplay = worldNameBuilder.toString();
		}

		if (subworldName != null && !subworldName.isEmpty()) {
			while (this.worldNameDisplayLength > this.maxWorldNameDisplayLength && subworldName.length() > 5) {
				worldNameBuilder = new StringBuilder(worldName);
				worldNameBuilder.append("...");
				subworldName = subworldName.substring(0, subworldName.length() - 1);
				worldNameBuilder.append(" - ").append(subworldName);
				this.worldNameDisplay = worldNameBuilder.toString();
				this.worldNameDisplayLength = this.getFontRenderer().getStringWidth(this.worldNameDisplay);
			}
		}
	}

	protected void actionPerformed(GuiButton button) {
		if (button.enabled) {
			if (button.id == 100) {
				this.getMinecraft().displayGuiScreen(new GuiWaypoints(this, this.master));
			}

			if (button.id == 103) {
				this.getMinecraft().displayGuiScreen(new GuiMinimapOptions(this, this.master));
			}

			if (button.id == 104) {
				this.getMinecraft().displayGuiScreen(new GuiSubworldsSelect(this, this.master));
			}

			if (button.id == 200) {
				this.getMinecraft().displayGuiScreen(this.parent);
			}
		}
	}

	private float bindZoom(float zoom) {
		zoom = Math.max(this.options.minZoom, zoom);
		return Math.min(this.options.maxZoom, zoom);
	}

	private float easeOut(float elapsedTime, float startValue, float finalDelta, float totalTime) {
		float value;
		if (elapsedTime == totalTime) {
			value = startValue + finalDelta;
		} else {
			value = finalDelta * (-((float) Math.pow(2.0, -10.0F * elapsedTime / totalTime)) + 1.0F) + startValue;
		}

		return value;
	}

	public void handleMouseInput() {
		this.timeOfLastMouseInput = System.currentTimeMillis();
		this.switchToMouseInput();
		super.handleMouseInput();
		int mouseDirectX = Mouse.getX();
		int mouseDirectY = this.mc.displayHeight - Mouse.getY();
		int mouseRoll = Mouse.getEventDWheel();
		if (mouseRoll != 0) {
			if (mouseRoll > 0) {
				this.zoomGoal *= 1.26F;
			} else {
				this.zoomGoal /= 1.26F;
			}

			this.zoomStart = this.zoom;
			this.zoomGoal = this.bindZoom(this.zoomGoal);
			this.timeOfZoom = System.currentTimeMillis();
			this.zoomDirectX = mouseDirectX;
			this.zoomDirectY = mouseDirectY;
		}

		if (this.mouseY > this.top && this.mouseY < this.bottom && Mouse.isButtonDown(1)) {
			this.timeOfLastKBInput = 0L;
		}
	}

	protected void mouseReleased(int mouseX, int mouseY, int mouseButton) {
		super.mouseReleased(mouseX, mouseY, mouseButton);
		if (mouseY > this.top && mouseY < this.bottom && mouseButton == 1) {
			int mouseDirectX = Mouse.getX();
			int mouseDirectY = this.mc.displayHeight - Mouse.getY();
			this.createPopup(mouseX, mouseY, mouseDirectX, mouseDirectY);
		}
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		if (!this.popupOpen()) {
			this.coordinates.mouseClicked(mouseX, mouseY, mouseButton);
			this.editingCoordinates = this.coordinates.isFocused();
			if (this.editingCoordinates && !this.lastEditingCoordinates) {
				int x;
				int z;
				if (this.oldNorth) {
					x = (int) Math.floor(this.mapCenterZ);
					z = -((int) Math.floor(this.mapCenterX));
				} else {
					x = (int) Math.floor(this.mapCenterX);
					z = (int) Math.floor(this.mapCenterZ);
				}

				this.coordinates.setText(x + ", " + z);
				this.coordinates.setTextColor(16777215);
			}

			this.lastEditingCoordinates = this.editingCoordinates;
		}
	}

	public void handleKeyboardInput() {
		int i = Keyboard.getEventKey() == 0 ? Keyboard.getEventCharacter() + 256 : Keyboard.getEventKey();
		boolean down = Keyboard.getEventKeyState();
		if (down) {
			KeyBinding.setKeyBindState(i, true);
			KeyBinding.onTick(i);
		} else {
			KeyBinding.setKeyBindState(i, false);
		}

		super.handleKeyboardInput();
	}

	protected void keyTyped(char typedChar, int keyCode) {
		if (!this.editingCoordinates
			&& (keyCode == this.mc.gameSettings.keyBindJump.getKeyCode() || keyCode == this.mc.gameSettings.keyBindSneak.getKeyCode())) {
			if (keyCode == this.mc.gameSettings.keyBindJump.getKeyCode()) {
				this.zoomGoal /= 1.26F;
			}

			if (keyCode == this.mc.gameSettings.keyBindSneak.getKeyCode()) {
				this.zoomGoal *= 1.26F;
			}

			this.zoomStart = this.zoom;
			this.zoomGoal = this.bindZoom(this.zoomGoal);
			this.timeOfZoom = System.currentTimeMillis();
			this.zoomDirectX = Display.getWidth() / 2;
			this.zoomDirectY = Display.getHeight() - Display.getHeight() / 2;
			this.switchToKeyboardInput();
		}

		this.clearPopups();
		if (keyCode == this.master.getMapOptions().keyBindMenu.getKeyCode()) {
			super.keyTyped('e', 1);
		}

		super.keyTyped(typedChar, keyCode);
		if (this.editingCoordinates) {
			this.coordinates.textboxKeyTyped(typedChar, keyCode);
			boolean isGood = this.isAcceptable(this.coordinates.getText());
			this.coordinates.setTextColor(isGood ? 16777215 : 16711680);
			if (typedChar == '\r' && this.coordinates.isFocused() && isGood) {
				String[] xz = this.coordinates.getText().split(",");
				this.centerAt(Integer.parseInt(xz[0].trim()), Integer.parseInt(xz[1].trim()));
				this.editingCoordinates = false;
				this.lastEditingCoordinates = false;
				this.switchToKeyboardInput();
			}
		}
	}

	private boolean isAcceptable(String input) {
		try {
			String[] xz = this.coordinates.getText().split(",");
			Integer.valueOf(xz[0].trim());
			Integer.valueOf(xz[1].trim());
			return true;
		} catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
			return false;
		}
	}

	private void switchToMouseInput() {
		this.timeOfLastKBInput = 0L;
		if (!this.mouseCursorShown) {
			this.mc.mouseHelper.ungrabMouseCursor();
		}

		this.mouseCursorShown = true;
	}

	private void switchToKeyboardInput() {
		this.timeOfLastKBInput = System.currentTimeMillis();
		this.mouseCursorShown = false;
		this.mc.mouseHelper.grabMouseCursor();
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float par3) {
		this.zoomGoal = this.bindZoom(this.zoomGoal);
		this.mouseX = mouseX;
		this.mouseY = mouseY;
		int mouseDirectX = Mouse.getX();
		int mouseDirectY = this.mc.displayHeight - Mouse.getY();
		if (this.zoom != this.zoomGoal) {
			float previousZoom = this.zoom;
			long timeSinceZoom = System.currentTimeMillis() - this.timeOfZoom;
			if ((float) timeSinceZoom < this.timeConstant * 2.0F) {
				this.zoom = this.easeOut((float) timeSinceZoom, this.zoomStart, this.zoomGoal - this.zoomStart, this.timeConstant * 2.0F);
			} else {
				this.zoom = this.zoomGoal;
			}

			float scaledZoom = this.zoom;
			if (this.mc.displayWidth > 1600) {
				scaledZoom = this.zoom * this.mc.displayWidth / 1600.0F;
			}

			float zoomDelta = this.zoom / previousZoom;
			float zoomOffsetX = this.centerX * this.guiToDirectMouse - this.zoomDirectX;
			float zoomOffsetY = (this.top + this.centerY) * this.guiToDirectMouse - this.zoomDirectY;
			float zoomDeltaX = zoomOffsetX - zoomOffsetX * zoomDelta;
			float zoomDeltaY = zoomOffsetY - zoomOffsetY * zoomDelta;
			this.mapCenterX += zoomDeltaX / scaledZoom;
			this.mapCenterZ += zoomDeltaY / scaledZoom;
		}

		this.options.zoom = this.zoomGoal;
		float scaledZoom = this.zoom;
		if (this.mc.displayWidth > 1600) {
			scaledZoom = this.zoom * this.mc.displayWidth / 1600.0F;
		}

		this.guiToMap = this.scScale / scaledZoom;
		this.mapToGui = 1.0F / this.scScale * scaledZoom;
		this.mouseDirectToMap = 1.0F / scaledZoom;
		this.guiToDirectMouse = this.scScale;
		this.drawDefaultBackground();
		if (Mouse.isButtonDown(0)) {
			if (!this.leftMouseButtonDown && !this.overPopup(mouseX, mouseY)) {
				this.deltaX = 0.0F;
				this.deltaY = 0.0F;
				this.lastMouseX = mouseDirectX;
				this.lastMouseY = mouseDirectY;
				this.leftMouseButtonDown = true;
			} else if (this.leftMouseButtonDown) {
				this.deltaX = (this.lastMouseX - mouseDirectX) * this.mouseDirectToMap;
				this.deltaY = (this.lastMouseY - mouseDirectY) * this.mouseDirectToMap;
				this.lastMouseX = mouseDirectX;
				this.lastMouseY = mouseDirectY;
				this.deltaXonRelease = this.deltaX;
				this.deltaYonRelease = this.deltaY;
				this.timeOfRelease = System.currentTimeMillis();
			}
		} else {
			long timeSinceRelease = System.currentTimeMillis() - this.timeOfRelease;
			if ((float) timeSinceRelease < this.timeConstant * 2.0F) {
				this.deltaX = this.deltaXonRelease * (float) Math.exp((float) (-timeSinceRelease) / this.timeConstant);
				this.deltaY = this.deltaYonRelease * (float) Math.exp((float) (-timeSinceRelease) / this.timeConstant);
			} else {
				this.deltaX = 0.0F;
				this.deltaY = 0.0F;
				this.deltaXonRelease = 0.0F;
				this.deltaYonRelease = 0.0F;
			}

			this.leftMouseButtonDown = false;
		}

		long timeSinceLastTick = System.currentTimeMillis() - this.timeAtLastTick;
		this.timeAtLastTick = System.currentTimeMillis();
		if (!this.editingCoordinates) {
			int kbDelta = 5;
			if (this.keyBindSprint.isKeyDown()) {
				kbDelta = 10;
			}

			if (this.keyBindForward.isKeyDown()) {
				this.deltaY -= kbDelta / scaledZoom * (float) timeSinceLastTick / 12.0F;
				this.switchToKeyboardInput();
			}

			if (this.keyBindBack.isKeyDown()) {
				this.deltaY += kbDelta / scaledZoom * (float) timeSinceLastTick / 12.0F;
				this.switchToKeyboardInput();
			}

			if (this.keyBindLeft.isKeyDown()) {
				this.deltaX -= kbDelta / scaledZoom * (float) timeSinceLastTick / 12.0F;
				this.switchToKeyboardInput();
			}

			if (this.keyBindRight.isKeyDown()) {
				this.deltaX += kbDelta / scaledZoom * (float) timeSinceLastTick / 12.0F;
				this.switchToKeyboardInput();
			}
		}

		this.mapCenterX = this.mapCenterX + this.deltaX;
		this.mapCenterZ = this.mapCenterZ + this.deltaY;
		if (this.oldNorth) {
			this.options.mapX = (int) this.mapCenterZ;
			this.options.mapZ = -((int) this.mapCenterX);
		} else {
			this.options.mapX = (int) this.mapCenterX;
			this.options.mapZ = (int) this.mapCenterZ;
		}

		this.centerX = this.getWidth() / 2;
		this.centerY = (this.bottom - this.top) / 2;
		int left;
		int right;
		int top;
		int bottom;
		if (this.oldNorth) {
			left = (int) Math.floor((this.mapCenterZ - this.centerY * this.guiToMap) / 256.0F);
			right = (int) Math.floor((this.mapCenterZ + this.centerY * this.guiToMap) / 256.0F);
			top = (int) Math.floor((-this.mapCenterX - this.centerX * this.guiToMap) / 256.0F);
			bottom = (int) Math.floor((-this.mapCenterX + this.centerX * this.guiToMap) / 256.0F);
		} else {
			left = (int) Math.floor((this.mapCenterX - this.centerX * this.guiToMap) / 256.0F);
			right = (int) Math.floor((this.mapCenterX + this.centerX * this.guiToMap) / 256.0F);
			top = (int) Math.floor((this.mapCenterZ - this.centerY * this.guiToMap) / 256.0F);
			bottom = (int) Math.floor((this.mapCenterZ + this.centerY * this.guiToMap) / 256.0F);
		}

		synchronized (this.closedLock) {
			if (this.closed) {
				return;
			}

			this.regions = this.persistentMap.getRegions(left - 1, right + 1, top - 1, bottom + 1);
		}

		GLShim.glColor3f(1.0F, 1.0F, 1.0F);
		GLShim.glTranslatef(this.centerX - this.mapCenterX * this.mapToGui, this.top + this.centerY - this.mapCenterZ * this.mapToGui, 0.0F);
		if (this.oldNorth) {
			GLShim.glRotatef(90.0F, 0.0F, 0.0F, 1.0F);
		}

		this.backGroundImageInfo = this.waypointManager.getBackgroundImageInfo();
		if (this.backGroundImageInfo != null) {
			GLUtils.disp(this.backGroundImageInfo.glid);
			this.drawTexturedModalRect(
				this.backGroundImageInfo.left * this.mapToGui,
				this.backGroundImageInfo.top * this.mapToGui,
				this.backGroundImageInfo.width * this.mapToGui,
				this.backGroundImageInfo.height * this.mapToGui
			);
		}

		for (CachedRegion region : this.regions) {
			int glid = region.getGLID();
			if (glid != 0) {
				GLUtils.disp(glid);
				if (this.mapOptions.filtering) {
					if (GLUtils.openGL14Enabled) {
						GLShim.glTexParameteri(GLShim.GL11_GL_TEXTURE_2D, GLShim.GL11_GL_TEXTURE_MIN_FILTER, GLShim.GL11_GL_LINEAR_MIPMAP_LINEAR);
					} else {
						GLShim.glTexParameteri(GLShim.GL11_GL_TEXTURE_2D, GLShim.GL11_GL_TEXTURE_MIN_FILTER, GLShim.GL11_GL_LINEAR);
					}

					GLShim.glTexParameteri(GLShim.GL11_GL_TEXTURE_2D, GLShim.GL11_GL_TEXTURE_MAG_FILTER, GLShim.GL11_GL_LINEAR);
				} else {
					if (GLUtils.openGL14Enabled) {
						GLShim.glTexParameteri(GLShim.GL11_GL_TEXTURE_2D, GLShim.GL11_GL_TEXTURE_MIN_FILTER, GLShim.GL11_GL_LINEAR_MIPMAP_LINEAR);
					} else {
						GLShim.glTexParameteri(GLShim.GL11_GL_TEXTURE_2D, GLShim.GL11_GL_TEXTURE_MIN_FILTER, GLShim.GL11_GL_NEAREST);
					}

					GLShim.glTexParameteri(GLShim.GL11_GL_TEXTURE_2D, GLShim.GL11_GL_TEXTURE_MAG_FILTER, GLShim.GL11_GL_NEAREST);
				}

				this.drawTexturedModalRect(
					region.getX() * 256 * this.mapToGui, region.getZ() * 256 * this.mapToGui, region.getWidth() * this.mapToGui, region.getWidth() * this.mapToGui
				);
			}
		}

		float cursorX;
		float cursorY;
		if (this.mouseCursorShown) {
			cursorX = mouseDirectX;
			cursorY = mouseDirectY - this.top * this.guiToDirectMouse;
		} else {
			cursorX = Display.getWidth() / 2;
			cursorY = Display.getHeight() - Display.getHeight() / 2 - this.top * this.guiToDirectMouse;
		}

		float cursorCoordZ;
		float cursorCoordX;
		if (this.oldNorth) {
			cursorCoordX = cursorY * this.mouseDirectToMap + (this.mapCenterZ - this.centerY * this.guiToMap);
			cursorCoordZ = -(cursorX * this.mouseDirectToMap + (this.mapCenterX - this.centerX * this.guiToMap));
		} else {
			cursorCoordX = cursorX * this.mouseDirectToMap + (this.mapCenterX - this.centerX * this.guiToMap);
			cursorCoordZ = cursorY * this.mouseDirectToMap + (this.mapCenterZ - this.centerY * this.guiToMap);
		}

		GLShim.glEnable(GLShim.GL11_GL_BLEND);
		if (this.options.showWaypoints) {
			for (Waypoint pt : this.waypointManager.getWaypoints()) {
				this.drawWaypoint(pt, cursorCoordX, cursorCoordZ, null, null, null, null);
			}

			if (this.waypointManager.getHighlightedWaypoint() != null) {
				this.drawWaypoint(
					this.waypointManager.getHighlightedWaypoint(),
					cursorCoordX,
					cursorCoordZ,
					this.master.getWaypointManager().getTextureAtlas().getAtlasSprite("voxelmap:images/waypoints/target.png"),
					1.0F,
					0.0F,
					0.0F
				);
			}
		}

		GLShim.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GLUtils.disp(playerGLID);
		GLShim.glTexParameteri(GLShim.GL11_GL_TEXTURE_2D, GLShim.GL11_GL_TEXTURE_MIN_FILTER, GLShim.GL11_GL_LINEAR);
		GLShim.glTexParameteri(GLShim.GL11_GL_TEXTURE_2D, GLShim.GL11_GL_TEXTURE_MAG_FILTER, GLShim.GL11_GL_LINEAR);
		float playerX = (float) GameVariableAccessShim.xCoordDouble();
		float playerZ = (float) GameVariableAccessShim.zCoordDouble();
		if (this.oldNorth) {
			GLShim.glPushMatrix();
			GLShim.glTranslatef(playerX * this.mapToGui, playerZ * this.mapToGui, 0.0F);
			GLShim.glRotatef(-90.0F, 0.0F, 0.0F, 1.0F);
			GLShim.glTranslatef(-(playerX * this.mapToGui), -(playerZ * this.mapToGui), 0.0F);
		}

		this.drawTexturedModalRect(
			-10.0F / this.scScale + playerX * this.mapToGui, -10.0F / this.scScale + playerZ * this.mapToGui, 20.0F / this.scScale, 20.0F / this.scScale
		);
		if (this.oldNorth) {
			GLShim.glPopMatrix();
		}

		if (this.oldNorth) {
			GLShim.glRotatef(-90.0F, 0.0F, 0.0F, 1.0F);
		}

		GLShim.glTranslatef(-(this.centerX - this.mapCenterX * this.mapToGui), -(this.top + this.centerY - this.mapCenterZ * this.mapToGui), 0.0F);
		if (this.mapOptions.biomeOverlay != 0) {
			float biomeScaleX = this.mapPixelsX / 760.0F;
			float biomeScaleY = this.mapPixelsY / 360.0F;
			boolean still = !this.leftMouseButtonDown;
			still = still && this.zoom == this.zoomGoal;
			still = still && this.deltaX == 0.0F && this.deltaY == 0.0F;
			still = still && ThreadManager.executorService.getActiveCount() == 0;
			if (still && !this.lastStill) {
				int column;
				if (this.oldNorth) {
					column = (int) Math.floor(Math.floor(this.mapCenterZ - this.centerY * this.guiToMap) / 256.0) - (left - 1);
				} else {
					column = (int) Math.floor(Math.floor(this.mapCenterX - this.centerX * this.guiToMap) / 256.0) - (left - 1);
				}

				for (int x = 0; x < this.biomeMapData.getWidth(); x++) {
					for (int z = 0; z < this.biomeMapData.getHeight(); z++) {
						float floatMapX;
						float floatMapZ;
						if (this.oldNorth) {
							floatMapX = z * biomeScaleY * this.mouseDirectToMap + (this.mapCenterZ - this.centerY * this.guiToMap);
							floatMapZ = -(x * biomeScaleX * this.mouseDirectToMap + (this.mapCenterX - this.centerX * this.guiToMap));
						} else {
							floatMapX = x * biomeScaleX * this.mouseDirectToMap + (this.mapCenterX - this.centerX * this.guiToMap);
							floatMapZ = z * biomeScaleY * this.mouseDirectToMap + (this.mapCenterZ - this.centerY * this.guiToMap);
						}

						int mapX = (int) Math.floor(floatMapX);
						int mapZ = (int) Math.floor(floatMapZ);
						int regionX = (int) Math.floor(mapX / 256.0F) - (left - 1);
						int regionZ = (int) Math.floor(mapZ / 256.0F) - (top - 1);
						if (!this.oldNorth && regionX != column || this.oldNorth && regionZ != column) {
							this.persistentMap.compress();
						}

						column = !this.oldNorth ? regionX : regionZ;
						CachedRegion region = this.regions[regionZ * (right + 1 - (left - 1) + 1) + regionX];
						int id = -1;
						if (region.getMapData() != null && region.isLoaded() && !region.isEmpty()) {
							int inRegionX = mapX - region.getX() * region.getWidth();
							int inRegionZ = mapZ - region.getZ() * region.getWidth();
							int height = region.getMapData().getHeight(inRegionX, inRegionZ);
							int light = region.getMapData().getLight(inRegionX, inRegionZ);
							if (height != 0 || light != 0) {
								id = region.getMapData().getBiomeID(inRegionX, inRegionZ);
							}
						}

						this.biomeMapData.setBiomeID(x, z, id);
					}
				}

				this.persistentMap.compress();
				this.biomeMapData.segmentBiomes();
				this.biomeMapData.findCenterOfSegments(true);
			}

			this.lastStill = still;
			if (still) {
				int minimumSize = (int) (20.0F * this.scScale / biomeScaleX);
				minimumSize *= minimumSize;
				ArrayList<AbstractMapData.BiomeLabel> labels = this.biomeMapData.getBiomeLabels();
				GLShim.glDisable(GLShim.GL11_GL_DEPTH_TEST);

				for (AbstractMapData.BiomeLabel label : labels) {
					if (label.segmentSize > minimumSize) {
						int nameWidth = this.chkLen(label.name);
						float x = label.x * biomeScaleX / this.scScale;
						float z = label.z * biomeScaleY / this.scScale;
						this.write(label.name, x - nameWidth / 2, this.top + z - 3.0F, 16777215);
					}
				}

				GLShim.glEnable(GLShim.GL11_GL_DEPTH_TEST);
			}
		}

		if (System.currentTimeMillis() - this.timeOfLastKBInput < 2000L) {
			int scWidth = this.scRes.getScaledWidth();
			int scHeight = this.scRes.getScaledHeight();
			this.mc.getTextureManager().bindTexture(ICONS);
			GlStateManager.enableBlend();
			GlStateManager.tryBlendFuncSeparate(775, 769, 1, 0);
			GlStateManager.enableAlpha();
			this.drawTexturedModalRect(scWidth / 2 - 7, scHeight / 2 - 7, 0, 0, 16, 16);
			GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
		} else {
			this.switchToMouseInput();
		}

		this.overlayBackground(0, this.top, 255, 255);
		this.overlayBackground(this.bottom, this.getHeight(), 255, 255);
		this.drawCenteredString(this.getFontRenderer(), this.screenTitle, this.getWidth() / 2, 16, 16777215);
		int x = (int) Math.floor(cursorCoordX);
		int z = (int) Math.floor(cursorCoordZ);
		if (!this.editingCoordinates) {
			this.drawString(this.getFontRenderer(), "X: " + x, this.sideMargin, 16, 16777215);
			this.drawString(this.getFontRenderer(), "Z: " + z, this.sideMargin + 64, 16, 16777215);
		} else {
			this.coordinates.drawTextBox();
		}

		if (this.subworldName != null && !this.subworldName.equals(this.master.getWaypointManager().getCurrentSubworldDescriptor(true))
			|| this.master.getWaypointManager().getCurrentSubworldDescriptor(true) != null
			&& !this.master.getWaypointManager().getCurrentSubworldDescriptor(true).equals(this.subworldName)) {
			this.buildWorldName();
		}

		this.drawString(this.getFontRenderer(), this.worldNameDisplay, this.getWidth() - this.sideMargin - this.worldNameDisplayLength, 16, 16777215);
		if (this.buttonMultiworld != null) {
			if ((this.subworldName == null || this.subworldName.isEmpty()) && this.master.getWaypointManager().isMultiworld()) {
				String attention = "";
				if ((int) (System.currentTimeMillis() / 1000L % 2L) == 0) {
					attention = "§c";
				}

				this.buttonMultiworld.displayString = attention + I18nUtils.getString("options.worldmap.multiworld");
			} else {
				this.buttonMultiworld.displayString = I18nUtils.getString("options.worldmap.multiworld");
			}
		}

		super.drawScreen(mouseX, mouseY, par3);
	}

	private void drawWaypoint(Waypoint pt, float cursorCoordX, float cursorCoordZ, Sprite icon, Float r, Float g, Float b) {
		if (pt.inWorld && pt.inDimension && this.isOnScreen(pt.getX(), pt.getZ())) {
			String name = pt.name;
			if (r == null) {
				r = pt.red;
			}

			if (g == null) {
				g = pt.green;
			}

			if (b == null) {
				b = pt.blue;
			}

			float ptX = pt.getX();
			float ptZ = pt.getZ();
			if (this.backGroundImageInfo != null && this.backGroundImageInfo.isInRange((int) ptX, (int) ptZ)
				|| this.persistentMap.isRegionLoaded((int) ptX, (int) ptZ)) {
				ptX += 0.5F;
				ptZ += 0.5F;
				boolean hover = cursorCoordX > ptX - 18.0F * this.guiToMap / this.guiToDirectMouse
					&& cursorCoordX < ptX + 18.0F * this.guiToMap / this.guiToDirectMouse
					&& cursorCoordZ > ptZ - 18.0F * this.guiToMap / this.guiToDirectMouse
					&& cursorCoordZ < ptZ + 18.0F * this.guiToMap / this.guiToDirectMouse;
				boolean target = false;
				TextureAtlas atlas = this.master.getWaypointManager().getTextureAtlas();
				GLUtils.disp(atlas.getGlTextureId());
				if (icon == null) {
					icon = atlas.getAtlasSprite("voxelmap:images/waypoints/waypoint" + pt.imageSuffix + ".png");
					if (icon == atlas.getMissingImage()) {
						icon = atlas.getAtlasSprite("voxelmap:images/waypoints/waypoint.png");
					}
				} else {
					name = "";
					target = true;
				}

				GLShim.glColor4f(r, g, b, !pt.enabled && !target && !hover ? 0.3F : 1.0F);
				GLShim.glTexParameteri(GLShim.GL11_GL_TEXTURE_2D, GLShim.GL11_GL_TEXTURE_MIN_FILTER, GLShim.GL11_GL_LINEAR);
				GLShim.glTexParameteri(GLShim.GL11_GL_TEXTURE_2D, GLShim.GL11_GL_TEXTURE_MAG_FILTER, GLShim.GL11_GL_LINEAR);
				if (this.oldNorth) {
					GLShim.glPushMatrix();
					GLShim.glTranslatef(ptX * this.mapToGui, ptZ * this.mapToGui, 0.0F);
					GLShim.glRotatef(-90.0F, 0.0F, 0.0F, 1.0F);
					GLShim.glTranslatef(-(ptX * this.mapToGui), -(ptZ * this.mapToGui), 0.0F);
				}

				this.drawTexturedModalRect(
					-16.0F / this.scScale + ptX * this.mapToGui, -16.0F / this.scScale + ptZ * this.mapToGui, icon, 32.0F / this.scScale, 32.0F / this.scScale
				);
				if (this.oldNorth) {
					GLShim.glPopMatrix();
				}

				if (this.mapOptions.biomeOverlay == 0 && this.options.showWaypointNames || target || hover) {
					float fontScale = 2.0F / this.scScale;
					int m = this.chkLen(name) / 2;
					GLShim.glPushMatrix();
					GLShim.glScalef(fontScale, fontScale, 1.0F);
					if (this.oldNorth) {
						GLShim.glTranslatef(ptX * this.mapToGui / fontScale, ptZ * this.mapToGui / fontScale, 0.0F);
						GLShim.glRotatef(-90.0F, 0.0F, 0.0F, 1.0F);
						GLShim.glTranslatef(-(ptX * this.mapToGui / fontScale), -(ptZ * this.mapToGui / fontScale), 0.0F);
					}

					this.write(
						name,
						ptX * this.mapToGui / fontScale - m,
						ptZ * this.mapToGui / fontScale + 16.0F / this.scScale / fontScale,
						!pt.enabled && !target && !hover ? 1442840575 : 16777215
					);
					GLUtils.disp(0);
					GLShim.glPopMatrix();
				}
			}
		}
	}

	private boolean isOnScreen(int x, int z) {
		int left;
		int right;
		int top;
		int bottom;
		if (this.oldNorth) {
			left = (int) Math.floor(this.mapCenterZ - this.centerY * this.guiToMap * 1.1);
			right = (int) Math.floor(this.mapCenterZ + this.centerY * this.guiToMap * 1.1);
			top = (int) Math.floor(-this.mapCenterX - this.centerX * this.guiToMap * 1.1);
			bottom = (int) Math.floor(-this.mapCenterX + this.centerX * this.guiToMap * 1.1);
		} else {
			left = (int) Math.floor(this.mapCenterX - this.centerX * this.guiToMap * 1.1);
			right = (int) Math.floor(this.mapCenterX + this.centerX * this.guiToMap * 1.1);
			top = (int) Math.floor(this.mapCenterZ - this.centerY * this.guiToMap * 1.1);
			bottom = (int) Math.floor(this.mapCenterZ + this.centerY * this.guiToMap * 1.1);
		}

		return x > left && x < right && z > top && z < bottom;
	}

	public void drawDefaultBackground() {
		drawRect(0, 0, this.getWidth(), this.getHeight(), -16777216);
	}

	protected void overlayBackground(int startY, int endY, int startAlpha, int endAlpha) {
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder vertexBuffer = tessellator.getBuffer();
		this.mc.getTextureManager().bindTexture(Gui.OPTIONS_BACKGROUND);
		GLShim.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		vertexBuffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
		vertexBuffer.pos(0.0, endY, 0.0).tex(0.0, endY / 32.0F).color(64, 64, 64, endAlpha).endVertex();
		vertexBuffer.pos(this.getWidth(), endY, 0.0)
			.tex(this.width / 32.0F, endY / 32.0F)
			.color(64, 64, 64, endAlpha)
			.endVertex();
		vertexBuffer.pos(this.getWidth(), startY, 0.0)
			.tex(this.width / 32.0F, startY / 32.0F)
			.color(64, 64, 64, startAlpha)
			.endVertex();
		vertexBuffer.pos(0.0, startY, 0.0).tex(0.0, startY / 32.0F).color(64, 64, 64, startAlpha).endVertex();
		tessellator.draw();
	}

	public void updateScreen() {
		this.coordinates.updateCursorCounter();
	}

	@Override
	public void onGuiClosed() {
		this.mc.gameSettings.keyBindForward.setKeyCode(this.forwardCode);
		this.mc.gameSettings.keyBindLeft.setKeyCode(this.leftCode);
		this.mc.gameSettings.keyBindBack.setKeyCode(this.backCode);
		this.mc.gameSettings.keyBindRight.setKeyCode(this.rightCode);
		this.mc.gameSettings.keyBindSprint.setKeyCode(this.sprintCode);
		this.keyBindForward.setKeyCode(0);
		this.keyBindLeft.setKeyCode(0);
		this.keyBindBack.setKeyCode(0);
		this.keyBindRight.setKeyCode(0);
		this.keyBindSprint.setKeyCode(0);
		KeyBinding.resetKeyBindingArrayAndHash();
		KeyBinding.unPressAllKeys();
		Keyboard.enableRepeatEvents(false);
		synchronized (this.closedLock) {
			this.closed = true;
			this.persistentMap.getRegions(0, -1, 0, -1);
			this.regions = new CachedRegion[0];
		}

		Keyboard.enableRepeatEvents(false);
	}

	public void drawTexturedModalRect(float x, float y, float width, float height) {
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder vertexBuffer = tessellator.getBuffer();
		vertexBuffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		vertexBuffer.pos(x + 0.0F, y + height, this.zLevel).tex(0.0, 1.0).endVertex();
		vertexBuffer.pos(x + width, y + height, this.zLevel).tex(1.0, 1.0).endVertex();
		vertexBuffer.pos(x + width, y + 0.0F, this.zLevel).tex(1.0, 0.0).endVertex();
		vertexBuffer.pos(x + 0.0F, y + 0.0F, this.zLevel).tex(0.0, 0.0).endVertex();
		tessellator.draw();
	}

	public void drawTexturedModalRect(Sprite icon, float x, float y) {
		float width = icon.getIconWidth() / this.scScale;
		float height = icon.getIconHeight() / this.scScale;
		this.drawTexturedModalRect(x, y, icon, width, height);
	}

	public void drawTexturedModalRect(float xCoord, float yCoord, Sprite icon, float widthIn, float heightIn) {
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder vertexBuffer = tessellator.getBuffer();
		vertexBuffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		vertexBuffer.pos(xCoord + 0.0F, yCoord + heightIn, this.zLevel).tex(icon.getMinU(), icon.getMaxV()).endVertex();
		vertexBuffer.pos(xCoord + widthIn, yCoord + heightIn, this.zLevel).tex(icon.getMaxU(), icon.getMaxV()).endVertex();
		vertexBuffer.pos(xCoord + widthIn, yCoord + 0.0F, this.zLevel).tex(icon.getMaxU(), icon.getMinV()).endVertex();
		vertexBuffer.pos(xCoord + 0.0F, yCoord + 0.0F, this.zLevel).tex(icon.getMinU(), icon.getMinV()).endVertex();
		tessellator.draw();
	}

	private void createPopup(int mouseX, int mouseY, int mouseDirectX, int mouseDirectY) {
		ArrayList<Popup.PopupEntry> entries = new ArrayList<>();

		float cursorX = mouseDirectX;
		float cursorY = mouseDirectY - this.top * this.guiToDirectMouse;

		float cursorCoordX;
		float cursorCoordZ;
		if (this.oldNorth) {
			cursorCoordX = cursorY * this.mouseDirectToMap + (this.mapCenterZ - this.centerY * this.guiToMap);
			cursorCoordZ = -(cursorX * this.mouseDirectToMap + (this.mapCenterX - this.centerX * this.guiToMap));
		} else {
			cursorCoordX = cursorX * this.mouseDirectToMap + (this.mapCenterX - this.centerX * this.guiToMap);
			cursorCoordZ = cursorY * this.mouseDirectToMap + (this.mapCenterZ - this.centerY * this.guiToMap);
		}

		int x = (int) Math.floor(cursorCoordX);
		int z = (int) Math.floor(cursorCoordZ);
		boolean canTeleport = this.canTeleport();
		canTeleport = canTeleport && (this.persistentMap.isGroundAt(x, z) || this.backGroundImageInfo != null && this.backGroundImageInfo.isGroundAt(x, z));
		Waypoint hovered = this.getHovered(cursorCoordX, cursorCoordZ);
		if (hovered != null && this.waypointManager.getWaypoints().contains(hovered)) {
			Popup.PopupEntry entry = new Popup.PopupEntry(I18nUtils.getString("selectServer.edit"), 4, true, true);
			entries.add(entry);
			entry = new Popup.PopupEntry(I18nUtils.getString("selectServer.delete"), 5, true, true);
			entries.add(entry);
			entry = new Popup.PopupEntry(
				I18nUtils.getString(hovered != this.waypointManager.getHighlightedWaypoint() ? "minimap.waypoints.highlight" : "minimap.waypoints.removehighlight"),
				6,
				true,
				true
			);
			entries.add(entry);
			entry = new Popup.PopupEntry(I18nUtils.getString("minimap.waypoints.teleportto"), 8, true, canTeleport);
			entries.add(entry);
			entry = new Popup.PopupEntry(I18nUtils.getString("minimap.waypoints.share"), 7, true, true);
			entries.add(entry);
		} else {
			Popup.PopupEntry entry = new Popup.PopupEntry(I18nUtils.getString("minimap.waypoints.newwaypoint"), 0, true, true);
			entries.add(entry);
			entry = new Popup.PopupEntry(I18nUtils.getString(hovered == null ? "minimap.waypoints.highlight" : "minimap.waypoints.removehighlight"), 1, true, true);
			entries.add(entry);
			entry = new Popup.PopupEntry(I18nUtils.getString("minimap.waypoints.teleportto"), 3, true, canTeleport);
			entries.add(entry);
			entry = new Popup.PopupEntry(I18nUtils.getString("minimap.waypoints.share"), 2, true, true);
			entries.add(entry);
		}

		this.createPopup(mouseX, mouseY, mouseDirectX, mouseDirectY, entries);
	}

	private Waypoint getHovered(float cursorCoordX, float cursorCoordZ) {
		Waypoint waypoint = null;

		for (Waypoint pt : this.waypointManager.getWaypoints()) {
			float ptX = pt.getX() + 0.5F;
			float ptZ = pt.getZ() + 0.5F;
			boolean hover = pt.inDimension
				&& pt.inWorld
				&& cursorCoordX > ptX - 18.0F * this.guiToMap / this.guiToDirectMouse
				&& cursorCoordX < ptX + 18.0F * this.guiToMap / this.guiToDirectMouse
				&& cursorCoordZ > ptZ - 18.0F * this.guiToMap / this.guiToDirectMouse
				&& cursorCoordZ < ptZ + 18.0F * this.guiToMap / this.guiToDirectMouse;
			if (hover) {
				waypoint = pt;
			}
		}

		if (waypoint == null) {
			Waypoint pt = this.waypointManager.getHighlightedWaypoint();
			if (pt != null) {
				float ptX = pt.getX() + 0.5F;
				float ptZ = pt.getZ() + 0.5F;
				boolean hover = pt.inDimension
					&& pt.inWorld
					&& cursorCoordX > ptX - 18.0F * this.guiToMap / this.guiToDirectMouse
					&& cursorCoordX < ptX + 18.0F * this.guiToMap / this.guiToDirectMouse
					&& cursorCoordZ > ptZ - 18.0F * this.guiToMap / this.guiToDirectMouse
					&& cursorCoordZ < ptZ + 18.0F * this.guiToMap / this.guiToDirectMouse;
				if (hover) {
					waypoint = pt;
				}
			}
		}

		return waypoint;
	}

	@Override
	public void popupAction(Popup popup, int action) {
		int mouseDirectX = popup.clickedDirectX;
		int mouseDirectY = popup.clickedDirectY;
		float cursorX = mouseDirectX;
		float cursorY = mouseDirectY - this.top * this.guiToDirectMouse;
		float cursorCoordX;
		float cursorCoordZ;
		if (this.oldNorth) {
			cursorCoordX = cursorY * this.mouseDirectToMap + (this.mapCenterZ - this.centerY * this.guiToMap);
			cursorCoordZ = -(cursorX * this.mouseDirectToMap + (this.mapCenterX - this.centerX * this.guiToMap));
		} else {
			cursorCoordX = cursorX * this.mouseDirectToMap + (this.mapCenterX - this.centerX * this.guiToMap);
			cursorCoordZ = cursorY * this.mouseDirectToMap + (this.mapCenterZ - this.centerY * this.guiToMap);
		}

		int x = (int) Math.floor(cursorCoordX);
		int z = (int) Math.floor(cursorCoordZ);
		int y = this.persistentMap.getHeightAt(x, z);
		Waypoint hovered = this.getHovered(cursorCoordX, cursorCoordZ);
		this.editClicked = false;
		this.addClicked = false;
		this.deleteClicked = false;
		switch (action) {
			case 0:
				if (hovered != null) {
					x = hovered.getX();
					z = hovered.getZ();
				}

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
				dimensions.add(this.mc.player.dimension);
				this.newWaypoint = new Waypoint(
					"",
					this.mc.player.dimension != -1 ? x : x * 8,
					this.mc.player.dimension != -1 ? z : z * 8,
					y,
					true,
					r,
					g,
					b,
					"",
					this.master.getWaypointManager().getCurrentSubworldDescriptor(false),
					dimensions
				);
				this.mc.displayGuiScreen(new GuiAddWaypoint(this, this.master, this.newWaypoint, false));
				break;
			case 1:
				if (hovered != null) {
					this.waypointManager.setHighlightedWaypoint(null, true);
				} else {
					y = y > 0 ? y : 64;
					TreeSet<Integer> dimensions2 = new TreeSet<>();
					dimensions2.add(this.mc.player.dimension);
					Waypoint fakePoint = new Waypoint(
						"",
						this.mc.player.dimension != -1 ? x : x * 8,
						this.mc.player.dimension != -1 ? z : z * 8,
						y,
						true,
						1.0F,
						0.0F,
						0.0F,
						"",
						this.master.getWaypointManager().getCurrentSubworldDescriptor(false),
						dimensions2
					);
					this.waypointManager.setHighlightedWaypoint(fakePoint, true);
				}
				break;
			case 2:
				y = y > 0 ? y : 64;
				CommandUtils.sendCoordinate(x, y, z);
				break;
			case 3:
				if (y == 0) {
					y = this.mc.player.dimension != -1 ? 255 : 64;
				}

				this.mc.player.sendChatMessage("/tp " + this.mc.player.getName() + " " + x + " " + y + " " + z);
				if (!this.mc.isIntegratedServerRunning()) {
					this.mc.player.sendChatMessage("/tppos " + x + " " + y + " " + z);
				}
				break;
			case 4:
				if (hovered != null) {
					this.editClicked = true;
					this.selectedWaypoint = hovered;
					this.mc.displayGuiScreen(new GuiAddWaypoint(this, this.master, hovered, true));
				}
				break;
			case 5:
				if (hovered != null) {
					this.deleteClicked = true;
					this.selectedWaypoint = hovered;
					String string = I18nUtils.getString("minimap.waypoints.deleteconfirm");
					String string1 = "'" + this.selectedWaypoint.name + "' " + I18nUtils.getString("selectServer.deleteWarning");
					String string2 = I18nUtils.getString("selectServer.deleteButton");
					String string3 = I18nUtils.getString("gui.cancel");
					GuiYesNo gui = new GuiYesNo(this, string, string1, string2, string3, this.waypointManager.getWaypoints().indexOf(this.selectedWaypoint));
					this.getMinecraft().displayGuiScreen(gui);
				}
				break;
			case 6:
				if (hovered != null) {
					this.waypointManager.setHighlightedWaypoint(hovered, true);
				}
				break;
			case 7:
				if (hovered != null) {
					CommandUtils.sendWaypoint(hovered);
				}
				break;
			case 8:
				if (hovered != null) {
					this.selectedWaypoint = hovered;
					boolean mp = !this.mc.isIntegratedServerRunning();
					y = this.selectedWaypoint.getY() > 0 ? this.selectedWaypoint.getY() : (this.mc.player.dimension != -1 ? 128 : 64);
					this.mc
						.player
						.sendChatMessage(
							"/tp " + this.mc.player.getName() + " " + this.selectedWaypoint.getX() + " " + y + " " + this.selectedWaypoint.getZ()
						);
					if (mp) {
						this.mc.player.sendChatMessage("/tppos " + this.selectedWaypoint.getX() + " " + y + " " + this.selectedWaypoint.getZ());
					} else {
						this.getMinecraft().displayGuiScreen(null);
					}
				}
				break;
			default:
				System.out.println("unimplemented command");
		}
	}

	@Override
	public boolean isEditing() {
		return this.editClicked;
	}

	public void confirmClicked(boolean confirm, int par2) {
		if (this.deleteClicked) {
			this.deleteClicked = false;
			if (confirm) {
				this.waypointManager.deleteWaypoint(this.selectedWaypoint);
				this.selectedWaypoint = null;
			}
		}

		if (this.editClicked) {
			this.editClicked = false;
			if (confirm) {
				this.waypointManager.saveWaypoints();
			}
		}

		if (this.addClicked) {
			this.addClicked = false;
			if (confirm) {
				this.waypointManager.addWaypoint(this.newWaypoint);
			}
		}

		this.getMinecraft().displayGuiScreen(this);
	}

	public boolean canTeleport() {
		boolean allowed;
		boolean singlePlayer = this.mc.isIntegratedServerRunning();
		if (singlePlayer) {
			try {
				allowed = Objects.requireNonNull(this.mc.getIntegratedServer()).getPlayerList().canSendCommands(this.mc.player.getGameProfile());
			} catch (Exception e) {
				allowed = Objects.requireNonNull(this.mc.getIntegratedServer()).worlds[0].getWorldInfo().areCommandsAllowed();
			}
		} else {
			allowed = true;
		}

		return allowed;
	}

	private int chkLen(String string) {
		return this.getFontRenderer().getStringWidth(string);
	}

	private void write(String string, float x, float y, int color) {
		this.getFontRenderer().drawStringWithShadow(string, x, y, color);
	}
}
