package plus.dragons.createenchantmentindustry.foundation.mixin;

import com.simibubi.create.content.fluids.OpenEndedPipe;
import io.github.fabricators_of_create.porting_lib.transfer.fluid.FluidTank;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import org.spongepowered.asm.mixin.Mixin;
import plus.dragons.createenchantmentindustry.content.contraptions.fluids.experience.ExperienceFluid;
import plus.dragons.createenchantmentindustry.entry.CeiFluids;
import plus.dragons.createenchantmentindustry.foundation.mixin.fabric.OpenEndFluidHandlerAccessor;

@Mixin(targets = "com.simibubi.create.content.fluids.OpenEndedPipe$OpenEndFluidHandler", remap = false)
public abstract class OpenEndFluidHandlerMixin extends FluidTank {

	public OpenEndFluidHandlerMixin(long capacity) {
		super(capacity);
	}

	@Override
	public long insert(FluidVariant resource, long maxAmount, TransactionContext transaction) {
		long filled = super.insert(resource, maxAmount, transaction);

		if (filled <= 0) return filled;

		Fluid fluid = resource.getFluid();

		// FIX: Removed .get() because these are 'Source' types
		boolean isExp = fluid.isSame(CeiFluids.EXPERIENCE) ||
				fluid.isSame(CeiFluids.HYPER_EXPERIENCE);

		if (isExp) {
			transaction.addCloseCallback((context, result) -> {
				if (result.wasCommitted()) {
					try {
						// Access the outer OpenEndedPipe class via the Accessor
						OpenEndFluidHandlerAccessor accessor = (OpenEndFluidHandlerAccessor) this;
						OpenEndedPipe pipeInstance = accessor.getPipe();

						if (pipeInstance != null) {
							Level level = pipeInstance.getWorld();
							if (level instanceof ServerLevel serverLevel) {
								// Execute the spill logic (XP spawning/Enchanting)
								ExperienceFluid.handleSpill(
										serverLevel,
										pipeInstance.getPos(),
										(int) filled,
										fluid
								);
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
		}

		return filled;
	}
}
