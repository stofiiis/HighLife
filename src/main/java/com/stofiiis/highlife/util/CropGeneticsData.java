package com.stofiiis.highlife.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.mojang.serialization.Codec;
import com.stofiiis.highlife.HighLifeMod;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

public final class CropGeneticsData extends SavedData {
    private static final Codec<CropGeneticsData> CODEC = Codec.unboundedMap(Codec.LONG, StrainData.CODEC)
            .xmap(CropGeneticsData::new, CropGeneticsData::toMap);

    private static final SavedDataType<CropGeneticsData> TYPE = new SavedDataType<>(
            HighLifeMod.MODID + "_crop_genetics",
            CropGeneticsData::new,
            CODEC);

    private final Map<Long, StrainData> cropStrains = new HashMap<>();

    public CropGeneticsData() {
    }

    private CropGeneticsData(Map<Long, StrainData> saved) {
        this.cropStrains.putAll(saved);
    }

    private Map<Long, StrainData> toMap() {
        return this.cropStrains;
    }

    public static CropGeneticsData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(TYPE);
    }

    public void put(BlockPos pos, StrainData strainData) {
        this.cropStrains.put(pos.asLong(), strainData);
        this.setDirty();
    }

    public Optional<StrainData> get(BlockPos pos) {
        return Optional.ofNullable(this.cropStrains.get(pos.asLong()));
    }

    public Optional<StrainData> remove(BlockPos pos) {
        StrainData removed = this.cropStrains.remove(pos.asLong());
        if (removed != null) {
            this.setDirty();
        }
        return Optional.ofNullable(removed);
    }
}
