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

		INK = Registry.register(BuiltInRegistries.FLUID,
				new ResourceLocation(EnchantmentIndustry.MOD_ID, "ink"), new ExperienceFluid.Source());
		FLOWING_INK = Registry.register(BuiltInRegistries.FLUID,
				new ResourceLocation(EnchantmentIndustry.MOD_ID, "flowing_ink"), new ExperienceFluid.Flowing());

		// Attribute Registration
		FluidVariantAttributes.register(EXPERIENCE, new ExperienceFluidAttributes("fluid.create_enchantment_industry.experience"));
		FluidVariantAttributes.register(HYPER_EXPERIENCE, new ExperienceFluidAttributes("fluid.create_enchantment_industry.hyper_experience"));
		FluidVariantAttributes.register(INK, new ExperienceFluidAttributes("fluid.create_enchantment_industry.ink"));
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

		// Note: Viscosity/Temperature are not part of the standard Fabric Attribute Handler interface
		// in the same way they are on Forge. If you need custom viscosity, it is usually handled
		// via tags or a custom Fluid API implementation.
	}
}
