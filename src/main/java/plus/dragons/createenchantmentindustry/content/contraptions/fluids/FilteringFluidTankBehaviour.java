package plus.dragons.createenchantmentindustry.content.contraptions.fluids;

import java.util.function.Predicate;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;

public class FilteringFluidTankBehaviour extends SmartFluidTankBehaviour {

	protected final Predicate<FluidVariant> filter;

	public FilteringFluidTankBehaviour(
			BehaviourType<SmartFluidTankBehaviour> type,
			Predicate<FluidVariant> filter,
			SmartBlockEntity be,
			int tanks,
			long tankCapacity, // FIXED: Changed from int to long
			boolean enforceVariety
	) {
		super(type, be, tanks, tankCapacity, enforceVariety);
		this.filter = filter;

		// Build the handlers array from the tanks initialized in super
		Storage<FluidVariant>[] handlers = new Storage[this.tanks.length];
		for (int i = 0; i < this.tanks.length; i++) {
			handlers[i] = this.tanks[i].getTank();
		}

		// Replace the behavior's capability with our filtered version
		this.capability = new FilteringInternalHandler(handlers, enforceVariety);
	}

	public static FilteringFluidTankBehaviour single(
			Predicate<FluidVariant> filter,
			SmartBlockEntity be,
			long capacity // FIXED: Changed from int to long
	) {
		return new FilteringFluidTankBehaviour(TYPE, filter, be, 1, capacity, false);
	}

	/**
	 * Fabric Transfer API filtering wrapper
	 */
	private class FilteringInternalHandler extends InternalFluidHandler {

		public FilteringInternalHandler(Storage<FluidVariant>[] handlers, boolean enforceVariety) {
			super(handlers, enforceVariety);
		}

		@Override
		public long insert(FluidVariant resource, long maxAmount, TransactionContext transaction) {
			if (!filter.test(resource))
				return 0;

			return super.insert(resource, maxAmount, transaction);
		}
	}
}
