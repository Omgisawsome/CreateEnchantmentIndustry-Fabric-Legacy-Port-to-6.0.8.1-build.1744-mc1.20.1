package plus.dragons.createenchantmentindustry.content.contraptions.fluids.experience;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class ExperienceFluid {

	public static final int UNIT_PER_MB = 81; // keep consistent everywhere
	protected final int xpRatio;

	public ExperienceFluid(int xpRatio) {
		this.xpRatio = xpRatio;
	}

	public ExperienceFluid() {
		this(1);
	}

	public static ExperienceOrb convertToOrb(ServerLevel level, Vec3 pos, int xp) {
		return new ExperienceOrb(level, pos.x, pos.y, pos.z, xp);
	}

	public static void drop(ServerLevel level, Vec3 pos, int realFluidAmount) {
		int fluidAmount = realFluidAmount / UNIT_PER_MB;

		while (fluidAmount > 0) {
			int orbSize = ExperienceOrb.getExperienceValue(fluidAmount);
			fluidAmount -= orbSize;

			if (!ExperienceOrb.tryMergeToExisting(level, pos, orbSize)) {
				level.addFreshEntity(convertToOrb(level, pos, orbSize));
			}
		}
	}

	public static void awardOrDrop(
			@Nullable Player player,
			ServerLevel level,
			Vec3 pos,
			Vec3 speed,
			int realAmount
	) {
		int amount = realAmount / UNIT_PER_MB;

		if (player == null) {
			drop(level, pos, realAmount);
			return;
		}

		player.giveExperiencePoints(amount);
	}

	public void applyAdditionalEffects(LivingEntity entity, int expAmount) {
		// optional hook
	}

	public int getXpRatio() {
		return xpRatio;
	}
}
