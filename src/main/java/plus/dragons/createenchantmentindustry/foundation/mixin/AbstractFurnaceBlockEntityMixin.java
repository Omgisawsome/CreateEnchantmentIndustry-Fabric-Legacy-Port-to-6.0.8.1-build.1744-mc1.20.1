package plus.dragons.createenchantmentindustry.foundation.mixin;

import static plus.dragons.createenchantmentindustry.EnchantmentIndustry.UNIT_PER_MB;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.FilteringStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SidedStorageBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.inventory.RecipeHolder;
import net.minecraft.world.inventory.StackedContentsCompatible;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import plus.dragons.createenchantmentindustry.entry.CeiFluids;
import io.github.fabricators_of_create.porting_lib.transfer.fluid.FluidTank;
import io.github.fabricators_of_create.porting_lib.fluids.FluidStack;

@Mixin(AbstractFurnaceBlockEntity.class)
public abstract class AbstractFurnaceBlockEntityMixin
		extends BaseContainerBlockEntity
		implements WorldlyContainer, RecipeHolder, StackedContentsCompatible, SidedStorageBlockEntity {

	protected AbstractFurnaceBlockEntityMixin(
			BlockEntityType<?> type,
			BlockPos pos,
			BlockState state
	) {
		super(type, pos, state);
	}

	@Shadow @Final
	private Object2IntOpenHashMap<ResourceLocation> recipesUsed;

	/* ------------------------------------------------------------
	 * XP calculation
	 * ------------------------------------------------------------ */

	@Unique
	protected long calculateExperienceStored() {
		float total = 0.0f; // Use float to match recipe.getExperience()

		for (var entry : recipesUsed.object2IntEntrySet()) {
			total += getLevel()
					.getRecipeManager()
					.byKey(entry.getKey())
					.map(r -> {
						if (r instanceof AbstractCookingRecipe cookingRecipe) {
							return cookingRecipe.getExperience() * entry.getIntValue();
						}
						return 0.0f;
					})
					.orElse(0.0f);
		}

		return (long) (total * UNIT_PER_MB);
	}

	/* ------------------------------------------------------------
	 * Hook recipe usage → fluid XP
	 * ------------------------------------------------------------ */

	@Inject(method = "setRecipeUsed", at = @At("TAIL"))
	private void injectSetRecipeUsed(@Nullable Recipe<?> recipe, CallbackInfo ci) {
		if (recipe instanceof AbstractCookingRecipe cookingRecipe) {
			long stored = internalTank.getFluidAmount();
			long added = (long) (cookingRecipe.getExperience() * UNIT_PER_MB);

			internalTank.setFluid(
					new FluidStack(
							CeiFluids.EXPERIENCE.get().getSource(),
							stored + added
					)
			);
		}
	}

	/* ------------------------------------------------------------
	 * Internal XP tank
	 * ------------------------------------------------------------ */

	@Unique
	protected final FluidTank internalTank = new FluidTank(
			Long.MAX_VALUE,
			fs -> fs.getFluid().isSame(CeiFluids.EXPERIENCE.get().getSource())
	) {
		@Override
		protected void onContentsChanged() {
			super.onContentsChanged();
			long expected = calculateExperienceStored();
			long current = getFluidAmount();
			long diff = expected - current;

			if (diff <= 0)
				return;

			// If current is 0, it means the tank was fully drained, so clear recipe history
			if (current == 0) {
				recipesUsed.clear();
				return;
			}

			var recipeManager = getLevel().getRecipeManager();

			for (var entry : recipesUsed.object2IntEntrySet()) {
				var recipeOpt = recipeManager.byKey(entry.getKey());
				if (recipeOpt.isEmpty())
					continue;

				if (recipeOpt.get() instanceof AbstractCookingRecipe recipe) {
					long xpPer = (long) (recipe.getExperience() * UNIT_PER_MB);
					if (xpPer <= 0) continue;

					int remove = (int) Math.min(diff / xpPer, entry.getIntValue());
					if (remove > 0) {
						diff -= xpPer * remove;
						recipesUsed.addTo(recipe.getId(), -remove);
					}
				}
			}
		}
	};

	@Unique
	public final Storage<FluidVariant> exposedExperienceTank =
			FilteringStorage.extractOnlyOf(internalTank);

	/* ------------------------------------------------------------
	 * Fabric fluid exposure
	 * ------------------------------------------------------------ */

	@Override
	public @Nullable Storage<FluidVariant> getFluidStorage(Direction side) {
		// Allow extraction from any horizontal side (piping out XP)
		if (side != null && side.getAxis().isHorizontal())
			return exposedExperienceTank;
		return null;
	}
}
