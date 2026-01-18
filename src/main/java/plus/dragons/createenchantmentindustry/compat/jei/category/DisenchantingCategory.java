package plus.dragons.createenchantmentindustry.compat.jei.category;

import com.simibubi.create.compat.jei.category.CreateRecipeCategory;
import com.simibubi.create.foundation.gui.AllGuiTextures;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import plus.dragons.createenchantmentindustry.content.contraptions.enchanting.disenchanter.DisenchantRecipe;
import plus.dragons.createenchantmentindustry.entry.CeiFluids;

// FABRIC NATIVE IMPORTS
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import io.github.fabricators_of_create.porting_lib.fluids.FluidStack;

public class DisenchantingCategory extends CreateRecipeCategory<DisenchantRecipe> {

	private final IDrawable disenchanter = new DisenchanterDrawable();

	public DisenchantingCategory(Info<DisenchantRecipe> info) {
		super(info);
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, DisenchantRecipe recipe, IFocusGroup focuses) {
		builder.addSlot(RecipeIngredientRole.INPUT, 27, 25)
				.setBackground(getRenderedSlot(), -1, -1)
				.addIngredients(recipe.getIngredients().get(0));

		builder.addSlot(RecipeIngredientRole.OUTPUT, 139, 25)
				.setBackground(getRenderedSlot(), -1, -1)
				.addFluidStack(CeiFluids.EXPERIENCE.get(), recipe.getExperience())
				.addTooltipCallback((recipeSlotView, tooltip) -> {
					long amount = recipe.getExperience();
					if (amount > 0) {
						// Using Fabric's native way to get a fluid name from a variant
						FluidVariant variant = FluidVariant.of(CeiFluids.EXPERIENCE.get());
						Component name = FluidVariantAttributes.getName(variant);

						tooltip.add(name.copy()
								.append(" ")
								.append(Component.literal(String.valueOf(amount)).append("mB")));
					}
				});

		if(!recipe.hasNoResult())
			builder.addSlot(RecipeIngredientRole.OUTPUT, 139, 5)
					.setBackground(getRenderedSlot(), -1, -1)
					.addItemStack(recipe.getResultItem(Minecraft.getInstance().level.registryAccess()));
	}

	@Override
	public void draw(DisenchantRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
		AllGuiTextures.JEI_SHADOW.render(guiGraphics, 62, 31);
		disenchanter.draw(guiGraphics, getBackground().getWidth() / 2 - 13, 16);
	}
}
