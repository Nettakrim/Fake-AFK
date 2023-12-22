package com.nettakrim.fake_afk.mixin;

import com.nettakrim.fake_afk.PlayerInventoryAccessor;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Iterator;
import java.util.List;

@Mixin(PlayerInventory.class)
public class PlayerInventoryMixin implements PlayerInventoryAccessor {
    @Final
    @Shadow
    private List<DefaultedList<ItemStack>> combinedInventory;

    @Override
    public Iterator<DefaultedList<ItemStack>> fakeAFK$getInventory() {
        return combinedInventory.iterator();
    }
}
