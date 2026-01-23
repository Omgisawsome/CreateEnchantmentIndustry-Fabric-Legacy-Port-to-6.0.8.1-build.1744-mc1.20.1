package plus.dragons.createenchantmentindustry.content.contraptions.enchanting.enchanter;

import com.google.common.collect.ImmutableList;
import com.simibubi.create.foundation.gui.menu.GhostItemMenu;
import io.github.fabricators_of_create.porting_lib.transfer.item.ItemStackHandler;
import io.github.fabricators_of_create.porting_lib.transfer.item.SlotItemHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.jetbrains.annotations.Nullable;

public class EnchantingGuideMenu extends GhostItemMenu<ItemStack> {
	private static final Component NO_ENCHANTMENT = Component.translatable("gui.enchanting_guide.no_enchantment");
	ImmutableList<Component> enchantments = ImmutableList.of(NO_ENCHANTMENT);
	private Component lastEnchantmentKey = Component.literal("");
	boolean directItemStackEdit;
	@Nullable BlockPos blockPos = null;

	public EnchantingGuideMenu(MenuType<?> type, int id, Inventory inv, FriendlyByteBuf extraData) {
		super(type, id, inv, extraData);
		this.directItemStackEdit = extraData.readBoolean();
		if (!directItemStackEdit) this.blockPos = extraData.readBlockPos();
	}

	public EnchantingGuideMenu(MenuType<?> type, int id, Inventory inv, ItemStack contentHolder, @Nullable BlockPos blockPos) {
		super(type, id, inv, contentHolder);
		this.blockPos = blockPos;
		this.directItemStackEdit = (blockPos == null);
	}

	private void updateEnchantments(ItemStack stack) {
		var map = EnchantmentHelper.getEnchantments(stack);
		if (map.isEmpty()) {
			enchantments = ImmutableList.of(NO_ENCHANTMENT);
		} else {
			enchantments = map.entrySet().stream()
					.map(entry -> entry.getKey().getFullname(entry.getValue()))
					.collect(ImmutableList.toImmutableList());
		}

		if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
			refreshClientScreen();
		}
	}

	private void refreshClientScreen() {
		if (Minecraft.getInstance().screen instanceof EnchantingGuideScreen screen) {
			screen.updateScrollInput(true);
		}
	}

	@Override
	protected ItemStackHandler createGhostInventory() {
		return new ItemStackHandler(1);
	}

	@Override
	protected boolean allowRepeats() {
		return false;
	}

	@Override
	protected void initAndReadInventory(ItemStack contentHolder) {
		super.initAndReadInventory(contentHolder);
		var tag = contentHolder.getOrCreateTag();
		if (tag.contains("target", Tag.TAG_COMPOUND)) {
			ItemStack target = ItemStack.of(tag.getCompound("target"));
			ghostInventory.setStackInSlot(0, target);
			updateEnchantments(target);
		}
	}

	@Override
	protected ItemStack createOnClient(FriendlyByteBuf extraData) {
		return extraData.readItem();
	}

	@Override
	protected void addSlots() {
		addPlayerSlots(44, 70);
		this.addSlot(new SlotItemHandler(ghostInventory, 0, 51, 22) {
			@Override
			public boolean mayPlace(ItemStack stack) {
				return stack.is(Items.ENCHANTED_BOOK) && !EnchantmentHelper.getEnchantments(stack).isEmpty();
			}
			@Override
			public void setChanged() {
				super.setChanged();
				updateEnchantments(getItem());
			}
		});
	}

	@Override
	public void clicked(int slotId, int dragType, ClickType clickTypeIn, Player player) {
		// Handle Ghost Slot (index 36)
		if (slotId == 36) {
			ItemStack held = getCarried();
			if (getSlot(36).mayPlace(held) || held.isEmpty()) {
				ghostInventory.setStackInSlot(0, held.copy().split(1));
				getSlot(36).setChanged();
			}
			return;
		}
		super.clicked(slotId, dragType, clickTypeIn, player);
	}

	@Override
	public ItemStack quickMoveStack(Player playerIn, int index) {
		if (index < 36) {
			ItemStack stackToInsert = playerInventory.getItem(index);
			if (getSlot(36).mayPlace(stackToInsert)) {
				ItemStack copy = stackToInsert.copy();
				copy.setCount(1);
				ghostInventory.setStackInSlot(0, copy);
				getSlot(36).setChanged();
			}
		} else {
			ghostInventory.setStackInSlot(0, ItemStack.EMPTY);
			getSlot(36).setChanged();
		}
		return ItemStack.EMPTY;
	}

	@Override
	protected void saveData(ItemStack contentHolder) {}

	@Override
	public boolean stillValid(Player player) {
		return directItemStackEdit || (blockPos != null && player.level().getBlockEntity(blockPos) instanceof BlazeEnchanterBlockEntity);
	}
}
