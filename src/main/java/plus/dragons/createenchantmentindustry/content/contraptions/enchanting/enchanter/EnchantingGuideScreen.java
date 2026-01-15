package plus.dragons.createenchantmentindustry.content.contraptions.enchanting.enchanter;

import com.google.common.collect.ImmutableList;
import com.simibubi.create.foundation.gui.menu.AbstractSimiContainerScreen;
import com.simibubi.create.foundation.gui.widget.Label;
import com.simibubi.create.foundation.gui.widget.SelectionScrollInput;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.Nullable;
import plus.dragons.createenchantmentindustry.entry.CeiPackets;

import java.util.Collections;
import java.util.List;

import static com.simibubi.create.foundation.gui.AllGuiTextures.PLAYER_INVENTORY;
import static plus.dragons.createenchantmentindustry.foundation.gui.CeiGuiTextures.ENCHANTING_GUIDE;

public class EnchantingGuideScreen extends AbstractSimiContainerScreen<EnchantingGuideMenu> {

	private static final int ENCHANTING_GUIDE_WIDTH = 178;
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
		scrollInput.forOptions(menu.enchantments);
		scrollInput.setState(index);
	}

	@Override
	protected void init() {
		setWindowSize(
				ENCHANTING_GUIDE.width,
				ENCHANTING_GUIDE.height + 4 + PLAYER_INVENTORY.height
		);
		setWindowOffset(-32, 0);
		super.init();

		int guideX = getLeftOfCentered(ENCHANTING_GUIDE_WIDTH);
		int guideY = topPos;

		extraAreas = ImmutableList.of(
				new Rect2i(guideX + ENCHANTING_GUIDE.width, guideY + ENCHANTING_GUIDE.height - 48, 48, 48),
				new Rect2i(guideX, guideY, imageWidth, imageHeight)
		);

		index = menu.contentHolder.getOrCreateTag().getInt("index");

		scrollInput = new SelectionScrollInput(guideX + 40, guideY + 22, 120, 16);
		scrollInputLabel = new Label(guideX + 43, guideY + 26, Component.literal("")).withShadow();
		scrollInput.calling(i -> this.index = i).writingTo(scrollInputLabel);
		addRenderableWidget(scrollInputLabel);
		addRenderableWidget(scrollInput);

		updateScrollInput(false);
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
		int invX = getLeftOfCentered(PLAYER_INVENTORY.width);
		int invY = topPos + ENCHANTING_GUIDE.height + 4;
		renderPlayerInventory(graphics, invX, invY);

		int guideX = getLeftOfCentered(ENCHANTING_GUIDE_WIDTH);
		int guideY = topPos;

		ENCHANTING_GUIDE.render(graphics, guideX, guideY);
		graphics.drawCenteredString(font, title, guideX + ENCHANTING_GUIDE_WIDTH / 2, guideY + 3, 0xFFFFFF);
	}

	@Override
	public void removed() {
		super.removed();
		if (directItemStackEdit)
			CeiPackets.channel.sendToServer(new EnchantingGuideEditPacket(index, menu.getSlot(36).getItem()));
		else
			CeiPackets.channel.sendToServer(new BlazeEnchanterEditPacket(index, menu.getSlot(36).getItem(), blockPos));
	}

	@Override
	public List<Rect2i> getExtraAreas() {
		return extraAreas;
	}

}
