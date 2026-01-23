package plus.dragons.createenchantmentindustry.content.contraptions.enchanting.enchanter;

import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.ImmutableList;
import com.simibubi.create.foundation.gui.menu.AbstractSimiContainerScreen;
import com.simibubi.create.foundation.gui.widget.Label;
import com.simibubi.create.foundation.gui.widget.SelectionScrollInput;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import plus.dragons.createenchantmentindustry.entry.CeiPackets;
import plus.dragons.createenchantmentindustry.foundation.gui.CeiGuiTextures;

import static com.simibubi.create.foundation.gui.AllGuiTextures.PLAYER_INVENTORY;

public class EnchantingGuideScreen extends AbstractSimiContainerScreen<EnchantingGuideMenu> {

	private static final int ENCHANTING_GUIDE_WIDTH = 178;
	private static final int TEXTURE_WIDTH = 188;
	private static final int TEXTURE_HEIGHT = 92;

	private List<Rect2i> extraAreas = Collections.emptyList();
	public int index;
	public SelectionScrollInput scrollInput;
	public Label scrollInputLabel;
	private final boolean directItemStackEdit;
	@Nullable
	private final BlockPos blockPos;

	public EnchantingGuideScreen(EnchantingGuideMenu container, Inventory inv, Component title) {
		super(container, inv, title);
		this.directItemStackEdit = container.directItemStackEdit;
		this.blockPos = container.blockPos;
	}

	public void updateScrollInput(boolean resetIndex) {
		if (resetIndex) index = 0;
		if (scrollInput != null) {
			scrollInput.forOptions(menu.enchantments);
			// Safety check to ensure index is within bounds
			if (index >= menu.enchantments.size()) index = 0;
			scrollInput.setState(index);
		}
	}

	@Override
	protected void init() {
		setWindowSize(
				TEXTURE_WIDTH,
				TEXTURE_HEIGHT + 4 + PLAYER_INVENTORY.getHeight()
		);
		setWindowOffset(-32, 0);
		super.init();

		int guideX = getLeftOfCentered(ENCHANTING_GUIDE_WIDTH);
		int guideY = topPos;

		extraAreas = ImmutableList.of(
				new Rect2i(guideX + TEXTURE_WIDTH, guideY + TEXTURE_HEIGHT - 48, 48, 48),
				new Rect2i(guideX, guideY, imageWidth, imageHeight)
		);

		// Read index directly from the menu's content holder tag
		CompoundTag tag = menu.contentHolder.getTag();
		index = (tag != null) ? tag.getInt("index") : 0;

		scrollInput = new SelectionScrollInput(guideX + 40, guideY + 22, 120, 16);
		scrollInputLabel = new Label(guideX + 43, guideY + 26, Component.literal("")).withShadow();

		// Update local index when scrolling
		scrollInput.calling(i -> this.index = i)
				.writingTo(scrollInputLabel);

		addRenderableWidget(scrollInputLabel);
		addRenderableWidget(scrollInput);

		updateScrollInput(false);
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
		int invX = getLeftOfCentered(PLAYER_INVENTORY.getWidth());
		int invY = topPos + TEXTURE_HEIGHT + 4;
		renderPlayerInventory(graphics, invX, invY);

		int guideX = getLeftOfCentered(ENCHANTING_GUIDE_WIDTH);
		int guideY = topPos;

		// FIXED: Call the static render method directly
		CeiGuiTextures.render(graphics, guideX, guideY);

		Component titleText = Component.translatable("item.create_enchantment_industry.enchanting_guide");
		graphics.drawCenteredString(font, titleText, guideX + ENCHANTING_GUIDE_WIDTH / 2, guideY + 3, 0xFFFFFF);
	}

	@Override
	public void removed() {
		super.removed();
		// Grab the item currently in the ghost slot (Slot 36)
		ItemStack resultStack = menu.getSlot(36).getItem();

		if (directItemStackEdit) {
			CeiPackets.channel.sendToServer(new EnchantingGuideEditPacket(index, resultStack));
		} else {
			// If editing a BlockEntity, we must have a valid blockPos
			if (blockPos != null) {
				CeiPackets.channel.sendToServer(new BlazeEnchanterEditPacket(index, resultStack, blockPos));
			}
		}
	}

	@Override
	public List<Rect2i> getExtraAreas() {
		return extraAreas;
	}
}
