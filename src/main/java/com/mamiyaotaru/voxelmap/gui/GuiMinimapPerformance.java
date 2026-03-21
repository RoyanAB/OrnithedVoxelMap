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

import java.util.Objects;

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
	protected String screenTitle = "Details / Performance";
	IVoxelMap master;
	private GuiButtonText worldSeedButton;
	private final GuiScreen parentScreen;
	private final MapSettingsManager options;

	public GuiMinimapPerformance(GuiScreen par1GuiScreen, IVoxelMap master) {
		this.parentScreen = par1GuiScreen;
		this.options = master.getMapOptions();
		this.master = master;
	}

	private int getLeftBorder() {
		return this.getWidth() / 2 - 155;
	}

	public void initGui() {
		this.screenTitle = I18nUtils.getString("options.minimap.detailsperformance");
		Keyboard.enableRepeatEvents(true);
		int leftBorder = this.getLeftBorder();
		int var2 = 0;

		for (EnumOptionsMinimap option : relevantOptions) {
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
		if (worldSeedDisplay.isEmpty()) {
			worldSeedDisplay = I18nUtils.getString("selectWorld.versionUnknown");
		}

		String buttonText = I18nUtils.getString("options.minimap.worldseed") + ": " + worldSeedDisplay;
		this.worldSeedButton = new GuiButtonText(
			this.worldSeedButtonID, this.getFontRenderer(), leftBorder + var2 % 2 * 160, this.getHeight() / 6 + 24 * (var2 >> 1), 150, 20, buttonText
		);
		this.worldSeedButton.setText(this.master.getWorldSeed());
		this.buttonList.add(this.worldSeedButton);

		for (Object buttonObj : this.getButtonList()) {
			if (buttonObj instanceof GuiOptionButtonMinimap) {
				GuiOptionButtonMinimap button = (GuiOptionButtonMinimap) buttonObj;
				if (button.returnEnumOptions().equals(EnumOptionsMinimap.SLIMECHUNKS)) {
					button.enabled = this.mc.isIntegratedServerRunning() || !this.master.getWorldSeed().isEmpty();
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

	protected void actionPerformed(GuiButton par1GuiButton) {
		if (par1GuiButton.id < 100 && par1GuiButton instanceof GuiOptionButtonMinimap) {
			this.options.setOptionValue(((GuiOptionButtonMinimap) par1GuiButton).returnEnumOptions(), 1);
			String perfBomb = "";
			if ((
				par1GuiButton.id == EnumOptionsMinimap.WATERTRANSPARENCY.ordinal()
					|| par1GuiButton.id == EnumOptionsMinimap.BLOCKTRANSPARENCY.ordinal()
					|| par1GuiButton.id == EnumOptionsMinimap.BIOMES.ordinal()
			)
				&& !this.options.multicore
				&& this.options.getOptionBooleanValue(Objects.requireNonNull(EnumOptionsMinimap.getEnumOptions(par1GuiButton.id)))) {
				perfBomb = "§c";
			}

			par1GuiButton.displayString = perfBomb + this.options.getKeyText(Objects.requireNonNull(EnumOptionsMinimap.getEnumOptions(par1GuiButton.id)));
		}

		for (Object buttonObj : this.getButtonList()) {
			if (buttonObj instanceof GuiOptionButtonMinimap) {
				GuiOptionButtonMinimap button = (GuiOptionButtonMinimap) buttonObj;
				if (button.returnEnumOptions().equals(EnumOptionsMinimap.SLIMECHUNKS)) {
					button.enabled = this.mc.isIntegratedServerRunning() || !this.master.getWorldSeed().isEmpty();
				}
			}
		}

		this.worldSeedButton.enabled = !this.mc.isIntegratedServerRunning();
		if (par1GuiButton.id == 200) {
			this.getMinecraft().displayGuiScreen(this.parentScreen);
		}
	}

	protected void keyTyped(char character, int keycode) {
		if (character == '\r' && this.worldSeedButton.isFocused()) {
			String newSeed = this.worldSeedButton.getText();
			this.master.setWorldSeed(newSeed);
			String worldSeedDisplay = this.master.getWorldSeed();
			if (worldSeedDisplay.isEmpty()) {
				worldSeedDisplay = I18nUtils.getString("selectWorld.versionUnknown");
			}

			this.worldSeedButton.displayString = I18nUtils.getString("options.minimap.worldseed") + ": " + worldSeedDisplay;
			this.worldSeedButton.setText(this.master.getWorldSeed());
			this.master.getMap().forceFullRender(true);

			for (Object buttonObj : this.getButtonList()) {
				if (buttonObj instanceof GuiOptionButtonMinimap) {
					GuiOptionButtonMinimap button = (GuiOptionButtonMinimap) buttonObj;
					if (button.returnEnumOptions().equals(EnumOptionsMinimap.SLIMECHUNKS)) {
						button.enabled = this.mc.isIntegratedServerRunning() || !this.master.getWorldSeed().isEmpty();
					}
				}
			}
		}

		this.worldSeedButton.textboxKeyTyped(character, keycode);
		super.keyTyped(character, keycode);
	}

	public void drawScreen(int par1, int par2, float par3) {
		super.drawMap();
		this.drawDefaultBackground();
		this.drawCenteredString(this.getFontRenderer(), this.screenTitle, this.getWidth() / 2, 20, 16777215);
		super.drawScreen(par1, par2, par3);
	}

	public void updateScreen() {
		this.worldSeedButton.updateCursorCounter();
	}
}
