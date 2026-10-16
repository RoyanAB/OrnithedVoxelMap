package com.mamiyaotaru.voxelmap.gui;

import com.mamiyaotaru.voxelmap.MapSettingsManager;
import com.mamiyaotaru.voxelmap.gui.overridden.EnumOptionsMinimap;
import com.mamiyaotaru.voxelmap.gui.overridden.GuiButtonText;
import com.mamiyaotaru.voxelmap.gui.overridden.GuiOptionButtonMinimap;
import com.mamiyaotaru.voxelmap.gui.overridden.GuiScreenMinimap;
import com.mamiyaotaru.voxelmap.interfaces.IVoxelMap;
import com.mamiyaotaru.voxelmap.util.I18nUtils;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Keyboard;

public class GuiMinimapPerformance extends GuiScreenMinimap {
	private static final EnumOptionsMinimap[] relevantOptions = new EnumOptionsMinimap[]{
		EnumOptionsMinimap.LIGHTING,
		EnumOptionsMinimap.TERRAIN,
		EnumOptionsMinimap.WATERTRANSPARENCY,
		EnumOptionsMinimap.BLOCKTRANSPARENCY,
		EnumOptionsMinimap.BIOMES,
		EnumOptionsMinimap.FILTERING,
		EnumOptionsMinimap.CHUNKGRID,
		EnumOptionsMinimap.BIOMEOVERLAY,
		EnumOptionsMinimap.SLIMECHUNKS
	};
	private final int worldSeedButtonID = relevantOptions.length;
	private GuiButtonText worldSeedButton;
	private final GuiScreen parentScreen;
	protected String screenTitle = "Details / Performance";
	private final MapSettingsManager options;
	IVoxelMap master;

	public GuiMinimapPerformance(GuiScreen par1GuiScreen, IVoxelMap master) {
		this.parentScreen = par1GuiScreen;
		this.options = master.getMapOptions();
		this.master = master;
	}

	private int getLeftBorder() {
		return this.getWidth() / 2 - 155;
	}

	@Override
	public void initGui() {
		this.screenTitle = I18nUtils.getString("options.minimap.detailsperformance");
		Keyboard.enableRepeatEvents(true);
		int leftBorder = this.getLeftBorder();
		int var2 = 0;

		for (int t = 0; t < relevantOptions.length; t++) {
			EnumOptionsMinimap option = relevantOptions[t];
			String text = this.options.getKeyText(option);
			if ((option == EnumOptionsMinimap.WATERTRANSPARENCY || option == EnumOptionsMinimap.BLOCKTRANSPARENCY || option == EnumOptionsMinimap.BIOMES)
				&& !this.options.multicore
				&& this.options.getOptionBooleanValue(option)) {
				text = "§c" + text;
			}

			GuiOptionButtonMinimap var7 = new GuiOptionButtonMinimap(
				option.returnEnumOrdinal(), leftBorder + var2 % 2 * 160, this.getHeight() / 6 + 24 * (var2 >> 1), option, text
			);
			this.buttonList.add(var7);
			var2++;
		}

		String worldSeedDisplay = this.master.getWorldSeed();
		if (worldSeedDisplay.equals("")) {
			worldSeedDisplay = I18nUtils.getString("selectWorld.versionUnknown");
		}

		String buttonText = I18nUtils.getString("options.minimap.worldseed") + ": " + worldSeedDisplay;
		this.worldSeedButton = new GuiButtonText(
			this.worldSeedButtonID, this.getFontRenderer(), leftBorder + var2 % 2 * 160, this.getHeight() / 6 + 24 * (var2 >> 1), 150, 20, buttonText
		);
		this.worldSeedButton.setText(this.master.getWorldSeed());
		this.buttonList.add(this.worldSeedButton);
		var2++;

		for (Object buttonObj : this.getButtonList()) {
			if (buttonObj instanceof GuiOptionButtonMinimap) {
				GuiOptionButtonMinimap button = (GuiOptionButtonMinimap) buttonObj;
				if (button.returnEnumOptions().equals(EnumOptionsMinimap.SLIMECHUNKS)) {
					button.enabled = this.mc.isIntegratedServerRunning() || !this.master.getWorldSeed().equals("");
				}
			}
		}

		this.worldSeedButton.enabled = !this.mc.isIntegratedServerRunning();
		this.buttonList.add(new GuiButton(200, this.getWidth() / 2 - 100, this.getHeight() / 6 + 168, I18nUtils.getString("gui.done")));
	}

