package plus.dragons.createenchantmentindustry.content.contraptions.fluids.experience;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import plus.dragons.createenchantmentindustry.EnchantmentIndustry;

public abstract class ExperienceFluid extends Fluid {
	protected final int xpRatio;

	protected ExperienceFluid(int xpRatio) {
		this.xpRatio = xpRatio;
	}

	public void applyAdditionalEffects(LivingEntity entity, int xpAmount) {}

	/**
	 * Logic for converting fluid amount to XP and awarding to player or dropping as orbs.
	 */
	public void awardOrDrop(@Nullable Player player, ServerLevel level, Vec3 pos, Vec3 motion, int realFluidAmount) {
		int xp = (int) ((realFluidAmount / EnchantmentIndustry.UNIT_PER_MB) * xpRatio);
		if (xp <= 0 && realFluidAmount > 0) xp = 1; // Ensure even tiny amounts drop at least 1 XP
		if (xp <= 0) return;

		if (player != null) {
			player.giveExperiencePoints(xp);
			applyAdditionalEffects(player, xp);
		} else {
			drop(level, pos, xp);
		}
	}

	/**
	 * Helper for OpenEndedPipes and spills.
	 */
	public static void handleSpill(ServerLevel level, BlockPos pos, long fluidAmount, Fluid fluid) {
		if (fluid instanceof ExperienceFluid expFluid) {
			expFluid.awardOrDrop(null, level, Vec3.atCenterOf(pos), Vec3.ZERO, (int) fluidAmount);
		}
	}

	public static void drop(ServerLevel level, Vec3 pos, int xp) {
		while (xp > 0) {
			int orbValue = ExperienceOrb.getExperienceValue(xp);
			xp -= orbValue;
			level.addFreshEntity(new ExperienceOrb(level, pos.x, pos.y, pos.z, orbValue));
		}
	}

	@Override public Item getBucket() { return Items.AIR; }
	@Override protected boolean canBeReplacedWith(FluidState state, BlockGetter level, BlockPos pos, Fluid fluid, Direction direction) { return false; }
	@Override protected Vec3 getFlow(BlockGetter blockReader, BlockPos pos, FluidState fluidState) { return Vec3.ZERO; }
	@Override public int getTickDelay(LevelReader level) { return 0; }
	@Override protected float getExplosionResistance() { return 100.0F; }
	@Override public float getHeight(FluidState state, BlockGetter level, BlockPos pos) { return 0; }
	@Override public float getOwnHeight(FluidState state) { return 0; }
	@Override protected BlockState createLegacyBlock(FluidState state) { return Blocks.AIR.defaultBlockState(); }
	@Override public VoxelShape getShape(FluidState state, BlockGetter level, BlockPos pos) { return Shapes.empty(); }

	public static class Flowing extends ExperienceFluid {
		public Flowing() { super(1); }
		@Override public boolean isSource(FluidState state) { return false; }
		@Override public int getAmount(FluidState state) { return 0; }
	}

	public static class Source extends ExperienceFluid {
		public Source() { super(1); }
		@Override public boolean isSource(FluidState state) { return true; }
		@Override public int getAmount(FluidState state) { return 8; }
	}
}
