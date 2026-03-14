package com.mamiyaotaru.voxelmap.gui;

import com.mamiyaotaru.voxelmap.gui.overridden.GuiScreenMinimap;
import com.mamiyaotaru.voxelmap.gui.overridden.IPopupGuiScreen;
import com.mamiyaotaru.voxelmap.gui.overridden.Popup;
import com.mamiyaotaru.voxelmap.gui.overridden.PopupGuiButton;
import com.mamiyaotaru.voxelmap.interfaces.IColorManager;
import com.mamiyaotaru.voxelmap.interfaces.IVoxelMap;
import com.mamiyaotaru.voxelmap.interfaces.IWaypointManager;
import com.mamiyaotaru.voxelmap.textures.Sprite;
import com.mamiyaotaru.voxelmap.textures.TextureAtlas;
import com.mamiyaotaru.voxelmap.util.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;

public class GuiAddWaypoint extends GuiScreenMinimap implements IPopupGuiScreen {
    protected DimensionContainer selectedDimension = null;
    protected Waypoint waypoint;
    IVoxelMap master;
    IWaypointManager waypointManager;
    IColorManager colorManager;
    private final IGuiWaypoints parentGui;
    private PopupGuiButton doneButton;
    private GuiSlotDimensions dimensionList;
    private String tooltip = null;
    private TextFieldWidget waypointName;
    private TextFieldWidget waypointX;
    private TextFieldWidget waypointZ;
    private TextFieldWidget waypointY;
    private PopupGuiButton buttonEnabled;
    private boolean choosingColor = false;
    private boolean choosingIcon = false;
    private final float red;
    private final float green;
    private final float blue;
    private final String suffix;
    private final boolean enabled;
    private boolean editing = false;

    public GuiAddWaypoint(IGuiWaypoints par1GuiScreen, IVoxelMap master, Waypoint par2Waypoint, boolean editing) {
        this.master = master;
        this.waypointManager = master.getWaypointManager();
        this.colorManager = master.getColorManager();
        this.parentGui = par1GuiScreen;
        this.waypoint = par2Waypoint;
        this.red = this.waypoint.red;
        this.green = this.waypoint.green;
        this.blue = this.waypoint.blue;
        this.suffix = this.waypoint.imageSuffix;
        this.enabled = this.waypoint.enabled;
        this.editing = editing;
    }

    static String setTooltip(GuiAddWaypoint par0GuiWaypoint, String par1Str) {
        return par0GuiWaypoint.tooltip = par1Str;
    }

    public void tick() {
        this.waypointName.tick();
        this.waypointX.tick();
        this.waypointY.tick();
        this.waypointZ.tick();
    }

