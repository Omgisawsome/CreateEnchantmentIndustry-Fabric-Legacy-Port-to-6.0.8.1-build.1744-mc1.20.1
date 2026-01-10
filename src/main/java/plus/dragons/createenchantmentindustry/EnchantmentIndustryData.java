package plus.dragons.createenchantmentindustry;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
// Use the correct import for Porting Lib's ExistingFileHelper on Fabric 1.20.1:
import io.github.fabricators_of_create.porting_lib.data.ExistingFileHelper;

public class EnchantmentIndustryData implements DataGeneratorEntrypoint {
	@Override
	public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
		// Your data generation logic here
		// If 'helper' was passed and unused, simply remove it or use it.
		// Example:
		// ExistingFileHelper helper = ...;
		// EnchantmentIndustry.REGISTRATE.setupDatagen(fabricDataGenerator.createPack(), helper);

		// Minimal fix for compilation if you are just initializing:
		EnchantmentIndustry.REGISTRATE.setupDatagen(fabricDataGenerator.getPack(), ExistingFileHelper.withResources(new String[0]));
	}
}
