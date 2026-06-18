package ru.levin.modules.render;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector4f;
import ru.levin.ExosWare;
import ru.levin.events.Event;
import ru.levin.events.impl.render.EventRender2D;
import ru.levin.manager.ClientManager;
import ru.levin.manager.Manager;
import ru.levin.manager.dragManager.Dragging;
import ru.levin.manager.fontManager.FontUtils;
import ru.levin.modules.Function;
import ru.levin.modules.FunctionAnnotation;
import ru.levin.modules.Type;
import ru.levin.modules.setting.BooleanSetting;
import ru.levin.modules.setting.MultiSetting;
import ru.levin.util.color.ColorUtil;
import ru.levin.util.math.MathUtil;
import ru.levin.util.render.RenderAddon;
import ru.levin.util.render.RenderUtil;

import java.awt.*;
import java.util.Collection;

@FunctionAnnotation(name = "HUD", desc = "Xilka Style HUD with separate windows", type = Type.Render)
public class HUD extends Function {

    public final MultiSetting elements = new MultiSetting("Elements",
            java.util.Arrays.asList("Watermark", "TargetHUD", "KeyBinds", "ArmorHUD", "Speed", "Coords", "Effects"),
            new String[]{"Watermark", "TargetHUD", "KeyBinds", "ArmorHUD", "Speed", "Coords", "Effects"});

    private final BooleanSetting blur = new BooleanSetting("Blur", true);

    public final Dragging watermarkDrag = ExosWare.getInstance().createDrag(this, "Watermark", 10, 10);
    public final Dragging targetDrag = ExosWare.getInstance().createDrag(this, "TargetHUD", 10, 40);
    public final Dragging keybindDrag = ExosWare.getInstance().createDrag(this, "KeybindHUD", 10, 100);
    public final Dragging armorDrag = ExosWare.getInstance().createDrag(this, "ArmorHUD", 400, 450);
    public final Dragging speedDrag = ExosWare.getInstance().createDrag(this, "Speed", 10, 220);
    public final Dragging coordsDrag = ExosWare.getInstance().createDrag(this, "Coords", 10, 250);
    public final Dragging effectsDrag = ExosWare.getInstance().createDrag(this, "Effects", 10, 280);

    private float healthAnimation;

    public HUD() {
        addSettings(elements, blur);
        this.bind = 90; // Z
    }

    @Override
    public void onEvent(Event event) {
        if (!(event instanceof EventRender2D e)) return;
        if (mc.player == null || mc.world == null) return;
        if (elements.get("Watermark")) renderWatermark(e);
        if (elements.get("TargetHUD")) renderTargetHUD(e);
        if (elements.get("KeyBinds")) renderKeybinds(e);
        if (elements.get("ArmorHUD")) renderArmor(e);
        if (elements.get("Speed")) renderSpeed(e);
        if (elements.get("Coords")) renderCoords(e);
        if (elements.get("Effects")) renderEffects(e);
    }

    private void renderWatermark(EventRender2D e) {
        float x = watermarkDrag.getX(), y = watermarkDrag.getY();
        String text = "sqvirtik | " + ClientManager.getFps() + " fps | " + ClientManager.getPing() + " ms";
        float width = FontUtils.durman[15].getWidth(text) + 20;
        MatrixStack ms = e.getDrawContext().getMatrices();
        drawPanel(ms, x, y, width, 18);
        FontUtils.durman[15].drawLeftAligned(ms, text, x + 8, y + 5, -1);
        watermarkDrag.setWidth(width);
        watermarkDrag.setHeight(18);
    }

    private void renderTargetHUD(EventRender2D e) {
        LivingEntity target = Manager.FUNCTION_MANAGER.attackAura.target instanceof LivingEntity ? (LivingEntity) Manager.FUNCTION_MANAGER.attackAura.target : mc.player;
        float x = targetDrag.getX(), y = targetDrag.getY();
        float width = 135, height = 42;
        DrawContext ctx = e.getDrawContext();
        MatrixStack ms = ctx.getMatrices();
        drawPanel(ms, x, y, width, height);
        RenderAddon.drawHead(ms, target, x + 5, y + 5, 30, 5);
        String name = target.getName().getString();
        if (name.length() > 12) name = name.substring(0, 12);
        FontUtils.durman[16].drawLeftAligned(ms, name, x + 42, y + 7, -1);
        float health = MathHelper.clamp(target.getHealth() / target.getMaxHealth(), 0, 1);
        healthAnimation = MathUtil.fast(healthAnimation, health, 15);
        RenderUtil.drawRoundedRect(ms, x + 42, y + 27, 80, 5, 2, new Color(30, 30, 30, 255).getRGB());
        RenderUtil.drawRoundedRect(ms, x + 42, y + 27, 80 * healthAnimation, 5, 2, ColorUtil.getColorStyle(0));
        FontUtils.durman[13].drawLeftAligned(ms, (int) target.getHealth() + " HP", x + 42, y + 17, new Color(200, 200, 200).getRGB());
        targetDrag.setWidth(width);
        targetDrag.setHeight(height);
    }

