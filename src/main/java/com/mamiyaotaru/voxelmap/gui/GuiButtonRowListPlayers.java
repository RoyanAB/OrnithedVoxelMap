package com.mamiyaotaru.voxelmap.gui;

import com.mamiyaotaru.voxelmap.util.I18nUtils;
import com.mamiyaotaru.voxelmap.util.TextUtils;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EnumPlayerModelParts;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

public class GuiButtonRowListPlayers extends GuiListExtended {
    final GuiSelectPlayer parentGui;
    private final Minecraft mc = Minecraft.getInstance();
    Row everyoneRow;
    private final ArrayList<NetworkPlayerInfo> players;
    private ArrayList<NetworkPlayerInfo> playersFiltered;

    public GuiButtonRowListPlayers(GuiSelectPlayer par1GuiSelectPlayer) {
        super(Minecraft.getInstance(), par1GuiSelectPlayer.getWidth(), par1GuiSelectPlayer.getHeight(), 89, par1GuiSelectPlayer.getHeight() - 65 + 4, 25);
        this.parentGui = par1GuiSelectPlayer;
        NetHandlerPlayClient netHandlerPlayClient = Minecraft.getInstance().player.connection;
        this.players = new ArrayList<>(netHandlerPlayClient.getPlayerInfoMap());
        this.sort();
        GuiButton everyoneButton = new GuiButton(-1, this.parentGui.getWidth() / 2 - 75, 0, 150, 20, I18nUtils.getString("minimap.waypointshare.all")) {
            public void onClick(double p_onClick_1_, double p_onClick_3_) {
            }
        };
        this.everyoneRow = new Row(everyoneButton);
        this.updateFilter("");
    }

    public String getPlayerName(NetworkPlayerInfo networkPlayerInfoIn) {
        return networkPlayerInfoIn.getDisplayName() != null
                ? networkPlayerInfoIn.getDisplayName().getString()
                : TextUtils.scrubCodes(networkPlayerInfoIn.getGameProfile().getName());
    }

    private GuiButton createButtonFor(Minecraft mcIn, int x, int y, NetworkPlayerInfo networkPlayerInfo, int index) {
        if (networkPlayerInfo == null) {
            return null;
        }

        String name = this.getPlayerName(networkPlayerInfo);
        return new GuiButton(index, x, y, 150, 20, name) {
            public void onClick(double p_onClick_1_, double p_onClick_3_) {
            }
        };
    }

    public Row getListEntry(int index) {
        return (Row) this.getChildren().get(index);
    }

    public int getListWidth() {
        return 400;
    }

    protected int getScrollBarX() {
        return super.getScrollBarX() + 32;
    }

    protected void sort() {
        final Collator collator = I18nUtils.getLocaleAwareCollator();
        Collections.sort(this.players, new Comparator<NetworkPlayerInfo>() {
            public int compare(NetworkPlayerInfo player1, NetworkPlayerInfo player2) {
                String name1 = GuiButtonRowListPlayers.this.getPlayerName(player1);
                String name2 = GuiButtonRowListPlayers.this.getPlayerName(player2);
                return collator.compare(name1, name2);
            }
        });
    }

    protected void updateFilter(String filterString) {
        this.playersFiltered = new ArrayList<>(this.players);
        Iterator<NetworkPlayerInfo> iterator = this.playersFiltered.iterator();

        while (iterator.hasNext()) {
            NetworkPlayerInfo networkPlayerInfo = iterator.next();
            String name = this.getPlayerName(networkPlayerInfo);
            if (!name.toLowerCase().contains(filterString)) {
                iterator.remove();
            }
        }

        this.clearEntries();
        this.addEntry(this.everyoneRow);

        for (int i = 0; i < this.playersFiltered.size(); i += 2) {
            NetworkPlayerInfo networkPlayerInfo1 = this.playersFiltered.get(i);
            NetworkPlayerInfo networkPlayerInfo2 = i < this.playersFiltered.size() - 1 ? this.playersFiltered.get(i + 1) : null;
            GuiButton guibutton1 = this.createButtonFor(this.mc, this.parentGui.getWidth() / 2 - 155, 0, networkPlayerInfo1, i);
            GuiButton guibutton2 = this.createButtonFor(this.mc, this.parentGui.getWidth() / 2 - 155 + 160, 0, networkPlayerInfo2, i + 1);
            this.addEntry(new Row(guibutton1, guibutton2));
        }
    }

