package com.simibubi.create.foundation.utility;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class Components {

	// This is the one DragonLib just asked for!
	public static MutableComponent translatable(String key) {
		return Component.translatable(key);
	}

	public static MutableComponent immut(Component component) {
		return component.copy();
	}

	public static MutableComponent stub() {
		return Component.literal("");
	}

	// Adding this one just in case it asks for literal next
	public static MutableComponent literal(String text) {
		return Component.literal(text);
	}
}
