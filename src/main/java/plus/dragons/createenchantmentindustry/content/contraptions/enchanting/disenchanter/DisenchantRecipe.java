package plus.dragons.createenchantmentindustry.content.contraptions.enchanting.disenchanter;

import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder;


import net.fabricmc.loader.impl.lib.tinyremapper.api.TrLogger;
import plus.dragons.createenchantmentindustry.entry.CeiFluids;
import plus.dragons.createenchantmentindustry.entry.CeiRecipeTypes;

public class DisenchantRecipe extends ProcessingRecipe<ItemStackHandlerContainer> {

    private final long experience;

    public DisenchantRecipe(ProcessingRecipeBuilder.ProcessingRecipeParams params) {
        super(CeiRecipeTypes.DISENCHANTING, params);
        if (fluidResults.isEmpty())
            throw new IllegalArgumentException("Illegal Disenchanting Recipe: " + id.toString() + " has no fluid output!");
        FluidStack fluid = fluidResults.get(0);
        if (!fluid.getFluid().isSame(CeiFluids.EXPERIENCE.get().getSource()))
            throw new IllegalArgumentException("Illegal Disenchanting Recipe: " + id.toString() + " has wrong type of fluid output!");
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
	public boolean matches(ItemStackHandlerContainer container, TrLogger.Level level) {
		return ingredients.get(0).test(container.getItem(0));
	}
}
