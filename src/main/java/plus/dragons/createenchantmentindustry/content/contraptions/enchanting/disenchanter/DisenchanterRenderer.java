package plus.dragons.createenchantmentindustry.content.contraptions.enchanting.disenchanter;

import java.util.Random;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.content.kinetics.belt.transport.TransportedItemStack;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import com.simibubi.create.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.simibubi.create.foundation.fluid.FluidRenderer;
import com.simibubi.create.foundation.render.ShadowRenderHelper;

import io.github.fabricators_of_create.porting_lib.fluids.FluidStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class DisenchanterRenderer extends SmartBlockEntityRenderer<DisenchanterBlockEntity> {

	public DisenchanterRenderer(BlockEntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	protected void renderSafe(DisenchanterBlockEntity be, float partialTicks, PoseStack ps,
							  MultiBufferSource buffer, int light, int overlay) {

		super.renderSafe(be, partialTicks, ps, buffer, light, overlay);
		renderItem(be, partialTicks, ps, buffer, light, overlay);
		renderFluid(be, partialTicks, ps, buffer, light);
	}

	protected void renderItem(DisenchanterBlockEntity be, float partialTicks, PoseStack ps,
							  MultiBufferSource buffer, int light, int overlay) {

		TransportedItemStack transported = be.heldItem;
		if (transported == null)
			return;

		Direction insertedFrom = transported.insertedFrom;
		boolean horizontal = insertedFrom.getAxis().isHorizontal();

		ps.pushPose();
		ps.translate(0.5, 13 / 16f, 0.5);

		float offset = horizontal
				? Mth.lerp(partialTicks, transported.prevBeltPosition, transported.beltPosition)
				: 0.5f;
		float sideOffset = horizontal
				? Mth.lerp(partialTicks, transported.prevSideOffset, transported.sideOffset)
				: 0.5f;

		Vec3 move = Vec3.atLowerCornerOf(insertedFrom.getOpposite().getNormal())
				.scale(0.5f - offset);
		ps.translate(move.x, 0, move.z);

		if (horizontal) {
			boolean alongX = insertedFrom.getClockWise().getAxis() == Direction.Axis.X;
			ps.translate(alongX ? sideOffset : 0, 0.005f, alongX ? 0 : -sideOffset);
		}

		ShadowRenderHelper.renderShadow(ps, buffer, .75f, .2f);

		ItemStack stack = transported.stack;
		ItemRenderer renderer = Minecraft.getInstance().getItemRenderer();
		boolean blockItem = renderer.getModel(stack, null, null, 0).isGui3d();

		ps.translate(0, blockItem ? 1 / 8f : 1 / 64f, 0);

		float spin = be.processingTicks == 0 ? 0
				: Mth.clamp(
				(be.processingTicks - partialTicks)
						/ DisenchanterBlockEntity.DISENCHANTER_TIME,
				0, 1
		) * 360f;

		ps.mulPose(Axis.YP.rotationDegrees(spin));

		if (!blockItem)
			ps.mulPose(Axis.XP.rotationDegrees(90));

		ps.scale(.5f, .5f, .5f);

		renderer.renderStatic(
				stack,
				ItemDisplayContext.FIXED,
				light,
				overlay,
				ps,
				buffer,
				be.getLevel(),
				0
		);

		ps.popPose();
	}

	protected void renderFluid(DisenchanterBlockEntity be, float partialTicks,
							   PoseStack ps, MultiBufferSource buffer, int light) {

		SmartFluidTankBehaviour tank = be.internalTank;
		if (tank == null)
			return;

		var primary = tank.getPrimaryTank();
		FluidStack fluid = primary.getRenderedFluid();
		float level = primary.getFluidLevel().getValue(partialTicks);

		if (!fluid.isEmpty() && level > 0) {
			float min = 2 / 16f;
			float max = 14 / 16f;
			float height = (7 / 16f) * level;

			FluidRenderer.renderFluidBox(
					fluid,
					min, 5 / 16f,
					min,
					max, 5 / 16f + height,
					max,
					buffer, ps, light, false
			);
		}
	}
}
