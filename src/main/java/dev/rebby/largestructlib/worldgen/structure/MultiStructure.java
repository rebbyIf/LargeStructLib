package dev.rebby.largestructlib.worldgen.structure;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class MultiStructure extends Structure {
    public static final MapCodec<MultiStructure> CODEC;

    public final List<Holder<Structure>> structures;

    public MultiStructure(StructureSettings settings, List<Holder<Structure>> structures) {
        super(settings);
        this.structures = structures;
    }

    @Override
    protected @NotNull Optional<GenerationStub> findGenerationPoint(@NotNull GenerationContext generationContext) {
        if (structures.isEmpty())
            return Optional.empty();

        List<StructurePiece> pieces = new ArrayList<>();
        for (Holder<Structure> holder : structures) {
            Predicate<Holder<Biome>> biomeCheck =  holder.value().biomes()::contains;
            GenerationContext separateContext = new GenerationContext(
                    generationContext.registryAccess(),
                    generationContext.chunkGenerator(),
                    generationContext.biomeSource(),
                    generationContext.randomState(),
                    generationContext.structureTemplateManager(),
                    generationContext.random(),
                    generationContext.seed(),
                    generationContext.chunkPos(),
                    generationContext.heightAccessor(),
                    biomeCheck
            );
            Optional<GenerationStub> optional = holder.value().findValidGenerationPoint(separateContext);
            if (optional.isEmpty())
                continue;
            pieces.addAll(optional.get().getPiecesBuilder().build().pieces());
        }

        if (pieces.isEmpty())
            return Optional.empty();

        return Optional.of(new Structure.GenerationStub(pieces.getFirst().getLocatorPosition(), (builder) -> {
            pieces.forEach(builder::addPiece);
        }));
    }

    @Override
    public @NotNull StructureType<?> type() {
        return ModStructureTypes.MULTI_STRUCTURE.get();
    }

    static {
        CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                settingsCodec(instance),
                Structure.CODEC.listOf().fieldOf("structures").forGetter(structure -> structure.structures)
        ).apply(instance, MultiStructure::new));
    }
}
