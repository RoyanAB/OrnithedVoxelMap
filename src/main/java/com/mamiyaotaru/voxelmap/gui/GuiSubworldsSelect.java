package com.mamiyaotaru.voxelmap.gui;

import com.mamiyaotaru.voxelmap.gui.overridden.GuiScreenMinimap;
import com.mamiyaotaru.voxelmap.interfaces.IVoxelMap;
import com.mamiyaotaru.voxelmap.interfaces.IWaypointManager;
import com.mamiyaotaru.voxelmap.util.I18nUtils;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.input.KeyboardInput;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.recipe.book.ClientRecipeBook;
import net.minecraft.text.TranslatableText;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class GuiSubworldsSelect extends GuiScreenMinimap implements BooleanConsumer {
    ClientPlayerEntity thePlayer;
    ClientPlayerEntity camera;
    private String title = "VoxelMap - Multiworld Selection";
    private String select = "Which world are you in?";
    private boolean multiworld = false;
    private ButtonWidget cancelBtn;
    private TextFieldWidget newNameField;
    private boolean newWorld = false;
    private float yaw;
    private final int thirdPersonViewOrig;
    private ButtonWidget[] selectButtons;
    private ButtonWidget[] editButtons;
    private String[] worlds;
    private final Screen parent;
    private final IVoxelMap master;
    private final IWaypointManager waypointManager;

    public GuiSubworldsSelect(Screen parent, IVoxelMap master) {
        this.minecraft = MinecraftClient.getInstance();
        this.parent = parent;
        this.thePlayer = this.getMinecraft().player;
        this.camera = new ClientPlayerEntity(
                this.getMinecraft(), this.getMinecraft().world, this.getMinecraft().getNetworkHandler(), this.thePlayer.getStatHandler(), new ClientRecipeBook(null)
        );
        this.camera.input = new KeyboardInput(this.getMinecraft().options);
        this.camera.refreshPositionAndAngles(this.thePlayer.x, this.thePlayer.y - this.thePlayer.getHeightOffset(), this.thePlayer.z, this.thePlayer.yaw, 0.0F);
        this.yaw = this.thePlayer.yaw;
        this.thirdPersonViewOrig = this.getMinecraft().options.perspective;
        this.master = master;
        this.waypointManager = master.getWaypointManager();
    }

    public void init() {
        ArrayList<String> knownSubworldNames = new ArrayList<>(this.waypointManager.getKnownSubworldNames());
        if (!this.multiworld && !this.waypointManager.isMultiworld() && !this.getMinecraft().isConnectedToRealms()) {
            ConfirmScreen var8 = new ConfirmScreen(
                    this,
                    new TranslatableText("worldmap.multiworld.isthismultiworld"),
                    new TranslatableText("worldmap.multiworld.explanation"),
                    I18nUtils.getString("gui.yes"),
                    I18nUtils.getString("gui.no")
            );
            this.getMinecraft().openScreen(var8);
        } else {
            this.getMinecraft().options.perspective = 0;
            this.getMinecraft().setCameraEntity(this.camera);
        }

        this.title = I18nUtils.getString("worldmap.multiworld.title");
        this.select = I18nUtils.getString("worldmap.multiworld.select");
        this.getButtonList().clear();
        int centerX = this.width / 2;
        int buttonsPerRow = this.width / 150;
        if (buttonsPerRow == 0) {
            buttonsPerRow = 1;
        }

        int buttonWidth = this.width / buttonsPerRow - 5;
        int xSpacing = (this.width - buttonsPerRow * buttonWidth) / 2;
        this.cancelBtn = new ButtonWidget(centerX - 100, this.height - 30, 200, 20, I18nUtils.getString("gui.cancel"), null) {
            public void onPress() {
                GuiSubworldsSelect.this.actionPerformed(this, 0);
            }
        };
        this.addButton(this.cancelBtn);
        final Collator collator = I18nUtils.getLocaleAwareCollator();
        Collections.sort(knownSubworldNames, new Comparator<String>() {
            public int compare(String name1, String name2) {
                return -collator.compare(name1, name2);
            }
        });
        int numKnownSubworlds = knownSubworldNames.size();
        int completeRows = (int) Math.floor((float) (numKnownSubworlds + 1) / buttonsPerRow);
        int lastRowShiftBy = (int) (Math.ceil((float) (numKnownSubworlds + 1) / buttonsPerRow) * buttonsPerRow - (numKnownSubworlds + 1));
        this.worlds = new String[numKnownSubworlds];
        this.selectButtons = new ButtonWidget[numKnownSubworlds + 1];
        this.editButtons = new ButtonWidget[numKnownSubworlds + 1];

        for (int t = 0; t < numKnownSubworlds; t++) {
            int shiftBy = 1;
            if (t / buttonsPerRow >= completeRows) {
                shiftBy = lastRowShiftBy + 1;
            }

            this.worlds[t] = knownSubworldNames.get(t);
            final int tt = t;
            this.selectButtons[t] = new ButtonWidget(
                    (buttonsPerRow - shiftBy - t % buttonsPerRow) * buttonWidth + xSpacing,
                    this.height - 60 - t / buttonsPerRow * 21,
                    buttonWidth - 32,
                    20,
                    this.worlds[t],
                    null
            ) {
                public void onPress() {
                    GuiSubworldsSelect.this.actionPerformed(this, tt + 1);
                }
            };
            this.editButtons[t] = new ButtonWidget(
                    (buttonsPerRow - shiftBy - t % buttonsPerRow) * buttonWidth + xSpacing + buttonWidth - 32,
                    this.height - 60 - t / buttonsPerRow * 21,
                    30,
                    20,
                    "⚒",
                    null
            ) {
                public void onPress() {
                    GuiSubworldsSelect.this.actionPerformed(this, -(tt + 1));
                }
            };
            this.addButton(this.selectButtons[t]);
            this.addButton(this.editButtons[t]);
        }

        final int numButtons = this.selectButtons.length - 1;
        if (!this.newWorld) {
            this.selectButtons[numButtons] = new ButtonWidget(
                    (buttonsPerRow - 1 - lastRowShiftBy - numButtons % buttonsPerRow) * buttonWidth + xSpacing,
                    this.height - 60 - numButtons / buttonsPerRow * 21,
                    buttonWidth - 2,
                    20,
                    "< " + I18nUtils.getString("worldmap.multiworld.newname") + " >",
                    null
            ) {
                public void onPress() {
                    GuiSubworldsSelect.this.actionPerformed(this, numButtons + 1);
                }
            };
            this.addButton(this.selectButtons[numButtons]);
        }

        this.newNameField = new TextFieldWidget(
                this.getFontRenderer(),
                (buttonsPerRow - 1 - lastRowShiftBy - numButtons % buttonsPerRow) * buttonWidth + xSpacing + 1,
                this.height - 60 - numButtons / buttonsPerRow * 21 + 1,
                buttonWidth - 4,
                18,
                ""
        );
    }

    protected void actionPerformed(ButtonWidget button, int id) {
        if (button.active) {
            this.newWorld = false;
            if (id == this.worlds.length + 1) {
                this.newWorld = true;
                this.addButton(this.selectButtons[this.worlds.length]);
                this.newNameField.method_1876(true);
            } else if (id == 0) {
                this.getMinecraft().openScreen(null);
            } else if (id > 0) {
                this.worldSelected(this.worlds[id - 1]);
            } else {
                this.editWorld(this.worlds[-id - 1]);
            }
        }
    }

    public void accept(boolean par1) {
        if (!par1) {
            this.getMinecraft().openScreen(this.parent);
        } else {
            this.multiworld = true;
            this.getMinecraft().openScreen(this);
        }
    }

    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        if (this.newWorld) {
            this.newNameField.mouseClicked(mouseX, mouseY, mouseButton);
        }

        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    public boolean keyPressed(int keysm, int scancode, int b) {
        if (this.newNameField.isFocused()) {
            this.newNameField.keyPressed(keysm, scancode, b);
            if ((keysm == 257 || keysm == 335) && this.newNameField.isFocused()) {
                String newName = this.newNameField.getText();
                if (newName != null && !newName.isEmpty()) {
                    this.worldSelected(newName);
                }
            }
        }

        return super.keyPressed(keysm, scancode, b);
    }

    public boolean charTyped(char typedChar, int keyCode) {
        if (this.newNameField.isFocused()) {
            this.newNameField.charTyped(typedChar, keyCode);
            if (keyCode == 28) {
                String newName = this.newNameField.getText();
                if (newName != null && !newName.isEmpty()) {
                    this.worldSelected(newName);
                }
            }
        }

        return super.charTyped(typedChar, keyCode);
    }

    public void tick() {
        this.newNameField.tick();
        super.tick();
    }

    public void render(int mouseX, int mouseY, float partialTicks) {
        int titleStringWidth = this.getFontRenderer().getStringWidth(this.title);
        titleStringWidth = Math.max(titleStringWidth, this.getFontRenderer().getStringWidth(this.select));
        fill(this.width / 2 - titleStringWidth / 2 - 5, 0, this.width / 2 + titleStringWidth / 2 + 5, 27, -1073741824);
        this.drawCenteredString(this.getFontRenderer(), this.title, this.width / 2, 5, 16777215);
        this.drawCenteredString(this.getFontRenderer(), this.select, this.width / 2, 15, 16711680);
        this.camera.prevPitch = this.camera.pitch = 0.0F;
        this.camera.prevYaw = this.camera.yaw = this.yaw;
        float var4 = 0.475F;
        this.camera.lastRenderY = this.camera.prevY = this.camera.y = this.thePlayer.y;
        this.camera.lastRenderX = this.camera.prevX = this.camera.x = this.thePlayer.x - var4 * Math.sin(this.yaw / 180.0 * Math.PI);
        this.camera.lastRenderZ = this.camera.prevZ = this.camera.z = this.thePlayer.z + var4 * Math.cos(this.yaw / 180.0 * Math.PI);
        float var5 = 1.0F;
        this.yaw = (float) (this.yaw + var5 * (1.0 + 0.7F * Math.cos((this.yaw + 45.0F) / 45.0 * Math.PI)));
        super.render(mouseX, mouseY, partialTicks);
        if (this.newWorld) {
            this.newNameField.render(mouseX, mouseY, partialTicks);
        }
    }

    @Override
    public void removed() {
        super.removed();
        this.getMinecraft().options.perspective = this.thirdPersonViewOrig;
        this.getMinecraft().setCameraEntity(this.thePlayer);
    }

    private void worldSelected(String selectedSubworldName) {
        this.waypointManager.setSubworldName(selectedSubworldName, false);
        if (this.parent == null) {
            this.getMinecraft().openScreen(null);
        } else {
            this.getMinecraft().openScreen(this.parent);
        }
    }

    private void editWorld(String subworldNameToEdit) {
        this.getMinecraft().openScreen(new GuiSubworldEdit(this, this.master, subworldNameToEdit));
    }
}
