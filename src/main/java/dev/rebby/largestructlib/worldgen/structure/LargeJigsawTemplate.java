package dev.rebby.largestructlib.worldgen.structure;

import dev.rebby.largestructlib.worldgen.structure.generator.LargeJigsawPlacement;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.minecraft.world.level.levelgen.structure.pools.DimensionPadding;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.pools.alias.PoolAliasLookup;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
import org.apache.commons.compress.utils.Lists;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class LargeJigsawTemplate {

    private final List<StructurePiece> pieces;
    private final BlockPos center;
    private AtomicBoolean isntSetUp;



    public LargeJigsawTemplate(BlockPos center) {
        this.center = center;
        this.isntSetUp = new AtomicBoolean(true);
        pieces = new CopyOnWriteArrayList<>();
    }

    public @NotNull Optional<Structure.GenerationStub> generate(
            Structure.GenerationContext context,
            Holder<StructureTemplatePool> startPool,
            Optional<ResourceLocation> startJigsawName,
            int maxDepth,
            BlockPos pos,
            boolean useExpansionHack,
            Optional<Heightmap.Types> projectStartToHeightmap,
            int maxDistanceFromCenter,
            PoolAliasLookup aliasLookup,
            DimensionPadding dimensionPadding,
            LiquidSettings liquidSettings,
            float frequency,
            int salt){

        if (isntSetUp.get()) {
            LegacyRandomSource r = new LegacyRandomSource(context.seed());
            LegacyRandomSource r1 = new LegacyRandomSource(center.asLong());
            LegacyRandomSource r2 = new LegacyRandomSource(salt);
            LegacyRandomSource r3 = new LegacyRandomSource(r.nextLong() + r1.nextLong() + r2.nextLong());
            if (r3.nextFloat() <= frequency)
                pieces.addAll(setUp(
                        context,
                        startPool,
                        startJigsawName,
                        maxDepth,
                        pos,
                        useExpansionHack,
                        projectStartToHeightmap,
                        maxDistanceFromCenter,
                        aliasLookup,
                        dimensionPadding,
                        liquidSettings,
                        new LegacyRandomSource(r.nextLong() + r1.nextLong() + r2.nextLong())));


            isntSetUp.set(false);
        }

        List<StructurePiece> pieces2 = Lists.newArrayList();
        BoundingBox chunkBorder = BoundingBox.fromCorners(pos.atY(0), pos.offset(15,0,15).atY(0));
        for (StructurePiece piece : pieces) {
            if (chunkBorder.isInside(piece.getBoundingBox().minX(),
                    0,
                    piece.getBoundingBox().minZ())) {
                pieces2.add(piece);
            }
        }

        if (pieces2.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(new Structure.GenerationStub(pieces2.getFirst().getLocatorPosition(), (builder) -> {
            pieces2.forEach(structurePiece -> {
                builder.addPiece(structurePiece);
                //pieces.remove(structurePiece);
            });
        }));
    }

    private List<StructurePiece> setUp(
            Structure.GenerationContext context,
            Holder<StructureTemplatePool> startPool,
            Optional<ResourceLocation> startJigsawName,
            int maxDepth,
            BlockPos pos,
            boolean useExpansionHack,
            Optional<Heightmap.Types> projectStartToHeightmap,
            int maxDistanceFromCenter,
            PoolAliasLookup aliasLookup,
            DimensionPadding dimensionPadding,
            LiquidSettings liquidSettings,
            RandomSource random){



        Optional<Structure.GenerationStub> stub = LargeJigsawPlacement.addPieces(context,
                startPool,
                startJigsawName,
                maxDepth,
                center,
                useExpansionHack,
                projectStartToHeightmap,
                maxDistanceFromCenter,
                aliasLookup,
                dimensionPadding,
                liquidSettings,
                random);
        if (stub.isPresent()) {
            StructurePiecesBuilder builder = new StructurePiecesBuilder();
            stub.get().generator().ifLeft(consumer ->
                    consumer.accept(builder));
            return builder.build().pieces();
        }

        return Lists.newArrayList();

    }

    public static class Empty extends LargeJigsawTemplate {
        public Empty(BlockPos center) {
            super(center);
        }

        @Override
        public @NotNull Optional<Structure.GenerationStub> generate(Structure.GenerationContext context, Holder<StructureTemplatePool> startPool, Optional<ResourceLocation> startJigsawName, int maxDepth, BlockPos pos, boolean useExpansionHack, Optional<Heightmap.Types> projectStartToHeightmap, int maxDistanceFromCenter, PoolAliasLookup aliasLookup, DimensionPadding dimensionPadding, LiquidSettings liquidSettings, float frequency, int salt) {
            return Optional.empty();
        }
    }


}
