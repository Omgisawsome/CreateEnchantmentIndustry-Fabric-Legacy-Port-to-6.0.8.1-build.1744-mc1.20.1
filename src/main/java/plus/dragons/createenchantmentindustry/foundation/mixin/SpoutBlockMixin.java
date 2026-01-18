package plus.dragons.createenchantmentindustry.foundation.mixin;

import javax.annotation.ParametersAreNonnullByDefault;

import org.spongepowered.asm.mixin.Mixin;

import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.content.fluids.spout.SpoutBlock;
import com.simibubi.create.content.fluids.spout.SpoutBlockEntity;
import com.simibubi.create.foundation.block.IBE;

// FIXED: Moved to Catnip math
import net.createmod.catnip.math.VecHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import plus.dragons.createenchantmentindustry.content.contraptions.fluids.experience.ExperienceFluid;
import plus.dragons.createenchantmentindustry.entry.CeiFluids;

@ParametersAreNonnullByDefault
@Mixin(value = SpoutBlock.class)
public abstract class SpoutBlockMixin extends Block implements IWrenchable, IBE<SpoutBlockEntity> {
	public SpoutBlockMixin(Properties pProperties) {
		super(pProperties);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
		if (!state.hasBlockEntity() || state.getBlock() == newState.getBlock())
			return;
		if (level instanceof ServerLevel serverLevel) {
			withBlockEntityDo(level, pos, te -> {
				var fluidStack = ((SpoutBlockEntityAccessor) te).getTank().getPrimaryHandler().getFluid();

				// FIXED: Using .is() check and Double Cast to avoid "Inconvertible Types"
				if (CeiFluids.EXPERIENCE.is(fluidStack.getFluid())) {
					ExperienceFluid expFluid = (ExperienceFluid) (Object) fluidStack.getFluid();
					expFluid.drop(serverLevel, VecHelper.getCenterOf(pos), (int) fluidStack.getAmount());
				}
			});
		}
		super.onRemove(state, level, pos, newState, isMoving);
	}
}
