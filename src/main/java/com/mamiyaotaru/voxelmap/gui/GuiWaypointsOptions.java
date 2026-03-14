package com.mamiyaotaru.voxelmap.gui;

import com.mamiyaotaru.voxelmap.MapSettingsManager;
import com.mamiyaotaru.voxelmap.gui.overridden.EnumOptionsMinimap;
import com.mamiyaotaru.voxelmap.gui.overridden.GuiOptionButtonMinimap;
import com.mamiyaotaru.voxelmap.gui.overridden.GuiOptionSliderMinimap;
import com.mamiyaotaru.voxelmap.gui.overridden.GuiScreenMinimap;
import com.mamiyaotaru.voxelmap.util.I18nUtils;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;

public class GuiWaypointsOptions extends GuiScreenMinimap {
    private static final EnumOptionsMinimap[] relevantOptions = new EnumOptionsMinimap[]{EnumOptionsMinimap.WAYPOINTDISTANCE, EnumOptionsMinimap.DEATHPOINTS};
    private final Screen parent;
    private final MapSettingsManager options;
    protected String screenTitle = "Waypoint Options";

    public GuiWaypointsOptions(Screen parent, MapSettingsManager options) {
        this.parent = parent;
        this.options = options;
    }

    public void init() {
        int var2 = 0;
        this.screenTitle = I18nUtils.getString("options.minimap.waypoints.title");

        for (int t = 0; t < relevantOptions.length; t++) {
            final EnumOptionsMinimap option = relevantOptions[t];
            if (option.isFloat()) {
                float distance = this.options.getOptionFloatValue(option);
                if (distance < 0.0F) {
                    distance = 10001.0F;
                }

                distance = (distance - 50.0F) / 9951.0F;
                this.addButton(
                        new GuiOptionSliderMinimap(this.getWidth() / 2 - 155 + var2 % 2 * 160, this.getHeight() / 6 + 24 * (var2 >> 1), option, distance, this.options)
                );
            } else {
                GuiOptionButtonMinimap var7 = new GuiOptionButtonMinimap(
                        this.getWidth() / 2 - 155 + var2 % 2 * 160, this.getHeight() / 6 + 24 * (var2 >> 1), option, this.options.getKeyText(option), null
                ) {
                    public void onPress() {
                        GuiWaypointsOptions.this.actionPerformed(this, option.returnEnumOrdinal());
                    }
                };
                this.addButton(var7);
            }

            var2++;
        }

        this.addButton(new ButtonWidget(this.getWidth() / 2 - 100, this.getHeight() / 6 + 168, 200, 20, I18nUtils.getString("gui.done"), null) {
            public void onPress() {
                GuiWaypointsOptions.this.actionPerformed(this, 200);
            }
        });
    }

    protected void actionPerformed(ButtonWidget par1GuiButton, int id) {
        if (par1GuiButton.active) {
            if (id < 100 && par1GuiButton instanceof GuiOptionButtonMinimap) {
                this.options.setOptionValue(((GuiOptionButtonMinimap) par1GuiButton).returnEnumOptions(), 1);
                par1GuiButton.setMessage(this.options.getKeyText(EnumOptionsMinimap.getEnumOptions(id)));
            }

            if (id == 200) {
                this.getMinecraft().openScreen(this.parent);
            }
        }
    }

    public void render(int mouseX, int mouseY, float partialTicks) {
        super.drawMap();
        this.renderBackground();
        this.drawCenteredString(this.font, this.screenTitle, this.getWidth() / 2, 20, 16777215);
        super.render(mouseX, mouseY, partialTicks);
    }
}
