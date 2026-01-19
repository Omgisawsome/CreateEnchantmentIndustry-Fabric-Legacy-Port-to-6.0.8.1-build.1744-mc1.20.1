package plus.dragons.createenchantmentindustry.compat.jei.category;

import com.simibubi.create.compat.jei.CreateJEI;
import com.simibubi.create.compat.jei.DoubleItemIcon;
import com.simibubi.create.compat.jei.EmptyBackground;
import com.simibubi.create.compat.jei.ItemIcon;
import com.simibubi.create.compat.jei.category.CreateRecipeCategory;

import mezz.jei.api.gui.drawable.IDrawable;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ItemLike;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/*
MIT License

Copyright (c) 2019 simibubi

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */

public class RecipeCategoryBuilder<T extends Recipe<?>> {

	private final String modid;
	private final Class<? extends T> recipeClass;

	private IDrawable background;
	private IDrawable icon;

	private final List<Consumer<List<T>>> recipeListConsumers = new ArrayList<>();
	private final List<Supplier<? extends ItemStack>> catalysts = new ArrayList<>();

	public RecipeCategoryBuilder(String modid, Class<? extends T> recipeClass) {
		this.modid = modid;
		this.recipeClass = recipeClass;
	}

	public RecipeCategoryBuilder<T> addRecipeListConsumer(Consumer<List<T>> consumer) {
		recipeListConsumers.add(consumer);
		return this;
	}

	public RecipeCategoryBuilder<T> addRecipes(Supplier<Collection<? extends T>> collection) {
		return addRecipeListConsumer(recipes -> recipes.addAll(collection.get()));
	}

	@SuppressWarnings("unchecked")
	public RecipeCategoryBuilder<T> addAllRecipesIf(Predicate<Recipe<?>> pred) {
		return addRecipeListConsumer(recipes ->
				CreateJEI.consumeAllRecipes(recipe -> {
					if (pred.test(recipe)) {
						recipes.add((T) recipe);
					}
				})
		);
	}

	@SuppressWarnings("unchecked")
	public RecipeCategoryBuilder<T> addAllRecipesIf(
			Predicate<Recipe<?>> pred,
			Function<Recipe<?>, T> converter
	) {
		return addRecipeListConsumer(recipes ->
				CreateJEI.consumeAllRecipes(recipe -> {
					if (pred.test(recipe)) {
						recipes.add(converter.apply(recipe));
					}
				})
		);
	}

	@SuppressWarnings("unchecked")
	public <O extends Recipe<?>> RecipeCategoryBuilder<T> addTransformedRecipes(
			Supplier<RecipeType<O>> recipeType,
			Function<O, T> converter
	) {
		return addRecipeListConsumer(recipes ->
				CreateJEI.consumeTypedRecipes(
						// FIXED: Cast to O to resolve capture conversion mismatch
						recipe -> recipes.add(converter.apply((O) recipe)),
						recipeType.get()
				)
		);
	}

	@SuppressWarnings("unchecked")
	public RecipeCategoryBuilder<T> addTypedRecipes(Supplier<RecipeType<? extends T>> recipeType) {
		return addRecipeListConsumer(recipes ->
				// FIXED: Use lambda with explicit cast (T) instead of method reference
				CreateJEI.consumeTypedRecipes(recipe -> recipes.add((T) recipe), recipeType.get())
		);
	}

	@SuppressWarnings("unchecked")
	public RecipeCategoryBuilder<T> addTypedRecipesIf(
			Supplier<RecipeType<? extends T>> recipeType,
			Predicate<Recipe<?>> pred
	) {
		return addRecipeListConsumer(recipes ->
				CreateJEI.consumeTypedRecipes(recipe -> {
					if (pred.test(recipe)) {
						// FIXED: Cast to T to resolve capture conversion mismatch
						recipes.add((T) recipe);
					}
				}, recipeType.get())
		);
	}

	public RecipeCategoryBuilder<T> catalystStack(Supplier<ItemStack> supplier) {
		catalysts.add(supplier);
		return this;
	}

	public RecipeCategoryBuilder<T> catalyst(Supplier<ItemLike> supplier) {
		return catalystStack(() -> new ItemStack(supplier.get().asItem()));
	}

	public RecipeCategoryBuilder<T> icon(IDrawable icon) {
		this.icon = icon;
		return this;
	}

	public RecipeCategoryBuilder<T> itemIcon(ItemLike item) {
		return icon(new ItemIcon(() -> new ItemStack(item)));
	}

	public RecipeCategoryBuilder<T> doubleItemIcon(ItemLike item1, ItemLike item2) {
		return icon(new DoubleItemIcon(
				() -> new ItemStack(item1),
				() -> new ItemStack(item2)
		));
	}

	public RecipeCategoryBuilder<T> background(IDrawable background) {
		this.background = background;
		return this;
	}

	public RecipeCategoryBuilder<T> emptyBackground(int width, int height) {
		return background(new EmptyBackground(width, height));
	}

	public CreateRecipeCategory<T> build(String name, CreateRecipeCategory.Factory<T> factory) {

		Supplier<List<T>> recipesSupplier = () -> {
			List<T> recipes = new ArrayList<>();
			for (Consumer<List<T>> consumer : recipeListConsumers)
				consumer.accept(recipes);
			return recipes;
		};

		ResourceLocation id = new ResourceLocation(modid, name);

		CreateRecipeCategory.Info<T> info = new CreateRecipeCategory.Info<>(
				new mezz.jei.api.recipe.RecipeType<>(id, recipeClass),
				Component.translatable(
						"recipe." + id.getNamespace() + "." + id.getPath()
				),
				background,
				icon,
				recipesSupplier,
				catalysts
		);

		return factory.create(info);
	}
}
