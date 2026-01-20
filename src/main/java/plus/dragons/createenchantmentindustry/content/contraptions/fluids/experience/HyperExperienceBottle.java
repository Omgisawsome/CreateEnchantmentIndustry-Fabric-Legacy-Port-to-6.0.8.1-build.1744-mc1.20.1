package plus.dragons.createenchantmentindustry.content.contraptions.fluids.experience;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import plus.dragons.createenchantmentindustry.entry.CeiEntityTypes;
import plus.dragons.createenchantmentindustry.entry.CeiFluids;
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
			serverLevel.levelEvent(2002, this.blockPosition(), PotionUtils.getColor(Potions.WATER));

			int amount = 3 + this.level().random.nextInt(5) + this.level().random.nextInt(5);

			// FABRIC FIX: Access the drop logic via the CeiFluids entry directly.
			// If the fluid instance itself doesn't have .drop(),
			// we use the HyperExperienceFluid class directly.
			HyperExperienceFluid.drop(serverLevel, this.position(), amount);

			this.discard();
		}
	}
}
