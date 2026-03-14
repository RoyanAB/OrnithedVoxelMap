package com.mamiyaotaru.voxelmap.gui;

import com.mamiyaotaru.voxelmap.RadarSettingsManager;
import com.mamiyaotaru.voxelmap.gui.overridden.GuiScreenMinimap;
import com.mamiyaotaru.voxelmap.util.CustomMob;
import com.mamiyaotaru.voxelmap.util.CustomMobsManager;
import com.mamiyaotaru.voxelmap.util.EnumMobs;
import com.mamiyaotaru.voxelmap.util.I18nUtils;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;

public class GuiMobs extends GuiScreenMinimap {
    protected final RadarSettingsManager options;
    private final GuiScreen parentScreen;
    protected String screenTitle = "Select Mobs";
    protected GuiTextField filter;
    protected String selectedMobName = null;
    private GuiSlotMobs mobsList;
    private GuiButton buttonEnable;
    private GuiButton buttonDisable;
    private String tooltip = null;

    public GuiMobs(GuiScreen parentScreen, RadarSettingsManager options) {
        this.parentScreen = parentScreen;
        this.options = options;
    }

    static String setTooltip(GuiMobs par0GuiWaypoints, String par1Str) {
        return par0GuiWaypoints.tooltip = par1Str;
    }

    public void tick() {
        this.filter.tick();
    }

    public void initGui() {
        this.screenTitle = I18nUtils.getString("options.minimap.mobs.title");
        this.mc.keyboardListener.enableRepeatEvents(true);
        this.mobsList = new GuiSlotMobs(this);
        int filterStringWidth = this.getFontRenderer().getStringWidth(I18nUtils.getString("minimap.waypoints.filter") + ":");
        this.filter = new GuiTextField(
                2, this.getFontRenderer(), this.getWidth() / 2 - 153 + filterStringWidth + 5, this.getHeight() - 56, 305 - filterStringWidth - 5, 20
        );
        this.filter.setMaxStringLength(35);
        this.filter.setFocused(true);
        this.addButton(
                this.buttonEnable = new GuiButton(-1, this.getWidth() / 2 - 154, this.getHeight() - 28, 100, 20, I18nUtils.getString("options.minimap.mobs.enable")) {
                    public void onClick(double p_onClick_1_, double p_onClick_3_) {
                        GuiMobs.this.actionPerformed(this);
                    }
                }
        );
        this.addButton(
                this.buttonDisable = new GuiButton(-2, this.getWidth() / 2 - 50, this.getHeight() - 28, 100, 20, I18nUtils.getString("options.minimap.mobs.disable")) {
                    public void onClick(double p_onClick_1_, double p_onClick_3_) {
                        GuiMobs.this.actionPerformed(this);
                    }
                }
        );
        this.addButton(new GuiButton(-200, this.getWidth() / 2 + 4 + 50, this.getHeight() - 28, 100, 20, I18nUtils.getString("gui.done")) {
            public void onClick(double p_onClick_1_, double p_onClick_3_) {
                GuiMobs.this.actionPerformed(this);
            }
        });
        boolean isSomethingSelected = this.selectedMobName != null;
        this.buttonEnable.enabled = isSomethingSelected;
        this.buttonDisable.enabled = isSomethingSelected;
    }

    protected void actionPerformed(GuiButton par1GuiButton) {
        if (par1GuiButton.enabled) {
            if (par1GuiButton.id == -1) {
                this.setMobEnabled(this.selectedMobName, true);
            }

            if (par1GuiButton.id == -2) {
                this.setMobEnabled(this.selectedMobName, false);
            }

            if (par1GuiButton.id == -200) {
                this.getMinecraft().displayGuiScreen(this.parentScreen);
            }
        }
    }

    public boolean keyPressed(int keysm, int scancode, int b) {
        if (this.filter.keyPressed(keysm, scancode, b)) {
            this.mobsList.updateFilter(this.filter.getText().toLowerCase());
        }

        return super.keyPressed(keysm, scancode, b);
    }

