package plus.dragons.createenchantmentindustry;

import java.nio.file.Path;
import java.util.Collections;

import io.github.fabricators_of_create.porting_lib.data.ExistingFileHelper;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

public class EnchantmentIndustryData implements DataGeneratorEntrypoint {

	@Override
	public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
		// FABRIC 1.20.1 FIX: Create a pack from the generator
		FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();

		// FABRIC/PORTING LIB FIX: ExistingFileHelper.withResources expects Paths
		// We use the generator's mod ID and an empty set of paths for a minimal helper
		ExistingFileHelper helper = ExistingFileHelper.withResources(Collections.emptySet(), new Path[0]);

		// Registrate for Fabric 0.6+ usually takes the Pack object
		EnchantmentIndustry.REGISTRATE.setupDatagen(pack, helper);
	}
}
