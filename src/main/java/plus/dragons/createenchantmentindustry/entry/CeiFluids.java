package plus.dragons.createenchantmentindustry.entry;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributeHandler;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Nullable;
import plus.dragons.createenchantmentindustry.EnchantmentIndustry;
import plus.dragons.createenchantmentindustry.content.contraptions.fluids.experience.ExperienceFluid;
import plus.dragons.createenchantmentindustry.content.contraptions.fluids.experience.HyperExperienceFluid;

public class CeiFluids {

	public static ExperienceFluid.Source EXPERIENCE;
	public static ExperienceFluid.Flowing FLOWING_EXPERIENCE;
	public static HyperExperienceFluid.Source HYPER_EXPERIENCE;
	public static HyperExperienceFluid.Flowing FLOWING_HYPER_EXPERIENCE;

	// Fixed: Ink needs its own Flowing variant to avoid tag errors
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

		// Use Experience as a base for Ink if the Ink class isn't ready,
		// but register them under unique IDs to satisfy the Recipe Manager.
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

		public int getViscosity(FluidVariant variant, @Nullable LevelReader world) {
			return 500;
		}
	}
}
