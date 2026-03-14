package com.mamiyaotaru.voxelmap.gui;

import com.mamiyaotaru.voxelmap.gui.overridden.GuiScreenMinimap;
import com.mamiyaotaru.voxelmap.interfaces.IVoxelMap;
import com.mamiyaotaru.voxelmap.interfaces.IWaypointManager;
import com.mamiyaotaru.voxelmap.util.I18nUtils;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;

import java.util.ArrayList;

public class GuiSubworldEdit extends GuiScreenMinimap implements BooleanConsumer {
    private final Screen parent;
    private final IWaypointManager waypointManager;
    private final ArrayList<String> knownSubworldNames;
    private String originalSubworldName = "";
    private String currentSubworldName = "";
    private TextFieldWidget subworldNameField;
    private ButtonWidget doneButton;
    private ButtonWidget deleteButton;
    private boolean deleteClicked = false;

    public GuiSubworldEdit(Screen parent, IVoxelMap master, String subworldName) {
        this.parent = parent;
        this.waypointManager = master.getWaypointManager();
        this.originalSubworldName = subworldName;
        this.knownSubworldNames = new ArrayList<>(this.waypointManager.getKnownSubworldNames());
    }

    public void tick() {
        this.subworldNameField.tick();
    }

    public void init() {
        this.getMinecraft().keyboard.enableRepeatEvents(true);
        this.getButtonList().clear();
        this.subworldNameField = new TextFieldWidget(this.getFontRenderer(), this.getWidth() / 2 - 100, this.getHeight() / 6 + 13, 200, 20, "");
        this.setFocused(this.subworldNameField);
        this.subworldNameField.method_1876(true);
        this.subworldNameField.setText(this.originalSubworldName);
        this.addButton(this.subworldNameField);
        this.addButton(
                this.doneButton = new ButtonWidget(this.getWidth() / 2 - 155, this.getHeight() / 6 + 168, 150, 20, I18nUtils.getString("gui.done"), null) {
                    public void onPress() {
                        GuiSubworldEdit.this.changeName();
                    }
                }
        );
        this.addButton(new ButtonWidget(this.getWidth() / 2 + 5, this.getHeight() / 6 + 168, 150, 20, I18nUtils.getString("gui.cancel"), null) {
            public void onPress() {
                GuiSubworldEdit.this.getMinecraft().openScreen(GuiSubworldEdit.this.parent);
            }
        });
        int buttonListY = this.getHeight() / 6 + 82 + 6;
        this.addButton(
                this.deleteButton = new ButtonWidget(this.getWidth() / 2 - 50, buttonListY + 24, 100, 20, I18nUtils.getString("selectServer.delete"), null) {
                    public void onPress() {
                        GuiSubworldEdit.this.delete();
                    }
                }
        );
        this.doneButton.active = this.isNameAcceptable();
        this.deleteButton.active = this.originalSubworldName.equals(this.subworldNameField.getText());
    }

    @Override
    public void removed() {
        this.getMinecraft().keyboard.enableRepeatEvents(false);
    }

    private void changeName() {
        if (!this.currentSubworldName.equals(this.originalSubworldName)) {
            this.waypointManager.changeSubworldName(this.originalSubworldName, this.currentSubworldName);
        }

        this.getMinecraft().openScreen(this.parent);
    }

    private void delete() {
        this.deleteClicked = true;
        String var4 = I18nUtils.getString("worldmap.subworld.deleteconfirm");
        TranslatableText explanation = new TranslatableText("selectServer.deleteWarning", this.originalSubworldName);
        String var6 = I18nUtils.getString("selectServer.deleteButton");
        String var7 = I18nUtils.getString("gui.cancel");
        ConfirmScreen var8 = new ConfirmScreen(this, new LiteralText(var4), explanation, var6, var7);
        this.getMinecraft().openScreen(var8);
    }

    public void accept(boolean par1) {
        if (this.deleteClicked) {
            this.deleteClicked = false;
            if (par1) {
                this.waypointManager.deleteSubworld(this.originalSubworldName);
            }

            this.getMinecraft().openScreen(this.parent);
        }
    }

    public boolean keyPressed(int keysm, int scancode, int b) {
        boolean OK = super.keyPressed(keysm, scancode, b);
        boolean acceptable = this.isNameAcceptable();
        this.doneButton.active = this.isNameAcceptable();
        this.deleteButton.active = this.originalSubworldName.equals(this.subworldNameField.getText());
        if ((keysm == 257 || keysm == 335) && acceptable) {
            this.changeName();
        }

        return OK;
    }

    public boolean charTyped(char character, int keycode) {
        boolean OK = super.charTyped(character, keycode);
        boolean acceptable = this.isNameAcceptable();
        this.doneButton.active = this.isNameAcceptable();
        this.deleteButton.active = this.originalSubworldName.equals(this.subworldNameField.getText());
        if (character == '\r' && acceptable) {
            this.changeName();
        }

        return OK;
    }

    public boolean mouseClicked(double mouseX, double mouseY, int par3) {
        this.subworldNameField.mouseClicked(mouseX, mouseY, par3);
        return super.mouseClicked(mouseX, mouseY, par3);
    }

    public void render(int mouseX, int mouseY, float partialTicks) {
        super.drawMap();
        this.renderBackground();
        this.drawCenteredString(this.getFontRenderer(), I18nUtils.getString("worldmap.subworld.edit"), this.getWidth() / 2, 20, 16777215);
        this.drawString(this.getFontRenderer(), I18nUtils.getString("worldmap.subworld.name"), this.getWidth() / 2 - 100, this.getHeight() / 6, 10526880);
        this.subworldNameField.render(mouseX, mouseY, partialTicks);
        super.render(mouseX, mouseY, partialTicks);
    }

    private boolean isNameAcceptable() {
        boolean acceptable = true;
        this.currentSubworldName = this.subworldNameField.getText();
        acceptable = acceptable && this.currentSubworldName.length() > 0;
        return acceptable && (this.currentSubworldName.equals(this.originalSubworldName) || !this.knownSubworldNames.contains(this.currentSubworldName));
    }
}
