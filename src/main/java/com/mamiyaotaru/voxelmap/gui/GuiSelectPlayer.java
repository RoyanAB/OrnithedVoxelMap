package com.mamiyaotaru.voxelmap.gui;

import com.mamiyaotaru.voxelmap.gui.overridden.GuiScreenMinimap;
import com.mamiyaotaru.voxelmap.interfaces.IVoxelMap;
import com.mamiyaotaru.voxelmap.util.I18nUtils;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.GuiYesNoCallback;

public class GuiSelectPlayer extends GuiScreenMinimap implements GuiYesNoCallback {
    private final GuiScreen parentScreen;
    private final int maxMessageLength = 78;
    protected String screenTitle = "Players";
    protected boolean allClicked = false;
    protected GuiTextField message;
    protected GuiTextField filter;
    private boolean sharingWaypoint = true;
    private GuiButtonRowListPlayers playerList;
    private String tooltip = null;
    private final String locInfo;

    public GuiSelectPlayer(GuiScreen parentScreen, IVoxelMap master, String locInfo, boolean sharingWaypoint) {
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

    public void initGui() {
        this.screenTitle = I18nUtils.getString(this.sharingWaypoint ? "minimap.waypointshare.title" : "minimap.waypointshare.titlecoordinate");
        this.mc.keyboardListener.enableRepeatEvents(true);
        this.playerList = new GuiButtonRowListPlayers(this);
        int messageStringWidth = this.getFontRenderer().getStringWidth(I18nUtils.getString("minimap.waypointshare.sharemessage") + ":");
        this.message = new GuiTextField(1, this.getFontRenderer(), this.getWidth() / 2 - 153 + messageStringWidth + 5, 34, 305 - messageStringWidth - 5, 20);
        this.message.setMaxStringLength(78);
        int filterStringWidth = this.getFontRenderer().getStringWidth(I18nUtils.getString("minimap.waypoints.filter") + ":");
        this.filter = new GuiTextField(
                2, this.getFontRenderer(), this.getWidth() / 2 - 153 + filterStringWidth + 5, this.getHeight() - 55, 305 - filterStringWidth - 5, 20
        );
        this.filter.setMaxStringLength(35);
        this.filter.setFocused(true);
        this.addButton(new GuiButton(-200, this.width / 2 - 100, this.height - 27, I18nUtils.getString("gui.cancel")) {
            public void onClick(double p_onClick_1_, double p_onClick_3_) {
                GuiSelectPlayer.this.actionPerformed(this);
            }
        });
    }

    protected void actionPerformed(GuiButton par1GuiButton) {
        if (par1GuiButton.enabled && par1GuiButton.id == -200) {
            this.getMinecraft().displayGuiScreen(this.parentScreen);
        }
    }

    public boolean keyPressed(int keysm, int scancode, int b) {
        this.message.keyPressed(keysm, scancode, b);
        if (this.filter.keyPressed(keysm, scancode, b)) {
            this.playerList.updateFilter(this.filter.getText().toLowerCase());
        }

        return super.keyPressed(keysm, scancode, b);
    }

    public boolean charTyped(char character, int keycode) {
        this.message.charTyped(character, keycode);
        if (this.filter.charTyped(character, keycode)) {
            this.playerList.updateFilter(this.filter.getText().toLowerCase());
        }

        return super.charTyped(character, keycode);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        this.message.mouseClicked(mouseX, mouseY, mouseButton);
        this.filter.mouseClicked(mouseX, mouseY, mouseButton);
        this.playerList.mouseClicked(mouseX, mouseY, mouseButton);
        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    public boolean mouseReleased(double mouseX, double mouseY, int mouseButton) {
        this.message.mouseReleased(mouseX, mouseY, mouseButton);
        this.filter.mouseReleased(mouseX, mouseY, mouseButton);
        this.playerList.mouseReleased(mouseX, mouseY, mouseButton);
        return super.mouseReleased(mouseX, mouseY, mouseButton);
    }

    public boolean mouseDragged(double mouseX, double mouseY, int mouseEvent, double deltaX, double deltaY) {
        return this.playerList.mouseDragged(mouseX, mouseY, mouseEvent, deltaX, deltaY);
    }

    public boolean mouseScrolled(double amount) {
        return this.playerList.mouseScrolled(amount);
    }

    public void confirmResult(boolean par1, int par2) {
        if (this.allClicked) {
            this.allClicked = false;
            if (par1) {
                String combined = this.message.getText() + " " + this.locInfo;
                if (combined.length() > 100) {
                    this.mc.player.sendChatMessage(this.message.getText());
                    this.mc.player.sendChatMessage(this.locInfo);
                } else {
                    this.mc.player.sendChatMessage(combined);
                }

                this.getMinecraft().displayGuiScreen(this.parentScreen);
            } else {
                this.getMinecraft().displayGuiScreen(this);
            }
        }
    }

    protected void sendMessageToPlayer(String name) {
        String combined = "/msg " + name + " " + this.message.getText() + " " + this.locInfo;
        if (combined.length() > 100) {
            this.mc.player.sendChatMessage("/msg " + name + " " + this.message.getText());
            this.mc.player.sendChatMessage("/msg " + name + " " + this.locInfo);
        } else {
            this.mc.player.sendChatMessage(combined);
        }

        this.getMinecraft().displayGuiScreen(this.parentScreen);
    }

    public void render(int mouseX, int mouseY, float partialTicks) {
        super.drawMap();
        this.tooltip = null;
        this.playerList.drawScreen(mouseX, mouseY, partialTicks);
        this.drawCenteredString(this.getFontRenderer(), this.screenTitle, this.getWidth() / 2, 20, 16777215);
        super.render(mouseX, mouseY, partialTicks);
        this.drawString(this.getFontRenderer(), I18nUtils.getString("minimap.waypointshare.sharemessage") + ":", this.getWidth() / 2 - 153, 39, 10526880);
        this.message.drawTextField(mouseX, mouseY, partialTicks);
        this.drawCenteredString(this.getFontRenderer(), I18nUtils.getString("minimap.waypointshare.sharewith"), this.getWidth() / 2, 75, 16777215);
        this.drawString(this.getFontRenderer(), I18nUtils.getString("minimap.waypoints.filter") + ":", this.getWidth() / 2 - 153, this.getHeight() - 50, 10526880);
        this.filter.drawTextField(mouseX, mouseY, partialTicks);
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
    }
}
