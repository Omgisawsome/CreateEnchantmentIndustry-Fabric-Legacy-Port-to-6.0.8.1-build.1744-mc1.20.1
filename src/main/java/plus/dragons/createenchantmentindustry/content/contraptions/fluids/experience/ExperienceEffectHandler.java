package plus.dragons.createenchantmentindustry.content.contraptions.fluids.experience;

import com.simibubi.create.content.fluids.OpenEndedPipe;
import io.github.fabricators_of_create.porting_lib.fluids.FluidStack;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import plus.dragons.createenchantmentindustry.foundation.advancement.CeiAdvancements;

/**
 * Fabric / Create 6 compatible experience fluid effect handler.
 */
public final class ExperienceEffectHandler {

	public static boolean canApply(OpenEndedPipe pipe, FluidStack fluid) {
		return fluid.getFluid() instanceof ExperienceFluid;
	}

	public static void apply(OpenEndedPipe pipe, FluidStack fluidStack) {
		// Use getWorld() for Fabric compatibility
		if (!(pipe.getWorld() instanceof ServerLevel level))
			return;

		if (!(fluidStack.getFluid() instanceof ExperienceFluid fluid))
			return;

		BlockPos outputPos = pipe.getOutputPos();
		BlockPos pipePos = pipe.getPos();

		Vec3 orbPos = Vec3.atCenterOf(outputPos);

		// Calculate velocity based on pipe orientation
		Vec3 speed = new Vec3(
				outputPos.getX() - pipePos.getX(),
				outputPos.getY() - pipePos.getY(),
				outputPos.getZ() - pipePos.getZ()
		).scale(0.2);

		int amount = (int) fluidStack.getAmount();

		AABB area = pipe.getAOE();
		var players = level.getEntitiesOfClass(Player.class, area, LivingEntity::isAlive);

		// No players found: drop orbs
		if (players.isEmpty()) {
			fluid.awardOrDrop(null, level, orbPos, speed, amount);
			return;
		}

		// Players found: distribute XP directly
		int perPlayer = amount / players.size();
		int remainder = amount % players.size();

		for (Player player : players) {
			if (player instanceof ServerPlayer sp) {
				CeiAdvancements.A_SHOWER_EXPERIENCE.getTrigger().trigger(sp);
				fluid.awardOrDrop(sp, level, orbPos, speed, perPlayer);
			}
		}

		// Randomly give remainder to one of the players
		if (remainder > 0) {
			Player lucky = players.get(level.random.nextInt(players.size()));
			fluid.awardOrDrop(lucky, level, orbPos, speed, remainder);
		}
	}
}
