package plus.dragons.createenchantmentindustry.content.contraptions.enchanting.disenchanter;

import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import plus.dragons.createenchantmentindustry.entry.CeiFluids;
import plus.dragons.createenchantmentindustry.entry.CeiRecipeTypes;

public class DisenchantRecipe extends ProcessingRecipe<Container> {

	private final long experience;

	public DisenchantRecipe(ProcessingRecipeBuilder.ProcessingRecipeParams params) {
		super(CeiRecipeTypes.DISENCHANTING.getType(), params);

		if (fluidResults.isEmpty()) {
			throw new IllegalArgumentException(
					"Illegal Disenchanting Recipe: " + id + " has no fluid output!"
			);
		}

		var fluid = fluidResults.get(0);

		if (!fluid.getFluid().isSame(CeiFluids.EXPERIENCE.get().getSource())) {
			throw new IllegalArgumentException(
					"Illegal Disenchanting Recipe: " + id + " has wrong fluid output!"
			);
		}

		this.experience = fluid.getAmount();
	}

	/* -------------------- REQUIRED RECIPE OVERRIDES -------------------- */

	@Override
	public ResourceLocation getId() {
		return id;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return CeiRecipeTypes.DISENCHANTING.getSerializer();
	}

	@Override
	public RecipeType<?> getType() {
		return CeiRecipeTypes.DISENCHANTING.getType();
	}

	@Override
	public boolean matches(Container container, Level level) {
		return ingredients.get(0).test(container.getItem(0));
	}

	/* -------------------- PROCESSING LIMITS -------------------- */

	@Override
	protected int getMaxInputCount() {
		return 1;
	}

	@Override
	protected int getMaxOutputCount() {
		return 1;
	}

	@Override
	protected int getMaxFluidOutputCount() {
		return 1;
	}

	@Override
	protected boolean canSpecifyDuration() {
		return false;
	}

	/* -------------------- CUSTOM API -------------------- */

	public boolean hasNoResult() {
		return results.isEmpty();
	}

	public long getExperience() {
		return experience;
	}
}
