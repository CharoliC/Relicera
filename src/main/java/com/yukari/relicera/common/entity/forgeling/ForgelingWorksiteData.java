package com.yukari.relicera.common.entity.forgeling;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

final class ForgelingWorksiteData extends SavedData {
    private static final String DATA_NAME = "relicera_forgeling_worksites";
    private static final String TAG_CLAIMS = "Claims";
    private static final String TAG_TABLE = "Table";
    private static final String TAG_OWNER = "Owner";

    private final Map<BlockPos, UUID> claims = new HashMap<>();

    static ForgelingWorksiteData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                ForgelingWorksiteData::load,
                ForgelingWorksiteData::new,
                DATA_NAME
        );
    }

    private static ForgelingWorksiteData load(CompoundTag tag) {
        ForgelingWorksiteData data = new ForgelingWorksiteData();
        ListTag claims = tag.getList(TAG_CLAIMS, Tag.TAG_COMPOUND);
        for (Tag entryTag : claims) {
            CompoundTag entry = (CompoundTag) entryTag;
            if (entry.contains(TAG_TABLE, Tag.TAG_LONG) && entry.hasUUID(TAG_OWNER)) {
                data.claims.put(BlockPos.of(entry.getLong(TAG_TABLE)), entry.getUUID(TAG_OWNER));
            }
        }
        return data;
    }

    boolean claim(BlockPos tablePos, UUID owner) {
        UUID existingOwner = claims.get(tablePos);
        if (existingOwner != null && !existingOwner.equals(owner)) {
            return false;
        }
        if (existingOwner == null) {
            claims.put(tablePos.immutable(), owner);
            setDirty();
        }
        return true;
    }

    boolean isAvailableTo(BlockPos tablePos, UUID owner) {
        UUID existingOwner = claims.get(tablePos);
        return existingOwner == null || existingOwner.equals(owner);
    }

    boolean isOwnedBy(BlockPos tablePos, UUID owner) {
        return owner.equals(claims.get(tablePos));
    }

    void release(BlockPos tablePos, UUID owner) {
        if (owner.equals(claims.get(tablePos))) {
            claims.remove(tablePos);
            setDirty();
        }
    }

    void removeInvalidTables(ServerLevel level) {
        if (claims.keySet().removeIf(tablePos -> level.hasChunkAt(tablePos)
                && !ForgelingWorksiteManager.isSmithingTable(level, tablePos))) {
            setDirty();
        }
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag claimsTag = new ListTag();
        claims.forEach((tablePos, owner) -> {
            CompoundTag entry = new CompoundTag();
            entry.putLong(TAG_TABLE, tablePos.asLong());
            entry.putUUID(TAG_OWNER, owner);
            claimsTag.add(entry);
        });
        tag.put(TAG_CLAIMS, claimsTag);
        return tag;
    }
}

