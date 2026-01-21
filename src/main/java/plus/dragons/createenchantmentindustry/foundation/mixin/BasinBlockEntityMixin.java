package plus.dragons.createenchantmentindustry.foundation.mixin;

import static plus.dragons.createenchantmentindustry.EnchantmentIndustry.UNIT_PER_MB;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
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

	@Shadow(remap = false)
	protected Couple<SmartFluidTankBehaviour> tanks;

	public BasinBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	/**
	 * Supports Experience dropping when a Basin containing XP fluids is destroyed.
	 */
	@Inject(method = "destroy", at = @At(value = "RETURN"), remap = false)
	private void ceEnchantmentIndustry$dropExperienceOnDestroy(CallbackInfo ci) {
		if (!(level instanceof ServerLevel serverLevel))
			return;

		if (tanks == null)
			return;

		for (SmartFluidTankBehaviour tank : tanks) {
			var fluidStack = tank.getPrimaryHandler().getFluid();

			// FIXED: Use isSame() for direct Fluid comparison instead of .is()
			if (fluidStack.getFluid().isSame(CeiFluids.EXPERIENCE) || fluidStack.getFluid().isSame(CeiFluids.HYPER_EXPERIENCE)) {
				// FIXED: Call static drop method to avoid complex casting issues
				ExperienceFluid.drop(
						serverLevel,
						VecHelper.getCenterOf(getBlockPos()),
						(int) (fluidStack.getAmount() / UNIT_PER_MB)
				);
			}
		}
	}
}