    public void init() {
        this.getMinecraft().keyboard.enableRepeatEvents(true);
        this.getButtonList().clear();
        this.waypointName = new TextFieldWidget(this.getFontRenderer(), this.getWidth() / 2 - 100, this.getHeight() / 6 + 13, 200, 20, "");
        this.waypointName.setText(this.waypoint.name);
        this.waypointX = new TextFieldWidget(this.getFontRenderer(), this.getWidth() / 2 - 100, this.getHeight() / 6 + 41 + 13, 56, 20, "");
        this.waypointX.setMaxLength(128);
        this.waypointX.setText("" + this.waypoint.getX());
        this.waypointZ = new TextFieldWidget(this.getFontRenderer(), this.getWidth() / 2 - 28, this.getHeight() / 6 + 41 + 13, 56, 20, "");
        this.waypointZ.setMaxLength(128);
        this.waypointZ.setText("" + this.waypoint.getZ());
        this.waypointY = new TextFieldWidget(this.getFontRenderer(), this.getWidth() / 2 + 44, this.getHeight() / 6 + 41 + 13, 56, 20, "");
        this.waypointY.setMaxLength(128);
        this.waypointY.setText("" + this.waypoint.getY());
        this.addButton(this.waypointName);
        this.addButton(this.waypointX);
        this.addButton(this.waypointZ);
        this.addButton(this.waypointY);
        int buttonListY = this.getHeight() / 6 + 82 + 6;
        this.addButton(
                this.buttonEnabled = new PopupGuiButton(
                        this.getWidth() / 2 - 101, buttonListY, 100, 20, "Enabled: " + (this.waypoint.enabled ? "On" : "Off"), null, this
                ) {
                    public void onPress() {
                        GuiAddWaypoint.this.actionPerformed(this, 6);
                    }
                }
        );
        this.addButton(
                new PopupGuiButton(this.getWidth() / 2 - 101, buttonListY + 24, 100, 20, I18nUtils.getString("minimap.waypoints.sortbycolor") + ":     ", null, this) {
                    public void onPress() {
                        GuiAddWaypoint.this.actionPerformed(this, 7);
                    }
                }
        );
        this.addButton(
                new PopupGuiButton(this.getWidth() / 2 - 101, buttonListY + 48, 100, 20, I18nUtils.getString("minimap.waypoints.sortbyicon") + ":     ", null, this) {
                    public void onPress() {
                        GuiAddWaypoint.this.actionPerformed(this, 8);
                    }
                }
        );
        this.doneButton = new PopupGuiButton(this.getWidth() / 2 - 155, this.getHeight() / 6 + 168, 150, 20, I18nUtils.getString("addServer.add"), null, this) {
            public void onPress() {
                GuiAddWaypoint.this.actionPerformed(this, 0);
            }
        };
        this.addButton(this.doneButton);
        this.addButton(new PopupGuiButton(this.getWidth() / 2 + 5, this.getHeight() / 6 + 168, 150, 20, I18nUtils.getString("gui.cancel"), null, this) {
            public void onPress() {
                GuiAddWaypoint.this.actionPerformed(this, 1);
            }
        });
        this.doneButton.active = this.waypointName.getText().length() > 0;
        this.setFocused(this.waypointName);
        this.waypointName.method_1876(true);
        this.dimensionList = new GuiSlotDimensions(this);
    }

    @Override
    public void removed() {
        this.getMinecraft().keyboard.enableRepeatEvents(false);
    }

    protected void actionPerformed(ButtonWidget par1GuiButton, int id) {
        if (par1GuiButton.active) {
            if (id == 6) {
                this.waypoint.enabled = !this.waypoint.enabled;
            }

            if (id == 7) {
                this.choosingColor = true;
            }

            if (id == 8) {
                this.choosingIcon = true;
            }

            if (id == 1) {
                this.waypoint.red = this.red;
                this.waypoint.green = this.green;
                this.waypoint.blue = this.blue;
                this.waypoint.imageSuffix = this.suffix;
                this.waypoint.enabled = this.enabled;
                if (this.parentGui != null) {
                    this.parentGui.accept(false);
                } else {
                    this.getMinecraft().openScreen(null);
                }
            } else if (id == 0) {
                this.waypoint.name = this.waypointName.getText();
                this.waypoint.setX(Integer.parseInt(this.waypointX.getText()));
                this.waypoint.setZ(Integer.parseInt(this.waypointZ.getText()));
                this.waypoint.setY(Integer.parseInt(this.waypointY.getText()));
                if (this.parentGui != null) {
                    this.parentGui.accept(true);
                } else {
                    if (this.editing) {
                        this.waypointManager.saveWaypoints();
                    } else {
                        this.waypointManager.addWaypoint(this.waypoint);
                    }

                    this.getMinecraft().openScreen(null);
                }
            }
        }
    }

    public boolean keyPressed(int keysm, int scancode, int b) {
        boolean OK = false;
        if (!this.popupOpen()) {
            OK = super.keyPressed(keysm, scancode, b);
            boolean acceptable = this.waypointName.getText().length() > 0;

            try {
                Integer.parseInt(this.waypointX.getText());
                Integer.parseInt(this.waypointZ.getText());
                Integer.parseInt(this.waypointY.getText());
            } catch (NumberFormatException e) {
                acceptable = false;
            }

            this.doneButton.active = acceptable;
            if (keysm == 257 || keysm == 335) {
                this.actionPerformed(this.doneButton, 0);
            }
        }

        return OK;
    }

