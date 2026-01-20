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

@Mixin(value = FluidTankBlock.class, remap = false)
public abstract class FluidTankBlockMixin extends Block implements IBE<FluidTankBlockEntity>, IWrenchable {
	public FluidTankBlockMixin(Properties pProperties) {
		super(pProperties);
	}

	@Inject(method = "onRemove", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/world/level/Level;removeBlockEntity(Lnet/minecraft/core/BlockPos;)V"),
			cancellable = true)
	private void injected(BlockState state, Level level, BlockPos pos, BlockState newState, boolean var4, CallbackInfo ci) {
		if (!(level instanceof ServerLevel serverLevel))
			return;

		BlockEntity be = level.getBlockEntity(pos);
		if (!(be instanceof FluidTankBlockEntity tankBE) || be instanceof CreativeFluidTankBlockEntity)
			return;

		FluidTankBlockEntity controllerBE = tankBE.getControllerBE();
		if (controllerBE == null) return;

		var fluidStack = controllerBE.getTankInventory().getFluid();
		if (fluidStack.getFluid() instanceof ExperienceFluid expFluid) {
			// Get backup of amount before we start removing logic
			long amount = fluidStack.getAmount();
			int maxSize = controllerBE.getTotalTankSize();

			// FABRIC FIX: Instead of manual splitMulti (which is internal/different on Fabric),
			// we let the original code proceed after we handle our drop logic,
			// or we call the removal logic specifically.

			Vec3 center = Vec3.atCenterOf(pos);

			if (maxSize <= 1) {
				ExperienceFluid.drop(serverLevel, center, (int) amount);
			} else {
				// Fabric uses different capacity multipliers.
				// We drop the proportional amount for one block.
				long capacityPerBlock = FluidTankBlockEntity.getCapacityMultiplier();
				long toDrop = Math.min(amount, capacityPerBlock);
				ExperienceFluid.drop(serverLevel, center, (int) toDrop);
			}

			// We don't cancel here anymore to let Create's native multiblock
			// destruction logic (which is complex on Fabric) run its course.
		}
	}
}
