package plus.dragons.createenchantmentindustry.entry;

import com.simibubi.create.content.fluids.VirtualFluid;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.fabric.SimpleFlowableFluid;
import com.tterrag.registrate.util.entry.FluidEntry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import plus.dragons.createenchantmentindustry.EnchantmentIndustry;

import static plus.dragons.createenchantmentindustry.EnchantmentIndustry.REGISTRATE;

public class CeiFluids {

	public static final ResourceLocation EXPERIENCE_STILL_RL = EnchantmentIndustry.genRL("fluid/experience_still");
	public static final ResourceLocation EXPERIENCE_FLOW_RL = EnchantmentIndustry.genRL("fluid/experience_flow");

	public static final FluidEntry<VirtualFluid> EXPERIENCE = REGISTRATE.virtualFluid("experience",
					EXPERIENCE_STILL_RL, EXPERIENCE_FLOW_RL)
			.lang("Liquid Experience")
			.source(prop -> new VirtualFluid(prop, true))
			.register();

	public static final ResourceLocation HYPER_EXPERIENCE_STILL_RL = EnchantmentIndustry.genRL("fluid/hyper_experience_still");
	public static final ResourceLocation HYPER_EXPERIENCE_FLOW_RL = EnchantmentIndustry.genRL("fluid/hyper_experience_flow");

	public static final FluidEntry<VirtualFluid> HYPER_EXPERIENCE = REGISTRATE.virtualFluid("hyper_experience",
					HYPER_EXPERIENCE_STILL_RL, HYPER_EXPERIENCE_FLOW_RL)
			.lang("Liquid Hyper Experience")
			.source(prop -> new VirtualFluid(prop, true))
			.register();

	public static final ResourceLocation INK_STILL_RL = EnchantmentIndustry.genRL("fluid/ink_still");
	public static final ResourceLocation INK_FLOW_RL = EnchantmentIndustry.genRL("fluid/ink_flow");

	// FIXED: Changed type to Flowing to match the return of .register() in this Registrate version
	public static final FluidEntry<SimpleFlowableFluid.Flowing> INK = REGISTRATE
			.fluid("ink", INK_STILL_RL, INK_FLOW_RL)
			.lang("Ink")
			.source(SimpleFlowableFluid.Source::new) // Still define source first for Fabric safety
			.tag(CeiTags.FluidTag.INK.tag)
			.bucket()
			.build()
			.register();

	public static void register() {
	}

	public static void handleInkEffect(LivingEntity entity) {
		if (entity.tickCount % 20 != 0) return;
		if (entity.isEyeInFluid(CeiTags.FluidTag.INK.tag)) {
			entity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, 0, true, false, false));
		}
	}

	public static void registerLavaReaction() {
		// Handled via Mixin in Fabric
	}
}
