package plus.dragons.createenchantmentindustry.entry;

import java.util.Optional;
import java.util.Locale;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeSerializer;
import com.simibubi.create.foundation.recipe.IRecipeTypeInfo;

import io.github.fabricators_of_create.porting_lib.util.ShapedRecipeUtil;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import plus.dragons.createenchantmentindustry.EnchantmentIndustry;
import plus.dragons.createenchantmentindustry.content.contraptions.enchanting.disenchanter.DisenchantRecipe;

public enum CeiRecipeTypes implements IRecipeTypeInfo {

	DISENCHANTING(DisenchantRecipe::new);

	private final ResourceLocation id;
	private final RecipeSerializer<?> serializer;
	@Nullable
	private final RecipeType<?> type;

	CeiRecipeTypes(ProcessingRecipeBuilder.ProcessingRecipeFactory<?> factory) {
		// FIXED: Manually convert enum name to snake_case id string
		// This replaces the missing EnchantmentIndustry.LANG.asId()
		String nameId = name().toLowerCase(Locale.ROOT);

		this.id = EnchantmentIndustry.genRL(nameId);

		this.serializer = Registry.register(
				BuiltInRegistries.RECIPE_SERIALIZER,
				id,
				new ProcessingRecipeSerializer<>(factory)
		);

		this.type = Registry.register(
				BuiltInRegistries.RECIPE_TYPE,
				id,
				simpleType(id)
		);
	}

	/* -------------------- REGISTRATION -------------------- */

	public static void register() {
		ShapedRecipeUtil.setCraftingSize(9, 9);
		// Fabric: classloading registers everything
	}

	private static <T extends Recipe<?>> RecipeType<T> simpleType(ResourceLocation id) {
		final String stringId = id.toString();
		return new RecipeType<>() {
			@Override
			public String toString() {
				return stringId;
			}
		};
	}

	/* -------------------- CREATE API -------------------- */

	@Override
	public ResourceLocation getId() {
		return id;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends RecipeSerializer<?>> T getSerializer() {
		return (T) serializer;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends RecipeType<?>> T getType() {
		return (T) type;
	}

	/* -------------------- HELPERS -------------------- */

	public <C extends Container, T extends Recipe<C>> Optional<T> find(C inv, Level level) {
		return level.getRecipeManager().getRecipeFor(getType(), inv, level);
	}
}