    public boolean charTyped(char character, int keycode) {
        boolean OK = false;
        if (!this.popupOpen()) {
            OK = super.charTyped(character, keycode);
            boolean acceptable = this.waypointName.getText().length() > 0;

            try {
                Integer.parseInt(this.waypointX.getText());
                Integer.parseInt(this.waypointZ.getText());
                Integer.parseInt(this.waypointY.getText());
            } catch (NumberFormatException e) {
                acceptable = false;
            }

            this.doneButton.active = acceptable;
        }

        return OK;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        if (!this.popupOpen()) {
            super.mouseClicked(mouseX, mouseY, mouseButton);
            this.waypointName.mouseClicked(mouseX, mouseY, mouseButton);
            this.waypointX.mouseClicked(mouseX, mouseY, mouseButton);
            this.waypointZ.mouseClicked(mouseX, mouseY, mouseButton);
            this.waypointY.mouseClicked(mouseX, mouseY, mouseButton);
        } else if (this.choosingColor) {
            if (mouseX >= this.getWidth() / 2 - 128
                    && mouseX < this.getWidth() / 2 + 128
                    && mouseY >= this.getHeight() / 2 - 128
                    && mouseY < this.getHeight() / 2 + 128) {
                int color = this.colorManager.getColorPicker().getRGB((int) mouseX - (this.getWidth() / 2 - 128), (int) mouseY - (this.getHeight() / 2 - 128));
                this.waypoint.red = (color >> 16 & 0xFF) / 255.0F;
                this.waypoint.green = (color >> 8 & 0xFF) / 255.0F;
                this.waypoint.blue = (color >> 0 & 0xFF) / 255.0F;
                this.choosingColor = false;
            }
        } else if (this.choosingIcon) {
            float scScale = (float) this.getMinecraft().window.getScaleFactor();
            TextureAtlas chooser = this.waypointManager.getTextureAtlasChooser();
            float scale = scScale / 2.0F;
            float displayWidthFloat = chooser.getWidth() / scale;
            float displayHeightFloat = chooser.getHeight() / scale;
            if (displayWidthFloat > this.getMinecraft().window.getFramebufferWidth()) {
                float adj = displayWidthFloat / this.getMinecraft().window.getFramebufferWidth();
                scale *= adj;
                displayWidthFloat /= adj;
                displayHeightFloat /= adj;
            }

            if (displayHeightFloat > this.getMinecraft().window.getFramebufferHeight()) {
                float adj = displayHeightFloat / this.getMinecraft().window.getFramebufferHeight();
                scale *= adj;
                displayWidthFloat /= adj;
                displayHeightFloat /= adj;
            }

            int displayWidth = (int) displayWidthFloat;
            int displayHeight = (int) displayHeightFloat;
            if (mouseX >= this.getWidth() / 2 - displayWidth / 2
                    && mouseX < this.getWidth() / 2 + displayWidth / 2
                    && mouseY >= this.getHeight() / 2 - displayHeight / 2
                    && mouseY < this.getHeight() / 2 + displayHeight / 2) {
                float x = ((float) mouseX - (this.getWidth() / 2 - displayWidth / 2)) * scale;
                float y = ((float) mouseY - (this.getHeight() / 2 - displayHeight / 2)) * scale;
                Sprite icon = chooser.getIconAt(x, y);
                if (icon != chooser.getMissingImage()) {
                    this.waypoint.imageSuffix = icon.getIconName().replace("voxelmap:images/waypoints/waypoint", "").replace(".png", "");
                    this.choosingIcon = false;
                }
            }
        }

        if (!this.popupOpen() && this.dimensionList != null) {
            this.dimensionList.mouseClicked(mouseX, mouseY, mouseButton);
        }

        return true;
    }

    public boolean mouseReleased(double mouseX, double mouseY, int mouseButton) {
        if (!this.popupOpen() && this.dimensionList != null) {
            this.dimensionList.mouseReleased(mouseX, mouseY, mouseButton);
        }

        return true;
    }

    public boolean mouseDragged(double mouseX, double mouseY, int mouseEvent, double deltaX, double deltaY) {
        return this.popupOpen() || this.dimensionList == null || this.dimensionList.mouseDragged(mouseX, mouseY, mouseEvent, deltaX, deltaY);
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        return this.popupOpen() || this.dimensionList == null || this.dimensionList.mouseScrolled(mouseX, mouseY, amount);
    }

    @Override
    public boolean overPopup(int x, int y) {
        return this.choosingColor || this.choosingIcon;
    }

    @Override
    public boolean popupOpen() {
        return this.choosingColor || this.choosingIcon;
    }

    @Override
    public void popupAction(Popup popup, int action) {
    }

