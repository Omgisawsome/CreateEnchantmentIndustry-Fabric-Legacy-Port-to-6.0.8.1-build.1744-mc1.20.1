package plus.dragons.createenchantmentindustry.foundation.mixin;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// UPDATED IMPORTS FOR BUILD 1744

import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import net.createmod.catnip.data.Couple;
import net.createmod.catnip.math.VecHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import plus.dragons.createenchantmentindustry.content.contraptions.fluids.experience.ExperienceFluid;
import plus.dragons.createenchantmentindustry.entry.CeiFluids;

@Mixin(value = BasinBlockEntity.class)
public abstract class BasinBlockEntityMixin extends SmartBlockEntity implements IHaveGoggleInformation {

	// In Build 1744, tanks might be protected or named differently.
	// If 'tanks' is still red, change the Shadow to:
	// @Shadow(remap = false) public Couple<SmartFluidTankBehaviour> tanks;
	@Shadow(remap = false)
	protected Couple<SmartFluidTankBehaviour> tanks;

	public BasinBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	// Support Experience Drop with Block Break
	@Inject(method = "destroy", at = @At(value = "RETURN"), remap = false)
	private void injected(CallbackInfo ci) {
		if (!(level instanceof ServerLevel serverLevel))
			return;

		if (tanks == null)
			return;

		for (SmartFluidTankBehaviour tank : tanks) {
			var fluidStack = tank.getPrimaryHandler().getFluid();

			// Use the "Double Cast" (Object) to bypass the inconvertible types error
			if (CeiFluids.EXPERIENCE.is(fluidStack.getFluid())) {
				ExperienceFluid expFluid = (ExperienceFluid) (Object) fluidStack.getFluid();
				expFluid.drop(serverLevel, VecHelper.getCenterOf(getBlockPos()), (int) fluidStack.getAmount());
			}
		}
	}
}