    public boolean charTyped(char character, int keycode) {
        if (this.filter.charTyped(character, keycode)) {
            this.mobsList.updateFilter(this.filter.getText().toLowerCase());
        }

        return super.charTyped(character, keycode);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        this.filter.mouseClicked(mouseX, mouseY, mouseButton);
        this.mobsList.mouseClicked(mouseX, mouseY, mouseButton);
        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    public boolean mouseReleased(double mouseX, double mouseY, int mouseButton) {
        this.filter.mouseReleased(mouseX, mouseY, mouseButton);
        this.mobsList.mouseReleased(mouseX, mouseY, mouseButton);
        return super.mouseReleased(mouseX, mouseY, mouseButton);
    }

    public boolean mouseDragged(double mouseX, double mouseY, int mouseEvent, double deltaX, double deltaY) {
        return this.mobsList.mouseDragged(mouseX, mouseY, mouseEvent, deltaX, deltaY);
    }

    public boolean mouseScrolled(double amount) {
        return this.mobsList.mouseScrolled(amount);
    }

    protected void setSelectedMob(String mob) {
        this.selectedMobName = mob;
    }

    private boolean isMobEnabled(String selectedMobName2) {
        EnumMobs mob = EnumMobs.getMobByName(this.selectedMobName);
        if (mob != null) {
            return mob.enabled;
        }

        CustomMob customMob = CustomMobsManager.getCustomMobByType(this.selectedMobName);
        return customMob != null && customMob.enabled;
    }

    private void setMobEnabled(String selectedMobName, boolean enabled) {
        for (EnumMobs mob : EnumMobs.values()) {
            if (mob.id.equals(selectedMobName)) {
                mob.enabled = enabled;
            }
        }

        for (CustomMob mobx : CustomMobsManager.mobs) {
            if (mobx.id.equals(selectedMobName)) {
                mobx.enabled = enabled;
            }
        }
    }

    protected void toggleMobVisibility() {
        EnumMobs mob = EnumMobs.getMobByName(this.selectedMobName);
        if (mob != null) {
            this.setMobEnabled(this.selectedMobName, !mob.enabled);
        } else {
            CustomMob customMob = CustomMobsManager.getCustomMobByType(this.selectedMobName);
            if (customMob != null) {
                this.setMobEnabled(this.selectedMobName, !customMob.enabled);
            }
        }
    }

    public void render(int mouseX, int mouseY, float partialticks) {
        super.drawMap();
        this.tooltip = null;
        this.mobsList.drawScreen(mouseX, mouseY, partialticks);
        this.drawCenteredString(this.getFontRenderer(), this.screenTitle, this.getWidth() / 2, 20, 16777215);
        boolean isSomethingSelected = this.selectedMobName != null;
        this.buttonEnable.enabled = isSomethingSelected && !this.isMobEnabled(this.selectedMobName);
        this.buttonDisable.enabled = isSomethingSelected && this.isMobEnabled(this.selectedMobName);
        super.render(mouseX, mouseY, partialticks);
        this.drawString(this.getFontRenderer(), I18nUtils.getString("minimap.waypoints.filter") + ":", this.getWidth() / 2 - 153, this.getHeight() - 51, 10526880);
        this.filter.drawTextField(mouseX, mouseY, partialticks);
        if (this.tooltip != null) {
            this.drawTooltip(this.tooltip, mouseX, mouseY);
        }
    }

    protected void drawTooltip(String par1Str, int par2, int par3) {
        if (par1Str != null) {
            int var4 = par2 + 12;
            int var5 = par3 - 12;
            int var6 = this.getFontRenderer().getStringWidth(par1Str);
            this.drawGradientRect(var4 - 3, var5 - 3, var4 + var6 + 3, var5 + 8 + 3, -1073741824, -1073741824);
            this.getFontRenderer().drawStringWithShadow(par1Str, var4, var5, -1);
        }
    }

    @Override
    public void onGuiClosed() {
        this.mc.keyboardListener.enableRepeatEvents(false);
        super.onGuiClosed();
    }
}
