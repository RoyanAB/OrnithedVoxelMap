package com.mamiyaotaru.voxelmap.gui;

import com.mamiyaotaru.voxelmap.gui.overridden.GuiScreenMinimap;
import com.mamiyaotaru.voxelmap.interfaces.IVoxelMap;
import com.mamiyaotaru.voxelmap.util.I18nUtils;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;

public class GuiSelectPlayer extends GuiScreenMinimap implements BooleanConsumer {
    private final Screen parentScreen;
    private final int maxMessageLength = 78;
    protected String screenTitle = "Players";
    protected boolean allClicked = false;
    protected TextFieldWidget message;
    protected TextFieldWidget filter;
    private boolean sharingWaypoint = true;
    private GuiButtonRowListPlayers playerList;
    private String tooltip = null;
    private final String locInfo;

    public GuiSelectPlayer(Screen parentScreen, IVoxelMap master, String locInfo, boolean sharingWaypoint) {
        this.parentScreen = parentScreen;
        this.locInfo = locInfo;
        this.sharingWaypoint = sharingWaypoint;
    }

    static String setTooltip(GuiSelectPlayer par0GuiWaypoints, String par1Str) {
        return par0GuiWaypoints.tooltip = par1Str;
    }

    public void tick() {
        this.message.tick();
        this.filter.tick();
    }

    public void init() {
        this.screenTitle = I18nUtils.getString(this.sharingWaypoint ? "minimap.waypointshare.title" : "minimap.waypointshare.titlecoordinate");
        this.getMinecraft().keyboard.enableRepeatEvents(true);
        this.playerList = new GuiButtonRowListPlayers(this);
        int messageStringWidth = this.getFontRenderer().getStringWidth(I18nUtils.getString("minimap.waypointshare.sharemessage") + ":");
        this.message = new TextFieldWidget(this.getFontRenderer(), this.getWidth() / 2 - 153 + messageStringWidth + 5, 34, 305 - messageStringWidth - 5, 20, "");
        this.message.setMaxLength(78);
        this.addButton(this.message);
        int filterStringWidth = this.getFontRenderer().getStringWidth(I18nUtils.getString("minimap.waypoints.filter") + ":");
        this.filter = new TextFieldWidget(
                this.getFontRenderer(), this.getWidth() / 2 - 153 + filterStringWidth + 5, this.getHeight() - 55, 305 - filterStringWidth - 5, 20, ""
        );
        this.filter.setMaxLength(35);
        this.addButton(this.filter);
        this.addButton(new ButtonWidget(this.width / 2 - 100, this.height - 27, 150, 20, I18nUtils.getString("gui.cancel"), null) {
            public void onPress() {
                GuiSelectPlayer.this.actionPerformed(this, -200);
            }
        });
        this.setFocused(this.filter);
        this.filter.method_1876(true);
    }

    protected void actionPerformed(ButtonWidget par1GuiButton, int id) {
        if (par1GuiButton.active && id == -200) {
            this.getMinecraft().openScreen(this.parentScreen);
        }
    }

    public boolean keyPressed(int keysm, int scancode, int b) {
        boolean OK = super.keyPressed(keysm, scancode, b);
        if (this.filter.isFocused()) {
            this.playerList.updateFilter(this.filter.getText().toLowerCase());
        }

        return OK;
    }

    public boolean charTyped(char character, int keycode) {
        boolean OK = super.charTyped(character, keycode);
        if (this.filter.isFocused()) {
            this.playerList.updateFilter(this.filter.getText().toLowerCase());
        }

        return OK;
    }

    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        this.playerList.mouseClicked(mouseX, mouseY, mouseButton);
        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    public boolean mouseReleased(double mouseX, double mouseY, int mouseButton) {
        this.playerList.mouseReleased(mouseX, mouseY, mouseButton);
        return super.mouseReleased(mouseX, mouseY, mouseButton);
    }

    public boolean mouseDragged(double mouseX, double mouseY, int mouseEvent, double deltaX, double deltaY) {
        return this.playerList.mouseDragged(mouseX, mouseY, mouseEvent, deltaX, deltaY);
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        return this.playerList.mouseScrolled(mouseX, mouseY, amount);
    }

    public void accept(boolean par1) {
        if (this.allClicked) {
            this.allClicked = false;
            if (par1) {
                String combined = this.message.getText() + " " + this.locInfo;
                if (combined.length() > 100) {
                    this.minecraft.player.sendChatMessage(this.message.getText());
                    this.minecraft.player.sendChatMessage(this.locInfo);
                } else {
                    this.minecraft.player.sendChatMessage(combined);
                }

                this.getMinecraft().openScreen(this.parentScreen);
            } else {
                this.getMinecraft().openScreen(this);
            }
        }
    }

    protected void sendMessageToPlayer(String name) {
        String combined = "/msg " + name + " " + this.message.getText() + " " + this.locInfo;
        if (combined.length() > 100) {
            this.getMinecraft().player.sendChatMessage("/msg " + name + " " + this.message.getText());
            this.getMinecraft().player.sendChatMessage("/msg " + name + " " + this.locInfo);
        } else {
            this.getMinecraft().player.sendChatMessage(combined);
        }

        this.getMinecraft().openScreen(this.parentScreen);
    }

    public void render(int mouseX, int mouseY, float partialTicks) {
        super.drawMap();
        this.tooltip = null;
        this.playerList.render(mouseX, mouseY, partialTicks);
        this.drawCenteredString(this.getFontRenderer(), this.screenTitle, this.getWidth() / 2, 20, 16777215);
        super.render(mouseX, mouseY, partialTicks);
        this.drawString(this.getFontRenderer(), I18nUtils.getString("minimap.waypointshare.sharemessage") + ":", this.getWidth() / 2 - 153, 39, 10526880);
        this.message.render(mouseX, mouseY, partialTicks);
        this.drawCenteredString(this.getFontRenderer(), I18nUtils.getString("minimap.waypointshare.sharewith"), this.getWidth() / 2, 75, 16777215);
        this.drawString(this.getFontRenderer(), I18nUtils.getString("minimap.waypoints.filter") + ":", this.getWidth() / 2 - 153, this.getHeight() - 50, 10526880);
        this.filter.render(mouseX, mouseY, partialTicks);
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
        this.getMinecraft().keyboard.enableRepeatEvents(false);
    }
}
