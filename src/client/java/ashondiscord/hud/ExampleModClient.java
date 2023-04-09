package ashondiscord.hud;

import me.x150.MessageSubscription;
import me.x150.renderer.event.Events;
import me.x150.renderer.event.RenderEvent;
import me.x150.renderer.font.FontRenderer;
import me.x150.renderer.render.MSAAFramebuffer;

import me.x150.renderer.render.Renderer2d;
import net.fabricmc.api.ClientModInitializer;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Objects;

public class ExampleModClient implements ClientModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("ashondiscord-hud");
	public static FontRenderer FR;

	@Override
	public void onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.
		LOGGER.info("Hello Fabric world! (client)");
		Events.manager.registerSubscribers(this);
	}

	//	public static final Color heldColor = new Color(255, 255, 255, 255);
	public static final Color heldColor = new Color(255, 255, 255, 255);
	//	public static final Color unheldColor = new Color(255, 255, 255, 192);
	public static final Color unheldColor = new Color(196, 196, 196, 255);
	//	public static final Color heldBG = new Color(0, 0, 0, 144);
	public static final Color heldBG = new Color(0, 0, 0, 255);
	//	public static final Color unheldBG = new Color(0, 0, 0, 96);
	public static final Color unheldBG = new Color(128, 128, 128, 128);

	public static final float size = 20;
	public static final float gap = 2;
	public static final float radius = 5;
	public static final float padding = 5;
	public static final float margin = 5;
	public static int fontSize = 8;
	public static final int baseSize = 8;

	public static ArrayList<Hotkey> prevHotkeys = new ArrayList<>();

	public static class Hotkey {
		public String name;
		public boolean pressed;
		public Color bg;
		public Color fg;
		public static int steps = 20;
		public static float rStep = (float) (heldBG.getRed() - unheldBG.getRed()) / steps;
		public static float gStep = (float) (heldBG.getGreen() - unheldBG.getGreen()) / steps;
		public static float bStep = (float) (heldBG.getBlue() - unheldBG.getBlue()) / steps;
		public static float aStep = (float) (heldBG.getAlpha() - unheldBG.getAlpha()) / steps;

		public Hotkey(String name, boolean pressed, Hotkey prev) {

			this.name = name;
			this.pressed = pressed;
//			this.bg = pressed ? heldBG : unheldBG;
//			this.fg = pressed ? heldColor : unheldColor;
			// ease the color change
			this.bg = pressed ? easeColor(heldBG, prev.bg, pressed) : easeColor(unheldBG, prev.bg, pressed);
//			this.fg = pressed ? easeColor(heldColor, prev.fg, pressed) : easeColor(unheldColor, prev.fg, pressed);
			this.fg = pressed ? heldColor : unheldColor;
		}

		public Hotkey(String name, boolean pressed) {
			this.name = name;
			this.pressed = pressed;
			this.bg = pressed ? heldBG : unheldBG;
			this.fg = pressed ? heldColor : unheldColor;
		}

		public static Color easeColor(Color target, Color prev, boolean pressed) {
			if (target.equals(prev)) return target;

			int newR = pressed ? prev.getRed() + (int) rStep : prev.getRed() - (int) rStep;
			int newG = pressed ? prev.getGreen() + (int) gStep : prev.getGreen() - (int) gStep;
			int newB = pressed ? prev.getBlue() + (int) bStep : prev.getBlue() - (int) bStep;
			int newA = pressed ? prev.getAlpha() + (int) aStep : prev.getAlpha() - (int) aStep;

			if (prev.getRed() == target.getRed()) {
				newR = target.getRed();
			}
			if (prev.getGreen() == target.getGreen()) {
				newG = target.getGreen();
			}
			if (prev.getBlue() == target.getBlue()) {
				newB = target.getBlue();
			}

			if (pressed) {
				if (rStep > 0 && newR > target.getRed()) newR = target.getRed();
				if (gStep > 0 && newG > target.getGreen()) newG = target.getGreen();
				if (bStep > 0 && newB > target.getBlue()) newB = target.getBlue();
				if (aStep > 0 && newA > target.getAlpha()) newA = target.getAlpha();
				if (rStep < 0 && newR < target.getRed()) newR = target.getRed();
				if (gStep < 0 && newG < target.getGreen()) newG = target.getGreen();
				if (bStep < 0 && newB < target.getBlue()) newB = target.getBlue();
				if (aStep < 0 && newA < target.getAlpha()) newA = target.getAlpha();
			} else {
				if (rStep < 0 && newR > target.getRed()) newR = target.getRed();
				if (gStep < 0 && newG > target.getGreen()) newG = target.getGreen();
				if (bStep < 0 && newB > target.getBlue()) newB = target.getBlue();
				if (aStep < 0 && newA > target.getAlpha()) newA = target.getAlpha();
				if (rStep > 0 && newR < target.getRed()) newR = target.getRed();
				if (gStep > 0 && newG < target.getGreen()) newG = target.getGreen();
				if (bStep > 0 && newB < target.getBlue()) newB = target.getBlue();
				if (aStep > 0 && newA < target.getAlpha()) newA = target.getAlpha();
			}

			if (newR > 255) newR = 255;
			if (newG > 255) newG = 255;
			if (newB > 255) newB = 255;
			if (newA > 255) newA = 255;
			if (newR < 0) newR = 0;
			if (newG < 0) newG = 0;
			if (newB < 0) newB = 0;
			if (newA < 0) newA = 0;

			LOGGER.info("new color: " + newR + ", " + newG + ", " + newB + ", " + newA);
			LOGGER.info("target color: " + target.getRed() + ", " + target.getGreen() + ", " + target.getBlue() + ", " + target.getAlpha());
			LOGGER.info("prev color: " + prev.getRed() + ", " + prev.getGreen() + ", " + prev.getBlue() + ", " + prev.getAlpha());
			LOGGER.info("rStep: " + rStep + ", gStep: " + gStep + ", bStep: " + bStep + ", aStep: " + aStep);

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
	}

	static class OxFG {
		public float r;
		public float g;
		public float b;
		public float a;

		public OxFG(float r, float g, float b, float a) {
			this.r = r;
			this.g = g;
			this.b = b;
			this.a = a;
		}
	}

	public Hotkey getHotkey(String key, Hotkey prev) {
		try {
//		String forward = MinecraftClient.getInstance().options.key.getBoundKeyLocalizedText().getString().toUpperCase();
			// replace key with the parameter key

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
		if (FR == null) { // this is set in the onHudRendered method so the window exists and so an error is not thrown
			// TODO: make this run everytime the window is resized

			// Set window size to 1280x720 for play testing in the dev environment/showcasing
//			MinecraftClient.getInstance().getWindow().setWindowedSize(1280, 720);

			// check if screen width / px is an integer
			if (MinecraftClient.getInstance().getWindow().getWidth() % baseSize != 0) {
				// try to find a font size that is an integer
				// EXPERIMENTAL FEATURE
				fontSize = getSize(0);
				if (MinecraftClient.getInstance().getWindow().getWidth() % fontSize == 0) {
					FR = new FontRenderer(new Font[] {
							new Font("JetBrainsMonoNL Nerd Font", Font.PLAIN, 8)
					}, fontSize);
					LOGGER.info("Using font size: " + fontSize);
				}
			} else {
				FR = new FontRenderer(new Font[] {
						new Font("JetBrainsMonoNL Nerd Font", Font.PLAIN, 8)
				}, baseSize);
				LOGGER.info("Using font size: " + baseSize);
			}
		}

		// get client fps
		int fps = MinecraftClient.getInstance().fpsDebugString.split(" ")[0].equals("fps:") ? 0 : Integer.parseInt(MinecraftClient.getInstance().fpsDebugString.split(" ")[0]);

		// Get the user's hotkeys and their states
		Hotkey forward = Objects.requireNonNull(getHotkey("forwardKey", prevHotkeys.size() > 0 ? prevHotkeys.get(0) : null)); // walking forward | w
		Hotkey backward = Objects.requireNonNull(getHotkey("backKey",  prevHotkeys.size() > 0 ? prevHotkeys.get(1) : null)); // walking backward | s
		Hotkey left = Objects.requireNonNull(getHotkey("leftKey",  prevHotkeys.size() > 0 ? prevHotkeys.get(2) : null)); // walking left | a
		Hotkey right = Objects.requireNonNull(getHotkey("rightKey",  prevHotkeys.size() > 0 ? prevHotkeys.get(3) : null)); // walking right | d
		Hotkey jump = Objects.requireNonNull(getHotkey("jumpKey",  prevHotkeys.size() > 0 ? prevHotkeys.get(4) : null)); // jumping | space
		Hotkey attack = Objects.requireNonNull(getHotkey("attackKey",  prevHotkeys.size() > 0 ? prevHotkeys.get(5) : null)); // attacking | left click/LMB
		Hotkey use = Objects.requireNonNull(getHotkey("useKey",  prevHotkeys.size() > 0 ? prevHotkeys.get(6) : null)); // using | right click/RMB

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

		// Get user's coordinates
		int precision = 1;
		assert MinecraftClient.getInstance().player != null;
		float x = (float) Math.round(MinecraftClient.getInstance().player.getX() * Math.pow(10, precision)) / (float) Math.pow(10, precision);
		float y = (float) Math.round(MinecraftClient.getInstance().player.getY() * Math.pow(10, precision)) / (float) Math.pow(10, precision);
		float z = (float) Math.round(MinecraftClient.getInstance().player.getZ() * Math.pow(10, precision)) / (float) Math.pow(10, precision);

		MSAAFramebuffer.use(4, () -> { // causes performance issues. 4 is lowest I can go without it looking bad
			// current solution is to kill Picom to give more gpu power to minecraft

			// FPS COUNTER
			Renderer2d.renderRoundedQuad(hud.getMatrixStack(), unheldBG, margin, margin, margin+size*3+gap*2, margin+size, radius, 10);
			FR.drawCenteredString(hud.getMatrixStack(), "FPS: " + fps, (size*3+gap*2)/2 + margin, margin+padding, unheldColor.getRed()/255f, unheldColor.getGreen()/255f, unheldColor.getBlue()/255f, unheldColor.getAlpha()/255f);

			// KEYSTROKES
			// W
			Renderer2d.renderRoundedQuad(hud.getMatrixStack(), forward.bg, margin+size+gap, margin+size+gap, margin+size*2+gap, margin+size*2+gap, radius, 10);
			FR.drawCenteredString(hud.getMatrixStack(), forward.name, margin+size+gap+size/2, margin+size+gap+padding, forward.getOxFG().r, forward.getOxFG().g, forward.getOxFG().b, forward.getOxFG().a);

			// A
			Renderer2d.renderRoundedQuad(hud.getMatrixStack(), left.bg, margin, margin+(size+gap)*2, margin+size, margin+(size+gap)*2+size, radius, 10);
			FR.drawCenteredString(hud.getMatrixStack(), left.name, margin+size/2, margin+(size+gap)*2+padding, left.getOxFG().r, left.getOxFG().g, left.getOxFG().b, left.getOxFG().a);

			// S
			Renderer2d.renderRoundedQuad(hud.getMatrixStack(), backward.bg, margin+size+gap, margin+(size+gap)*2, margin+size*2+gap, margin+(size+gap)*2+size, radius, 10);
			FR.drawCenteredString(hud.getMatrixStack(), backward.name, margin+size+gap+size/2, margin+(size+gap)*2+padding, backward.getOxFG().r, backward.getOxFG().g, backward.getOxFG().b, backward.getOxFG().a);

			// D
			Renderer2d.renderRoundedQuad(hud.getMatrixStack(), right.bg, margin+size*2+gap*2, margin+(size+gap)*2, margin+size*3+gap*2, margin+(size+gap)*2+size, radius, 10);
			FR.drawCenteredString(hud.getMatrixStack(), right.name, margin+size*2+gap*2+size/2, margin+(size+gap)*2+padding, right.getOxFG().r, right.getOxFG().g, right.getOxFG().b, right.getOxFG().a);

			// SPACE
			Renderer2d.renderRoundedQuad(hud.getMatrixStack(), jump.bg, margin, margin+(size+gap)*3, margin+size*3+gap*2, margin+(size+gap)*3+size, radius, 10);
			FR.drawCenteredString(hud.getMatrixStack(), jump.name, margin+(size*3+gap*2)/2, margin+(size+gap)*3+padding, jump.getOxFG().r, jump.getOxFG().g, jump.getOxFG().b, jump.getOxFG().a);

			// LMB
			Renderer2d.renderRoundedQuad(hud.getMatrixStack(), attack.bg, margin, margin+(size+gap)*4, margin+size*1.5, margin+(size+gap)*4+size, radius, 10);
			FR.drawCenteredString(hud.getMatrixStack(), attack.name, margin+size*1.5f/2, margin+(size+gap)*4+padding, attack.getOxFG().r, attack.getOxFG().g, attack.getOxFG().b, attack.getOxFG().a);

			// RMB
			Renderer2d.renderRoundedQuad(hud.getMatrixStack(), use.bg, margin+size*1.5+gap, margin+(size+gap)*4, margin+size*3+gap*2, margin+(size+gap)*4+size, radius, 10);
			FR.drawCenteredString(hud.getMatrixStack(), use.name, margin+size*1.5f+gap+size*1.5f/2, margin+(size+gap)*4+padding, use.getOxFG().r, use.getOxFG().g, use.getOxFG().b, use.getOxFG().a);

			// COORDINATES (Bottom left)
			int height = MinecraftClient.getInstance().getWindow().getScaledHeight();

			// fixed width + centered text
//			Renderer2d.renderRoundedQuad(hud.getMatrixStack(), unheldBG, margin, height-margin-size, margin+size*6+gap*2, height-margin, radius, 10);
//			FR.drawCenteredString(hud.getMatrixStack(), "X: " + x + " Y: " + y + " Z: " + z, (size*6+gap*2)/2 + margin, height-margin-size+padding, unheldColor.getRed()/255f, unheldColor.getGreen()/255f, unheldColor.getBlue()/255f, unheldColor.getAlpha()/255f);

			// dynamic width + left aligned text
			String coords = "X: " + x + " Y: " + y + " Z: " + z;
			float width = FR.getStringWidth(coords);
			Renderer2d.renderRoundedQuad(hud.getMatrixStack(), unheldBG, margin, height-margin-size, margin+width+padding*2, height-margin, radius, 10);
			FR.drawString(hud.getMatrixStack(), coords, margin+padding, height-margin-size+padding, unheldColor.getRed()/255f, unheldColor.getGreen()/255f, unheldColor.getBlue()/255f, unheldColor.getAlpha()/255f);
		});
	}
}