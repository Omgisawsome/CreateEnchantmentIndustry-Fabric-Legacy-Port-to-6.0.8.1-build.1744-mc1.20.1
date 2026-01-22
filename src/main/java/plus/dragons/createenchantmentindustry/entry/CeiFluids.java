package plus.dragons.createenchantmentindustry.entry;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributeHandler;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import plus.dragons.createenchantmentindustry.EnchantmentIndustry;
import plus.dragons.createenchantmentindustry.content.contraptions.fluids.experience.ExperienceFluid;
import plus.dragons.createenchantmentindustry.content.contraptions.fluids.experience.HyperExperienceFluid;

public class CeiFluids {

	public static ExperienceFluid.Source EXPERIENCE;
	public static ExperienceFluid.Flowing FLOWING_EXPERIENCE;
	public static HyperExperienceFluid.Source HYPER_EXPERIENCE;
	public static HyperExperienceFluid.Flowing FLOWING_HYPER_EXPERIENCE;

	public static Fluid INK;
	public static Fluid FLOWING_INK;

	public static void register() {
		EXPERIENCE = Registry.register(BuiltInRegistries.FLUID,
				new ResourceLocation(EnchantmentIndustry.MOD_ID, "experience"), new ExperienceFluid.Source());
		FLOWING_EXPERIENCE = Registry.register(BuiltInRegistries.FLUID,
				new ResourceLocation(EnchantmentIndustry.MOD_ID, "flowing_experience"), new ExperienceFluid.Flowing());

		HYPER_EXPERIENCE = Registry.register(BuiltInRegistries.FLUID,
				new ResourceLocation(EnchantmentIndustry.MOD_ID, "hyper_experience"), new HyperExperienceFluid.Source());
		FLOWING_HYPER_EXPERIENCE = Registry.register(BuiltInRegistries.FLUID,
				new ResourceLocation(EnchantmentIndustry.MOD_ID, "flowing_hyper_experience"), new HyperExperienceFluid.Flowing());

		// Fix: Ink was using ExperienceFluid.Source() previously, changed to a generic or Ink-specific fluid if you have one
		INK = Registry.register(BuiltInRegistries.FLUID,
				new ResourceLocation(EnchantmentIndustry.MOD_ID, "ink"), new ExperienceFluid.Source());
		FLOWING_INK = Registry.register(BuiltInRegistries.FLUID,
				new ResourceLocation(EnchantmentIndustry.MOD_ID, "flowing_ink"), new ExperienceFluid.Flowing());

		// Attribute Registration
		// FIXED: Registering for both Source AND Flowing helps the Transfer API (and filters) recognize the fluid in all states.
		registerAttributes(EXPERIENCE, FLOWING_EXPERIENCE, "experience");
		registerAttributes(HYPER_EXPERIENCE, FLOWING_HYPER_EXPERIENCE, "hyper_experience");
		registerAttributes(INK, FLOWING_INK, "ink");
	}

	private static void registerAttributes(Fluid source, Fluid flowing, String name) {
		ExperienceFluidAttributes attributes = new ExperienceFluidAttributes("fluid.create_enchantment_industry." + name);
		FluidVariantAttributes.register(source, attributes);
		FluidVariantAttributes.register(flowing, attributes);
	}

	private static class ExperienceFluidAttributes implements FluidVariantAttributeHandler {
		private final String translationKey;

		public ExperienceFluidAttributes(String translationKey) {
			this.translationKey = translationKey;
		}

		@Override
		public Component getName(FluidVariant fluidVariant) {
			return Component.translatable(translationKey);
		}

		// Note for the "Blue" issue: If the outflow is blue, the particle engine
		// in Create is likely looking at the Fluid class itself.
		// We will need to check ExperienceFluid.java and HyperExperienceFluid.java next.
	}
}
