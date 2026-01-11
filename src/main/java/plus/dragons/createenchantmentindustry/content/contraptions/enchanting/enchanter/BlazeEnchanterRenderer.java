package plus.dragons.createenchantmentindustry.content.contraptions.enchanting.enchanter;

import static plus.dragons.createenchantmentindustry.content.contraptions.enchanting.enchanter.BlazeEnchanterBlockEntity.ENCHANTING_TIME;

import java.util.Set;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.BookModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
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

public class BlazeEnchanterRenderer
		implements BlockEntityRenderer<BlazeEnchanterBlockEntity> {

	public static final Material BOOK_MATERIAL =
			new Material(
					TextureAtlas.LOCATION_BLOCKS,
					EnchantmentIndustry.genRL("block/blaze_enchanter_book")
			);

	private static final float PI = (float) Math.PI;
	private final BookModel bookModel;

	public BlazeEnchanterRenderer(BlockEntityRendererProvider.Context context) {
		this.bookModel = new BookModel(context.bakeLayer(ModelLayers.BOOK));
	}

	@Override
	public void render(
			BlazeEnchanterBlockEntity be,
			float partialTicks,
			PoseStack ps,
			MultiBufferSource buffer,
			int light,
			int overlay
	) {
		if (be.getLevel() == null)
			return;

		ps.pushPose();

		renderItem(be, partialTicks, ps, buffer, light, overlay);
		renderBook(be, partialTicks, ps, buffer);

		ps.popPose();
	}

	/* -------------------------------- ITEM -------------------------------- */

	private void renderItem(
			BlazeEnchanterBlockEntity be,
			float partialTicks,
			PoseStack ps,
			MultiBufferSource buffer,
			int light,
			int overlay
	) {
		if (be.heldItem == null)
			return;

		var transported = be.heldItem;
		Direction insertedFrom = transported.insertedFrom;
		boolean horizontal = insertedFrom.getAxis().isHorizontal();

		ps.pushPose();

		float beltOffset = horizontal
				? Mth.lerp(partialTicks, transported.prevBeltPosition, transported.beltPosition)
				: 0.5f;

		float bob = Mth.sin((be.getLevel().getGameTime() + partialTicks) * 0.2f) * 0.05f;

		ps.translate(0.5, 0.75 + bob, 0.5);

		Vec3 offsetVec =
				Vec3.atLowerCornerOf(insertedFrom.getOpposite().getNormal())
						.scale(0.5f - beltOffset);
		ps.translate(offsetVec.x, 0, offsetVec.z);

		ps.scale(0.5f, 0.5f, 0.5f);

		ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
		itemRenderer.renderStatic(
				transported.stack,
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

	/* -------------------------------- BOOK -------------------------------- */

	private void renderBook(
			BlazeEnchanterBlockEntity be,
			float partialTicks,
			PoseStack ps,
			MultiBufferSource buffer
	) {
		ps.pushPose();

		ps.translate(0.5, 0.25, 0.5);

		float time = be.getLevel().getGameTime() + partialTicks;
		ps.translate(0.0, 0.1f + Mth.sin(time * 0.1f) * 0.01f, 0.0);

		float horizontalAngle = be.headAngle.getValue(partialTicks);
		ps.mulPose(Axis.YP.rotation(horizontalAngle + PI / 2));
		ps.mulPose(Axis.ZP.rotationDegrees(80.0f));

		float flip = Mth.lerp(partialTicks, be.oFlip, be.flip);
		float page0 = Mth.frac(flip + 0.25f) * 1.6f - 0.3f;
		float page1 = Mth.frac(flip + 0.75f) * 1.6f - 0.3f;

		bookModel.setupAnim(
				time,
				Mth.clamp(page0, 0.0f, 1.0f),
				Mth.clamp(page1, 0.0f, 1.0f),
				1.0f
		);

		VertexConsumer vc = BOOK_MATERIAL.buffer(buffer, RenderType::entitySolid);
		bookModel.render(
				ps,
				vc,
				LightTexture.FULL_BRIGHT,
				OverlayTexture.NO_OVERLAY,
				1f, 1f, 1f, 1f
		);

		ps.popPose();
	}

	/* --------------------------- TEXTURE STITCH --------------------------- */

	public static void loadTexture(ResourceLocation atlas, Set<ResourceLocation> sprites) {
		if (atlas.equals(InventoryMenu.BLOCK_ATLAS)) {
			sprites.add(BOOK_MATERIAL.texture());
		}
	}
}
