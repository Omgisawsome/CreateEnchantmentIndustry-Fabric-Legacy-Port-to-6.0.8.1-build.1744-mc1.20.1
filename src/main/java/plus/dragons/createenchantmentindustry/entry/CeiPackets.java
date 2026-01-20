package plus.dragons.createenchantmentindustry.entry;

import java.util.function.Function;

import com.simibubi.create.foundation.networking.SimplePacketBase;

import me.pepperbell.simplenetworking.SimpleChannel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import plus.dragons.createenchantmentindustry.EnchantmentIndustry;
import plus.dragons.createenchantmentindustry.content.contraptions.enchanting.enchanter.BlazeEnchanterEditPacket;
import plus.dragons.createenchantmentindustry.content.contraptions.enchanting.enchanter.EnchantingGuideEditPacket;

public enum CeiPackets {

	// Client to Server
	CONFIGURE_ENCHANTING_GUIDE_FOR_BLAZE(EnchantingGuideEditPacket.class, EnchantingGuideEditPacket::new, SimplePacketBase.NetworkDirection.PLAY_TO_SERVER),
	CONFIGURE_BLAZE_ENCHANTER(BlazeEnchanterEditPacket.class, BlazeEnchanterEditPacket::new, SimplePacketBase.NetworkDirection.PLAY_TO_SERVER);

	public static final ResourceLocation CHANNEL_NAME = EnchantmentIndustry.genRL("main");
	public static SimpleChannel channel;

	private final PacketType<?> packetType;

	<T extends SimplePacketBase> CeiPackets(Class<T> type, Function<FriendlyByteBuf, T> factory,
											SimplePacketBase.NetworkDirection direction) {
		packetType = new PacketType<>(type, factory, direction);
	}

	public static void registerPackets() {
		channel = new SimpleChannel(CHANNEL_NAME);
		for (CeiPackets packet : values())
			packet.packetType.register();
	}

	public static SimpleChannel getChannel() {
		return channel;
	}

	/**
	 * FABRIC FIX: Use the channel's native sendToClientsAround which handles
	 * the conversion from SimplePacketBase internally or via the registered decoder.
	 */
	public static void sendToNear(Level world, BlockPos pos, int range, SimplePacketBase message) {
		if (!(world instanceof ServerLevel serverLevel)) return;

		// SimpleNetworking on Fabric uses this approach for "ToNear"
		getChannel().sendToClientsAround(message, serverLevel, pos, range);
	}

	private static class PacketType<T extends SimplePacketBase> {
		private static int index = 0;

		private final Function<FriendlyByteBuf, T> decoder;
		private final Class<T> type;
		private final SimplePacketBase.NetworkDirection direction;

		private PacketType(Class<T> type, Function<FriendlyByteBuf, T> factory, SimplePacketBase.NetworkDirection direction) {
			this.decoder = factory;
			this.type = type;
			this.direction = direction;
		}

		private void register() {
			// Porting Lib / Simple Networking registration
			int id = index++;
			switch (direction) {
				case PLAY_TO_CLIENT -> getChannel().registerS2CPacket(type, id, decoder);
				case PLAY_TO_SERVER -> getChannel().registerC2SPacket(type, id, decoder);
			}
		}
	}
}