    public void render(int mouseX, int mouseY, float partialTicks) {
        super.drawMap();
        float scScale = (float) this.getMinecraft().window.getScaleFactor();
        this.tooltip = null;
        this.buttonEnabled
                .setMessage(
                        I18nUtils.getString("minimap.waypoints.enabled")
                                + " "
                                + (this.waypoint.enabled ? I18nUtils.getString("options.on") : I18nUtils.getString("options.off"))
                );
        if (!this.choosingColor && !this.choosingIcon) {
            this.renderBackground();
        }

        this.dimensionList.render(mouseX, mouseY, partialTicks);
        this.drawCenteredString(
                this.getFontRenderer(),
                (this.parentGui == null || !this.parentGui.isEditing()) && !this.editing
                        ? I18nUtils.getString("minimap.waypoints.new")
                        : I18nUtils.getString("minimap.waypoints.edit"),
                this.getWidth() / 2,
                20,
                16777215
        );
        this.drawString(this.getFontRenderer(), I18nUtils.getString("minimap.waypoints.name"), this.getWidth() / 2 - 100, this.getHeight() / 6, 10526880);
        this.drawString(this.getFontRenderer(), I18nUtils.getString("X"), this.getWidth() / 2 - 100, this.getHeight() / 6 + 41, 10526880);
        this.drawString(this.getFontRenderer(), I18nUtils.getString("Z"), this.getWidth() / 2 - 28, this.getHeight() / 6 + 41, 10526880);
        this.drawString(this.getFontRenderer(), I18nUtils.getString("Y"), this.getWidth() / 2 + 44, this.getHeight() / 6 + 41, 10526880);
        this.waypointName.render(mouseX, mouseY, partialTicks);
        this.waypointX.render(mouseX, mouseY, partialTicks);
        this.waypointZ.render(mouseX, mouseY, partialTicks);
        this.waypointY.render(mouseX, mouseY, partialTicks);
        int buttonListY = this.getHeight() / 6 + 82 + 6;
        super.render(mouseX, mouseY, partialTicks);
        GLShim.glColor4f(this.waypoint.red, this.waypoint.green, this.waypoint.blue, 1.0F);
        GLUtils.disp(-1);
        this.blit(this.getWidth() / 2 - 25, buttonListY + 24 + 5, 0, 0, 16, 10);
        TextureAtlas chooser = this.waypointManager.getTextureAtlasChooser();
        GLUtils.disp(chooser.getGlId());
        GLShim.glTexParameteri(3553, 10241, 9729);
        Sprite icon = chooser.getAtlasSprite("voxelmap:images/waypoints/waypoint" + this.waypoint.imageSuffix + ".png");
        this.drawTexturedModalRect(this.getWidth() / 2 - 25, buttonListY + 48 + 2, icon, 16.0F, 16.0F);
        if (this.choosingColor || this.choosingIcon) {
            this.renderBackground();
        }

        if (this.choosingColor) {
            GLShim.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            GLUtils.img(new Identifier("voxelmap", "images/colorpicker.png"));
            GLShim.glTexParameteri(3553, 10241, 9728);
            this.blit(this.getWidth() / 2 - 128, this.getHeight() / 2 - 128, 0, 0, 256, 256);
        }

        if (this.choosingIcon) {
            float scale = scScale / 2.0F;
            float displayWidthFloat = chooser.getWidth() / scale;
            float displayHeightFloat = chooser.getHeight() / scale;
            if (displayWidthFloat > this.getMinecraft().window.getFramebufferWidth()) {
                float adj = displayWidthFloat / this.getMinecraft().window.getFramebufferWidth();
                displayWidthFloat /= adj;
                displayHeightFloat /= adj;
            }

            if (displayHeightFloat > this.getMinecraft().window.getFramebufferHeight()) {
                float adj = displayHeightFloat / this.getMinecraft().window.getFramebufferHeight();
                displayWidthFloat /= adj;
                displayHeightFloat /= adj;
            }

            int displayWidth = (int) displayWidthFloat;
            int displayHeight = (int) displayHeightFloat;
            GLUtils.disp(-1);
            GLShim.glTexParameteri(3553, 10241, 9728);
            GLShim.glColor4f(0.0F, 0.0F, 0.0F, 1.0F);
            this.blit(this.getWidth() / 2 - displayWidth / 2 - 1, this.getHeight() / 2 - displayHeight / 2 - 1, 0, 0, displayWidth + 2, displayHeight + 2);
            GLShim.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            this.blit(this.getWidth() / 2 - displayWidth / 2, this.getHeight() / 2 - displayHeight / 2, 0, 0, displayWidth, displayHeight);
            GLShim.glColor4f(this.waypoint.red, this.waypoint.green, this.waypoint.blue, 1.0F);
            GLShim.glEnable(3042);
            GLUtils.disp(chooser.getGlId());
            GLShim.glTexParameteri(3553, 10241, 9729);
            blit(
                    this.getWidth() / 2 - displayWidth / 2,
                    this.getHeight() / 2 - displayHeight / 2,
                    displayWidth,
                    displayHeight,
                    0.0F,
                    0.0F,
                    chooser.getWidth(),
                    chooser.getHeight(),
                    chooser.getImageWidth(),
                    chooser.getImageHeight()
            );
            if (mouseX >= this.getWidth() / 2 - displayWidth / 2
                    && mouseX <= this.getWidth() / 2 + displayWidth / 2
                    && mouseY >= this.getHeight() / 2 - displayHeight / 2
                    && mouseY <= this.getHeight() / 2 + displayHeight / 2) {
                float x = (mouseX - (this.getWidth() / 2 - displayWidth / 2)) * scale;
                float y = (mouseY - (this.getHeight() / 2 - displayHeight / 2)) * scale;
                icon = chooser.getIconAt(x, y);
                if (icon != chooser.getMissingImage()) {
                    this.tooltip = icon.getIconName().replace("voxelmap:images/waypoints/waypoint", "").replace(".png", "");
                }
            }

            GLShim.glDisable(3042);
            GLShim.glTexParameteri(3553, 10241, 9728);
        }

        this.renderTooltip(this.tooltip, mouseX, mouseY);
    }

