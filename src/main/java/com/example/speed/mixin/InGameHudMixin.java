package com.example.speed.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;

@Mixin(InGameHud.class)
public class InGameHudMixin {

    // Позиции элементов (можно будет сохранять в файл)
    private float watermarkX = 10, watermarkY = 10;
    private float armorX = 400, armorY = 450;
    private float infoX = 10, infoY = 100;

    @Inject(method = "render", at = @At("TAIL"))
    private void onRender(DrawContext context, float tickDelta, CallbackInfo ci) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;

        renderWatermark(context, mc);
        renderArmor(context, mc);
        renderInfo(context, mc);
    }

    private void renderWatermark(DrawContext context, MinecraftClient mc) {
        int fps = (int) mc.getCurrentFps();
        int ping = 0;
        PlayerListEntry entry = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
        if (entry != null) {
            ping = entry.getLatency();
        }

        String text = "Asik Client | " + fps + " fps | " + ping + " ms";
        int x = (int) watermarkX;
        int y = (int) watermarkY;
        int textWidth = mc.textRenderer.getWidth(text);
        int width = textWidth + 20;
        int height = 18;

        // Фон
        context.fill(x, y, x + width, y + height, new Color(15, 15, 15, 170).getRGB());
        // Текст
        context.drawTextWithShadow(mc.textRenderer, Text.literal(text), x + 8, y + 5, 0xFFFFFFFF);
    }

    private void renderArmor(DrawContext context, MinecraftClient mc) {
        int x = (int) armorX;
        int y = (int) armorY;

        context.fill(x, y, x + 82, y + 20, new Color(15, 15, 15, 170).getRGB());

        int offset = 0;
        for (int i = 3; i >= 0; i--) {
            ItemStack stack = mc.player.getInventory().armor.get(i);
            if (stack.isEmpty()) continue;
            context.drawItem(stack, x + 4 + offset, y + 2);
            offset += 20;
        }
    }

    private void renderInfo(DrawContext context, MinecraftClient mc) {
        int x = (int) infoX;
        int y = (int) infoY;

        int posX = (int) mc.player.getX();
        int posY = (int) mc.player.getY();
        int posZ = (int) mc.player.getZ();
        long time = mc.world.getTimeOfDay() / 1000;
        String text = "X: " + posX + " Y: " + posY + " Z: " + posZ + " | Time: " + time + "h";

        int textWidth = mc.textRenderer.getWidth(text);
        int width = textWidth + 20;
        int height = 18;

        context.fill(x, y, x + width, y + height, new Color(15, 15, 15, 170).getRGB());
        context.drawTextWithShadow(mc.textRenderer, Text.literal(text), x + 8, y + 5, 0xFFFFFFFF);
    }
}
