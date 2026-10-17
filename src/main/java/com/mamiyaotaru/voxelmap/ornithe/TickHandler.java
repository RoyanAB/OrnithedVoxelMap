package com.mamiyaotaru.voxelmap.ornithe;

import com.mamiyaotaru.voxelmap.VoxelMap;
import com.mamiyaotaru.voxelmap.util.CommandUtils;
import com.mamiyaotaru.voxelmap.util.GLShim;
import com.mamiyaotaru.voxelmap.util.ReflectionUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.Timer;
import net.minecraft.util.text.ITextComponent;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public class TickHandler {
	private final VoxelMap voxelMap;
	private Timer timer = null;

	public TickHandler(VoxelMap voxelMap) {
		this.voxelMap = voxelMap;
	}

	public void onTick() {
		if (this.timer == null) {
			this.timer = (Timer) ReflectionUtils.getPrivateFieldValueByType(Minecraft.getMinecraft(), Minecraft.class, Timer.class);
		}

		boolean clock = this.timer.elapsedTicks > 0;
		this.voxelMap.onTick(Minecraft.getMinecraft(), clock);
	}

	public void onRenderOverlay() {
		this.voxelMap.onTickInGame(Minecraft.getMinecraft());
	}

	public void onRenderHand(float partialTicks) {
		this.voxelMap.getWaypointManager().renderWaypoints(partialTicks);
		GlStateManager.blendFunc(GLShim.GL11_GL_SRC_ALPHA, GLShim.GL11_GL_ONE_MINUS_SRC_ALPHA);
		GlStateManager.shadeModel(GLShim.GL11_GL_FLAT);
		GlStateManager.depthMask(true);
		GlStateManager.enableCull();
		GlStateManager.disableBlend();
		GlStateManager.disableFog();
		GlStateManager.disableLighting();
	}

	public void onChat(ITextComponent chat, CallbackInfo ci) {
		String message = chat.getFormattedText();
		if (!CommandUtils.checkForWaypoints(chat, message)) {
			ci.cancel();
		}
	}

	public void onSendChatMessage(String message, CallbackInfo ci) {
		if (message.startsWith("/newWaypoint")) {
			CommandUtils.waypointClicked(message);
			ci.cancel();
		} else if (message.startsWith("/ztp")) {
			CommandUtils.teleport(message);
			ci.cancel();
		}
	}
}
