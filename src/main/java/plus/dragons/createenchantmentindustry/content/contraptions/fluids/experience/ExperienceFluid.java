package plus.dragons.createenchantmentindustry.content.contraptions.fluids.experience;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ExperienceOrb;
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
import plus.dragons.createenchantmentindustry.entry.CeiFluids;

import javax.annotation.Nullable;

public abstract class ExperienceFluid extends FlowingFluid {

	public static void handleSpill(ServerLevel level, BlockPos pos, int amount, Fluid fluid) {
		if (amount <= 0) return;

		double mB = amount / 81.0;
		float xpPerMb = 1f / 20f;
		boolean isHyper = fluid.isSame(CeiFluids.HYPER_EXPERIENCE) || fluid.isSame(CeiFluids.FLOWING_HYPER_EXPERIENCE);

		if (isHyper) {
			xpPerMb *= 10f;
		}

		double totalXp = mB * xpPerMb;
		int xpToSpawn = (int) Math.floor(totalXp);

		if (level.random.nextFloat() < (totalXp - xpToSpawn)) {
			xpToSpawn++;
		}

		if (xpToSpawn > 0) {
			drop(level, Vec3.atCenterOf(pos).add(0, -0.2, 0), xpToSpawn, Vec3.ZERO, isHyper);
		}
	}

	public void awardOrDrop(@Nullable Player player, ServerLevel level, Vec3 pos, Vec3 speed, int amount) {
		double mB = amount / 81.0;
		float xpPerMb = 1f / 20f;
		boolean isHyper = this.isSame(CeiFluids.HYPER_EXPERIENCE) || this.isSame(CeiFluids.FLOWING_HYPER_EXPERIENCE);

		if (isHyper) {
			xpPerMb *= 10f;
		}

		double totalXp = mB * xpPerMb;
		int xpToSpawn = (int) Math.floor(totalXp);
		if (level.random.nextFloat() < (totalXp - xpToSpawn)) xpToSpawn++;

		if (xpToSpawn <= 0) return;

		if (player != null) {
			player.giveExperiencePoints(xpToSpawn);
		} else {
			drop(level, pos, xpToSpawn, speed, isHyper);
		}
	}

	public static void drop(ServerLevel level, Vec3 pos, int xpAmount) {
		drop(level, pos, xpAmount, Vec3.ZERO, false);
	}

	// Overload for HyperExperienceFluid to call
	public static void drop(ServerLevel level, Vec3 pos, int xpAmount, boolean isHyper) {
		drop(level, pos, xpAmount, Vec3.ZERO, isHyper);
	}

	public static void drop(ServerLevel level, Vec3 pos, int xpAmount, Vec3 speed, boolean isHyper) {
		int remaining = xpAmount;
		while (remaining > 0) {
			int value = ExperienceOrb.getExperienceValue(remaining);
			remaining -= value;

			// Choose between normal and blue orbs
			ExperienceOrb orb;
			if (isHyper) {
				orb = new HyperExperienceOrb(level, pos.x, pos.y, pos.z, value);
			} else {
				orb = new ExperienceOrb(level, pos.x, pos.y, pos.z, value);
			}

			orb.setDeltaMovement(
					speed.x + (level.random.nextDouble() - 0.5D) * 0.1D,
					speed.y + 0.05D,
					speed.z + (level.random.nextDouble() - 0.5D) * 0.1D
			);

			level.addFreshEntity(orb);
		}
	}

	@Override public Fluid getFlowing() { return CeiFluids.FLOWING_EXPERIENCE; }
	@Override public Fluid getSource() { return CeiFluids.EXPERIENCE; }
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
		return fluid == getSource() || fluid == getFlowing() ||
				fluid == CeiFluids.HYPER_EXPERIENCE || fluid == CeiFluids.FLOWING_HYPER_EXPERIENCE;
	}

	@Override protected void beforeDestroyingBlock(LevelAccessor world, BlockPos pos, BlockState state) {}

	public static class Source extends ExperienceFluid {
		@Override public boolean isSource(FluidState state) { return true; }
		@Override public int getAmount(FluidState state) { return 8; }
	}

	public static class Flowing extends ExperienceFluid {
		@Override public boolean isSource(FluidState state) { return false; }
		@Override public int getAmount(FluidState state) { return state.getValue(LEVEL); }
	}
}
