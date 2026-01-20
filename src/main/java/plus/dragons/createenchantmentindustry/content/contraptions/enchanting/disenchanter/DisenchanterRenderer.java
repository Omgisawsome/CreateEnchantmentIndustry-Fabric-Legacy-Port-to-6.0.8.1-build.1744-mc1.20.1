package plus.dragons.createenchantmentindustry.content.contraptions.enchanting.disenchanter;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.content.kinetics.belt.transport.TransportedItemStack;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import com.simibubi.create.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.simibubi.create.foundation.fluid.FluidRenderer;
import com.simibubi.create.foundation.render.ShadowRenderHelper;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import io.github.fabricators_of_create.porting_lib.fluids.FluidStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.Random;

// Path confirmed from your previous message
import plus.dragons.createenchantmentindustry.content.contraptions.enchanting.disenchanter.Disenchanting;

public class DisenchanterRenderer extends SmartBlockEntityRenderer<DisenchanterBlockEntity> {
	public DisenchanterRenderer(BlockEntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	protected void renderSafe(DisenchanterBlockEntity be, float partialTicks, PoseStack ps, MultiBufferSource buffer,
							  int light, int overlay) {
		super.renderSafe(be, partialTicks, ps, buffer, light, overlay);
		renderItem(be, partialTicks, ps, buffer, light, overlay);
		renderFluid(be, partialTicks, ps, buffer, light, overlay);
	}

	protected void renderItem(DisenchanterBlockEntity be, float partialTicks, PoseStack ps, MultiBufferSource buffer,
							  int light, int overlay) {
		TransportedItemStack transported = be.heldItem;
		if (transported == null) return;

		TransformStack ts = TransformStack.of(ps);
		Direction insertedFrom = transported.insertedFrom;
		if (insertedFrom == null) insertedFrom = Direction.UP;
		boolean horizontal = insertedFrom.getAxis().isHorizontal();

		ps.pushPose();
		ps.translate(.5f, 13 / 16f, .5f);

		float offset = horizontal ? Mth.lerp(partialTicks, transported.prevBeltPosition, transported.beltPosition) : .5f;
		float sideOffset = horizontal ? Mth.lerp(partialTicks, transported.prevSideOffset, transported.sideOffset) : .5f;

		Vec3 offsetVec = Vec3.atLowerCornerOf(insertedFrom.getOpposite().getNormal()).scale(.5f - offset);
		ps.translate(offsetVec.x, 0, offsetVec.z);
		if (horizontal) {
			boolean alongX = insertedFrom.getClockWise().getAxis() == Direction.Axis.X;
			ps.translate(alongX ? sideOffset : 0, 0.005f, alongX ? 0 : -sideOffset);
		} else {
			ps.translate(0, 0.005f, 0);
		}

		ShadowRenderHelper.renderShadow(ps, buffer, .75f, .2f);

		ItemStack itemStack = transported.stack;
		Random r = new Random(0);
		ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
		int count = Mth.log2(itemStack.getCount()) / 2;
		boolean blockItem = itemRenderer.getModel(itemStack, null, null, 0).isGui3d();

		if (blockItem) {
			ps.translate(0, 1 / 8f, 0);
		} else {
			ps.translate(0, 1 / 64f, 0);
		}

		int positive = insertedFrom.getAxisDirection().getStep();
		float verticalAngle = positive * offset * 360 + 180;
		if (insertedFrom.getAxis() != Direction.Axis.X) ts.rotateX(verticalAngle);
		if (insertedFrom.getAxis() != Direction.Axis.Z) ts.rotateZ(-verticalAngle);

		int processingTicks = be.processingTicks;
		float processingProgress = switch (processingTicks) {
			case 0, DisenchanterBlockEntity.DISENCHANTER_TIME -> 0;
			default -> Mth.clamp((processingTicks - partialTicks) / (float)DisenchanterBlockEntity.DISENCHANTER_TIME, 0, 1);
		};
		ts.rotateY(processingProgress * 360);

		for (int i = 0; i <= count; i++) {
			ps.pushPose();
			if (blockItem) {
				ps.translate(r.nextFloat() * .0625f * i, 0, r.nextFloat() * .0625f * i);
			}
			ps.scale(.5f, .5f, .5f);
			if (!blockItem) ts.rotateX(90);
			itemRenderer.renderStatic(itemStack, ItemDisplayContext.FIXED, light, overlay, ps, buffer, be.getLevel(), 0);
			ps.popPose();

			if (!blockItem) ts.rotateY(10);
			ps.translate(0, blockItem ? 1 / 64d : 1 / 16d, 0);
		}
		ps.popPose();
	}

	protected void renderFluid(DisenchanterBlockEntity be, float partialTicks, PoseStack ps, MultiBufferSource buffer,
							   int light, int overlay) {
		SmartFluidTankBehaviour tank = be.internalTank;
		if (tank == null) return;

		SmartFluidTankBehaviour.TankSegment primaryTank = tank.getPrimaryTank();
		FluidStack tankFluidStack = primaryTank.getRenderedFluid();
		float level = primaryTank.getFluidLevel().getValue(partialTicks);

		// Internal tank surface
		if (!tankFluidStack.isEmpty() && level != 0) {
			float yMin = 5f / 16f;
			float yOffset = (7f / 16f) * level;

			ps.pushPose();
			ps.translate(0.5f, yMin, 0.5f);
			// Using a short upward stream to represent the fluid surface since Box is missing
			FluidRenderer.renderFluidStream(tankFluidStack, Direction.UP, 6/16f, yOffset, false, buffer, ps, light);
			ps.popPose();
		}

		if (be.processingTicks == 0) return;

		TransportedItemStack transported = be.heldItem;
		if (transported == null) return;

		var result = Disenchanting.disenchantResult(transported.stack, be.getLevel());
		if (result == null) return;
		FluidStack xp = result.getFirst();
		if (xp.isEmpty()) return;

		float processingProgress = Mth.clamp(1 - (be.processingTicks - partialTicks - 5) / 10f, 0, 1);
		float radius = 1/16f + (processingProgress * 1/16f);

		ps.pushPose();
		ps.translate(0.5, 13/16d, 0.5);

		// CORRECTED Signature based on your error:
		// (FluidStack, Direction, float radius, float length, boolean upsideDown, MultiBufferSource, PoseStack, int light)
		FluidRenderer.renderFluidStream(xp, Direction.DOWN, radius, 6/16f, false, buffer, ps, light);

		ps.popPose();
	}
}
