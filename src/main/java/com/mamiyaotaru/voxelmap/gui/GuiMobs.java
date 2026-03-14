package com.mamiyaotaru.voxelmap.gui;

import com.mamiyaotaru.voxelmap.RadarSettingsManager;
import com.mamiyaotaru.voxelmap.gui.overridden.GuiScreenMinimap;
import com.mamiyaotaru.voxelmap.util.CustomMob;
import com.mamiyaotaru.voxelmap.util.CustomMobsManager;
import com.mamiyaotaru.voxelmap.util.EnumMobs;
import com.mamiyaotaru.voxelmap.util.I18nUtils;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;

public class GuiMobs extends GuiScreenMinimap {
    protected final RadarSettingsManager options;
    private final Screen parentScreen;
    protected String screenTitle = "Select Mobs";
    protected TextFieldWidget filter;
    protected String selectedMobId = null;
    private GuiSlotMobs mobsList;
    private ButtonWidget buttonEnable;
    private ButtonWidget buttonDisable;
    private String tooltip = null;

    public GuiMobs(Screen parentScreen, RadarSettingsManager options) {
        this.parentScreen = parentScreen;
        this.options = options;
    }

    static String setTooltip(GuiMobs par0GuiWaypoints, String par1Str) {
        return par0GuiWaypoints.tooltip = par1Str;
    }

    public void tick() {
        this.filter.tick();
    }

    public void init() {
        this.screenTitle = I18nUtils.getString("options.minimap.mobs.title");
        this.getMinecraft().keyboard.enableRepeatEvents(true);
        this.mobsList = new GuiSlotMobs(this);
        int filterStringWidth = this.getFontRenderer().getStringWidth(I18nUtils.getString("minimap.waypoints.filter") + ":");
        this.filter = new TextFieldWidget(
                this.getFontRenderer(), this.getWidth() / 2 - 153 + filterStringWidth + 5, this.getHeight() - 56, 305 - filterStringWidth - 5, 20, ""
        );
        this.filter.setMaxLength(35);
        this.addButton(this.filter);
        this.addButton(
                this.buttonEnable = new ButtonWidget(
                        this.getWidth() / 2 - 154, this.getHeight() - 28, 100, 20, I18nUtils.getString("options.minimap.mobs.enable"), null
                ) {
                    public void onPress() {
                        GuiMobs.this.actionPerformed(this, -1);
                    }
                }
        );
        this.addButton(
                this.buttonDisable = new ButtonWidget(
                        this.getWidth() / 2 - 50, this.getHeight() - 28, 100, 20, I18nUtils.getString("options.minimap.mobs.disable"), null
                ) {
                    public void onPress() {
                        GuiMobs.this.actionPerformed(this, -2);
                    }
                }
        );
        this.addButton(new ButtonWidget(this.getWidth() / 2 + 4 + 50, this.getHeight() - 28, 100, 20, I18nUtils.getString("gui.done"), null) {
            public void onPress() {
                GuiMobs.this.actionPerformed(this, -200);
            }
        });
        this.setFocused(this.filter);
        this.filter.method_1876(true);
        boolean isSomethingSelected = this.selectedMobId != null;
        this.buttonEnable.active = isSomethingSelected;
        this.buttonDisable.active = isSomethingSelected;
    }

    protected void actionPerformed(ButtonWidget par1GuiButton, int id) {
        if (par1GuiButton.active) {
            if (id == -1) {
                this.setMobEnabled(this.selectedMobId, true);
            }

            if (id == -2) {
                this.setMobEnabled(this.selectedMobId, false);
            }

            if (id == -200) {
                this.getMinecraft().openScreen(this.parentScreen);
            }
        }
    }

    public boolean keyPressed(int keysm, int scancode, int b) {
        boolean OK = super.keyPressed(keysm, scancode, b);
        if (this.filter.isFocused()) {
            this.mobsList.updateFilter(this.filter.getText().toLowerCase());
        }

        return OK;
    }

    public boolean charTyped(char character, int keycode) {
        boolean OK = super.charTyped(character, keycode);
        if (this.filter.isFocused()) {
            this.mobsList.updateFilter(this.filter.getText().toLowerCase());
        }

        return OK;
    }

    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        this.mobsList.mouseClicked(mouseX, mouseY, mouseButton);
        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    public boolean mouseReleased(double mouseX, double mouseY, int mouseButton) {
        this.mobsList.mouseReleased(mouseX, mouseY, mouseButton);
        return super.mouseReleased(mouseX, mouseY, mouseButton);
    }

    public boolean mouseDragged(double mouseX, double mouseY, int mouseEvent, double deltaX, double deltaY) {
        return this.mobsList.mouseDragged(mouseX, mouseY, mouseEvent, deltaX, deltaY);
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        return this.mobsList.mouseScrolled(mouseX, mouseY, amount);
    }

    protected void setSelectedMob(String id) {
        this.selectedMobId = id;
    }

    private boolean isMobEnabled(String mobId) {
        EnumMobs mob = EnumMobs.getMobByName(mobId);
        if (mob != null) {
            return mob.enabled;
        }

        CustomMob customMob = CustomMobsManager.getCustomMobByType(mobId);
        return customMob != null && customMob.enabled;
    }

    private void setMobEnabled(String mobId, boolean enabled) {
        for (EnumMobs mob : EnumMobs.values()) {
            if (mob.id.equals(mobId)) {
                mob.enabled = enabled;
            }
        }

        for (CustomMob mobx : CustomMobsManager.mobs) {
            if (mobx.id.equals(mobId)) {
                mobx.enabled = enabled;
            }
        }
    }

    protected void toggleMobVisibility() {
        EnumMobs mob = EnumMobs.getMobByName(this.selectedMobId);
        if (mob != null) {
            this.setMobEnabled(this.selectedMobId, !mob.enabled);
        } else {
            CustomMob customMob = CustomMobsManager.getCustomMobByType(this.selectedMobId);
            if (customMob != null) {
                this.setMobEnabled(this.selectedMobId, !customMob.enabled);
            }
        }
    }

    public void render(int mouseX, int mouseY, float partialticks) {
        super.drawMap();
        this.tooltip = null;
        this.mobsList.render(mouseX, mouseY, partialticks);
        this.drawCenteredString(this.getFontRenderer(), this.screenTitle, this.getWidth() / 2, 20, 16777215);
        boolean isSomethingSelected = this.selectedMobId != null;
        this.buttonEnable.active = isSomethingSelected && !this.isMobEnabled(this.selectedMobId);
        this.buttonDisable.active = isSomethingSelected && this.isMobEnabled(this.selectedMobId);
        super.render(mouseX, mouseY, partialticks);
        this.drawString(this.getFontRenderer(), I18nUtils.getString("minimap.waypoints.filter") + ":", this.getWidth() / 2 - 153, this.getHeight() - 51, 10526880);
        this.filter.render(mouseX, mouseY, partialticks);
        if (this.tooltip != null) {
            this.renderTooltip(this.tooltip, mouseX, mouseY);
        }
    }

    public void renderTooltip(String par1Str, int par2, int par3) {
        if (par1Str != null) {
            int var4 = par2 + 12;
            int var5 = par3 - 12;
            int var6 = this.getFontRenderer().getStringWidth(par1Str);
            this.fillGradient(var4 - 3, var5 - 3, var4 + var6 + 3, var5 + 8 + 3, -1073741824, -1073741824);
            this.getFontRenderer().drawWithShadow(par1Str, var4, var5, -1);
        }
    }

    @Override
    public void removed() {
        this.minecraft.keyboard.enableRepeatEvents(false);
        super.removed();
    }
}
