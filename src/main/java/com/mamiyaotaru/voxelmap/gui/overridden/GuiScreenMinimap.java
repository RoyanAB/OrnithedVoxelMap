package com.mamiyaotaru.voxelmap.gui.overridden;

import com.mamiyaotaru.voxelmap.MapSettingsManager;
import com.mamiyaotaru.voxelmap.VoxelMap;
import com.mamiyaotaru.voxelmap.util.GLShim;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.text.LiteralText;

import java.util.List;

public class GuiScreenMinimap extends Screen {
    protected GuiScreenMinimap() {
        this(new LiteralText(""));
    }

    protected GuiScreenMinimap(LiteralText textComponent_1) {
        super(textComponent_1);
        this.blitOffset = 0;
    }

    public void drawMap() {
        if (!VoxelMap.instance.getMapOptions().showUnderMenus) {
            VoxelMap.instance.getMap().drawMinimap(this.minecraft);
            GLShim.glClear(256);
        }
    }

    public void removed() {
        MapSettingsManager.instance.saveAll();
    }

    public MinecraftClient getMinecraft() {
        return this.minecraft;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public List<AbstractButtonWidget> getButtonList() {
        return this.buttons;
    }

    public TextRenderer getFontRenderer() {
        return this.font;
    }
}
