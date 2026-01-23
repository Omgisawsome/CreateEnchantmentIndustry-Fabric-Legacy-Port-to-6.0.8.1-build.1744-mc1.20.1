package plus.dragons.createenchantmentindustry.foundation.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import plus.dragons.createenchantmentindustry.EnchantmentIndustry;

public class CeiGuiTextures {
	public static final ResourceLocation ENCHANTING_GUIDE_LOCATION =
			EnchantmentIndustry.genRL("textures/gui/enchanting_guide.png");

	// Original dimensions from your file: 185x48
	// Note: If your actual PNG is larger (like the 188x92 you used in the Screen class),
	// use those dimensions instead.
	public static final int WIDTH = 188;
	public static final int HEIGHT = 92;

	public static void render(GuiGraphics graphics, int x, int y) {
		graphics.blit(ENCHANTING_GUIDE_LOCATION, x, y, 0, 0, WIDTH, HEIGHT, 256, 256);
	}
}
