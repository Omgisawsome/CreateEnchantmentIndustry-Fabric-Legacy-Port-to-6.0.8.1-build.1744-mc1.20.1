package plus.dragons.createenchantmentindustry.content.contraptions.enchanting.enchanter;

import com.simibubi.create.foundation.networking.SimplePacketBase;
import io.github.fabricators_of_create.porting_lib.util.NBTSerializer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import plus.dragons.createenchantmentindustry.entry.CeiItems;

public class EnchantingGuideEditPacket extends SimplePacketBase {

	private final int index;
	private final ItemStack itemStack;

	// Constructor for sending the packet
	public EnchantingGuideEditPacket(int index, ItemStack enchantedBook) {
		this.index = index;
		this.itemStack = enchantedBook;
	}

	// Constructor for receiving the packet
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
	public void handle() {
		// Get the sender on the server side safely
		ServerPlayer sender = SimplePacketBase.getSender(ServerPlayer.class);
		if (sender == null)
			return;

		ItemStack mainHandItem = sender.getMainHandItem();
		if (!CeiItems.ENCHANTING_GUIDE.isIn(mainHandItem))
			return;

		// Write the index and target ItemStack to the tag
		CompoundTag tag = mainHandItem.getOrCreateTag();
		tag.putInt("index", index);
		tag.put("target", NBTSerializer.serializeNBT(itemStack));

		// Apply a small cooldown to prevent spam
		sender.getCooldowns().addCooldown(mainHandItem.getItem(), 5);
	}
}
