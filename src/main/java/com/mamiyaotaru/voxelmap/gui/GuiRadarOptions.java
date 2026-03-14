package com.mamiyaotaru.voxelmap.gui;

import com.mamiyaotaru.voxelmap.RadarSettingsManager;
import com.mamiyaotaru.voxelmap.gui.overridden.EnumOptionsMinimap;
import com.mamiyaotaru.voxelmap.gui.overridden.GuiOptionButtonMinimap;
import com.mamiyaotaru.voxelmap.gui.overridden.GuiScreenMinimap;
import com.mamiyaotaru.voxelmap.interfaces.IVoxelMap;
import com.mamiyaotaru.voxelmap.util.I18nUtils;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;

public class GuiRadarOptions extends GuiScreenMinimap {
    private static final EnumOptionsMinimap[] relevantOptionsFull = new EnumOptionsMinimap[]{
            EnumOptionsMinimap.SHOWRADAR,
            EnumOptionsMinimap.RADARMODE,
            EnumOptionsMinimap.SHOWHOSTILES,
            EnumOptionsMinimap.SHOWNEUTRALS,
            EnumOptionsMinimap.SHOWPLAYERS,
            EnumOptionsMinimap.SHOWPLAYERNAMES,
            EnumOptionsMinimap.SHOWPLAYERHELMETS,
            EnumOptionsMinimap.SHOWMOBHELMETS,
            EnumOptionsMinimap.RADARFILTERING,
            EnumOptionsMinimap.RADAROUTLINES
    };
    private static final EnumOptionsMinimap[] relevantOptionsSimple = new EnumOptionsMinimap[]{
            EnumOptionsMinimap.SHOWRADAR,
            EnumOptionsMinimap.RADARMODE,
            EnumOptionsMinimap.SHOWHOSTILES,
            EnumOptionsMinimap.SHOWNEUTRALS,
            EnumOptionsMinimap.SHOWPLAYERS,
            EnumOptionsMinimap.SHOWFACING
    };
    private static EnumOptionsMinimap[] relevantOptions;
    private final Screen parent;
    private final RadarSettingsManager options;
    protected String screenTitle = "Radar Options";

    public GuiRadarOptions(Screen parent, IVoxelMap master) {
        this.parent = parent;
        this.options = master.getRadarOptions();
    }

    public void init() {
        this.getButtonList().clear();
        this.children.clear();
        int var2 = 0;
        this.screenTitle = I18nUtils.getString("options.minimap.radar.title");
        if (this.options.radarMode == 2) {
            relevantOptions = relevantOptionsFull;
        } else {
            relevantOptions = relevantOptionsSimple;
        }

        for (int t = 0; t < relevantOptions.length; t++) {
            final EnumOptionsMinimap option = relevantOptions[t];
            GuiOptionButtonMinimap optionButton = new GuiOptionButtonMinimap(
                    this.getWidth() / 2 - 155 + var2 % 2 * 160, this.getHeight() / 6 + 24 * (var2 >> 1), option, this.options.getKeyText(option), null
            ) {
                public void onPress() {
                    GuiRadarOptions.this.actionPerformed(this, option.returnEnumOrdinal());
                }
            };
            this.addButton(optionButton);
            var2++;
        }

        for (Object buttonObj : this.getButtonList()) {
            if (buttonObj instanceof GuiOptionButtonMinimap) {
                GuiOptionButtonMinimap button = (GuiOptionButtonMinimap) buttonObj;
                if (!button.returnEnumOptions().equals(EnumOptionsMinimap.SHOWRADAR)) {
                    button.active = this.options.showRadar;
                }

                if (button.returnEnumOptions().equals(EnumOptionsMinimap.SHOWPLAYERS)) {
                    button.active = button.active && (this.options.radarAllowed || this.options.radarPlayersAllowed);
                } else if (button.returnEnumOptions().equals(EnumOptionsMinimap.SHOWNEUTRALS) || button.returnEnumOptions().equals(EnumOptionsMinimap.SHOWHOSTILES)
                ) {
                    button.active = button.active && (this.options.radarAllowed || this.options.radarMobsAllowed);
                } else if (!button.returnEnumOptions().equals(EnumOptionsMinimap.SHOWPLAYERHELMETS)
                        && !button.returnEnumOptions().equals(EnumOptionsMinimap.SHOWPLAYERNAMES)) {
                    if (button.returnEnumOptions().equals(EnumOptionsMinimap.SHOWMOBHELMETS)) {
                        button.active = button.active
                                && (this.options.showNeutrals || this.options.showHostiles)
                                && (this.options.radarAllowed || this.options.radarMobsAllowed);
                    }
                } else {
                    button.active = button.active && this.options.showPlayers && (this.options.radarAllowed || this.options.radarPlayersAllowed);
                }
            }
        }

        if (this.options.radarMode == 2) {
            this.addButton(
                    new ButtonWidget(this.getWidth() / 2 - 155, this.getHeight() / 6 + 144 - 6, 150, 20, I18nUtils.getString("options.minimap.radar.selectmobs"), null) {
                        public void onPress() {
                            GuiRadarOptions.this.actionPerformed(this, 101);
                        }
                    }
            );
        }

        this.addButton(new ButtonWidget(this.getWidth() / 2 - 100, this.getHeight() / 6 + 168, 200, 20, I18nUtils.getString("gui.done"), null) {
            public void onPress() {
                GuiRadarOptions.this.actionPerformed(this, 200);
            }
        });
    }