	@Override
	public void onGuiClosed() {
		Keyboard.enableRepeatEvents(false);
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		if (button.id < 100 && button instanceof GuiOptionButtonMinimap) {
			this.options.setOptionValue(((GuiOptionButtonMinimap) button).returnEnumOptions(), 1);
			String perfBomb = "";
			if ((
				button.id == EnumOptionsMinimap.WATERTRANSPARENCY.ordinal()
					|| button.id == EnumOptionsMinimap.BLOCKTRANSPARENCY.ordinal()
					|| button.id == EnumOptionsMinimap.BIOMES.ordinal()
			)
				&& !this.options.multicore
				&& this.options.getOptionBooleanValue(EnumOptionsMinimap.getEnumOptions(button.id))) {
				perfBomb = "§c";
			}

			button.displayString = perfBomb + this.options.getKeyText(EnumOptionsMinimap.getEnumOptions(button.id));
		}

		for (Object buttonObj : this.getButtonList()) {
			if (buttonObj instanceof GuiOptionButtonMinimap) {
				GuiOptionButtonMinimap buttonx = (GuiOptionButtonMinimap) buttonObj;
				if (buttonx.returnEnumOptions().equals(EnumOptionsMinimap.SLIMECHUNKS)) {
					buttonx.enabled = this.mc.isIntegratedServerRunning() || !this.master.getWorldSeed().equals("");
				}
			}
		}

		this.worldSeedButton.enabled = !this.mc.isIntegratedServerRunning();
		if (button.id == 200) {
			this.getMinecraft().displayGuiScreen(this.parentScreen);
		}
	}

	@Override
	protected void keyTyped(char character, int keyCode) {
		if (character == '\r' && this.worldSeedButton.isFocused()) {
			String newSeed = this.worldSeedButton.getText();
			this.master.setWorldSeed(newSeed);
			String worldSeedDisplay = this.master.getWorldSeed();
			if (worldSeedDisplay.equals("")) {
				worldSeedDisplay = I18nUtils.getString("selectWorld.versionUnknown");
			}

			String buttonText = I18nUtils.getString("options.minimap.worldseed") + ": " + worldSeedDisplay;
			this.worldSeedButton.displayString = buttonText;
			this.worldSeedButton.setText(this.master.getWorldSeed());
			this.master.getMap().forceFullRender(true);

			for (Object buttonObj : this.getButtonList()) {
				if (buttonObj instanceof GuiOptionButtonMinimap) {
					GuiOptionButtonMinimap button = (GuiOptionButtonMinimap) buttonObj;
					if (button.returnEnumOptions().equals(EnumOptionsMinimap.SLIMECHUNKS)) {
						button.enabled = this.mc.isIntegratedServerRunning() || !this.master.getWorldSeed().equals("");
					}
				}
			}
		}

		this.worldSeedButton.textboxKeyTyped(character, keyCode);
		super.keyTyped(character, keyCode);
	}

	@Override
	public void drawScreen(int par1, int par2, float partialTicks) {
		super.drawMap();
		this.drawDefaultBackground();
		this.drawCenteredString(this.getFontRenderer(), this.screenTitle, this.getWidth() / 2, 20, 16777215);
		super.drawScreen(par1, par2, partialTicks);
	}

	@Override
	public void updateScreen() {
		this.worldSeedButton.updateCursorCounter();
	}
}
