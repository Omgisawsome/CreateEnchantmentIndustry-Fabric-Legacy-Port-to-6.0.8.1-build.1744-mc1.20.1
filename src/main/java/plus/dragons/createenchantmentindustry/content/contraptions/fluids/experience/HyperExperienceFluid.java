package plus.dragons.createenchantmentindustry.content.contraptions.fluids.experience;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import plus.dragons.createenchantmentindustry.entry.CeiFluids;

public abstract class HyperExperienceFluid extends FlowingFluid {

	/**
	 * Helper to check if a fluid is Hyper Experience (Source or Flowing)
	 */
	public static boolean is(Fluid fluid) {
		return fluid == CeiFluids.HYPER_EXPERIENCE || fluid == CeiFluids.FLOWING_HYPER_EXPERIENCE;
	}

	public static void drop(ServerLevel level, Vec3 pos, int xpAmount) {
		ExperienceFluid.drop(level, pos, xpAmount, true);
	}

	public void awardOrDrop(@Nullable Player player, ServerLevel level, Vec3 pos, Vec3 speed, int amount) {
		double mB = amount / 81.0;
		int xpAmount = (int) Math.floor((mB / 20.0) * 10.0);

		if (xpAmount <= 0 && amount > 0) {
			if (level.random.nextFloat() < (mB / 20.0 * 10.0)) xpAmount = 1;
		}

		if (xpAmount <= 0) return;

		if (player != null) {
			player.giveExperiencePoints(xpAmount);
		} else {
			ExperienceFluid.drop(level, pos, xpAmount, speed, true);
		}
	}

	@Override public Fluid getFlowing() { return CeiFluids.FLOWING_HYPER_EXPERIENCE; }
	@Override public Fluid getSource() { return CeiFluids.HYPER_EXPERIENCE; }
	@Override protected boolean canConvertToSource(Level level) { return false; }
	@Override protected boolean canBeReplacedWith(FluidState state, BlockGetter world, BlockPos pos, Fluid fluid, Direction direction) { return false; }
	@Override public Vec3 getFlow(BlockGetter world, BlockPos pos, FluidState state) { return Vec3.ZERO; }
	@Override public int getTickDelay(LevelReader world) { return 5; }
	@Override protected float getExplosionResistance() { return 100.0F; }
	@Override protected int getSlopeFindDistance(LevelReader world) { return 4; }
	@Override protected int getDropOff(LevelReader world) { return 1; }
	@Override public Item getBucket() { return Items.AIR; }
	@Override protected BlockState createLegacyBlock(FluidState state) { return Blocks.AIR.defaultBlockState(); }

	@Override
	public boolean isSame(Fluid fluid) {
		// STRICT ISOLATION: This prevents filters from confusing Blue XP with Green XP
		return fluid == getSource() || fluid == getFlowing();
	}

	@Override protected void beforeDestroyingBlock(LevelAccessor world, BlockPos pos, BlockState state) {}

	public static class Source extends HyperExperienceFluid {
		@Override public boolean isSource(FluidState state) { return true; }
		@Override public int getAmount(FluidState state) { return 8; }
	}

	public static class Flowing extends HyperExperienceFluid {
		@Override public boolean isSource(FluidState state) { return false; }
		@Override public int getAmount(FluidState state) { return state.getValue(LEVEL); }
	}
}
