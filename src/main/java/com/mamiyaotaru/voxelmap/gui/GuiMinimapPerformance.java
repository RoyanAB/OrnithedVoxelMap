package com.mamiyaotaru.voxelmap.gui;

import com.mamiyaotaru.voxelmap.MapSettingsManager;
import com.mamiyaotaru.voxelmap.gui.overridden.EnumOptionsMinimap;
import com.mamiyaotaru.voxelmap.gui.overridden.GuiButtonText;
import com.mamiyaotaru.voxelmap.gui.overridden.GuiOptionButtonMinimap;
import com.mamiyaotaru.voxelmap.gui.overridden.GuiScreenMinimap;
import com.mamiyaotaru.voxelmap.interfaces.IVoxelMap;
import com.mamiyaotaru.voxelmap.util.I18nUtils;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;

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
    private final Screen parentScreen;
    private final MapSettingsManager options;

    public GuiMinimapPerformance(Screen par1GuiScreen, IVoxelMap master) {
        this.parentScreen = par1GuiScreen;
        this.options = master.getMapOptions();
        this.master = master;
    }

    private int getLeftBorder() {
        return this.getWidth() / 2 - 155;
    }

    public void init() {
        this.screenTitle = I18nUtils.getString("options.minimap.detailsperformance");
        this.getMinecraft().keyboard.enableRepeatEvents(true);
        int leftBorder = this.getLeftBorder();
        int var2 = 0;

        for (int t = 0; t < relevantOptions.length; t++) {
            final EnumOptionsMinimap option = relevantOptions[t];
            String text = this.options.getKeyText(option);
            if ((option == EnumOptionsMinimap.WATERTRANSPARENCY || option == EnumOptionsMinimap.BLOCKTRANSPARENCY || option == EnumOptionsMinimap.BIOMES)
                    && !this.options.multicore
                    && this.options.getOptionBooleanValue(option)) {
                text = "§c" + text;
            }

            GuiOptionButtonMinimap var7 = new GuiOptionButtonMinimap(leftBorder + var2 % 2 * 160, this.getHeight() / 6 + 24 * (var2 >> 1), option, text, null) {
                public void onPress() {
                    GuiMinimapPerformance.this.actionPerformed(this, option.returnEnumOrdinal());
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
                this.getFontRenderer(), leftBorder + var2 % 2 * 160, this.getHeight() / 6 + 24 * (var2 >> 1), 150, 20, buttonText, null
        ) {
            public void onPress() {
                GuiMinimapPerformance.this.actionPerformed(this, GuiMinimapPerformance.this.worldSeedButtonID);
            }
        };
        this.worldSeedButton.setText(this.master.getWorldSeed());
        this.addButton(this.worldSeedButton);
        var2++;

        for (Object buttonObj : this.getButtonList()) {
            if (buttonObj instanceof GuiOptionButtonMinimap) {
                GuiOptionButtonMinimap button = (GuiOptionButtonMinimap) buttonObj;
                if (button.returnEnumOptions().equals(EnumOptionsMinimap.SLIMECHUNKS)) {
                    button.active = this.getMinecraft().isIntegratedServerRunning() || !this.master.getWorldSeed().equals("");
                }
            }
        }

        this.worldSeedButton.active = !this.getMinecraft().isIntegratedServerRunning();
        this.addButton(new ButtonWidget(this.getWidth() / 2 - 100, this.getHeight() / 6 + 168, 200, 20, I18nUtils.getString("gui.done"), null) {
            public void onPress() {
                GuiMinimapPerformance.this.actionPerformed(this, 200);
            }
        });
    }

    @Override
    public void removed() {
        this.getMinecraft().keyboard.enableRepeatEvents(false);
    }

    protected void actionPerformed(ButtonWidget par1GuiButton, int id) {
        if (id < 100 && par1GuiButton instanceof GuiOptionButtonMinimap) {
            this.options.setOptionValue(((GuiOptionButtonMinimap) par1GuiButton).returnEnumOptions(), 1);
            String perfBomb = "";
            if ((
                    id == EnumOptionsMinimap.WATERTRANSPARENCY.ordinal()
                            || id == EnumOptionsMinimap.BLOCKTRANSPARENCY.ordinal()
                            || id == EnumOptionsMinimap.BIOMES.ordinal()
            )
                    && !this.options.multicore
                    && this.options.getOptionBooleanValue(EnumOptionsMinimap.getEnumOptions(id))) {
                perfBomb = "§c";
            }

            par1GuiButton.setMessage(perfBomb + this.options.getKeyText(EnumOptionsMinimap.getEnumOptions(id)));
        }

        for (Object buttonObj : this.getButtonList()) {
            if (buttonObj instanceof GuiOptionButtonMinimap) {
                GuiOptionButtonMinimap button = (GuiOptionButtonMinimap) buttonObj;
                if (button.returnEnumOptions().equals(EnumOptionsMinimap.SLIMECHUNKS)) {
                    button.active = this.getMinecraft().isIntegratedServerRunning() || !this.master.getWorldSeed().equals("");
                }
            }
        }

        this.worldSeedButton.active = !this.getMinecraft().isIntegratedServerRunning();
        if (id == this.worldSeedButtonID) {
            this.worldSeedButton.setEditing(true);
        }

        if (id == 200) {
            this.getMinecraft().openScreen(this.parentScreen);
        }
    }

    public boolean keyPressed(int keysm, int scancode, int b) {
        if (keysm == 258) {
            this.worldSeedButton.keyPressed(keysm, scancode, b);
        }

        if ((keysm == 257 || keysm == 335) && this.worldSeedButton.isEditing()) {
            this.newSeed();
        }

        return super.keyPressed(keysm, scancode, b);
    }

    public boolean charTyped(char character, int keycode) {
        boolean OK = super.charTyped(character, keycode);
        if (character == '\r' && this.worldSeedButton.isEditing()) {
            this.newSeed();
        }

        return OK;
    }

    private void newSeed() {
        String newSeed = this.worldSeedButton.getText();
        this.master.setWorldSeed(newSeed);
        String worldSeedDisplay = this.master.getWorldSeed();
        if (worldSeedDisplay.equals("")) {
            worldSeedDisplay = I18nUtils.getString("selectWorld.versionUnknown");
        }

        String buttonText = I18nUtils.getString("options.minimap.worldseed") + ": " + worldSeedDisplay;
        this.worldSeedButton.setMessage(buttonText);
        this.worldSeedButton.setText(this.master.getWorldSeed());
        this.master.getMap().forceFullRender(true);

        for (Object buttonObj : this.getButtonList()) {
            if (buttonObj instanceof GuiOptionButtonMinimap) {
                GuiOptionButtonMinimap button = (GuiOptionButtonMinimap) buttonObj;
                if (button.returnEnumOptions().equals(EnumOptionsMinimap.SLIMECHUNKS)) {
                    button.active = this.getMinecraft().isIntegratedServerRunning() || !this.master.getWorldSeed().equals("");
                }
            }
        }
    }

    public void render(int mouseX, int mouseY, float partialTicks) {
        super.drawMap();
        this.renderBackground();
        this.drawCenteredString(this.getFontRenderer(), this.screenTitle, this.getWidth() / 2, 20, 16777215);
        super.render(mouseX, mouseY, partialTicks);
    }

    public void tick() {
        this.worldSeedButton.tick();
    }
}
