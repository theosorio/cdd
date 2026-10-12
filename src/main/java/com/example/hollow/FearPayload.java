package com.example.hollow;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/** Server -> victim client: play a scary client-side effect. */
public record FearPayload(int effect) implements CustomPayload {
    public static final Id<FearPayload> ID = new Id<>(Identifier.of("hollowremote", "fear"));
    public static final PacketCodec<RegistryByteBuf, FearPayload> CODEC =
            PacketCodec.tuple(PacketCodecs.INTEGER, FearPayload::effect, FearPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() { return ID; }
}
