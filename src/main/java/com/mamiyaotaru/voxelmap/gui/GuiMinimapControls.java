package com.mamiyaotaru.voxelmap.gui;

import com.mamiyaotaru.voxelmap.MapSettingsManager;
import com.mamiyaotaru.voxelmap.gui.overridden.GuiScreenMinimap;
import com.mamiyaotaru.voxelmap.interfaces.IVoxelMap;
import com.mamiyaotaru.voxelmap.util.I18nUtils;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.client.util.InputMappings.Type;

public class GuiMinimapControls extends GuiScreenMinimap {
    public KeyBinding buttonId = null;
    protected String screenTitle = "Controls";
    private final GuiScreen parentScreen;
    private final MapSettingsManager options;

    public GuiMinimapControls(GuiScreen par1GuiScreen, IVoxelMap master) {
        this.parentScreen = par1GuiScreen;
        this.options = master.getMapOptions();
    }

    private int getLeftBorder() {
        return this.getWidth() / 2 - 155;
    }

    public void initGui() {
        int var2 = this.getLeftBorder();

        for (int var3 = 0; var3 < this.options.keyBindings.length; var3++) {
            this.addButton(
                    new GuiButton(var3, var2 + var3 % 2 * 160, this.getHeight() / 6 + 24 * (var3 >> 1), 70, 20, this.options.getKeybindDisplayString(var3)) {
                        public void onClick(double p_onClick_1_, double p_onClick_3_) {
                            GuiMinimapControls.this.actionPerformed(this);
                        }
                    }
            );
        }

        this.addButton(new GuiButton(200, this.getWidth() / 2 - 100, this.getHeight() / 6 + 168, I18nUtils.getString("gui.done")) {
            public void onClick(double p_onClick_1_, double p_onClick_3_) {
                GuiMinimapControls.this.actionPerformed(this);
            }
        });
        this.screenTitle = I18nUtils.getString("controls.minimap.title");
    }

    protected void actionPerformed(GuiButton par1GuiButton) {
        for (int buttonListIndex = 0; buttonListIndex < this.options.keyBindings.length; buttonListIndex++) {
            this.getButtonList().get(buttonListIndex).displayString = this.options.getKeybindDisplayString(buttonListIndex);
        }

        if (par1GuiButton.id == 200) {
            this.getMinecraft().displayGuiScreen(this.parentScreen);
        } else {
            this.buttonId = this.options.keyBindings[par1GuiButton.id];
            par1GuiButton.displayString = "> " + this.options.getKeybindDisplayString(par1GuiButton.id) + " <";
        }
    }

    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        if (this.buttonId != null) {
            this.options.setKeyBinding(this.buttonId, Type.MOUSE.getOrMakeInput(mouseButton));
            this.buttonId = null;
            KeyBinding.resetKeyBindingArrayAndHash();
            return true;
        } else {
            return super.mouseClicked(mouseX, mouseY, mouseButton);
        }
    }

    public boolean keyPressed(int keysm, int scancode, int b) {
        if (this.buttonId != null) {
            if (keysm == 256) {
                this.options.setKeyBinding(this.buttonId, InputMappings.INPUT_INVALID);
            } else {
                this.options.setKeyBinding(this.buttonId, InputMappings.getInputByCode(keysm, scancode));
            }

            this.buttonId = null;
            KeyBinding.resetKeyBindingArrayAndHash();
            return true;
        } else {
            return super.keyPressed(keysm, scancode, b);
        }
    }

    public void render(int mouseX, int mouseY, float partialTicks) {
        super.drawMap();
        this.drawDefaultBackground();
        this.drawCenteredString(this.getFontRenderer(), this.screenTitle, this.getWidth() / 2, 20, 16777215);
        int leftBorder = this.getLeftBorder();

        for (int keyCounter = 0; keyCounter < this.options.keyBindings.length; keyCounter++) {
            boolean keycodeCollision = false;
            KeyBinding keyBinding = this.options.keyBindings[keyCounter];

            for (int compareKeyCounter = 0; compareKeyCounter < this.options.game.gameSettings.keyBindings.length; compareKeyCounter++) {
                if (compareKeyCounter < this.options.keyBindings.length) {
                    KeyBinding compareBinding = this.options.keyBindings[compareKeyCounter];
                    if (keyBinding != compareBinding && keyBinding.func_197983_b(compareBinding)) {
                        keycodeCollision = true;
                        break;
                    }
                }

                if (compareKeyCounter < this.options.game.gameSettings.keyBindings.length) {
                    KeyBinding compareBinding = this.options.game.gameSettings.keyBindings[compareKeyCounter];
                    if (keyBinding != compareBinding && keyBinding.func_197983_b(compareBinding)) {
                        keycodeCollision = true;
                        break;
                    }
                }
            }

            if (this.buttonId == this.options.keyBindings[keyCounter]) {
                this.getButtonList().get(keyCounter).displayString = "§f> §e??? §f<";
            } else if (keycodeCollision) {
                this.getButtonList().get(keyCounter).displayString = "§c" + this.options.getKeybindDisplayString(keyCounter);
            } else {
                this.getButtonList().get(keyCounter).displayString = this.options.getKeybindDisplayString(keyCounter);
            }

            this.drawString(
                    this.getFontRenderer(),
                    this.options.getKeyBindingDescription(keyCounter),
                    leftBorder + keyCounter % 2 * 160 + 70 + 6,
                    this.getHeight() / 6 + 24 * (keyCounter >> 1) + 7,
                    -1
            );
        }

        this.drawCenteredString(
                this.getFontRenderer(), I18nUtils.getString("controls.minimap.unbind1"), this.getWidth() / 2, this.getHeight() / 6 + 115, 16777215
        );
        this.drawCenteredString(
                this.getFontRenderer(), I18nUtils.getString("controls.minimap.unbind2"), this.getWidth() / 2, this.getHeight() / 6 + 129, 16777215
        );
        super.render(mouseX, mouseY, partialTicks);
    }
}
