package com.example.hollow;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/** Client -> server: which prank, and who to aim it at. */
public record ActionPayload(int action, int mode) implements CustomPayload {
    public static final Id<ActionPayload> ID = new Id<>(Identifier.of("hollowremote", "action"));
    public static final PacketCodec<RegistryByteBuf, ActionPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.INTEGER, ActionPayload::action,
            PacketCodecs.INTEGER, ActionPayload::mode,
            ActionPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() { return ID; }
}
