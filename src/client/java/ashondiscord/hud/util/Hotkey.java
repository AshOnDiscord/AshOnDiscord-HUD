package ashondiscord.hud.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;

import java.awt.*;
import java.lang.reflect.Field;

import static ashondiscord.hud.ExampleModClient.*;

public class Hotkey {
    public String name;
    public boolean pressed;
    public Color bg;
    public Color fg;
    public static float seconds = 0.125f;
//		public static int steps = 20;
//		public static float rStep = (float) (heldBG.getRed() - unheldBG.getRed()) / steps;
//		public static float gStep = (float) (heldBG.getGreen() - unheldBG.getGreen()) / steps;
//		public static float bStep = (float) (heldBG.getBlue() - unheldBG.getBlue()) / steps;
//		public static float aStep = (float) (heldBG.getAlpha() - unheldBG.getAlpha()) / steps;

    public Hotkey(String name, boolean pressed, Hotkey prev) {

        this.name = name;
        this.pressed = pressed;
//			this.bg = pressed ? heldBG : unheldBG;
//			this.fg = pressed ? heldColor : unheldColor;
        // ease the color change
        this.bg = pressed ? easeColor(heldBG, prev.bg, unheldBG) : easeColor(unheldBG, prev.bg, heldBG);
        this.fg = pressed ? easeColor(heldColor, prev.fg, unheldColor) : easeColor(unheldColor, prev.fg, heldColor);
//			this.fg = pressed ? heldColor : unheldColor;
    }

    public Hotkey(String name, boolean pressed) {
        this.name = name;
        this.pressed = pressed;
        this.bg = pressed ? heldBG : unheldBG;
        this.fg = pressed ? heldColor : unheldColor;
    }

    public static Color easeColor(Color target, Color prev, Color from) {
        if (target.equals(prev)) return target;

        // use deltaTime and seconds to calculate the step. deltaTime is in ms
        double rStep = (float) (target.getRed() - from.getRed()) / (seconds * 1000 / deltaTime);
        double gStep = (float) (target.getGreen() - from.getGreen()) / (seconds * 1000 / deltaTime);
        double bStep = (float) (target.getBlue() - from.getBlue()) / (seconds * 1000 / deltaTime);
        double aStep = (float) (target.getAlpha() - from.getAlpha()) / (seconds * 1000 / deltaTime);

//			LOGGER.info(String.valueOf(deltaTime));

        int newR = prev.getRed() + (int) rStep;
        int newG = prev.getGreen() + (int) gStep;
        int newB = prev.getBlue() + (int) bStep;
        int newA = prev.getAlpha() + (int) aStep;

        if (prev.getRed() == target.getRed()) {
            newR = target.getRed();
        }
        if (prev.getGreen() == target.getGreen()) {
            newG = target.getGreen();
        }
        if (prev.getBlue() == target.getBlue()) {
            newB = target.getBlue();
        }

        if (rStep > 0 && newR > target.getRed()) newR = target.getRed();
        if (gStep > 0 && newG > target.getGreen()) newG = target.getGreen();
        if (bStep > 0 && newB > target.getBlue()) newB = target.getBlue();
        if (aStep > 0 && newA > target.getAlpha()) newA = target.getAlpha();
        if (rStep < 0 && newR < target.getRed()) newR = target.getRed();
        if (gStep < 0 && newG < target.getGreen()) newG = target.getGreen();
        if (bStep < 0 && newB < target.getBlue()) newB = target.getBlue();
        if (aStep < 0 && newA < target.getAlpha()) newA = target.getAlpha();

        if (newR > 255) newR = 255;
        if (newG > 255) newG = 255;
        if (newB > 255) newB = 255;
        if (newA > 255) newA = 255;
        if (newR < 0) newR = 0;
        if (newG < 0) newG = 0;
        if (newB < 0) newB = 0;
        if (newA < 0) newA = 0;

//			LOGGER.info(newR + " | " + newG + " | " + newB + " | " + newA);

//			LOGGER.info("new color: " + newR + ", " + newG + ", " + newB + ", " + newA);
//			LOGGER.info("target color: " + target.getRed() + ", " + target.getGreen() + ", " + target.getBlue() + ", " + target.getAlpha());
//			LOGGER.info("prev color: " + prev.getRed() + ", " + prev.getGreen() + ", " + prev.getBlue() + ", " + prev.getAlpha());
//			LOGGER.info("rStep: " + rStep + ", gStep: " + gStep + ", bStep: " + bStep + ", aStep: " + aStep);

        return new Color(newR, newG, newB, newA);
    }

    public OxFG getOxFG() {
        return new OxFG(
                fg.getRed() / 255f,
                fg.getGreen() / 255f,
                fg.getBlue() / 255f,
                fg.getAlpha() / 255f
        );
    }

    public static Hotkey getHotkey(String key, Hotkey prev) {

        try {
            MinecraftClient mc = MinecraftClient.getInstance();
            Class<? extends GameOptions> cls = mc.options.getClass();
            Field field = cls.getDeclaredField(key);
            field.setAccessible(true);
            KeyBinding keyBinding = (KeyBinding) field.get(mc.options);
            String name = keyBinding.getBoundKeyLocalizedText().getString().toUpperCase();

            boolean pressed = keyBinding.isPressed();
            if (name.equals("LEFT BUTTON")) {
                name = "LMB";
            } else if (name.equals("RIGHT BUTTON")) {
                name = "RMB";
            }

            if (prev == null) {
                return new Hotkey(name, pressed);
            }
            return new Hotkey(name, pressed, prev);
        } catch (Exception e) {
            LOGGER.error("Error getting hotkey(" + key + "): " + e.getMessage());
            e.printStackTrace();

            // FIXME: this is a temporary fix for when the field is not found on certain environments
            // While this is technically probably more performant than using a reflection, there is a lot of code duplication.
            // It is not very maintainable as when a new key is added, it must be added to both places.
            KeyBinding keyBinding = switch (key) {
                case "forwardKey" -> MinecraftClient.getInstance().options.forwardKey;
                case "backKey" -> MinecraftClient.getInstance().options.backKey;
                case "leftKey" -> MinecraftClient.getInstance().options.leftKey;
                case "rightKey" -> MinecraftClient.getInstance().options.rightKey;
                case "jumpKey" -> MinecraftClient.getInstance().options.jumpKey;
                case "sneakKey" -> MinecraftClient.getInstance().options.sneakKey;
                case "sprintKey" -> MinecraftClient.getInstance().options.sprintKey;
                case "attackKey" -> MinecraftClient.getInstance().options.attackKey;
                case "useKey" -> MinecraftClient.getInstance().options.useKey;
                default -> null;
            };
            if (keyBinding == null) {
                LOGGER.error("Error getting hotkey(" + key + "): keyBinding is null");
                return null;
            }
            String name = keyBinding.getBoundKeyLocalizedText().getString().toUpperCase();
            if (name.equals("LEFT BUTTON")) {
                name = "LMB";
            } else if (name.equals("RIGHT BUTTON")) {
                name = "RMB";
            }
            if (prev == null) {
                return new Hotkey(name, keyBinding.isPressed());
            }
            return new Hotkey(name, keyBinding.isPressed(), prev);
        }
    }
}

