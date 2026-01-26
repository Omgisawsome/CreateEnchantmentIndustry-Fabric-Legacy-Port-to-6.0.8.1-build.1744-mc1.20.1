package plus.dragons.createenchantmentindustry.content.contraptions.fluids;

import java.lang.reflect.Field;
import java.util.function.Predicate;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;

import io.github.fabricators_of_create.porting_lib.fluids.FluidStack;
import io.github.fabricators_of_create.porting_lib.transfer.fluid.FluidTank;

public class FilteringFluidTankBehaviour extends SmartFluidTankBehaviour {

	public static final BehaviourType<FilteringFluidTankBehaviour> TYPE = new BehaviourType<>();

	private Predicate<FluidStack> filter;

	@SuppressWarnings("unchecked")
	public FilteringFluidTankBehaviour(BehaviourType<?> type, SmartBlockEntity be, int tanks, long tankCapacity, boolean enforce) {
		super((BehaviourType) type, be, tanks, tankCapacity, enforce);
		this.filter = f -> true;

		if (this.tanks != null) {
			for (SmartFluidTankBehaviour.TankSegment segment : this.tanks) {
				try {
					Field tankField = SmartFluidTankBehaviour.TankSegment.class.getDeclaredField("tank");
					tankField.setAccessible(true);
					Object internalTank = tankField.get(segment);
					if (internalTank instanceof FluidTank fluidTank) {
						fluidTank.setValidator(this::isFluidValid);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static FilteringFluidTankBehaviour single(Predicate<FluidStack> filter, SmartBlockEntity be, long capacity) {
		FilteringFluidTankBehaviour behaviour = new FilteringFluidTankBehaviour(TYPE, be, 1, capacity, false);
		behaviour.filter = filter;
		return behaviour;
	}

	public boolean isFluidValid(FluidStack fluid) {
		if (filter == null) return true;
		return filter.test(fluid);
	}
}