    public void setSelectedDimension(DimensionContainer dimension) {
        this.selectedDimension = dimension;
    }

    public void toggleDimensionSelected() {
        if (this.waypoint.dimensions.size() > 1
                && this.waypoint.dimensions.contains(this.selectedDimension)
                && this.selectedDimension != this.master.getDimensionManager().getDimensionContainerByDimension(MinecraftClient.getInstance().world.dimension)) {
            this.waypoint.dimensions.remove(this.selectedDimension);
        } else this.waypoint.dimensions.add(this.selectedDimension);
    }

    public void renderTooltip(String par1Str, int mouseX, int mouseY) {
        if (par1Str != null && !par1Str.equals("")) {
            int var4 = mouseX + 12;
            int var5 = mouseY - 12;
            int var6 = this.getFontRenderer().getStringWidth(par1Str);
            this.fillGradient(var4 - 3, var5 - 3, var4 + var6 + 3, var5 + 8 + 3, -1073741824, -1073741824);
            this.getFontRenderer().drawWithShadow(par1Str, var4, var5, -1);
        }
    }

    public void drawTexturedModalRect(Sprite icon, float x, float y) {
        float width = icon.getIconWidth() / 2.0F;
        float height = icon.getIconHeight() / 2.0F;
        this.drawTexturedModalRect(x, y, icon, width, height);
    }

    public void drawTexturedModalRect(float xCoord, float yCoord, Sprite icon, float widthIn, float heightIn) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder vertexbuffer = tessellator.getBuffer();
        vertexbuffer.begin(7, VertexFormats.POSITION_TEXTURE);
        vertexbuffer.vertex(xCoord + 0.0F, yCoord + heightIn, this.blitOffset).texture(icon.getMinU(), icon.getMaxV()).next();
        vertexbuffer.vertex(xCoord + widthIn, yCoord + heightIn, this.blitOffset).texture(icon.getMaxU(), icon.getMaxV()).next();
        vertexbuffer.vertex(xCoord + widthIn, yCoord + 0.0F, this.blitOffset).texture(icon.getMaxU(), icon.getMinV()).next();
        vertexbuffer.vertex(xCoord + 0.0F, yCoord + 0.0F, this.blitOffset).texture(icon.getMinU(), icon.getMinV()).next();
        tessellator.draw();
    }
}
