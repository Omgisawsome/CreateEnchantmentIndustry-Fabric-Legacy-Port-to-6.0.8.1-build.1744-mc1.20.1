package plus.dragons.createenchantmentindustry.content.contraptions.fluids;

import java.util.Iterator;
import java.util.function.Predicate;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

public class FilteringFluidTankBehaviour extends SmartFluidTankBehaviour {

	protected final Predicate<FluidVariant> filter;

	public FilteringFluidTankBehaviour(
			BehaviourType<SmartFluidTankBehaviour> type,
			Predicate<FluidVariant> filter,
			SmartBlockEntity be,
			int tanks,
			int tankCapacity,
			boolean enforceVariety
	) {
		super(type, be, tanks, tankCapacity, enforceVariety);
		this.filter = filter;

		// Wrap Create's Fabric storage instead of extending Forge tanks
		this.capability = new FilteringStorage(this.capability);
	}

	public static FilteringFluidTankBehaviour single(
			Predicate<FluidVariant> filter,
			SmartBlockEntity be,
			int capacity
	) {
		return new FilteringFluidTankBehaviour(TYPE, filter, be, 1, capacity, false);
	}

	/**
	 * Fabric Transfer API filtering wrapper
	 */
	private class FilteringStorage implements Storage<FluidVariant> {

		private final Storage<FluidVariant> delegate;

		private FilteringStorage(Storage<FluidVariant> delegate) {
			this.delegate = delegate;
		}

		@Override
		public long insert(
				FluidVariant resource,
				long maxAmount,
				TransactionContext transaction
		) {
			if (!filter.test(resource))
				return 0;

			return delegate.insert(resource, maxAmount, transaction);
		}

		@Override
		public long extract(
				FluidVariant resource,
				long maxAmount,
				TransactionContext transaction
		) {
			return delegate.extract(resource, maxAmount, transaction);
		}

		@Override
		public Iterator<StorageView<FluidVariant>> iterator() {
			return delegate.iterator();
		}
	}
}