    protected void actionPerformed(ButtonWidget buttonClicked, int id) {
        if (buttonClicked.active) {
            if (id < 100 && buttonClicked instanceof GuiOptionButtonMinimap) {
                this.options.setOptionValue(((GuiOptionButtonMinimap) buttonClicked).returnEnumOptions(), 1);
                if (((GuiOptionButtonMinimap) buttonClicked).returnEnumOptions().equals(EnumOptionsMinimap.RADARMODE)) {
                    this.init();
                    return;
                }

                buttonClicked.setMessage(this.options.getKeyText(EnumOptionsMinimap.getEnumOptions(id)));

                for (Object buttonObj : this.getButtonList()) {
                    if (buttonObj instanceof GuiOptionButtonMinimap) {
                        GuiOptionButtonMinimap button = (GuiOptionButtonMinimap) buttonObj;
                        if (!button.returnEnumOptions().equals(EnumOptionsMinimap.SHOWRADAR)) {
                            button.active = this.options.showRadar;
                        }

                        if (button.returnEnumOptions().equals(EnumOptionsMinimap.SHOWPLAYERS)) {
                            button.active = button.active && (this.options.radarAllowed || this.options.radarPlayersAllowed);
                        } else if (button.returnEnumOptions().equals(EnumOptionsMinimap.SHOWNEUTRALS)
                                || button.returnEnumOptions().equals(EnumOptionsMinimap.SHOWHOSTILES)) {
                            button.active = button.active && (this.options.radarAllowed || this.options.radarMobsAllowed);
                        } else if (button.returnEnumOptions().equals(EnumOptionsMinimap.SHOWPLAYERHELMETS)
                                || button.returnEnumOptions().equals(EnumOptionsMinimap.SHOWPLAYERNAMES)) {
                            button.active = button.active && this.options.showPlayers && (this.options.radarAllowed || this.options.radarPlayersAllowed);
                        } else if (button.returnEnumOptions().equals(EnumOptionsMinimap.SHOWMOBHELMETS)) {
                            button.active = button.active
                                    && (this.options.showNeutrals || this.options.showHostiles)
                                    && (this.options.radarAllowed || this.options.radarMobsAllowed);
                        }
                    } else if (buttonObj instanceof ButtonWidget) {
                        ButtonWidget buttonx = (ButtonWidget) buttonObj;
                        if (id == 101) {
                            buttonx.active = this.options.showRadar;
                        }
                    }
                }
            }

            if (id == 101) {
                this.getMinecraft().openScreen(new GuiMobs(this, this.options));
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
