package com.mamiyaotaru.voxelmap.gui;

import com.mamiyaotaru.voxelmap.gui.overridden.GuiScreenMinimap;
import com.mamiyaotaru.voxelmap.interfaces.IVoxelMap;
import com.mamiyaotaru.voxelmap.interfaces.IWaypointManager;
import com.mamiyaotaru.voxelmap.util.I18nUtils;
import net.minecraft.client.gui.*;

import java.util.ArrayList;

public class GuiSubworldEdit extends GuiScreenMinimap implements GuiYesNoCallback {
    private final GuiScreen parent;
    private final IWaypointManager waypointManager;
    private final ArrayList<String> knownSubworldNames;
    private String originalSubworldName = "";
    private String currentSubworldName = "";
    private GuiTextField subworldNameField;
    private boolean deleteClicked = false;

    public GuiSubworldEdit(GuiScreen parent, IVoxelMap master, String subworldName) {
        this.parent = parent;
        this.waypointManager = master.getWaypointManager();
        this.originalSubworldName = subworldName;
        this.knownSubworldNames = new ArrayList<>(this.waypointManager.getKnownSubworldNames());
    }

    public void tick() {
        this.subworldNameField.tick();
    }

    public void initGui() {
        this.mc.keyboardListener.enableRepeatEvents(true);
        this.getButtonList().clear();
        this.addButton(new GuiButton(0, this.getWidth() / 2 - 155, this.getHeight() / 6 + 168, 150, 20, I18nUtils.getString("gui.done")) {
            public void onClick(double p_onClick_1_, double p_onClick_3_) {
                GuiSubworldEdit.this.actionPerformed(this);
            }
        });
        this.addButton(new GuiButton(1, this.getWidth() / 2 + 5, this.getHeight() / 6 + 168, 150, 20, I18nUtils.getString("gui.cancel")) {
            public void onClick(double p_onClick_1_, double p_onClick_3_) {
                GuiSubworldEdit.this.actionPerformed(this);
            }
        });
        this.subworldNameField = new GuiTextField(2, this.getFontRenderer(), this.getWidth() / 2 - 100, this.getHeight() / 6 + 13, 200, 20);
        this.subworldNameField.setFocused(true);
        this.subworldNameField.setText(this.originalSubworldName);
        int buttonListY = this.getHeight() / 6 + 82 + 6;
        this.addButton(new GuiButton(7, this.getWidth() / 2 - 50, buttonListY + 24, 100, 20, I18nUtils.getString("selectServer.delete")) {
            public void onClick(double p_onClick_1_, double p_onClick_3_) {
                GuiSubworldEdit.this.actionPerformed(this);
            }
        });
        this.getButtonList().get(0).enabled = this.isNameAcceptable();
        this.getButtonList().get(2).enabled = this.originalSubworldName.equals(this.subworldNameField.getText());
    }

    @Override
    public void onGuiClosed() {
        this.mc.keyboardListener.enableRepeatEvents(false);
    }

    protected void actionPerformed(GuiButton par1GuiButton) {
        if (par1GuiButton.enabled) {
            if (par1GuiButton.id == 0) {
                if (!this.currentSubworldName.equals(this.originalSubworldName)) {
                    this.waypointManager.changeSubworldName(this.originalSubworldName, this.currentSubworldName);
                }

                this.getMinecraft().displayGuiScreen(this.parent);
            } else if (par1GuiButton.id == 1) {
                this.getMinecraft().displayGuiScreen(this.parent);
            } else if (par1GuiButton.id == 7) {
                this.deleteClicked = true;
                String var4 = I18nUtils.getString("worldmap.subworld.deleteconfirm");
                String var5 = "'" + this.originalSubworldName + "' " + I18nUtils.getString("selectServer.deleteWarning");
                String var6 = I18nUtils.getString("selectServer.deleteButton");
                String var7 = I18nUtils.getString("gui.cancel");
                GuiYesNo var8 = new GuiYesNo(this, var4, var5, var6, var7, 0);
                this.getMinecraft().displayGuiScreen(var8);
            }
        }
    }

    public void confirmResult(boolean par1, int par2) {
        if (this.deleteClicked) {
            this.deleteClicked = false;
            if (par1) {
                this.waypointManager.deleteSubworld(this.originalSubworldName);
            }

            this.getMinecraft().displayGuiScreen(this.parent);
        }
    }

    public boolean charTyped(char character, int keycode) {
        this.subworldNameField.charTyped(character, keycode);
        boolean acceptable = this.isNameAcceptable();
        if (character == '\r' && acceptable) {
            this.actionPerformed(this.getButtonList().get(0));
        }

        this.getButtonList().get(0).enabled = acceptable;
        this.getButtonList().get(2).enabled = this.originalSubworldName.equals(this.subworldNameField.getText());
        return super.charTyped(character, keycode);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int par3) {
        this.subworldNameField.mouseClicked(mouseX, mouseY, par3);
        return super.mouseClicked(mouseX, mouseY, par3);
    }

    public void render(int mouseX, int mouseY, float partialTicks) {
        super.drawMap();
        this.drawDefaultBackground();
        this.drawCenteredString(this.getFontRenderer(), I18nUtils.getString("worldmap.subworld.edit"), this.getWidth() / 2, 20, 16777215);
        this.drawString(this.getFontRenderer(), I18nUtils.getString("worldmap.subworld.name"), this.getWidth() / 2 - 100, this.getHeight() / 6, 10526880);
        this.subworldNameField.drawTextField(mouseX, mouseY, partialTicks);
        super.render(mouseX, mouseY, partialTicks);
    }

    private boolean isNameAcceptable() {
        boolean acceptable = true;
        this.currentSubworldName = this.subworldNameField.getText();
        acceptable = acceptable && this.currentSubworldName.length() > 0;
        return acceptable && (this.currentSubworldName.equals(this.originalSubworldName) || !this.knownSubworldNames.contains(this.currentSubworldName));
    }
}
