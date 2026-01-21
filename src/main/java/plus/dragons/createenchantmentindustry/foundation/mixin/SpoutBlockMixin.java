package plus.dragons.createenchantmentindustry.foundation.mixin;

import static plus.dragons.createenchantmentindustry.EnchantmentIndustry.UNIT_PER_MB;

import javax.annotation.ParametersAreNonnullByDefault;

import org.spongepowered.asm.mixin.Mixin;

import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.content.fluids.spout.SpoutBlock;
import com.simibubi.create.content.fluids.spout.SpoutBlockEntity;
import com.simibubi.create.foundation.block.IBE;

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
		if (!state.hasBlockEntity() || state.getBlock() == newState.getBlock()) {
			super.onRemove(state, level, pos, newState, isMoving);
			return;
		}

		if (level instanceof ServerLevel serverLevel) {
			withBlockEntityDo(level, pos, te -> {
				var fluidStack = ((SpoutBlockEntityAccessor) te).getTank().getPrimaryHandler().getFluid();

				// Check for Experience or Hyper Experience
				if (fluidStack.getFluid().isSame(CeiFluids.EXPERIENCE) || fluidStack.getFluid().isSame(CeiFluids.HYPER_EXPERIENCE)) {

					// MATH FIX: Ensure we have at least 1 XP point if there is fluid present
					long mbAmount = fluidStack.getAmount();
					int xpToDrop = (int) (mbAmount / UNIT_PER_MB);

					// If there's a tiny bit of fluid (less than 1 full point),
					// we still want to drop 1 point so it's not lost forever.
					if (xpToDrop <= 0 && mbAmount > 0) {
						xpToDrop = 1;
					}

					if (xpToDrop > 0) {
						ExperienceFluid.drop(
								serverLevel,
								VecHelper.getCenterOf(pos),
								xpToDrop
						);
					}
				}
			});
		}
		super.onRemove(state, level, pos, newState, isMoving);
	}
}
