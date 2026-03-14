package com.mamiyaotaru.voxelmap.gui;

import com.mamiyaotaru.voxelmap.util.I18nUtils;
import com.mamiyaotaru.voxelmap.util.TextUtils;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.render.entity.PlayerModelPart;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.LiteralText;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

public class GuiButtonRowListPlayers extends EntryListWidget {
    final GuiSelectPlayer parentGui;
    private final MinecraftClient client = MinecraftClient.getInstance();
    GuiButtonRowListPlayers.Row everyoneRow;
    private final ArrayList<PlayerListEntry> players;
    private ArrayList<PlayerListEntry> playersFiltered;

    public GuiButtonRowListPlayers(GuiSelectPlayer par1GuiSelectPlayer) {
        super(MinecraftClient.getInstance(), par1GuiSelectPlayer.getWidth(), par1GuiSelectPlayer.getHeight(), 89, par1GuiSelectPlayer.getHeight() - 65 + 4, 25);
        this.parentGui = par1GuiSelectPlayer;
        ClientPlayNetworkHandler netHandlerPlayClient = MinecraftClient.getInstance().player.networkHandler;
        this.players = new ArrayList<>(netHandlerPlayClient.getPlayerList());
        this.sort();
        ButtonWidget everyoneButton = new ButtonWidget(this.parentGui.getWidth() / 2 - 75, 0, 150, 20, I18nUtils.getString("minimap.waypointshare.all"), null) {
            public void onPress() {
            }
        };
        this.everyoneRow = new GuiButtonRowListPlayers.Row(everyoneButton, -1);
        this.updateFilter("");
    }

    public String getPlayerName(PlayerListEntry ScoreboardEntryIn) {
        return ScoreboardEntryIn.getDisplayName() != null
                ? ScoreboardEntryIn.getDisplayName().getString()
                : TextUtils.scrubCodes(ScoreboardEntryIn.getProfile().getName());
    }

    private ButtonWidget createButtonFor(MinecraftClient mcIn, int x, int y, PlayerListEntry ScoreboardEntry) {
        if (ScoreboardEntry == null) {
            return null;
        }

        String name = this.getPlayerName(ScoreboardEntry);
        return new ButtonWidget(x, y, 150, 20, name, null) {
            public void onPress() {
            }
        };
    }

    public GuiButtonRowListPlayers.Row getListEntry(int index) {
        return this.getListEntry(index);
    }

    public int getRowWidth() {
        return 400;
    }

    protected int getScrollbarPosition() {
        return super.getScrollbarPosition() + 32;
    }

    protected void sort() {
        final Collator collator = I18nUtils.getLocaleAwareCollator();
        Collections.sort(this.players, new Comparator<PlayerListEntry>() {
            public int compare(PlayerListEntry player1, PlayerListEntry player2) {
                String name1 = GuiButtonRowListPlayers.this.getPlayerName(player1);
                String name2 = GuiButtonRowListPlayers.this.getPlayerName(player2);
                return collator.compare(name1, name2);
            }
        });
    }

    protected void updateFilter(String filterString) {
        this.playersFiltered = new ArrayList<>(this.players);
        Iterator<PlayerListEntry> iterator = this.playersFiltered.iterator();

        while (iterator.hasNext()) {
            PlayerListEntry ScoreboardEntry = iterator.next();
            String name = this.getPlayerName(ScoreboardEntry);
            if (!name.toLowerCase().contains(filterString)) {
                iterator.remove();
            }
        }

        this.clearEntries();
        this.addEntry(this.everyoneRow);

        for (int i = 0; i < this.playersFiltered.size(); i += 2) {
            PlayerListEntry ScoreboardEntry1 = this.playersFiltered.get(i);
            PlayerListEntry ScoreboardEntry2 = i < this.playersFiltered.size() - 1 ? this.playersFiltered.get(i + 1) : null;
            ButtonWidget guibutton1 = this.createButtonFor(this.client, this.parentGui.getWidth() / 2 - 155, 0, ScoreboardEntry1);
            ButtonWidget guibutton2 = this.createButtonFor(this.client, this.parentGui.getWidth() / 2 - 155 + 160, 0, ScoreboardEntry2);
            this.addEntry(new GuiButtonRowListPlayers.Row(guibutton1, i, guibutton2, i + 1));
        }
    }

