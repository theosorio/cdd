package com.example.hollow;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class RemoteItem extends Item {
    public RemoteItem(Settings settings) { super(settings); }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (world.isClient) HollowMod.OPEN_SCREEN.run();
        return TypedActionResult.success(user.getStackInHand(hand), world.isClient);
    }
}
