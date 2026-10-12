package com.example.hollow;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Server-side logic. mode: 0 = me, 1 = nearby players (64 blocks, not me), 2 = every other player. */
public final class Pranks {
    private static final Random RNG = new Random();

    public static void run(ServerPlayerEntity user, int action, int mode) {
        if (!user.getMainHandStack().isOf(HollowMod.REMOTE) && !user.getOffHandStack().isOf(HollowMod.REMOTE)) return;

        List<ServerPlayerEntity> targets = new ArrayList<>();
        if (mode == 0) {
            targets.add(user);
        } else {
            for (ServerPlayerEntity p : user.getServer().getPlayerManager().getPlayerList()) {
                if (p == user) continue;
                if (mode == 2 || (p.getWorld() == user.getWorld() && p.squaredDistanceTo(user) < 64 * 64)) targets.add(p);
            }
        }

        for (ServerPlayerEntity p : targets) {
            switch (action) {
                case 0, 2, 3, 4, 9 -> send(p, action);      // pure client-side scares
                case 1 -> {                                  // blackout
                    p.addStatusEffect(new StatusEffectInstance(StatusEffects.DARKNESS, 400, 0, false, false));
                    p.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 80, 0, false, false));
                    send(p, 1);
                }
                case 5 -> p.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 300, 0, false, false));
                case 6 -> {                                  // harmless cosmetic lightning nearby
                    ServerWorld w = (ServerWorld) p.getWorld();
                    LightningEntity bolt = EntityType.LIGHTNING_BOLT.create(w);
                    if (bolt != null) {
                        double ox = (RNG.nextBoolean() ? 1 : -1) * (8 + RNG.nextInt(8));
                        double oz = (RNG.nextBoolean() ? 1 : -1) * (8 + RNG.nextInt(8));
                        bolt.refreshPositionAfterTeleport(new Vec3d(p.getX() + ox, p.getY(), p.getZ() + oz));
                        bolt.setCosmetic(true);
                        w.spawnEntity(bolt);
                    }
                }
                case 7 -> {                                  // nightfall + storm
                    ServerWorld w = (ServerWorld) p.getWorld();
                    w.setTimeOfDay(18000);
                    w.setWeather(0, 6000, true, true);
                }
                case 8 -> p.sendMessage(Text.literal(user.getName().getString() + " left the game").formatted(Formatting.YELLOW));
                default -> {}
            }
            if (action == 3) p.sendMessage(Text.literal("I SEE YOU").formatted(Formatting.DARK_RED, Formatting.BOLD), true);
        }
    }

    private static void send(ServerPlayerEntity p, int effect) {
        ServerPlayNetworking.send(p, new FearPayload(effect));
    }
}
