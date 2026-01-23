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
	// FIXED: Matched the key to your en_us.json
	private static final Component NO_ENCHANTMENT = Component.translatable("create_enchantment_industry.gui.enchanting_guide.no_enchantment");

	public ImmutableList<Component> enchantments = ImmutableList.of(NO_ENCHANTMENT);
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
		// Ensure enchantments are loaded on init
		initAndReadInventory(contentHolder);
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
		// Using FabricLoader to check environment is safer than direct Minecraft call in common code
		if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
			ClientInternal.refresh();
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
		// The ghost slot is index 36
		this.addSlot(new SlotItemHandler(ghostInventory, 0, 51, 22) {
			@Override
			public boolean mayPlace(ItemStack stack) {
				return (stack.is(Items.ENCHANTED_BOOK) || stack.is(Items.BOOK)) && !EnchantmentHelper.getEnchantments(stack).isEmpty();
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
		if (slotId == 36) {
			ItemStack held = getCarried();
			if (held.isEmpty() || mayPlace(getSlot(36), held)) {
				ItemStack ghostStack = held.copy();
				ghostStack.setCount(1);
				ghostInventory.setStackInSlot(0, ghostStack);
				getSlot(36).setChanged();
			}
			return;
		}
		super.clicked(slotId, dragType, clickTypeIn, player);
	}

	// Helper to check placement without duplicating logic
	private boolean mayPlace(net.minecraft.world.inventory.Slot slot, ItemStack stack) {
		return slot instanceof SlotItemHandler handler && handler.mayPlace(stack);
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
		} else if (index == 36) {
			ghostInventory.setStackInSlot(0, ItemStack.EMPTY);
			getSlot(36).setChanged();
		}
		return ItemStack.EMPTY;
	}

	@Override
	protected void saveData(ItemStack contentHolder) {
		// We handle saving via packets in the Screen, but keeping NBT updated here
		// helps prevent desyncs if the menu is closed unexpectedly.
		var tag = contentHolder.getOrCreateTag();
		ItemStack target = ghostInventory.getStackInSlot(0);
		if (target.isEmpty()) tag.remove("target");
		else tag.put("target", target.save(new net.minecraft.nbt.CompoundTag()));
	}

	@Override
	public boolean stillValid(Player player) {
		return directItemStackEdit || (blockPos != null && player.level().getBlockEntity(blockPos) instanceof BlazeEnchanterBlockEntity);
	}

	// Nested class to isolate Client-only code from the Server
	private static class ClientInternal {
		private static void refresh() {
			if (Minecraft.getInstance().screen instanceof EnchantingGuideScreen screen) {
				screen.updateScrollInput(true);
			}
		}
	}
}
