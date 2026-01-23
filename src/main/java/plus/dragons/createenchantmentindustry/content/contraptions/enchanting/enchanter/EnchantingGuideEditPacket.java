package plus.dragons.createenchantmentindustry.content.contraptions.enchanting.enchanter;

import com.simibubi.create.foundation.networking.SimplePacketBase;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import plus.dragons.createenchantmentindustry.entry.CeiItems;

public class EnchantingGuideEditPacket extends SimplePacketBase {
	private final int index;
	private final ItemStack itemStack;

	public EnchantingGuideEditPacket(int index, ItemStack itemStack) {
		this.index = index;
		this.itemStack = itemStack;
	}

	public EnchantingGuideEditPacket(FriendlyByteBuf buffer) {
		this.index = buffer.readInt();
		this.itemStack = buffer.readItem();
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeInt(index);
		buffer.writeItem(itemStack);
	}

	@Override
	public boolean handle(Context context) {
		context.enqueueWork(() -> {
			ServerPlayer sender = context.getSender();
			if (sender == null) return;

			ItemStack guide = sender.getItemInHand(InteractionHand.MAIN_HAND);
			if (!guide.is(CeiItems.ENCHANTING_GUIDE.get())) {
				guide = sender.getItemInHand(InteractionHand.OFF_HAND);
			}

			if (guide.is(CeiItems.ENCHANTING_GUIDE.get())) {
				CompoundTag tag = guide.getOrCreateTag();
				tag.putInt("index", index);

				// Use vanilla item saving to ensure 1.20.1 compatibility
				if (itemStack.isEmpty()) {
					tag.remove("target");
				} else {
					tag.put("target", itemStack.save(new CompoundTag()));
				}

				// IMPORTANT: In 1.20.1, we need to make sure the inventory
				// knows the stack changed so it syncs back to client.
				sender.containerMenu.broadcastChanges();
			}
		});
		return true;
	}
}
