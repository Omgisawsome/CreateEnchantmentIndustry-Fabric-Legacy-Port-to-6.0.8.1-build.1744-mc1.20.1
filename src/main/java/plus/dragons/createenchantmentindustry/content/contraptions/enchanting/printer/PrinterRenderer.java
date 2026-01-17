package plus.dragons.createenchantmentindustry.content.contraptions.enchanting.printer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import com.simibubi.create.foundation.blockEntity.renderer.SmartBlockEntityRenderer;

// Porting Lib FluidStack
import io.github.fabricators_of_create.porting_lib.fluids.FluidStack;

// Flywheel / Catnip imports
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.platform.CatnipServices;

// Using Vanilla FluidState as you requested
import net.minecraft.world.level.material.FluidState;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import plus.dragons.createenchantmentindustry.entry.CeiBlockPartials;

public class PrinterRenderer extends SmartBlockEntityRenderer<PrinterBlockEntity> {
	public PrinterRenderer(BlockEntityRendererProvider.Context context) {
		super(context);
	}

	private static final PartialModel[] TUBE = {CeiBlockPartials.PRINTER_TOP, CeiBlockPartials.PRINTER_MIDDLE};

	@Override
	protected void renderSafe(PrinterBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
		super.renderSafe(be, partialTicks, ms, buffer, light, overlay);
		SmartFluidTankBehaviour tank = be.tank;
		if (tank == null) return;

		SmartFluidTankBehaviour.TankSegment primaryTank = tank.getPrimaryTank();
		FluidStack fluidStack = primaryTank.getRenderedFluid();
		float level = primaryTank.getFluidLevel().getValue(partialTicks);

		if (!fluidStack.isEmpty() && level != 0) {
			level = Math.max(level, 0.175f);
			float min = 2.5f / 16f;
			float max = min + (11 / 16f);
			float yOffset = (11 / 16f) * level;
			ms.pushPose();
			ms.translate(0, yOffset, 0);

			// FIXED: Get the vanilla FluidState from the FluidStack
			FluidState vanillaState = fluidStack.getFluid().defaultFluidState();

			// We cast the service to its generic form to ensure it accepts the vanilla state
			// if the Service definition is actually using the vanilla class.
			CatnipServices.FLUID_RENDERER.renderFluidBox(
					vanillaState,         // Arg 1: net.minecraft.world.level.material.FluidState
					min,                  // 2: x1
					min - yOffset,        // 3: y1
					min,                  // 4: z1
					max,                  // 5: x2
					min,                  // 6: y2
					max,                  // 7: z2
					buffer,               // 8
					ms,                   // 9
					light,                // 10
					false,                // 11
					false                 // 12
			);

			ms.popPose();
		}

		int processingTicks = be.processingTicks;
		float processingPT = (float) processingTicks - partialTicks;

		float squeeze = calculateSqueeze(processingPT);

		ms.pushPose();
		for (PartialModel bit : TUBE) {
			ms.translate(0, -3 * squeeze / 32f, 0);
			CachedBuffers.partial(bit, be.getBlockState())
					.light(light)
					.renderInto(ms, buffer.getBuffer(RenderType.solid()));
		}
		ms.popPose();

		CachedBuffers.partial(CeiBlockPartials.PRINTER_BOTTOM, be.getBlockState())
				.translate(0, squeeze / 2f, 0)
				.light(light)
				.renderInto(ms, buffer.getBuffer(RenderType.solid()));
	}

	private float calculateSqueeze(float processingPT) {
		if (processingPT < 0) return 0;
		if (processingPT <= 10) return Mth.lerp(processingPT / 10f, 0, -1);
		if (processingPT <= 90) return -1;
		if (processingPT <= 100) return Mth.lerp((100 - processingPT) / 10f, 0, -1);
		return 0;
	}
}
