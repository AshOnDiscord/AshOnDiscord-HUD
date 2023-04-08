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
import org.apache.commons.logging.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.lang.reflect.Field;
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

	public static Color heldColor = new Color(255, 255, 255, 255);
	public static Color unheldColor = new Color(255, 255, 255, 192);
	public static Color heldBG = new Color(0, 0, 0, 144);
	public static Color unheldBG = new Color(0, 0, 0, 96);

	public static float size = 20;
	public static float gap = 2;
	public static float radius = 5;
	public static float padding = 5;
	public static float margin = 5;
	public static int fontSize = 8;
	public static int baseSize = 8;
	static class Hotkey {
		public String name;
		public boolean pressed;
		public Color bg;
		public Color fg;
		public Hotkey(String name, boolean pressed) {
			this.name = name;
			this.pressed = pressed;
			this.bg = pressed ? heldBG : unheldBG;
			this.fg = pressed ? heldColor : unheldColor;
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

	public Hotkey getHotkey(String key) {
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
			return new Hotkey(name, pressed);
		} catch (Exception e) {
			LOGGER.error("Error getting hotkey(" + key + "): " + e.getMessage());
			e.printStackTrace();
			return null;
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

		if (FR == null) {
			// Set window size to 1280x720
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

			// check if screen width / px is an integer

		}


//

		// get client fps
		int fps = MinecraftClient.getInstance().fpsDebugString.split(" ")[0].equals("fps:") ? 0 : Integer.parseInt(MinecraftClient.getInstance().fpsDebugString.split(" ")[0]);

		// Get the user's hotkeys and their states
		Hotkey forward = Objects.requireNonNull(getHotkey("forwardKey")); // walking forward | w
		Hotkey backward = Objects.requireNonNull(getHotkey("backKey")); // walking backward | s
		Hotkey left = Objects.requireNonNull(getHotkey("leftKey")); // walking left | a
		Hotkey right = Objects.requireNonNull(getHotkey("rightKey")); // walking right | d
		Hotkey jump = Objects.requireNonNull(getHotkey("jumpKey")); // jumping | space
		Hotkey attack = Objects.requireNonNull(getHotkey("attackKey")); // attacking | left click/LMB
		Hotkey use = Objects.requireNonNull(getHotkey("useKey")); // using | right click/RMB

		MSAAFramebuffer.use(8, () -> {
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
		});
	}
}