    public void buttonClicked(int id) {
        if (id == -1) {
            this.parentGui.allClicked = true;
            LiteralText title = new LiteralText(I18nUtils.getString("minimap.waypointshare.sharewitheveryone"));
            LiteralText explanation = new LiteralText(I18nUtils.getString("minimap.waypointshare.sharewitheveryone2"));
            String affirm = I18nUtils.getString("gui.yes");
            String deny = I18nUtils.getString("gui.cancel");
            ConfirmScreen confirmScreen = new ConfirmScreen(this.parentGui, title, explanation, affirm, deny);
            this.client.openScreen(confirmScreen);
        } else {
            PlayerListEntry ScoreboardEntry = this.playersFiltered.get(id);
            String name = this.getPlayerName(ScoreboardEntry);
            this.parentGui.sendMessageToPlayer(name);
        }
    }

    public class Row extends Entry {
        private final MinecraftClient client = MinecraftClient.getInstance();
        private ButtonWidget button = null;
        private ButtonWidget button1 = null;
        private ButtonWidget button2 = null;
        private int id = 0;
        private int id1 = 0;
        private int id2 = 0;

        public Row(ButtonWidget button, int id) {
            this.button = button;
            this.id = id;
        }

        public Row(ButtonWidget button1, int id1, ButtonWidget button2, int id2) {
            this.button1 = button1;
            this.id1 = id1;
            this.button2 = button2;
            this.id2 = id2;
        }

        public void render(int slotIndex, int y, int x, int listWidth, int itemHeight, int mouseX, int mouseY, boolean isSelected, float partialTicks) {
            this.drawButton(this.button, this.id, slotIndex, x, y, listWidth, itemHeight, mouseX, mouseY, isSelected, partialTicks);
            this.drawButton(this.button1, this.id1, slotIndex, x, y, listWidth, itemHeight, mouseX, mouseY, isSelected, partialTicks);
            this.drawButton(this.button2, this.id2, slotIndex, x, y, listWidth, itemHeight, mouseX, mouseY, isSelected, partialTicks);
        }

        private void drawButton(
                ButtonWidget button,
                int id,
                int slotIndex,
                int x,
                int y,
                int listWidth,
                int slotHeight,
                int mouseX,
                int mouseY,
                boolean isSelected,
                float partialTicks
        ) {
            if (button != null) {
                button.y = y;
                button.render(mouseX, mouseY, partialTicks);
                if (id != -1) {
                    this.drawIconForButton(button, id);
                }

                if (button.isHovered() && mouseY >= GuiButtonRowListPlayers.this.top && mouseY <= GuiButtonRowListPlayers.this.bottom) {
                    String tooltip = I18nUtils.getString("minimap.waypointshare.sharewithname", button.getMessage());
                    GuiSelectPlayer.setTooltip(GuiButtonRowListPlayers.this.parentGui, tooltip);
                }
            }
        }

        private void drawIconForButton(ButtonWidget button, int id) {
            PlayerListEntry networkPlayerInfo = GuiButtonRowListPlayers.this.playersFiltered.get(id);
            GameProfile gameProfile = networkPlayerInfo.getProfile();
            PlayerEntity entityPlayer = this.client.world.getPlayerByUuid(gameProfile.getId());
            this.client.getTextureManager().bindTexture(networkPlayerInfo.getSkinTexture());
            Screen.blit(button.x + 6, button.y + 6, 8, 8, 8.0F, 8.0F, 8, 8, 64, 64);
            if (entityPlayer != null && entityPlayer.isPartVisible(PlayerModelPart.HAT)) {
                Screen.blit(button.x + 6, button.y + 6, 8, 8, 40.0F, 8.0F, 8, 8, 64, 64);
            }
        }

        public boolean mouseClicked(double mouseX, double mouseY, int mouseEvent) {
            if (this.button != null && this.button.mouseClicked(mouseX, mouseY, mouseEvent)) {
                GuiButtonRowListPlayers.this.buttonClicked(this.id);
                return true;
            } else if (this.button1 != null && this.button1.mouseClicked(mouseX, mouseY, mouseEvent)) {
                GuiButtonRowListPlayers.this.buttonClicked(this.id1);
                return true;
            } else if (this.button2 != null && this.button2.mouseClicked(mouseX, mouseY, mouseEvent)) {
                GuiButtonRowListPlayers.this.buttonClicked(this.id2);
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
