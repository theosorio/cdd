package com.example.hollow;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class RemoteScreen extends Screen {
    private static int mode = 1; // remembered between opens
    private static final String[] MODES = {"Target: ME (test)", "Target: NEARBY players", "Target: EVERYONE else"};
    private static final String[] NAMES = {
            "Jumpscare", "Blackout", "Footsteps Behind", "Whispers", "Lights Flicker",
            "Nausea", "Thunderstrike", "Nightfall", "Fake Leave Msg", "Creeper Hiss"
    };

    public RemoteScreen() { super(Text.literal("The Hollow Remote")); }

    @Override
    protected void init() {
        int cx = width / 2, top = height / 2 - 85;
        addDrawableChild(ButtonWidget.builder(Text.literal(MODES[mode]), b -> {
            mode = (mode + 1) % 3;
            b.setMessage(Text.literal(MODES[mode]));
        }).dimensions(cx - 100, top, 200, 20).build());

        for (int i = 0; i < NAMES.length; i++) {
            final int a = i;
            int x = cx - 100 + (i % 2) * 102, y = top + 26 + (i / 2) * 24;
            addDrawableChild(ButtonWidget.builder(Text.literal(NAMES[i]), b -> {
                ClientPlayNetworking.send(new ActionPayload(a, mode));
                if (mode == 0) close(); // so you can actually see effects on yourself
            }).dimensions(x, y, 98, 20).build());
        }
        addDrawableChild(ButtonWidget.builder(Text.literal("Close"), b -> close())
                .dimensions(cx - 100, top + 26 + 5 * 24 + 4, 200, 20).build());
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        super.render(ctx, mx, my, delta);
        ctx.drawCenteredTextWithShadow(textRenderer, Text.literal("THE HOLLOW REMOTE"), width / 2, height / 2 - 103, 0xFFAA0000);
    }

    @Override
    public boolean shouldPause() { return false; }
}