    public void buttonClicked(int id) {
        if (id == -1) {
            this.parentGui.allClicked = true;
            String title = I18nUtils.getString("minimap.waypointshare.sharewitheveryone");
            String explanation = I18nUtils.getString("minimap.waypointshare.sharewitheveryone2");
            String affirm = I18nUtils.getString("gui.yes");
            String deny = I18nUtils.getString("gui.cancel");
            GuiYesNo confirmScreen = new GuiYesNo(this.parentGui, title, explanation, affirm, deny, 0);
            this.mc.displayGuiScreen(confirmScreen);
        } else {
            NetworkPlayerInfo networkPlayerInfo = this.playersFiltered.get(id);
            String name = this.getPlayerName(networkPlayerInfo);
            this.parentGui.sendMessageToPlayer(name);
        }
    }

    public class Row extends IGuiListEntry {
        private final Minecraft client = Minecraft.getInstance();
        private GuiButton button = null;
        private GuiButton button1 = null;
        private GuiButton button2 = null;

        public Row(GuiButton button) {
            this.button = button;
        }

        public Row(GuiButton button1, GuiButton button2) {
            this.button1 = button1;
            this.button2 = button2;
        }

        public void drawEntry(int slotIndex, int listWidth, int mouseX, int mouseY, boolean isSelected, float partialTicks) {
            int x = this.getX();
            int y = this.getY();
            this.drawButton(this.button, slotIndex, x, y, listWidth, GuiButtonRowListPlayers.this.slotHeight, mouseX, mouseY, isSelected, partialTicks);
            this.drawButton(this.button1, slotIndex, x, y, listWidth, GuiButtonRowListPlayers.this.slotHeight, mouseX, mouseY, isSelected, partialTicks);
            this.drawButton(this.button2, slotIndex, x, y, listWidth, GuiButtonRowListPlayers.this.slotHeight, mouseX, mouseY, isSelected, partialTicks);
        }

        private void drawButton(
                GuiButton button, int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float partialTicks
        ) {
            if (button != null) {
                button.y = y;
                button.render(mouseX, mouseY, partialTicks);
                if (button.id != -1) {
                    this.drawIconForButton(button);
                }

                if (button.isMouseOver() && mouseY >= GuiButtonRowListPlayers.this.top && mouseY <= GuiButtonRowListPlayers.this.bottom) {
                    String tooltip = I18nUtils.getString("minimap.waypointshare.sharewithname", button.displayString);
                    GuiSelectPlayer.setTooltip(GuiButtonRowListPlayers.this.parentGui, tooltip);
                }
            }
        }

        private void drawIconForButton(GuiButton button) {
            NetworkPlayerInfo networkPlayerInfo = GuiButtonRowListPlayers.this.playersFiltered.get(button.id);
            GameProfile gameProfile = networkPlayerInfo.getGameProfile();
            EntityPlayer entityPlayer = GuiButtonRowListPlayers.this.mc.world.getPlayerEntityByUUID(gameProfile.getId());
            GuiButtonRowListPlayers.this.mc.getTextureManager().bindTexture(networkPlayerInfo.getLocationSkin());
            Gui.drawScaledCustomSizeModalRect(button.x + 6, button.y + 6, 8.0F, 8.0F, 8, 8, 8, 8, 64.0F, 64.0F);
            if (entityPlayer != null && entityPlayer.isWearing(EnumPlayerModelParts.HAT)) {
                Gui.drawScaledCustomSizeModalRect(button.x + 6, button.y + 6, 40.0F, 8.0F, 8, 8, 8, 8, 64.0F, 64.0F);
            }
        }

        public boolean mouseClicked(double mouseX, double mouseY, int mouseEvent) {
            if (this.button != null && this.button.mouseClicked(mouseX, mouseY, mouseEvent)) {
                GuiButtonRowListPlayers.this.buttonClicked(this.button.id);
                return true;
            } else if (this.button1 != null && this.button1.mouseClicked(mouseX, mouseY, mouseEvent)) {
                GuiButtonRowListPlayers.this.buttonClicked(this.button1.id);
                return true;
            } else if (this.button2 != null && this.button2.mouseClicked(mouseX, mouseY, mouseEvent)) {
                GuiButtonRowListPlayers.this.buttonClicked(this.button2.id);
                return true;
            } else {
                return false;
            }
        }

        public boolean mouseReleased(double mouseX, double mouseY, int mouseEvent) {
            if (this.button != null) {
                this.button.mouseReleased(mouseX, mouseY, mouseEvent);
                return true;
            } else if (this.button1 != null) {
                this.button1.mouseReleased(mouseX, mouseY, mouseEvent);
                return true;
            } else if (this.button2 != null) {
                this.button2.mouseReleased(mouseX, mouseY, mouseEvent);
                return true;
            } else {
                return false;
            }
        }
    }
}
