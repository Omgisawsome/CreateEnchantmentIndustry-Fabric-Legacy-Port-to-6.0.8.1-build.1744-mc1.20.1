package plus.dragons.createenchantmentindustry.content.contraptions.enchanting.enchanter;

import com.simibubi.create.content.fluids.transfer.GenericItemEmptying;
import com.simibubi.create.content.kinetics.belt.transport.TransportedItemStack;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;

@MethodsReturnNonnullByDefault
public class EnchantingItemHandler implements SingleSlotStorage<ItemVariant> {
	private final BlazeEnchanterBlockEntity be;
	private final Direction side;

	public EnchantingItemHandler(BlazeEnchanterBlockEntity be, Direction side) {
		this.be = be;
		this.side = side;
	}

	@Override
	public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		// Only allow insertion if the enchanter is empty
		if (!be.getHeldItemStack().isEmpty())
			return 0;

		ItemStack stack = resource.toStack();

		// Enchanting logic check
		if (Enchanting.getValidEnchantment(stack, be.targetItem, be.hyper()) == null)
			return 0;

		// Fabric Transfer API: Blaze Enchanter processes 1 item at a time
		int toInsert = 1;

		// Create the transported stack for the BE
		TransportedItemStack heldItem = new TransportedItemStack(stack.copy());
		heldItem.stack.setCount(toInsert);
		heldItem.prevBeltPosition = 0;
		heldItem.beltPosition = 0;

		// Use the BE's participant to allow transaction rollbacks
		be.snapshotParticipant.updateSnapshots(transaction);
		be.setHeldItem(heldItem, side.getOpposite());

		return toInsert;
	}

	@Override
	public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		TransportedItemStack held = be.heldItem;
		if (held == null || held.stack.isEmpty())
			return 0;

		// Ensure the requested resource matches what is held
		if (!resource.matches(held.stack))
			return 0;

		int toExtract = Math.min((int) maxAmount, held.stack.getCount());

		// Update snapshot before modification
		be.snapshotParticipant.updateSnapshots(transaction);

		ItemStack newStack = held.stack.copy();
		newStack.shrink(toExtract);
		be.heldItem.stack = newStack;

		if (be.heldItem.stack.isEmpty())
			be.heldItem = null;

		return toExtract;
	}

	@Override
	public boolean isResourceBlank() {
		return getResource().isBlank();
	}

	@Override
	public ItemVariant getResource() {
		return ItemVariant.of(getStack());
	}

	@Override
	public long getAmount() {
		ItemStack stack = getStack();
		return stack.isEmpty() ? 0 : stack.getCount();
	}

	@Override
	public long getCapacity() {
		// Standard slot capacity
		return 64;
	}

	public ItemStack getStack() {
		TransportedItemStack held = be.heldItem;
		if (held == null || held.stack == null || held.stack.isEmpty())
			return ItemStack.EMPTY;
		return held.stack;
	}
}
