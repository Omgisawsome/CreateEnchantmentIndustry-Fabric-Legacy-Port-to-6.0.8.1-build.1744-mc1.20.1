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
		Fluid fluid = resource.getFluid();

		if (maxAmount <= 0) return filled;

		boolean isExp = fluid.isSame(CeiFluids.EXPERIENCE) ||
				fluid.isSame(CeiFluids.HYPER_EXPERIENCE);

		if (isExp) {
			transaction.addCloseCallback((context, result) -> {
				if (result.wasCommitted()) {
					try {
						// This now works because the Accessor is registered in the mixins.json
						OpenEndFluidHandlerAccessor accessor = (OpenEndFluidHandlerAccessor) this;
						OpenEndedPipe pipeInstance = accessor.getPipe();

						if (pipeInstance != null) {
							Level level = pipeInstance.getWorld();
							if (level instanceof ServerLevel serverLevel) {
								ExperienceFluid.handleSpill(
										serverLevel,
										pipeInstance.getOutputPos(),
										(int) maxAmount,
										fluid
								);
							}
						}
					} catch (Throwable t) {
						System.err.println("[CEI] Failed to handle XP spill from Open Ended Pipe:");
						t.printStackTrace();
					}
				}
			});
			return maxAmount;
		}

		return filled;
	}
}
