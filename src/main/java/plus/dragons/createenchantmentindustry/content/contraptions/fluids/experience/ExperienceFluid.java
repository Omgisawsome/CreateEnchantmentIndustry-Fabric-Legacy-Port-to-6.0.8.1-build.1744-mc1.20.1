package plus.dragons.createenchantmentindustry.content.contraptions.fluids.experience;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * Base Experience Fluid logic.
 * Create 6 / Fabric compatible.
 */
public class ExperienceFluid {

	/** 81 units = 1 XP (matches vanilla orb math) */
	public static final int UNIT_PER_MB = 81;

	protected final int xpRatio;

	public ExperienceFluid(int xpRatio) {
		this.xpRatio = xpRatio;
	}

	public ExperienceFluid() {
		this(1);
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

	protected void drop(ServerLevel level, Vec3 pos, int xp) {
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