    private void renderKeybinds(EventRender2D e) {
        float x = keybindDrag.getX(), y = keybindDrag.getY();
        float width = 105, offset = 0;
        MatrixStack ms = e.getDrawContext().getMatrices();
        drawPanel(ms, x, y, width, 18);
        FontUtils.durman[15].drawLeftAligned(ms, "Keybinds", x + 8, y + 5, -1);
        y += 20;
        for (Function f : Manager.FUNCTION_MANAGER.getFunctions()) {
            if (!f.state || f.bind == 0) continue;
            drawPanel(ms, x, y + offset, width, 16);
            FontUtils.durman[13].drawLeftAligned(ms, f.name, x + 6, y + 4 + offset, -1);
            String bind = "[" + ClientManager.getKey(f.bind) + "]";
            FontUtils.durman[13].drawRightAligned(ms, bind, x + width - 6, y + 4 + offset, ColorUtil.getColorStyle(0));
            offset += 18;
        }
        keybindDrag.setWidth(width);
        keybindDrag.setHeight(offset + 20);
    }

    private void renderArmor(EventRender2D e) {
        float x = armorDrag.getX(), y = armorDrag.getY();
        drawPanel(e.getDrawContext().getMatrices(), x, y, 82, 20);
        int offset = 0;
        for (int i = 3; i >= 0; i--) {
            var stack = mc.player.getInventory().armor.get(i);
            if (stack.isEmpty()) continue;
            e.getDrawContext().drawItem(stack, (int) x + 4 + offset, (int) y + 2);
            offset += 20;
        }
        armorDrag.setWidth(82);
        armorDrag.setHeight(20);
    }

    private void renderSpeed(EventRender2D e) {
        float x = speedDrag.getX(), y = speedDrag.getY();
        Vec3d vel = mc.player.getVelocity();
        String text = String.format("Speed: %.2f m/s", Math.sqrt(vel.x * vel.x + vel.y * vel.y + vel.z * vel.z));
        float pad = 6, w = FontUtils.durman[15].getWidth(text) + pad * 2;
        MatrixStack ms = e.getDrawContext().getMatrices();
        drawPanel(ms, x, y, w, 18);
        FontUtils.durman[15].drawLeftAligned(ms, text, x + pad, y + 5, 0xFFFFFF);
        speedDrag.setWidth(w);
        speedDrag.setHeight(18);
    }

    private void renderCoords(EventRender2D e) {
        float x = coordsDrag.getX(), y = coordsDrag.getY();
        Vec3d pos = mc.player.getPos();
        String text = String.format("XYZ: %.1f / %.1f / %.1f", pos.x, pos.y, pos.z);
        float pad = 6, w = FontUtils.durman[15].getWidth(text) + pad * 2;
        MatrixStack ms = e.getDrawContext().getMatrices();
        drawPanel(ms, x, y, w, 18);
        FontUtils.durman[15].drawLeftAligned(ms, text, x + pad, y + 5, 0xFFFFFF);
        coordsDrag.setWidth(w);
        coordsDrag.setHeight(18);
    }

    private void renderEffects(EventRender2D e) {
        Collection<StatusEffectInstance> effects = mc.player.getStatusEffects();
        if (effects.isEmpty()) return;
        float x = effectsDrag.getX(), y = effectsDrag.getY();
        StringBuilder sb = new StringBuilder();
        for (StatusEffectInstance eff : effects) {
            sb.append(Text.translatable(eff.getEffectType().getTranslationKey()).getString()).append(": ").append(eff.getDuration() / 20).append("s\n");
        }
        String[] lines = sb.toString().split("\n");
        float pad = 6, maxW = 0;
        for (String line : lines) maxW = Math.max(maxW, FontUtils.durman[15].getWidth(line));
        float w = maxW + pad * 2, h = lines.length * 12 + pad * 2;
        MatrixStack ms = e.getDrawContext().getMatrices();
        drawPanel(ms, x, y, w, h);
        float ty = y + pad;
        for (String line : lines) {
            FontUtils.durman[15].drawLeftAligned(ms, line, x + pad, ty, 0xFFFFFF);
            ty += 12;
        }
        effectsDrag.setWidth(w);
        effectsDrag.setHeight(h);
    }

    private void drawPanel(MatrixStack ms, float x, float y, float w, float h) {
        if (blur.get()) RenderUtil.drawBlur(ms, x, y, w, h, new Vector4f(5, 5, 5, 5), 15, Color.WHITE.getRGB());
        RenderUtil.drawRoundedRect(ms, x, y, w, h, 5, new Color(15, 15, 15, 170).getRGB());
    }
}
