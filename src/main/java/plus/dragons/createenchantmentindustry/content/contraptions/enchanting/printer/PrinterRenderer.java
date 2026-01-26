package plus.dragons.createenchantmentindustry.content.contraptions.enchanting.printer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import com.simibubi.create.foundation.blockEntity.renderer.SmartBlockEntityRenderer;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import io.github.fabricators_of_create.porting_lib.fluids.FluidStack;
import net.createmod.catnip.platform.CatnipServices;
import net.createmod.catnip.render.CachedBuffers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import plus.dragons.createenchantmentindustry.entry.CeiBlockPartials;

public class PrinterRenderer extends SmartBlockEntityRenderer<PrinterBlockEntity> {
	public PrinterRenderer(BlockEntityRendererProvider.Context context) {
		super(context);
	}

	private static final PartialModel[] TUBE = {CeiBlockPartials.PRINTER_TOP, CeiBlockPartials.PRINTER_MIDDLE};

	@Override
	protected void renderSafe(PrinterBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
		super.renderSafe(be, partialTicks, ms, buffer, light, overlay);

		// 1. Calculate Animation Squeeze FIRST
		float squeeze = 0;
		int processingTicks = be.processingTicks;
		float processingPT = processingTicks - partialTicks;

		if (processingPT < 0) {
			squeeze = 0;
		} else if (processingPT <= 10) {
			squeeze = Mth.lerp(processingPT / 10f, 0, -1);
		} else if (processingPT <= 90) {
			squeeze = -1;
		} else if (processingPT <= 100) {
			squeeze = Mth.lerp((100 - processingPT) / 10f, 0, -1);
		}

		// Calculate the vertical offset for the head (and fluid)
		float headOffset = -3 * squeeze / 32f;

		// 2. Render Fluid
		SmartFluidTankBehaviour tank = be.tank;
		if (tank != null) {
			SmartFluidTankBehaviour.TankSegment primaryTank = tank.getPrimaryTank();
			FluidStack fluidStack = primaryTank.getRenderedFluid();
			float level = primaryTank.getFluidLevel().getValue(partialTicks);

			if (!fluidStack.isEmpty() && level > 0) {
				float min = 2.5f / 16f;
				float max = min + (11f / 16f);
				float fluidHeight = (max - min) * level;

				ms.pushPose();
				ms.translate(0, headOffset, 0); // KEY FIX: Move fluid with the head!

				// Convert to Vanilla FluidState for Catnip API
				FluidState fluidState = fluidStack.getFluid().defaultFluidState();

				CatnipServices.FLUID_RENDERER.renderFluidBox(
						fluidState,
						min, min, min,
						max, min + fluidHeight, max,
						buffer, ms, light, false, false);

				ms.popPose();
			}
		}

		// 3. Render Moving Parts (Tube/Head)
		ms.pushPose();
		ms.translate(0, headOffset, 0);
		for (PartialModel bit : TUBE) {
			CachedBuffers
					.partial(bit, be.getBlockState())
					.light(light)
					.renderInto(ms, buffer.getBuffer(RenderType.solid()));
		}
		ms.popPose();

		// 4. Render Static Bottom
		CachedBuffers
				.partial(CeiBlockPartials.PRINTER_BOTTOM, be.getBlockState())
				.translate(0, squeeze / 2f, 0)
				.light(light)
				.renderInto(ms, buffer.getBuffer(RenderType.solid()));
	}
}
