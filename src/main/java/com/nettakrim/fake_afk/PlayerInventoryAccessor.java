package com.nettakrim.fake_afk;

import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;

import java.util.Iterator;

public interface PlayerInventoryAccessor {
    Iterator<DefaultedList<ItemStack>> fakeAFK$getInventory();
}
