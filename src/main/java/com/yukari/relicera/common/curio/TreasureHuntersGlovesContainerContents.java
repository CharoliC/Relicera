package com.yukari.relicera.common.curio;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

public final class TreasureHuntersGlovesContainerContents {
    private static final String STACK_KEY = "Key";
    private static final String COUNT_KEY = "Count";

    private TreasureHuntersGlovesContainerContents() {
    }

    public static ListTag createSignature(Container container) {
        Map<String, Integer> countsByStack = new TreeMap<>();
        mergeInventoryCounts(container, countsByStack);

        ListTag signature = new ListTag();
        countsByStack.forEach((key, count) -> {
            CompoundTag entry = new CompoundTag();
            entry.putString(STACK_KEY, key);
            entry.putInt(COUNT_KEY, count);
            signature.add(entry);
        });
        return signature;
    }

    public static boolean canRefresh(List<ListTag> initialSignatures,
                                     List<? extends Container> currentContainers,
                                     int maxRemovedItemCount) {
        Map<String, Integer> initialCounts = new TreeMap<>();
        Map<String, Integer> currentCounts = new TreeMap<>();
        initialSignatures.forEach(signature -> mergeSignature(signature, initialCounts));
        currentContainers.forEach(container -> mergeInventoryCounts(container, currentCounts));

        for (Map.Entry<String, Integer> entry : currentCounts.entrySet()) {
            if (entry.getValue() > initialCounts.getOrDefault(entry.getKey(), 0)) {
                return false;
            }
        }

        long removedItemCount = initialCounts.entrySet().stream()
                .mapToLong(entry -> entry.getValue() - currentCounts.getOrDefault(entry.getKey(), 0))
                .sum();
        return removedItemCount <= maxRemovedItemCount;
    }

    private static void mergeInventoryCounts(Container container, Map<String, Integer> countsByStack) {
        for (int slot = 0; slot < container.getContainerSize(); slot++) {
            ItemStack stack = container.getItem(slot);
            if (stack.isEmpty()) {
                continue;
            }

            ItemStack single = stack.copy();
            single.setCount(1);
            CompoundTag stackTag = single.save(new CompoundTag());
            stackTag.remove("Count");
            countsByStack.merge(stackTag.toString(), stack.getCount(), Integer::sum);
        }
    }

    private static void mergeSignature(ListTag signature, Map<String, Integer> countsByStack) {
        for (int index = 0; index < signature.size(); index++) {
            CompoundTag entry = signature.getCompound(index);
            countsByStack.merge(entry.getString(STACK_KEY), entry.getInt(COUNT_KEY), Integer::sum);
        }
    }
}
