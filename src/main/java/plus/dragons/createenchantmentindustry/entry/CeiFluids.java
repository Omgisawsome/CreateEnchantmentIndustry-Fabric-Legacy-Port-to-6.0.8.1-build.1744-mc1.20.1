package plus.dragons.createenchantmentindustry.entry;

import com.simibubi.create.content.fluids.VirtualFluid;
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
			.register();

	public static final ResourceLocation HYPER_EXPERIENCE_STILL_RL = EnchantmentIndustry.genRL("fluid/hyper_experience_still");
	public static final ResourceLocation HYPER_EXPERIENCE_FLOW_RL = EnchantmentIndustry.genRL("fluid/hyper_experience_flow");

	public static final FluidEntry<VirtualFluid> HYPER_EXPERIENCE = REGISTRATE.virtualFluid("hyper_experience",
					HYPER_EXPERIENCE_STILL_RL, HYPER_EXPERIENCE_FLOW_RL)
			.lang("Liquid Hyper Experience")
			.register();

	public static final ResourceLocation INK_STILL_RL = EnchantmentIndustry.genRL("fluid/ink_still");
	public static final ResourceLocation INK_FLOW_RL = EnchantmentIndustry.genRL("fluid/ink_flow");

	// FIXED: Explicitly defining the source before the bucket to satisfy Registrate's state check
	public static final FluidEntry<SimpleFlowableFluid.Flowing> INK = REGISTRATE
			.fluid("ink", INK_STILL_RL, INK_FLOW_RL)
			.lang("Ink")
			.source(SimpleFlowableFluid.Source::new) // Define source first
			.tag(CeiTags.FluidTag.INK.tag)           // Add tags
			.bucket()                                // Now bucket will find the source correctly
			.build()
			.register();

	public static void register() {
		// Trigger static init
	}

	public static void handleInkEffect(LivingEntity entity) {
		if (entity.tickCount % 20 != 0) return;
		if (entity.isEyeInFluid(CeiTags.FluidTag.INK.tag)) {
			entity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, 0, true, false, false));
		}
	}
}
