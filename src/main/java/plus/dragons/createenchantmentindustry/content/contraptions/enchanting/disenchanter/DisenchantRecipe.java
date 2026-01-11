package plus.dragons.createenchantmentindustry.content.contraptions.enchanting.disenchanter;

import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder;

import io.github.fabricators_of_create.porting_lib.fluids.FluidStack;
import net.minecraft.world.Container;
import plus.dragons.createenchantmentindustry.entry.CeiFluids;
import plus.dragons.createenchantmentindustry.entry.CeiRecipeTypes;

public class DisenchantRecipe extends ProcessingRecipe<Container> {

	private final long experience;

	public DisenchantRecipe(ProcessingRecipeBuilder.ProcessingRecipeParams params) {
		super(CeiRecipeTypes.DISENCHANTING, params);

		if (fluidResults.isEmpty())
			throw new IllegalArgumentException(
					"Illegal Disenchanting Recipe: " + id + " has no fluid output!"
			);

		FluidStack fluid = fluidResults.get(0);

		if (!fluid.getFluid().isSame(CeiFluids.EXPERIENCE.get().getSource()))
			throw new IllegalArgumentException(
					"Illegal Disenchanting Recipe: " + id + " has wrong fluid output!"
			);

		this.experience = fluid.getAmount();
	}

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

	public boolean hasNoResult() {
		return results.isEmpty();
	}

	public long getExperience() {
		return experience;
	}

	@Override
	public boolean matches(Container container, net.minecraft.world.level.Level level) {
		return ingredients.get(0).test(container.getItem(0));
	}
}
