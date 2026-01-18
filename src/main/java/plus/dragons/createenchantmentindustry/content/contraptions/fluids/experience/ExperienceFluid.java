package plus.dragons.createenchantmentindustry.content.contraptions.fluids.experience;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

import org.jetbrains.annotations.Nullable;

/**
 * Base Experience Fluid logic.
 * Create 6 / Fabric compatible.
 */
public class ExperienceFluid extends Fluid {

	/** 81 units = 1 XP (matches vanilla orb math) */
	public static final int UNIT_PER_MB = 81;

	protected final int xpRatio;

	public ExperienceFluid(int xpRatio) {
		this.xpRatio = xpRatio;
	}

	public ExperienceFluid() {
		this(1);
	}

	@Override
	public Item getBucket() {
		return null;
	}

	@Override
	protected boolean canBeReplacedWith(FluidState state, BlockGetter level, BlockPos pos, Fluid fluid, Direction direction) {
		return false;
	}

	@Override
	protected Vec3 getFlow(BlockGetter blockReader, BlockPos pos, FluidState fluidState) {
		return null;
	}

	@Override
	public int getTickDelay(LevelReader level) {
		return 0;
	}

	@Override
	protected float getExplosionResistance() {
		return 0;
	}

	@Override
	public float getHeight(FluidState state, BlockGetter level, BlockPos pos) {
		return 0;
	}

	@Override
	public float getOwnHeight(FluidState state) {
		return 0;
	}

	@Override
	protected BlockState createLegacyBlock(FluidState state) {
		return null;
	}

	@Override
	public boolean isSource(FluidState state) {
		return false;
	}

	@Override
	public int getAmount(FluidState state) {
		return 0;
	}

	@Override
	public VoxelShape getShape(FluidState state, BlockGetter level, BlockPos pos) {
		return null;
	}

	/* ------------------------------------------------------------
	 * Orb conversion
	 * ------------------------------------------------------------ */

	public ExperienceOrb convertToOrb(
			Level level,
			double x,
			double y,
			double z,
			int realFluidAmount
	) {
		int xp = (realFluidAmount / UNIT_PER_MB) * xpRatio;
		return new ExperienceOrb(level, x, y, z, xp);
	}

	/* ------------------------------------------------------------
	 * XP handling
	 * ------------------------------------------------------------ */

	public void awardOrDrop(
			@Nullable Player player,
			ServerLevel level,
			Vec3 pos,
			Vec3 speed,
			int realFluidAmount
	) {
		int xp = (realFluidAmount / UNIT_PER_MB) * xpRatio;

		if (xp <= 0)
			return;

		if (player != null) {
			player.giveExperiencePoints(xp);
			applyAdditionalEffects(player, xp);
			return;
		}

		drop(level, pos, xp);
	}

	public void drop(ServerLevel level, Vec3 pos, int xp) {
		while (xp > 0) {
			int orbValue = ExperienceOrb.getExperienceValue(xp);
			xp -= orbValue;

			if (!ExperienceOrb.tryMergeToExisting(level, pos, orbValue)) {
				level.addFreshEntity(
						new ExperienceOrb(level, pos.x, pos.y, pos.z, orbValue)
				);
			}
		}
	}

	/* ------------------------------------------------------------
	 * Extension hooks
	 * ------------------------------------------------------------ */

	public void applyAdditionalEffects(LivingEntity entity, int xpAmount) {
		// overridden by HyperExperienceFluid
	}

	public int getXpRatio() {
		return xpRatio;
	}
}
