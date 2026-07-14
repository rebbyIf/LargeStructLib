package dev.rebby.largestructlib.worldgen.structure;

import dev.rebby.largestructlib.LargeStructureLib;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import net.minecraft.world.level.levelgen.structure.pools.DimensionPadding;
import net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.pools.alias.PoolAliasLookup;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
import org.apache.commons.compress.utils.Lists;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class LargeJigsawTemplate {

    private final List<StructurePiece> pieces;
    private final BlockPos center;
    private boolean isntSetUp;

    public LargeJigsawTemplate(BlockPos center) {
        this.center = center;
        this.isntSetUp = true;
        pieces = new ArrayList<>();
    }

    public boolean isntSetUp() {
        return isntSetUp;
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
            LiquidSettings liquidSettings){

        if (isntSetUp) {
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
                    liquidSettings));


            isntSetUp = false;
        }

        List<StructurePiece> pieces2 = Lists.newArrayList();
        BoundingBox chunkBoarder = BoundingBox.fromCorners(pos.atY(0), pos.offset(15,0,15).atY(0));
        for (StructurePiece piece : pieces) {
            if (chunkBoarder.isInside(piece.getBoundingBox().minX(),
                    0,
                    piece.getBoundingBox().minZ())) {
                pieces2.add(piece);
            }
        }

        if (pieces2.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(new Structure.GenerationStub(pieces2.getFirst().getLocatorPosition(), (builder) -> {
            pieces2.forEach(builder::addPiece);
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
            LiquidSettings liquidSettings){

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
                liquidSettings);
        if (stub.isPresent()) {
            StructurePiecesBuilder builder = new StructurePiecesBuilder();
            stub.get().generator().ifLeft(consumer -> consumer.accept(builder));
            return builder.build().pieces();
        }

        return Lists.newArrayList();

    }
}
