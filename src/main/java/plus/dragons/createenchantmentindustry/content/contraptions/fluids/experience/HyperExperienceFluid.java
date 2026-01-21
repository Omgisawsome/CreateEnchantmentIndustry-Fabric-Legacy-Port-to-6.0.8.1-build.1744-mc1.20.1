package plus.dragons.createenchantmentindustry.content.contraptions.fluids.experience;

import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.material.FluidState;

public abstract class HyperExperienceFluid extends ExperienceFluid {
	protected HyperExperienceFluid() { super(10); }

	@Override
	public void applyAdditionalEffects(LivingEntity entity, int expAmount) {
		int duration = 200 * Mth.ceillog2(Math.max(1, expAmount));
		entity.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, duration));
		entity.addEffect(new MobEffectInstance(MobEffects.GLOWING, duration));
	}

	public static class Flowing extends HyperExperienceFluid {
		public Flowing() { super(); }
		@Override public boolean isSource(FluidState state) { return false; }
		@Override public int getAmount(FluidState state) { return 0; }
	}

	public static class Source extends HyperExperienceFluid {
		public Source() { super(); }
		@Override public boolean isSource(FluidState state) { return true; }
		@Override public int getAmount(FluidState state) { return 8; }
	}
}
