package com.mamiyaotaru.voxelmap.gui;

import com.mamiyaotaru.voxelmap.MapSettingsManager;
import com.mamiyaotaru.voxelmap.gui.overridden.GuiScreenMinimap;
import com.mamiyaotaru.voxelmap.interfaces.IVoxelMap;
import com.mamiyaotaru.voxelmap.util.I18nUtils;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.InputUtil.Type;

public class GuiMinimapControls extends GuiScreenMinimap {
    public KeyBinding buttonId = null;
    protected String screenTitle = "Controls";
    private final Screen parentScreen;
    private final MapSettingsManager options;

    public GuiMinimapControls(Screen par1GuiScreen, IVoxelMap master) {
        this.parentScreen = par1GuiScreen;
        this.options = master.getMapOptions();
    }

    private int getLeftBorder() {
        return this.getWidth() / 2 - 155;
    }

    public void init() {
        int var2 = this.getLeftBorder();

        for (int var3 = 0; var3 < this.options.keyBindings.length; var3++) {
            final int id = var3;
            this.addButton(
                    new ButtonWidget(var2 + var3 % 2 * 160, this.getHeight() / 6 + 24 * (var3 >> 1), 70, 20, this.options.getKeybindDisplayString(var3), null) {
                        public void onPress() {
                            GuiMinimapControls.this.actionPerformed(this, id);
                        }
                    }
            );
        }

        this.addButton(new ButtonWidget(this.getWidth() / 2 - 100, this.getHeight() / 6 + 168, 200, 20, I18nUtils.getString("gui.done"), null) {
            public void onPress() {
                GuiMinimapControls.this.actionPerformed(this, 200);
            }
        });
        this.screenTitle = I18nUtils.getString("controls.minimap.title");
    }

    protected void actionPerformed(ButtonWidget par1GuiButton, int id) {
        for (int buttonListIndex = 0; buttonListIndex < this.options.keyBindings.length; buttonListIndex++) {
            this.getButtonList().get(buttonListIndex).setMessage(this.options.getKeybindDisplayString(buttonListIndex));
        }

        if (id == 200) {
            this.getMinecraft().openScreen(this.parentScreen);
        } else {
            this.buttonId = this.options.keyBindings[id];
            par1GuiButton.setMessage("> " + this.options.getKeybindDisplayString(id) + " <");
        }
    }

    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        if (this.buttonId != null) {
            this.options.setKeyBinding(this.buttonId, Type.MOUSE.createFromCode(mouseButton));
            this.buttonId = null;
            KeyBinding.updateKeysByCode();
            return true;
        } else {
            return super.mouseClicked(mouseX, mouseY, mouseButton);
        }
    }

    public boolean keyPressed(int keysm, int scancode, int b) {
        if (this.buttonId != null) {
            if (keysm == 256) {
                this.options.setKeyBinding(this.buttonId, InputUtil.UNKNOWN_KEYCODE);
            } else {
                this.options.setKeyBinding(this.buttonId, InputUtil.getKeyCode(keysm, scancode));
            }

            this.buttonId = null;
            KeyBinding.updateKeysByCode();
            return true;
        } else {
            return super.keyPressed(keysm, scancode, b);
        }
    }

    public void render(int mouseX, int mouseY, float partialTicks) {
        super.drawMap();
        this.renderBackground();
        this.drawCenteredString(this.getFontRenderer(), this.screenTitle, this.getWidth() / 2, 20, 16777215);
        int leftBorder = this.getLeftBorder();

        for (int keyCounter = 0; keyCounter < this.options.keyBindings.length; keyCounter++) {
            boolean keycodeCollision = false;
            KeyBinding keyBinding = this.options.keyBindings[keyCounter];

            for (int compareKeyCounter = 0; compareKeyCounter < this.options.game.options.keysAll.length; compareKeyCounter++) {
                if (compareKeyCounter < this.options.keyBindings.length) {
                    KeyBinding compareBinding = this.options.keyBindings[compareKeyCounter];
                    if (keyBinding != compareBinding && keyBinding.equals(compareBinding)) {
                        keycodeCollision = true;
                        break;
                    }
                }

                if (compareKeyCounter < this.options.game.options.keysAll.length) {
                    KeyBinding compareBinding = this.options.game.options.keysAll[compareKeyCounter];
                    if (keyBinding != compareBinding && keyBinding.equals(compareBinding)) {
                        keycodeCollision = true;
                        break;
                    }
                }
            }

            if (this.buttonId == this.options.keyBindings[keyCounter]) {
                this.getButtonList().get(keyCounter).setMessage("§f> §e??? §f<");
            } else if (keycodeCollision) {
                this.getButtonList().get(keyCounter).setMessage("§c" + this.options.getKeybindDisplayString(keyCounter));
            } else {
                this.getButtonList().get(keyCounter).setMessage(this.options.getKeybindDisplayString(keyCounter));
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
