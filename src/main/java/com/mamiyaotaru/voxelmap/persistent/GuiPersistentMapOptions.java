package com.mamiyaotaru.voxelmap.persistent;

import com.mamiyaotaru.voxelmap.gui.overridden.EnumOptionsMinimap;
import com.mamiyaotaru.voxelmap.gui.overridden.GuiOptionButtonMinimap;
import com.mamiyaotaru.voxelmap.gui.overridden.GuiOptionSliderMinimap;
import com.mamiyaotaru.voxelmap.gui.overridden.GuiScreenMinimap;
import com.mamiyaotaru.voxelmap.interfaces.IVoxelMap;
import com.mamiyaotaru.voxelmap.util.I18nUtils;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;

public class GuiPersistentMapOptions extends GuiScreenMinimap {
    private static EnumOptionsMinimap[] relevantOptions;
    private static EnumOptionsMinimap[] relevantOptions2;
    private final Screen parent;
    private final PersistentMapSettingsManager options;
    protected String screenTitle = "Worldmap Options";
    protected String cacheSettings = "Zoom/Cache Settings";
    protected String warning = "Edit at your own risk";

    public GuiPersistentMapOptions(Screen parent, IVoxelMap master) {
        this.parent = parent;
        this.options = master.getPersistentMapOptions();
    }

    public void init() {
        relevantOptions = new EnumOptionsMinimap[]{EnumOptionsMinimap.SHOWWAYPOINTS, EnumOptionsMinimap.SHOWWAYPOINTNAMES};
        this.screenTitle = I18nUtils.getString("options.worldmap.title");
        this.cacheSettings = I18nUtils.getString("options.worldmap.cachesettings");
        this.warning = I18nUtils.getString("options.worldmap.warning");
        int var2 = 0;

        for (int t = 0; t < relevantOptions.length; t++) {
            final EnumOptionsMinimap option = relevantOptions[t];
            GuiOptionButtonMinimap var7 = new GuiOptionButtonMinimap(
                    this.getWidth() / 2 - 155 + var2 % 2 * 160, this.getHeight() / 6 + 24 * (var2 >> 1), option, this.options.getKeyText(option), null
            ) {
                public void onPress() {
                    GuiPersistentMapOptions.this.actionPerformed(this, option.returnEnumOrdinal());
                }
            };
            this.addButton(var7);
            var2++;
        }

        relevantOptions2 = new EnumOptionsMinimap[]{EnumOptionsMinimap.MINZOOM, EnumOptionsMinimap.MAXZOOM, EnumOptionsMinimap.CACHESIZE};
        var2 += 2;

        for (int t = 0; t < relevantOptions2.length; t++) {
            final EnumOptionsMinimap option = relevantOptions2[t];
            if (option.isFloat()) {
                float sValue = this.options.getOptionFloatValue(option);
                float fValue = 0.0F;
                switch (option) {
                    case MINZOOM:
                        fValue = (sValue - -3.0F) / (5 - -3);
                        break;
                    case MAXZOOM:
                        fValue = (sValue - -3.0F) / (5 - -3);
                        break;
                    case CACHESIZE:
                        fValue = sValue / 5000.0F;
                        break;
                    default:
                        throw new IllegalArgumentException(
                                "Add code to handle EnumOptionMinimap: " + option.getName() + ". (possibly not a float value applicable to persistent map)"
                        );
                }

                this.addButton(
                        new GuiOptionSliderMinimap(this.getWidth() / 2 - 155 + var2 % 2 * 160, this.getHeight() / 6 + 24 * (var2 >> 1), option, fValue, this.options)
                );
            } else {
                GuiOptionButtonMinimap var7 = new GuiOptionButtonMinimap(
                        this.getWidth() / 2 - 155 + var2 % 2 * 160, this.getHeight() / 6 + 24 * (var2 >> 1), option, this.options.getKeyText(option), null
                ) {
                    public void onPress() {
                        GuiPersistentMapOptions.this.actionPerformed(this, option.returnEnumOrdinal());
                    }
                };
                this.addButton(var7);
            }

            var2++;
        }

        this.addButton(
                new ButtonWidget(
                        this.getWidth() / 2 - 100,
                        this.getHeight() / 6 + 168,
                        200,
                        20,
                        I18nUtils.getString("gui.done"),
                        buttonWidget_1 -> this.getMinecraft().openScreen(this.parent)
                )
        );

        for (Object buttonObj : this.getButtonList()) {
            if (buttonObj instanceof GuiOptionButtonMinimap) {
                GuiOptionButtonMinimap button = (GuiOptionButtonMinimap) buttonObj;
                if (button.returnEnumOptions().equals(EnumOptionsMinimap.SHOWWAYPOINTNAMES)) {
                    button.active = this.options.showWaypoints;
                }
            }
        }
    }

    protected void actionPerformed(ButtonWidget par1GuiButton, int id) {
        if (par1GuiButton.active && id < 100 && par1GuiButton instanceof GuiOptionButtonMinimap) {
            this.options.setOptionValue(((GuiOptionButtonMinimap) par1GuiButton).returnEnumOptions(), 1);
            par1GuiButton.setMessage(this.options.getKeyText(EnumOptionsMinimap.getEnumOptions(id)));

            for (Object buttonObj : this.getButtonList()) {
                if (buttonObj instanceof GuiOptionButtonMinimap) {
                    GuiOptionButtonMinimap button = (GuiOptionButtonMinimap) buttonObj;
                    if (button.returnEnumOptions().equals(EnumOptionsMinimap.SHOWWAYPOINTNAMES)) {
                        button.active = this.options.showWaypoints;
                    }
                }
            }
        }
    }

    public void render(int par1, int par2, float par3) {
        for (Object buttonObj : this.getButtonList()) {
            if (buttonObj instanceof GuiOptionSliderMinimap) {
                GuiOptionSliderMinimap slider = (GuiOptionSliderMinimap) buttonObj;
                EnumOptionsMinimap option = slider.returnEnumOptions();
                float sValue = this.options.getOptionFloatValue(option);
                float fValue = 0.0F;
                switch (option) {
                    case MINZOOM:
                        fValue = (sValue - -3.0F) / (5 - -3);
                        break;
                    case MAXZOOM:
                        fValue = (sValue - -3.0F) / (5 - -3);
                        break;
                    case CACHESIZE:
                        fValue = sValue / 5000.0F;
                        break;
                    default:
                        throw new IllegalArgumentException(
                                "Add code to handle EnumOptionMinimap: " + option.getName() + ". (possibly not a float value applicable to persistent map)"
                        );
                }

                if (this.getFocused() != slider) {
                    slider.setValue(fValue);
                }
            }
        }

        super.drawMap();
        this.renderBackground();
        this.drawCenteredString(this.getFontRenderer(), this.screenTitle, this.getWidth() / 2, 20, 16777215);
        this.drawCenteredString(this.getFontRenderer(), this.cacheSettings, this.getWidth() / 2, this.getHeight() / 6 + 24, 16777215);
        this.drawCenteredString(this.getFontRenderer(), this.warning, this.getWidth() / 2, this.getHeight() / 6 + 34, 16777215);
        super.render(par1, par2, par3);
    }
}
