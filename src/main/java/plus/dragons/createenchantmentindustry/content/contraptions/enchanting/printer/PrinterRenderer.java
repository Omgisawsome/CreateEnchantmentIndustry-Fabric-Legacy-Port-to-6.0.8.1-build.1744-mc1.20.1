package plus.dragons.createenchantmentindustry.content.contraptions.enchanting.printer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.simibubi.create.foundation.fluid.FluidRenderer;
import io.github.fabricators_of_create.porting_lib.fluids.FluidStack;
import net.createmod.catnip.render.SuperByteBuffer; // Corrected import for 0.6.x
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.Mth;

import plus.dragons.createenchantmentindustry.entry.CeiBlockPartials;

public class PrinterRenderer extends SmartBlockEntityRenderer<PrinterBlockEntity> {

	public PrinterRenderer(BlockEntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	protected void renderSafe(
			PrinterBlockEntity be,
			float partialTicks,
			PoseStack ms,
			MultiBufferSource buffer,
			int light,
			int overlay
	) {
		super.renderSafe(be, partialTicks, ms, buffer, light, overlay);

		// --- Fluid inside printer ---
		if (be.tank != null) {
			var primary = be.tank.getPrimaryTank();
			FluidStack fluid = primary.getRenderedFluid();
			float level = primary.getFluidLevel().getValue(partialTicks);

			if (!fluid.isEmpty() && level > 0) {
				level = Math.max(level, 0.175f);
				float min = 2.5f / 16f;
				float max = min + 11f / 16f;
				float yOffset = (11f / 16f) * level;

				ms.pushPose();
				ms.translate(0, yOffset, 0);

				FluidRenderer.renderFluidBox(
						fluid,
						min, min - yOffset,
						min, max, min, max,
						buffer, ms, light, false
				);

				ms.popPose();
			}
		}

		// --- Animation Squeeze ---
		int ticks = be.processingTicks;
		float progress = ticks - partialTicks;
		float squeeze = 0f;
		if (progress >= 0f) {
			if (progress <= 10f) squeeze = Mth.lerp(progress / 10f, 0, -1);
			else if (progress <= PrinterBlockEntity.COPYING_TIME - 10) squeeze = -1;
			else squeeze = Mth.lerp((PrinterBlockEntity.COPYING_TIME - progress) / 10f, 0, -1);
		}

		// --- Render tube partials ---
		// Note: CeiBlockPartials must return instances of the new SuperByteBuffer
		SuperByteBuffer top = CeiBlockPartials.PRINTER_TOP.renderInto(be.getBlockState());
		SuperByteBuffer middle = CeiBlockPartials.PRINTER_MIDDLE.renderInto(be.getBlockState());

		for (SuperByteBuffer partial : new SuperByteBuffer[] { top, middle }) {
			ms.pushPose();
			ms.translate(0, -3 * squeeze / 32f, 0);
			partial.light(light).renderInto(ms, buffer.getBuffer(RenderType.solid()));
			ms.popPose();
		}

		// --- Render bottom block partial ---
		SuperByteBuffer bottom = CeiBlockPartials.PRINTER_BOTTOM.renderInto(be.getBlockState());
		bottom.translate(0, squeeze / 2f, 0)
				.light(light)
				.renderInto(ms, buffer.getBuffer(RenderType.solid()));
	}
}
