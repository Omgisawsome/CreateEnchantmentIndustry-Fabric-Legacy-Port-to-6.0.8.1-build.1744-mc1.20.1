package plus.dragons.createenchantmentindustry.content.contraptions.enchanting.printer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import com.simibubi.create.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.simibubi.create.foundation.fluid.FluidRenderer;

// FIXED: FluidStack is in Porting Lib for this build
import io.github.fabricators_of_create.porting_lib.fluids.FluidStack;

// FIXED: PartialModel and CachedBuffers are in Catnip
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.createmod.catnip.render.CachedBuffers;

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

			// Render using the FluidStack directly
			FluidRenderer.renderFluidBox(fluidStack, min, min - yOffset, min, max, min, max, buffer, ms, light, false);

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
