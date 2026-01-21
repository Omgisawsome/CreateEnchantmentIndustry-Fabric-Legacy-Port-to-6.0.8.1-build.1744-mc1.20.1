package plus.dragons.createenchantmentindustry.content.contraptions.fluids.experience;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import plus.dragons.createenchantmentindustry.entry.CeiEntityTypes;
import plus.dragons.createenchantmentindustry.entry.CeiItems;

public class HyperExperienceBottle extends ThrowableItemProjectile {
	public HyperExperienceBottle(EntityType<? extends HyperExperienceBottle> pEntityType, Level pLevel) {
		super(pEntityType, pLevel);
	}

	public HyperExperienceBottle(double pX, double pY, double pZ, Level pLevel) {
		super(CeiEntityTypes.HYPER_EXPERIENCE_BOTTLE.get(), pX, pY, pZ, pLevel);
	}

	public HyperExperienceBottle(LivingEntity pShooter, Level pLevel) {
		super(CeiEntityTypes.HYPER_EXPERIENCE_BOTTLE.get(), pShooter, pLevel);
	}

	@Override
	protected Item getDefaultItem() {
		return CeiItems.HYPER_EXP_BOTTLE.get();
	}

	@SuppressWarnings("unchecked")
	public static EntityType.Builder<?> build(EntityType.Builder<?> builder) {
		EntityType.Builder<HyperExperienceBottle> entityBuilder = (EntityType.Builder<HyperExperienceBottle>) builder;
		return entityBuilder.sized(.25f, .25f);
	}

	@Override
	protected float getGravity() {
		return 0.07F;
	}

	@Override
	protected void onHit(HitResult pResult) {
		super.onHit(pResult);
		if (this.level() instanceof ServerLevel serverLevel) {
			// FIX: Changed color from water to Hyper Blue/Cyan (0x33FFFB)
			// 2002 is the splash potion sound/particle event
			serverLevel.levelEvent(2002, this.blockPosition(), 0x33FFFB);

			// Base XP for a bottle is roughly 3-11. We multiply this by 10 for "Hyper"
			int baseAmount = 3 + this.level().random.nextInt(5) + this.level().random.nextInt(5);
			int hyperAmount = baseAmount * 10;

			// Now triggers ExperienceFluid.drop(..., true) which spawns the blue orb entity
			HyperExperienceFluid.drop(serverLevel, this.position(), hyperAmount);

			this.discard();
		}
	}
}
