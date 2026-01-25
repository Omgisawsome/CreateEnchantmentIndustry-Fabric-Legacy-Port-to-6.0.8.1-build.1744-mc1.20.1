package plus.dragons.createenchantmentindustry.content.contraptions.enchanting.enchanter;

import java.util.Set;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.simibubi.create.foundation.blockEntity.renderer.SmartBlockEntityRenderer;

import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.createmod.catnip.math.AngleHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.BookModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;

import plus.dragons.createenchantmentindustry.EnchantmentIndustry;
import plus.dragons.createenchantmentindustry.entry.CeiBlockPartials;

public class BlazeEnchanterRenderer extends SmartBlockEntityRenderer<BlazeEnchanterBlockEntity> {

	public static final Material BOOK_MATERIAL = new Material(
			TextureAtlas.LOCATION_BLOCKS,
			EnchantmentIndustry.genRL("block/blaze_enchanter_book")
	);

	private final BookModel bookModel;

	public BlazeEnchanterRenderer(BlockEntityRendererProvider.Context context) {
		super(context);
		this.bookModel = new BookModel(context.bakeLayer(ModelLayers.BOOK));
	}

	@Override
	protected void renderSafe(BlazeEnchanterBlockEntity be, float partialTicks, PoseStack ps, MultiBufferSource buffer,
							  int light, int overlay) {

		renderBlaze(be, partialTicks, ps, buffer, light, overlay);
		renderBook(be, partialTicks, ps, buffer, light, overlay);
		renderItem(be, partialTicks, ps, buffer, light, overlay);
	}

	private void renderBlaze(BlazeEnchanterBlockEntity be, float partialTicks, PoseStack ps, MultiBufferSource buffer,
							 int light, int overlay) {
		ps.pushPose();
		ps.translate(0.5, 0, 0.5);
		float headAngle = AngleHelper.rad(Mth.lerp(partialTicks, be.oHeadAngle, be.headAngle));
		ps.mulPose(Axis.YP.rotation(headAngle));
		ps.translate(-0.5, 0, -0.5);

		boolean active = be.processingTicks > 0;
		boolean hyper = be.hyper();

		SuperByteBuffer blazeBuffer;
		if (active) {
			blazeBuffer = CachedBuffers.partial(
					hyper ? CeiBlockPartials.BLAZE_ENCHANTER_ACTIVE_HYPER : CeiBlockPartials.BLAZE_ENCHANTER_ACTIVE,
					be.getBlockState()
			);
		} else {
			blazeBuffer = CachedBuffers.partial(
					hyper ? CeiBlockPartials.BLAZE_ENCHANTER_IDLE_HYPER : CeiBlockPartials.BLAZE_ENCHANTER_IDLE,
					be.getBlockState()
			);
		}

		blazeBuffer
				.light(LightTexture.FULL_BRIGHT)
				.renderInto(ps, buffer.getBuffer(RenderType.translucent()));

		ps.popPose();
	}

	private void renderBook(BlazeEnchanterBlockEntity be, float partialTicks, PoseStack ps, MultiBufferSource buffer, int light, int overlay) {
		if (be.targetItem.isEmpty()) return;

		ps.pushPose();

		// Height adjustment
		ps.translate(0.5, 0.825, 0.5);

		float time = (float) be.getLevel().getGameTime() + partialTicks;
		ps.translate(0, Mth.sin(time * 0.1f) * 0.05f, 0);

		float headAngle = AngleHelper.rad(Mth.lerp(partialTicks, be.oHeadAngle, be.headAngle));

		// FIX: Changed -headAngle to headAngle so it rotates the same way as the blaze
		ps.mulPose(Axis.YP.rotation(headAngle + (float)Math.PI / 2));

		ps.mulPose(Axis.ZP.rotationDegrees(80.0f));

		float flip = Mth.lerp(partialTicks, be.oFlip, be.flip);
		float page0 = Mth.frac(flip + 0.25f) * 1.6f - 0.3f;
		float page1 = Mth.frac(flip + 0.75f) * 1.6f - 0.3f;

		bookModel.setupAnim(time, Mth.clamp(page0, 0.0f, 1.0f), Mth.clamp(page1, 0.0f, 1.0f), 1.0f);
		VertexConsumer vc = BOOK_MATERIAL.buffer(buffer, RenderType::entitySolid);
		bookModel.render(ps, vc, light, OverlayTexture.NO_OVERLAY, 1f, 1f, 1f, 1f);
		ps.popPose();
	}

	private void renderItem(BlazeEnchanterBlockEntity be, float partialTicks, PoseStack ps, MultiBufferSource buffer,
							int light, int overlay) {
		if (be.heldItem == null) return;

		var transported = be.heldItem;
		Direction insertedFrom = transported.insertedFrom;
		if (insertedFrom == null) insertedFrom = Direction.UP;

		boolean horizontal = insertedFrom.getAxis().isHorizontal();

		ps.pushPose();
		float beltOffset = horizontal ? Mth.lerp(partialTicks, transported.prevBeltPosition, transported.beltPosition) : 0.5f;
		float bob = Mth.sin((be.getLevel().getGameTime() + partialTicks) * 0.2f) * 0.05f;

		// Item Height Adjustment
		ps.translate(0.5, 0.9 + bob, 0.5);

		Vec3 offsetVec = Vec3.atLowerCornerOf(insertedFrom.getOpposite().getNormal()).scale(0.5f - beltOffset);
		ps.translate(offsetVec.x, 0, offsetVec.z);
		ps.scale(0.5f, 0.5f, 0.5f);

		ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
		itemRenderer.renderStatic(transported.stack, ItemDisplayContext.FIXED, light, overlay, ps, buffer, be.getLevel(), 0);
		ps.popPose();
	}

	public static void loadTexture(ResourceLocation atlas, Set<ResourceLocation> sprites) {
		if (atlas.equals(InventoryMenu.BLOCK_ATLAS)) {
			sprites.add(BOOK_MATERIAL.texture());
		}
	}
}
