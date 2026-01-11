package plus.dragons.createenchantmentindustry.compat.jei.category;

import com.mojang.math.Axis;
import com.simibubi.create.foundation.gui.CustomLightingSettings;

import mezz.jei.api.gui.drawable.IDrawable;
import net.minecraft.client.gui.GuiGraphics;
import plus.dragons.createenchantmentindustry.entry.CeiBlocks;

import static com.simibubi.create.compat.jei.category.animations.AnimatedKinetics.defaultBlockElement;

public class DisenchanterDrawable implements IDrawable {

	public static final CustomLightingSettings DEFAULT_LIGHTING =
			CustomLightingSettings.builder()
					.firstLightRotation(12.5f, 45.0f)
					.secondLightRotation(-20.0f, 50.0f)
					.build();

	@Override
	public int getWidth() {
		return 50;
	}

	@Override
	public int getHeight() {
		return 30;
	}

	@Override
	public void draw(GuiGraphics graphics, int xOffset, int yOffset) {
		var poseStack = graphics.pose();

		poseStack.pushPose();
		poseStack.translate(xOffset + 25, yOffset + 20, 100);
		poseStack.mulPose(Axis.XP.rotationDegrees(-15.5f));
		poseStack.mulPose(Axis.YP.rotationDegrees(22.5f));

		defaultBlockElement(CeiBlocks.DISENCHANTER.getDefaultState())
				.lighting(DEFAULT_LIGHTING)
				.scale(20)
				.render(graphics);

		poseStack.popPose();
	}
}
