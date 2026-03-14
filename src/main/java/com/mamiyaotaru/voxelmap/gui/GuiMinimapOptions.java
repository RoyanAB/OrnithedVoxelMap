package com.mamiyaotaru.voxelmap.gui;

import com.mamiyaotaru.voxelmap.MapSettingsManager;
import com.mamiyaotaru.voxelmap.gui.overridden.EnumOptionsMinimap;
import com.mamiyaotaru.voxelmap.gui.overridden.GuiOptionButtonMinimap;
import com.mamiyaotaru.voxelmap.gui.overridden.GuiScreenMinimap;
import com.mamiyaotaru.voxelmap.interfaces.IVoxelMap;
import com.mamiyaotaru.voxelmap.persistent.GuiPersistentMapOptions;
import com.mamiyaotaru.voxelmap.util.I18nUtils;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;

public class GuiMinimapOptions extends GuiScreenMinimap {
    private static EnumOptionsMinimap[] relevantOptions;
    private final MapSettingsManager options;
    protected String screenTitle = "Minimap Options";
    private final Screen parent;
    private final IVoxelMap master;

    public GuiMinimapOptions(Screen parent, IVoxelMap master) {
        this.parent = parent;
        this.master = master;
        this.options = master.getMapOptions();
    }

    public void init() {
        relevantOptions = new EnumOptionsMinimap[]{
                EnumOptionsMinimap.COORDS,
                EnumOptionsMinimap.HIDE,
                EnumOptionsMinimap.LOCATION,
                EnumOptionsMinimap.SIZE,
                EnumOptionsMinimap.SQUARE,
                EnumOptionsMinimap.ROTATES,
                EnumOptionsMinimap.BEACONS,
                EnumOptionsMinimap.CAVEMODE
        };
        int var2 = 0;
        this.screenTitle = I18nUtils.getString("options.minimap.title");

        for (int t = 0; t < relevantOptions.length; t++) {
            final EnumOptionsMinimap option = relevantOptions[t];
            GuiOptionButtonMinimap var7 = new GuiOptionButtonMinimap(
                    this.getWidth() / 2 - 155 + var2 % 2 * 160, this.getHeight() / 6 + 24 * (var2 >> 1), option, this.options.getKeyText(option), null
            ) {
                public void onPress() {
                    GuiMinimapOptions.this.actionPerformed(this, option.returnEnumOrdinal());
                }
            };
            this.addButton(var7);
            if (option.equals(EnumOptionsMinimap.CAVEMODE)) {
                var7.active = this.options.cavesAllowed;
            }

            var2++;
        }

        ButtonWidget radarOptionsButton = new ButtonWidget(
                this.getWidth() / 2 - 155, this.getHeight() / 6 + 120 - 6, 150, 20, I18nUtils.getString("options.minimap.radar"), null
        ) {
            public void onPress() {
                GuiMinimapOptions.this.actionPerformed(this, 101);
            }
        };
        radarOptionsButton.active = this.master.getRadarOptions().radarAllowed
                || this.master.getRadarOptions().radarMobsAllowed
                || this.master.getRadarOptions().radarPlayersAllowed;
        this.addButton(radarOptionsButton);
        this.addButton(
                new ButtonWidget(this.getWidth() / 2 + 5, this.getHeight() / 6 + 120 - 6, 150, 20, I18nUtils.getString("options.minimap.detailsperformance"), null) {
                    public void onPress() {
                        GuiMinimapOptions.this.actionPerformed(this, 103);
                    }
                }
        );
        this.addButton(new ButtonWidget(this.getWidth() / 2 - 155, this.getHeight() / 6 + 144 - 6, 150, 20, I18nUtils.getString("options.controls"), null) {
            public void onPress() {
                GuiMinimapOptions.this.actionPerformed(this, 102);
            }
        });
        this.addButton(
                new ButtonWidget(this.getWidth() / 2 + 5, this.getHeight() / 6 + 144 - 6, 150, 20, I18nUtils.getString("options.minimap.worldmap"), null) {
                    public void onPress() {
                        GuiMinimapOptions.this.actionPerformed(this, 100);
                    }
                }
        );
        this.addButton(new ButtonWidget(this.getWidth() / 2 - 100, this.getHeight() / 6 + 168, 200, 20, I18nUtils.getString("gui.done"), null) {
            public void onPress() {
                GuiMinimapOptions.this.actionPerformed(this, 200);
            }
        });
    }

    protected void actionPerformed(ButtonWidget par1GuiButton, int id) {
        if (par1GuiButton.active) {
            if (id < 100 && par1GuiButton instanceof GuiOptionButtonMinimap) {
                this.options.setOptionValue(((GuiOptionButtonMinimap) par1GuiButton).returnEnumOptions(), 1);
                par1GuiButton.setMessage(this.options.getKeyText(EnumOptionsMinimap.getEnumOptions(id)));
                if (((GuiOptionButtonMinimap) par1GuiButton).returnEnumOptions() == EnumOptionsMinimap.OLDNORTH) {
                    this.master.getWaypointManager().setOldNorth(this.options.oldNorth);
                }
            }

            if (id == 103) {
                this.getMinecraft().openScreen(new GuiMinimapPerformance(this, this.master));
            }

            if (id == 102) {
                this.getMinecraft().openScreen(new GuiMinimapControls(this, this.master));
            }

            if (id == 101) {
                this.getMinecraft().openScreen(new GuiRadarOptions(this, this.master));
            }

            if (id == 100) {
                this.getMinecraft().openScreen(new GuiPersistentMapOptions(this, this.master));
            }

            if (id == 200) {
                this.getMinecraft().openScreen(this.parent);
            }
        }
    }

    public void render(int mouseX, int mouseY, float partialTicks) {
        super.drawMap();
        this.renderBackground();
        this.drawCenteredString(this.getFontRenderer(), this.screenTitle, this.getWidth() / 2, 20, 16777215);
        super.render(mouseX, mouseY, partialTicks);
    }
}
