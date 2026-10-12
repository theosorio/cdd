package com.example.hollow;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;

public class HollowMod implements ModInitializer {
    public static final String ID = "hollowremote";
    public static Item REMOTE;
    /** Set by the client entrypoint so common code never touches client classes. */
    public static Runnable OPEN_SCREEN = () -> {};

    @Override
    public void onInitialize() {
        REMOTE = Registry.register(Registries.ITEM, Identifier.of(ID, "cursed_remote"),
                new RemoteItem(new Item.Settings().maxCount(1).rarity(Rarity.EPIC)));
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(e -> e.add(REMOTE));

        PayloadTypeRegistry.playC2S().register(ActionPayload.ID, ActionPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(FearPayload.ID, FearPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(ActionPayload.ID,
                (payload, ctx) -> Pranks.run(ctx.player(), payload.action(), payload.mode()));
    }
}
