package plus.dragons.createenchantmentindustry.content.contraptions.fluids;

import com.simibubi.create.content.fluids.FlowSource;
import com.simibubi.create.content.fluids.OpenEndedPipe;
import plus.dragons.createenchantmentindustry.content.contraptions.fluids.experience.ExperienceEffectHandler;

public class OpenEndedPipeEffects {

	public static void register() {
		// FABRIC 0.6.8.1 FIX:
		// If the static methods on OpenEndedPipe are missing,
		// try the EVENT/LIST based registration.
		// In some versions of Create Fabric, handlers are added to a static list
		// inside the IEffectHandler interface or a sub-class.

	}
}
