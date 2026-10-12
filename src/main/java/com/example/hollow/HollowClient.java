package com.example.hollow;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class HollowClient implements ClientModInitializer {
    private static final java.util.Random RNG = new java.util.Random();
    private static int jump = 0, flicker = 0;

    private static final class Task {
        int delay; final Runnable run;
        Task(int d, Runnable r) { delay = d; run = r; }
    }
    private static final List<Task> TASKS = new ArrayList<>();

    @Override
    public void onInitializeClient() {
        HollowMod.OPEN_SCREEN = () -> MinecraftClient.getInstance().setScreen(new RemoteScreen());

        ClientPlayNetworking.registerGlobalReceiver(FearPayload.ID, (p, ctx) -> trigger(p.effect()));

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            if (jump > 0) jump--;
            if (flicker > 0) flicker--;
            for (Iterator<Task> it = TASKS.iterator(); it.hasNext(); ) {
                Task t = it.next();
                if (--t.delay <= 0) { it.remove(); t.run.run(); }
            }
        });

        HudRenderCallback.EVENT.register(HollowClient::render);
    }

    // ---------- effects ----------
    static void trigger(int effect) {
        MinecraftClient mc = MinecraftClient.getInstance();
        ClientPlayerEntity pl = mc.player;
        if (pl == null) return;
        switch (effect) {
            case 0 -> {                                   // JUMPSCARE
                jump = 30;
                flat(SoundEvents.ENTITY_WARDEN_ROAR, 1.0f, 0.7f);
                flat(SoundEvents.ENTITY_GHAST_SCREAM, 1.0f, 0.5f);
            }
            case 1 -> {                                   // BLACKOUT: accelerating heartbeat
                for (int i = 0; i < 12; i++) {
                    final float pitch = 0.8f + i * 0.04f;
                    later(10 + i * Math.max(8, 22 - i * 2), () -> flat(SoundEvents.ENTITY_WARDEN_HEARTBEAT, 1.2f, pitch));
                }
                later(60, () -> flat(SoundEvents.ENTITY_WARDEN_NEARBY_CLOSEST, 0.9f, 0.6f));
            }
            case 2 -> {                                   // FOOTSTEPS closing in from behind
                Vec3d dir = behindDir(pl);
                for (int i = 0; i < 8; i++) {
                    final double dist = 9.0 - i * 1.1;
                    later(8 + i * 12, () -> at(SoundEvents.BLOCK_STONE_STEP, pl.getPos().add(dir.multiply(dist)), 1.4f, 0.6f));
                }
                later(8 + 8 * 12 + 20, () -> at(SoundEvents.ENTITY_WARDEN_NEARBY_CLOSEST, pl.getPos().add(dir.multiply(1.0)), 1.5f, 0.5f));
            }
            case 3 -> {                                   // WHISPERS all around
                for (int i = 0; i < 10; i++) {
                    later(5 + i * 14, () -> {
                        double a = RNG.nextDouble() * Math.PI * 2;
                        Vec3d off = new Vec3d(Math.cos(a) * 4, 0, Math.sin(a) * 4);
                        at(RNG.nextBoolean() ? SoundEvents.ENTITY_WARDEN_AMBIENT : SoundEvents.ENTITY_WARDEN_NEARBY_CLOSE,
                                pl.getPos().add(off), 1.2f, 0.5f + RNG.nextFloat() * 0.4f);
                    });
                }
            }
            case 4 -> {                                   // LIGHTS FLICKER
                flicker = 70;
                flat(SoundEvents.ENTITY_ELDER_GUARDIAN_CURSE, 0.8f, 0.6f);
            }
            case 9 -> {                                   // CREEPER HISS directly behind
                Vec3d b = pl.getPos().add(behindDir(pl).multiply(1.5));
                at(SoundEvents.ENTITY_CREEPER_PRIMED, b, 1.5f, 1.0f);
            }
            default -> {}
        }
    }

    // ---------- helpers ----------
    private static void later(int ticks, Runnable r) { TASKS.add(new Task(ticks, r)); }

    private static Vec3d behindDir(ClientPlayerEntity pl) {
        Vec3d look = pl.getRotationVec(1.0f).multiply(1, 0, 1);
        if (look.lengthSquared() < 1.0E-4) look = new Vec3d(0, 0, 1);
        return look.normalize().multiply(-1);
    }

    private static void flat(SoundEvent s, float vol, float pitch) {
        MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(s, pitch, vol));
    }

    private static void at(SoundEvent s, Vec3d pos, float vol, float pitch) {
        MinecraftClient.getInstance().getSoundManager().play(
                new PositionedSoundInstance(s, SoundCategory.MASTER, vol, pitch, Random.create(), pos.x, pos.y, pos.z));
    }

    // ---------- HUD ----------
    private static void render(DrawContext c, net.minecraft.client.render.RenderTickCounter t) {
        int w = c.getScaledWindowWidth(), h = c.getScaledWindowHeight();

        if (flicker > 0 && RNG.nextInt(3) != 0) c.fill(0, 0, w, h, 0xFF000000);

        if (jump > 0) {
            boolean red = (jump % 4) < 2;
            c.fill(0, 0, w, h, red ? 0xFF3A0000 : 0xFF000000);
            int cx = w / 2 + RNG.nextInt(9) - 4, cy = h / 2 + RNG.nextInt(9) - 4;
            int s = Math.min(w, h) * 6 / 10;
            // face
            c.fill(cx - s / 2, cy - s / 2, cx + s / 2, cy + s / 2, red ? 0xFFE8E2D0 : 0xFFB8B2A0);
            // eye sockets + glowing pupils
            for (int side = -1; side <= 1; side += 2) {
                int ex = cx + side * s / 4;
                c.fill(ex - s / 8, cy - s / 4, ex + s / 8, cy - s / 20, 0xFF000000);
                c.fill(ex - s / 30, cy - s / 7, ex + s / 30, cy - s / 12, 0xFFFF0000);
            }
            // gaping mouth with jagged teeth
            int mx1 = cx - s / 3, mx2 = cx + s / 3, my1 = cy + s / 12, my2 = cy + s * 2 / 5;
            c.fill(mx1, my1, mx2, my2, 0xFF000000);
            int teeth = 10, tw = (mx2 - mx1) / teeth;
            for (int i = 0; i < teeth; i++) {
                int len = s / 14 + (i % 2) * s / 14;
                c.fill(mx1 + i * tw, my1, mx1 + i * tw + tw - 2, my1 + len, 0xFFF4F0E4);
                c.fill(mx1 + i * tw, my2 - len, mx1 + i * tw + tw - 2, my2, 0xFFF4F0E4);
            }
        }
    }
}
