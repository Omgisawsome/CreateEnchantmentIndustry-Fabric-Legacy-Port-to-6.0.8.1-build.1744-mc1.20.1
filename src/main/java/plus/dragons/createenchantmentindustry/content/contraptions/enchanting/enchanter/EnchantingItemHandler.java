package plus.dragons.createenchantmentindustry.content.contraptions.enchanting.enchanter;

import com.simibubi.create.content.kinetics.belt.transport.TransportedItemStack;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;

public class EnchantingItemHandler implements SingleSlotStorage<ItemVariant> {
	private final BlazeEnchanterBlockEntity be;
	private final Direction side;

	public EnchantingItemHandler(BlazeEnchanterBlockEntity be, Direction side) {
		this.be = be;
		this.side = side;
	}

	@Override
	public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		if (!be.getHeldItemStack().isEmpty() || maxAmount < 1) return 0;

		ItemStack stack = resource.toStack();
		if (Enchanting.getValidEnchantment(stack, be.targetItem, be.hyper()) == null) return 0;

		// Use the BE's participant for rollback support
		be.snapshotParticipant.updateSnapshots(transaction);

		TransportedItemStack heldItem = new TransportedItemStack(stack.copy());
		heldItem.stack.setCount(1);
		be.setHeldItem(heldItem, side.getOpposite());

		return 1;
	}

	@Override
	public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		TransportedItemStack held = be.heldItem;
		if (held == null || held.stack.isEmpty() || !resource.matches(held.stack)) return 0;

		int toExtract = Math.min((int) maxAmount, held.stack.getCount());
		be.snapshotParticipant.updateSnapshots(transaction);

		ItemStack newStack = held.stack.copy();
		newStack.shrink(toExtract);
		be.heldItem.stack = newStack;
		if (be.heldItem.stack.isEmpty()) be.heldItem = null;

		return toExtract;
	}

	@Override public ItemVariant getResource() { return ItemVariant.of(getStack()); }
	@Override public long getAmount() { return getStack().isEmpty() ? 0 : getStack().getCount(); }
	@Override public long getCapacity() { return 64; }
	@Override public boolean isResourceBlank() { return getResource().isBlank(); }

	private ItemStack getStack() {
		return be.heldItem == null ? ItemStack.EMPTY : be.heldItem.stack;
	}
}
