package plus.dragons.createenchantmentindustry.foundation.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.content.fluids.tank.CreativeFluidTankBlockEntity;
import com.simibubi.create.content.fluids.tank.FluidTankBlock;
import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import com.simibubi.create.foundation.block.IBE;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import plus.dragons.createenchantmentindustry.content.contraptions.fluids.experience.ExperienceFluid;

// Set remap = true for production/MultiMC compatibility
@Mixin(value = FluidTankBlock.class, remap = true)
public abstract class FluidTankBlockMixin extends Block implements IBE<FluidTankBlockEntity>, IWrenchable {
	public FluidTankBlockMixin(Properties pProperties) {
		super(pProperties);
	}

	/**
	 * Updated for Create 6.0.8.1 (1.20.1)
	 * We inject at HEAD to capture the tank data before the BlockEntity is invalidated.
	 */
	@Inject(method = "onRemove",
			at = @At("HEAD"),
			cancellable = false)
	private void ce_onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving, CallbackInfo ci) {
		// Exit early if it's a client world or if the block isn't actually being replaced
		if (!(level instanceof ServerLevel serverLevel) || state.is(newState.getBlock()))
			return;

		BlockEntity be = level.getBlockEntity(pos);
		if (!(be instanceof FluidTankBlockEntity tankBE) || be instanceof CreativeFluidTankBlockEntity)
			return;

		// Create 6.0 multiblock logic: ensure we get the correct controller for experience drops
		FluidTankBlockEntity controllerBE = tankBE.getControllerBE();
		if (controllerBE == null) return;

		var fluidStack = controllerBE.getTankInventory().getFluid();

		// Check if the fluid is liquid experience
		if (fluidStack.getFluid() instanceof ExperienceFluid) {
			long amount = fluidStack.getAmount();
			int totalTanks = controllerBE.getTotalTankSize();
			Vec3 center = Vec3.atCenterOf(pos);

			if (totalTanks <= 1) {
				// Drop the entire contents if it's a single tank
				ExperienceFluid.drop(serverLevel, center, (int) amount);
			} else {
				// Create 6.0 uses a capacity multiplier for multiblock tanks
				long capacityPerBlock = FluidTankBlockEntity.getCapacityMultiplier();

				// Calculate how much should drop from this specific segment
				long toDrop = Math.min(amount, capacityPerBlock);
				ExperienceFluid.drop(serverLevel, center, (int) toDrop);
			}
		}
	}
}
