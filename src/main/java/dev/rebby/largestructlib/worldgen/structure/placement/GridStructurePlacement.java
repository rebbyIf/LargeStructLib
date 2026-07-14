package dev.rebby.largestructlib.worldgen.structure.placement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.rebby.largestructlib.worldgen.structure.ModStructureTypes;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacementType;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class GridStructurePlacement extends StructurePlacement {

    public static final MapCodec<GridStructurePlacement> CODEC = RecordCodecBuilder.mapCodec(
            instance -> placementCodec(instance).apply(instance,
                    GridStructurePlacement::new));

    public GridStructurePlacement(Vec3i locateOffset, FrequencyReductionMethod frequencyReductionMethod,
                                  float frequency, int salt, Optional<ExclusionZone> exclusionZone) {
        super(locateOffset, frequencyReductionMethod, frequency, salt, exclusionZone);
    }

    @Override
    protected boolean isPlacementChunk(@NotNull ChunkGeneratorStructureState chunkGeneratorStructureState, int chunkX, int chunkZ) {
        return true;
    }

    @Override
    public @NotNull StructurePlacementType<?> type() {
        return ModStructureTypes.Placement.GRID.get();
    }
}
