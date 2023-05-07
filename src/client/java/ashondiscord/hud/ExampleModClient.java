package ashondiscord.hud;

import ashondiscord.hud.util.Combo;
import ashondiscord.hud.util.Hit;
import ashondiscord.hud.util.Hotkey;
import me.x150.MessageSubscription;
import me.x150.renderer.event.Events;
import me.x150.renderer.event.RenderEvent;
import me.x150.renderer.font.FontRenderer;
import me.x150.renderer.render.MSAAFramebuffer;

import me.x150.renderer.render.Renderer2d;
import net.fabricmc.api.ClientModInitializer;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.ArrayList;
import java.util.Objects;

public class ExampleModClient implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("ashondiscord-hud");
    public static FontRenderer FR;

    public static ArrayList<Long> lCps = new ArrayList<>();
    public static ArrayList<Long> rCps = new ArrayList<>();

    @Override
    public void onInitializeClient() {
        // This entrypoint is suitable for setting up client-specific logic, such as rendering.
        LOGGER.info("Hello Fabric world! (client)");
        Events.manager.registerSubscribers(this);
    }

    //#region Settings
    public static final Color heldColor = new Color(255, 255, 255, 255);
    public static final Color unheldColor = new Color(255, 255, 255, 255);
    public static final Color heldBG = new Color(255, 255, 255, 100);
    public static final Color unheldBG = new Color(0, 0, 0, 160);

    public static final float size = 20;
    public static final float gap = 2;
    public static final float radius = 5;
    public static final float padding = 5;
    public static final float margin = 5;
    public static int fontSize = 8;
    public static final int baseSize = 8;
    public static int accuracy = 1; // number of decimal places to round to
    //#endregion
    public static double deltaTime = 0;
    public static double lastTime = System.currentTimeMillis();

    public static ArrayList<Hotkey> prevHotkeys = new ArrayList<>();

    public static ArrayList<Hit> hitDistances = new ArrayList<>();
    public static ArrayList<Combo> comboCounter = new ArrayList<>();

    public static int getSize(int offset) {
        int width = MinecraftClient.getInstance().getWindow().getWidth();
//		int height = MinecraftClient.getInstance().getWindow().getHeight();

        if (width % (baseSize + offset) == 0) {
            LOGGER.info("Using font size: " + (baseSize + offset) + " | " + offset);
            return baseSize + offset;
        }
        if (baseSize - offset > 0 && width % (baseSize - offset) == 0) {
            LOGGER.info("Using font size: " + (baseSize - offset) + " | " + -offset);
            return baseSize - offset;
        }
        if (offset > 20) {
            LOGGER.warn("Could not find a font size that is an integer with your screen resolution, this may cause issues with rendering.");
            return baseSize;
        }
        return getSize(offset + 1);
    }

    @MessageSubscription
    void onHudRendered(RenderEvent.Hud hud) {
        // clear hits older than 1 second from arrayList<Hits> hitDistances
        long currentTime = System.currentTimeMillis();
        hitDistances.removeIf(hit -> currentTime - hit.time > 1000);
        // clear combos older than 5 seconds from arrayList<Combo> comboCounter
        comboCounter.removeIf(combo -> currentTime - combo.time > 5000); // will tweak it later

//		LOGGER.info(String.valueOf(deltaTime));
//		LOGGER.info(String.valueOf(lastTime));
        deltaTime = System.currentTimeMillis() - lastTime;
        lastTime = System.currentTimeMillis();

        if (FR == null) { // this is set in the onHudRendered method so the window exists and so an error is not thrown
            // TODO: make this run everytime the window is resized

            // Set window size to 1280x720 for play testing in the dev environment/showcasing
			MinecraftClient.getInstance().getWindow().setWindowedSize(1280, 720);

            // check if screen width / px is an integer
            if (MinecraftClient.getInstance().getWindow().getWidth() % baseSize != 0) {
                // try to find a font size that is an integer
                // EXPERIMENTAL FEATURE
                fontSize = getSize(0);
                if (MinecraftClient.getInstance().getWindow().getWidth() % fontSize == 0) {
                    FR = new FontRenderer(new Font[]{
                            new Font("JetBrainsMonoNL Nerd Font", Font.PLAIN, 8)
                    }, fontSize);
                    LOGGER.info("Using font size: " + fontSize);
                }
            } else {
                FR = new FontRenderer(new Font[]{
                        new Font("JetBrainsMonoNL Nerd Font", Font.PLAIN, 8)
                }, baseSize);
                LOGGER.info("Using font size: " + baseSize);
            }
        }

        // get client fps
        int fps = MinecraftClient.getInstance().fpsDebugString.split(" ")[0].equals("fps:") ? 0 : Integer.parseInt(MinecraftClient.getInstance().fpsDebugString.split(" ")[0]);

        // Get the user's hotkeys and their states
        Hotkey forward = Objects.requireNonNull(Hotkey.getHotkey("forwardKey", prevHotkeys.size() > 0 ? prevHotkeys.get(0) : null)); // walking forward | w
        Hotkey backward = Objects.requireNonNull(Hotkey.getHotkey("backKey", prevHotkeys.size() > 0 ? prevHotkeys.get(1) : null)); // walking backward | s
        Hotkey left = Objects.requireNonNull(Hotkey.getHotkey("leftKey", prevHotkeys.size() > 0 ? prevHotkeys.get(2) : null)); // walking left | a
        Hotkey right = Objects.requireNonNull(Hotkey.getHotkey("rightKey", prevHotkeys.size() > 0 ? prevHotkeys.get(3) : null)); // walking right | d
        Hotkey jump = Objects.requireNonNull(Hotkey.getHotkey("jumpKey", prevHotkeys.size() > 0 ? prevHotkeys.get(4) : null)); // jumping | space
        Hotkey attack = Objects.requireNonNull(Hotkey.getHotkey("attackKey", prevHotkeys.size() > 0 ? prevHotkeys.get(5) : null)); // attacking | left click/LMB
        Hotkey use = Objects.requireNonNull(Hotkey.getHotkey("useKey", prevHotkeys.size() > 0 ? prevHotkeys.get(6) : null)); // using | right click/RMB

//		check for clicks
        if (attack.pressed && !prevHotkeys.get(5).pressed) {
            lCps.add(System.currentTimeMillis());
        }
        // now for the right click
        if (use.pressed && !prevHotkeys.get(6).pressed) {
            rCps.add(System.currentTimeMillis());
        }
        lCps.removeIf(click -> click < System.currentTimeMillis() - 1000);
        rCps.removeIf(click -> click < System.currentTimeMillis() - 1000);

        // update the previous hotkeys
        if (prevHotkeys.size() == 0) {
            prevHotkeys.add(forward);
            prevHotkeys.add(backward);
            prevHotkeys.add(left);
            prevHotkeys.add(right);
            prevHotkeys.add(jump);
            prevHotkeys.add(attack);
            prevHotkeys.add(use);
        } else {
            prevHotkeys.set(0, forward);
            prevHotkeys.set(1, backward);
            prevHotkeys.set(2, left);
            prevHotkeys.set(3, right);
            prevHotkeys.set(4, jump);
            prevHotkeys.set(5, attack);
            prevHotkeys.set(6, use);
        }

        assert MinecraftClient.getInstance().player != null;
        PlayerListEntry entry = MinecraftClient.getInstance().player.networkHandler.getPlayerListEntry(MinecraftClient.getInstance().player.getUuid());
        int ping = entry != null ? entry.getLatency() : 0;

        // Get user's coordinates
        int precision = 1;
        assert MinecraftClient.getInstance().player != null;
        float x = (float) Math.round(MinecraftClient.getInstance().player.getX() * Math.pow(10, precision)) / (float) Math.pow(10, precision);
        float y = (float) Math.round(MinecraftClient.getInstance().player.getY() * Math.pow(10, precision)) / (float) Math.pow(10, precision);
        float z = (float) Math.round(MinecraftClient.getInstance().player.getZ() * Math.pow(10, precision)) / (float) Math.pow(10, precision);

        MSAAFramebuffer.use(4, () -> { // causes performance issues. 4 is lowest I can go without it looking bad
            // current solution is to kill Picom to give more gpu power to minecraft

            // FPS COUNTER
            Renderer2d.renderRoundedQuad(hud.getMatrixStack(), unheldBG, margin, margin, margin + size * 3 + gap * 2, margin + size, radius, 10);
            FR.drawCenteredString(hud.getMatrixStack(), "FPS: " + fps, (size * 3 + gap * 2) / 2 + margin, margin + padding, unheldColor.getRed() / 255f, unheldColor.getGreen() / 255f, unheldColor.getBlue() / 255f, unheldColor.getAlpha() / 255f);

            // KEYSTROKES
            // W
            Renderer2d.renderRoundedQuad(hud.getMatrixStack(), forward.bg, margin + size + gap, margin + size + gap, margin + size * 2 + gap, margin + size * 2 + gap, radius, 10);
            FR.drawCenteredString(hud.getMatrixStack(), forward.name, margin + size + gap + size / 2, margin + size + gap + padding, forward.getOxFG().r, forward.getOxFG().g, forward.getOxFG().b, forward.getOxFG().a);

            // A
            Renderer2d.renderRoundedQuad(hud.getMatrixStack(), left.bg, margin, margin + (size + gap) * 2, margin + size, margin + (size + gap) * 2 + size, radius, 10);
            FR.drawCenteredString(hud.getMatrixStack(), left.name, margin + size / 2, margin + (size + gap) * 2 + padding, left.getOxFG().r, left.getOxFG().g, left.getOxFG().b, left.getOxFG().a);

            // S
            Renderer2d.renderRoundedQuad(hud.getMatrixStack(), backward.bg, margin + size + gap, margin + (size + gap) * 2, margin + size * 2 + gap, margin + (size + gap) * 2 + size, radius, 10);
            FR.drawCenteredString(hud.getMatrixStack(), backward.name, margin + size + gap + size / 2, margin + (size + gap) * 2 + padding, backward.getOxFG().r, backward.getOxFG().g, backward.getOxFG().b, backward.getOxFG().a);

            // D
            Renderer2d.renderRoundedQuad(hud.getMatrixStack(), right.bg, margin + size * 2 + gap * 2, margin + (size + gap) * 2, margin + size * 3 + gap * 2, margin + (size + gap) * 2 + size, radius, 10);
            FR.drawCenteredString(hud.getMatrixStack(), right.name, margin + size * 2 + gap * 2 + size / 2, margin + (size + gap) * 2 + padding, right.getOxFG().r, right.getOxFG().g, right.getOxFG().b, right.getOxFG().a);

            // SPACE
            Renderer2d.renderRoundedQuad(hud.getMatrixStack(), jump.bg, margin, margin + (size + gap) * 3, margin + size * 3 + gap * 2, margin + (size + gap) * 3 + size, radius, 10);
            FR.drawCenteredString(hud.getMatrixStack(), jump.name, margin + (size * 3 + gap * 2) / 2, margin + (size + gap) * 3 + padding, jump.getOxFG().r, jump.getOxFG().g, jump.getOxFG().b, jump.getOxFG().a);

            // LMB
            Renderer2d.renderRoundedQuad(hud.getMatrixStack(), attack.bg, margin, margin + (size + gap) * 4, margin + size * 1.5, margin + (size + gap) * 4 + size, radius, 10);
            FR.drawCenteredString(hud.getMatrixStack(), attack.name, margin + size * 1.5f / 2, margin + (size + gap) * 4 + padding, attack.getOxFG().r, attack.getOxFG().g, attack.getOxFG().b, attack.getOxFG().a);

            // RMB
            Renderer2d.renderRoundedQuad(hud.getMatrixStack(), use.bg, margin + size * 1.5 + gap, margin + (size + gap) * 4, margin + size * 3 + gap * 2, margin + (size + gap) * 4 + size, radius, 10);
            FR.drawCenteredString(hud.getMatrixStack(), use.name, margin + size * 1.5f + gap + size * 1.5f / 2, margin + (size + gap) * 4 + padding, use.getOxFG().r, use.getOxFG().g, use.getOxFG().b, use.getOxFG().a);

            // "CPS: {lcps.size()} | {rcps.size()}" 3 wide
            Renderer2d.renderRoundedQuad(hud.getMatrixStack(), unheldBG, margin, margin + (size + gap) * 5, margin + size * 3 + gap * 2, margin + (size + gap) * 5 + size, radius, 10);
            FR.drawCenteredString(hud.getMatrixStack(), "CPS: " + lCps.size() + " | " + rCps.size(), (size * 3 + gap * 2) / 2 + margin, margin + (size + gap) * 5 + padding, unheldColor.getRed() / 255f, unheldColor.getGreen() / 255f, unheldColor.getBlue() / 255f, unheldColor.getAlpha() / 255f);

            // ping "{ping}ms" 3 wide
            Renderer2d.renderRoundedQuad(hud.getMatrixStack(), unheldBG, margin, margin + (size + gap) * 6, margin + size * 3 + gap * 2, margin + (size + gap) * 6 + size, radius, 10);
            FR.drawCenteredString(hud.getMatrixStack(), ping + "ms", (size * 3 + gap * 2) / 2 + margin, margin + (size + gap) * 6 + padding, unheldColor.getRed() / 255f, unheldColor.getGreen() / 255f, unheldColor.getBlue() / 255f, unheldColor.getAlpha() / 255f);

            // reach "hitDistances.size() >= 1 ? {reach}m : No Hit" 3 wide
            // reach is hitDistances.get(0);
            Renderer2d.renderRoundedQuad(hud.getMatrixStack(), unheldBG, margin, margin + (size + gap) * 7, margin + size * 3 + gap * 2, margin + (size + gap) * 7 + size, radius, 10);
            FR.drawCenteredString(hud.getMatrixStack(), hitDistances.size() >= 1 ? Math.round(hitDistances.get(0).distance * Math.pow(10, accuracy))/Math.pow(10, accuracy) + "m" : "No Hit", (size * 3 + gap * 2) / 2 + margin, margin + (size + gap) * 7 + padding, unheldColor.getRed() / 255f, unheldColor.getGreen() / 255f, unheldColor.getBlue() / 255f, unheldColor.getAlpha() / 255f);

            // combo "combo" 3 wide
            // "comboCounter.size() >= 1 ? comboCounter.get(0).count : No Combo"
            Renderer2d.renderRoundedQuad(hud.getMatrixStack(), unheldBG, margin, margin + (size + gap) * 8, margin + size * 3 + gap * 2, margin + (size + gap) * 8 + size, radius, 10);
            FR.drawCenteredString(hud.getMatrixStack(), comboCounter.size() >= 1 ? comboCounter.get(0).count + "x" : "No Combo", (size * 3 + gap * 2) / 2 + margin, margin + (size + gap) * 8 + padding, unheldColor.getRed() / 255f, unheldColor.getGreen() / 255f, unheldColor.getBlue() / 255f, unheldColor.getAlpha() / 255f);

            // COORDINATES (Bottom left)
            int height = MinecraftClient.getInstance().getWindow().getScaledHeight();

            // fixed width + centered text
//			Renderer2d.renderRoundedQuad(hud.getMatrixStack(), unheldBG, margin, height-margin-size, margin+size*6+gap*2, height-margin, radius, 10);
//			FR.drawCenteredString(hud.getMatrixStack(), "X: " + x + " Y: " + y + " Z: " + z, (size*6+gap*2)/2 + margin, height-margin-size+padding, unheldColor.getRed()/255f, unheldColor.getGreen()/255f, unheldColor.getBlue()/255f, unheldColor.getAlpha()/255f);

            // dynamic width + left aligned text
            String coords = "X: " + x + " Y: " + y + " Z: " + z;
            float width = FR.getStringWidth(coords);
            Renderer2d.renderRoundedQuad(hud.getMatrixStack(), unheldBG, margin, height - margin - size, margin + width + padding * 2, height - margin, radius, 10);
            FR.drawString(hud.getMatrixStack(), coords, margin + padding, height - margin - size + padding, unheldColor.getRed() / 255f, unheldColor.getGreen() / 255f, unheldColor.getBlue() / 255f, unheldColor.getAlpha() / 255f);
        });
    }
}