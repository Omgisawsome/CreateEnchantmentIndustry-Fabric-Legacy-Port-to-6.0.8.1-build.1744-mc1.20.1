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
		double total = 0.0;

		for (var entry : recipesUsed.object2IntEntrySet()) {
			total += getLevel()
					.getRecipeManager()
					.byKey(entry.getKey())
					.map(r -> ((AbstractCookingRecipe) r).getExperience() * entry.getIntValue())
					.orElse(0.0);
		}

		return (long) (total * UNIT_PER_MB);
	}

	/* ------------------------------------------------------------
	 * Hook recipe usage → fluid XP
	 * ------------------------------------------------------------ */

	@Inject(method = "setRecipeUsed", at = @At("TAIL"))
	private void injectSetRecipeUsed(@Nullable Recipe<?> recipe, CallbackInfo ci) {
		if (recipe == null)
			return;

		long stored = internalTank.getFluidAmount();
		long added = stored == 0
				? calculateExperienceStored()
				: (long) (((AbstractCookingRecipe) recipe).getExperience() * UNIT_PER_MB);

		internalTank.setFluid(
				new FluidStack(
						FluidVariant.of(CeiFluids.EXPERIENCE.getSource()),
						stored + added
				)
		);
	}

	/* ------------------------------------------------------------
	 * Internal XP tank
	 * ------------------------------------------------------------ */

	@Unique
	protected final FluidTank internalTank = new FluidTank(
			Long.MAX_VALUE,
			fs -> fs.getType().equals(FluidVariant.of(CeiFluids.EXPERIENCE.getSource()))
	) {
		@Override
		protected void onContentsChanged() {
			long expected = calculateExperienceStored();
			long diff = expected - getFluidAmount();

			if (diff <= 0)
				return;

			if (diff >= expected) {
				recipesUsed.clear();
				return;
			}

			var recipeManager = getLevel().getRecipeManager();

			for (var entry : recipesUsed.object2IntEntrySet()) {
				var recipeOpt = recipeManager.byKey(entry.getKey());
				if (recipeOpt.isEmpty())
					continue;

				var recipe = (AbstractCookingRecipe) recipeOpt.get();
				long xpPer = (long) (recipe.getExperience() * UNIT_PER_MB);

				int remove = (int) Math.min(diff / xpPer, entry.getIntValue());
				if (remove > 0) {
					diff -= xpPer * remove;
					recipesUsed.addTo(recipe.getId(), -remove);
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
		if (side != null && side.getAxis().isHorizontal())
			return exposedExperienceTank;
		return null;
	}
}
