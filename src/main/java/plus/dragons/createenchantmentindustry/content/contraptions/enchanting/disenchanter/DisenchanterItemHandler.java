package plus.dragons.createenchantmentindustry.content.contraptions.enchanting.disenchanter;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import com.simibubi.create.content.kinetics.belt.transport.TransportedItemStack;
import java.util.Iterator;
import java.util.Collections;

public class DisenchanterItemHandler implements SingleSlotStorage<ItemVariant> {
	private final DisenchanterBlockEntity be;
	private final Direction side;

	public DisenchanterItemHandler(DisenchanterBlockEntity be, Direction side) {
		this.be = be;
		this.side = side;
	}

	@Override
	public long insert(ItemVariant variant, long maxAmount, TransactionContext transaction) {
		if (!be.getHeldItemStack().isEmpty()) return 0;

		// Disenchanting.disenchantAndInsert handles the logic check
		ItemStack stack = variant.toStack((int) Math.min(maxAmount, 64));
		ItemStack remainder = Disenchanting.disenchantAndInsert(be, stack, true);

		if (remainder.getCount() == stack.getCount()) return 0;

		int inserted = stack.getCount() - remainder.getCount();

		transaction.addCloseCallback((t, result) -> {
			if (result.wasCommitted()) {
				TransportedItemStack transportedStack = new TransportedItemStack(stack);
				transportedStack.beltPosition = side.getAxis().isVertical() ? .5f : 0;
				transportedStack.prevSideOffset = transportedStack.sideOffset;
				transportedStack.prevBeltPosition = transportedStack.beltPosition;
				be.setHeldItem(transportedStack, side);
				be.notifyUpdate();
			}
		});

		return inserted;
	}

	@Override
	public long extract(ItemVariant variant, long maxAmount, TransactionContext transaction) {
		ItemStack held = be.getHeldItemStack();
		if (held.isEmpty() || !ItemVariant.of(held).equals(variant)) return 0;

		long extracted = Math.min(maxAmount, held.getCount());
		transaction.addCloseCallback((t, result) -> {
			if (result.wasCommitted()) {
				held.shrink((int) extracted);
				if (held.isEmpty()) be.heldItem = null;
				be.notifyUpdate();
			}
		});
		return extracted;
	}

	@Override
	public boolean isResourceBlank() {
		return getResource().isBlank();
	}

	@Override
	public ItemVariant getResource() {
		return ItemVariant.of(be.getHeldItemStack());
	}

	@Override
	public long getAmount() {
		return be.getHeldItemStack().getCount();
	}

	@Override
	public long getCapacity() {
		return 64;
	}

	@Override
	public Iterator<StorageView<ItemVariant>> iterator() {
		return Collections.singleton((StorageView<ItemVariant>) this).iterator();
	}
}
