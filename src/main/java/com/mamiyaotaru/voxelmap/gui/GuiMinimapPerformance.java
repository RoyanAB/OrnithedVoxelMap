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
        this.mc.keyboardListener.enableRepeatEvents(true);
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
            ) {
                public void onClick(double p_onClick_1_, double p_onClick_3_) {
                    GuiMinimapPerformance.this.actionPerformed(this);
                }
            };
            this.addButton(var7);
            var2++;
        }

        String worldSeedDisplay = this.master.getWorldSeed();
        if (worldSeedDisplay.equals("")) {
            worldSeedDisplay = I18nUtils.getString("selectWorld.versionUnknown");
        }

        String buttonText = I18nUtils.getString("options.minimap.worldseed") + ": " + worldSeedDisplay;
        this.worldSeedButton = new GuiButtonText(
                this.worldSeedButtonID, this.getFontRenderer(), leftBorder + var2 % 2 * 160, this.getHeight() / 6 + 24 * (var2 >> 1), 150, 20, buttonText
        ) {
            public void onClick(double p_onClick_1_, double p_onClick_3_) {
                GuiMinimapPerformance.this.actionPerformed(this);
            }
        };
        this.worldSeedButton.setText(this.master.getWorldSeed());
        this.addButton(this.worldSeedButton);
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
        this.addButton(new GuiButton(200, this.getWidth() / 2 - 100, this.getHeight() / 6 + 168, I18nUtils.getString("gui.done")) {
            public void onClick(double p_onClick_1_, double p_onClick_3_) {
                GuiMinimapPerformance.this.actionPerformed(this);
            }
        });
    }

    @Override
    public void onGuiClosed() {
        this.mc.keyboardListener.enableRepeatEvents(false);
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
                    && this.options.getOptionBooleanValue(EnumOptionsMinimap.getEnumOptions(par1GuiButton.id))) {
                perfBomb = "§c";
            }

            par1GuiButton.displayString = perfBomb + this.options.getKeyText(EnumOptionsMinimap.getEnumOptions(par1GuiButton.id));
        }

        for (Object buttonObj : this.getButtonList()) {
            if (buttonObj instanceof GuiOptionButtonMinimap) {
                GuiOptionButtonMinimap button = (GuiOptionButtonMinimap) buttonObj;
                if (button.returnEnumOptions().equals(EnumOptionsMinimap.SLIMECHUNKS)) {
                    button.enabled = this.mc.isIntegratedServerRunning() || !this.master.getWorldSeed().equals("");
                }
            }
        }

        this.worldSeedButton.enabled = !this.mc.isIntegratedServerRunning();
        if (par1GuiButton.id == 200) {
            this.getMinecraft().displayGuiScreen(this.parentScreen);
        }
    }

    public boolean charTyped(char character, int keycode) {
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

        this.worldSeedButton.charTyped(character, keycode);
        return super.charTyped(character, keycode);
    }

    public void render(int mouseX, int mouseY, float partialTicks) {
        super.drawMap();
        this.drawDefaultBackground();
        this.drawCenteredString(this.getFontRenderer(), this.screenTitle, this.getWidth() / 2, 20, 16777215);
        super.render(mouseX, mouseY, partialTicks);
    }

    public void tick() {
        this.worldSeedButton.tick();
    }
}
