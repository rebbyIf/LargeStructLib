package dev.rebby.largestructlib.worldgen.structure.piece;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.rebby.largestructlib.worldgen.structure.check_value.CheckValue;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.pools.alias.PoolAliasLookup;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public record AlternatingDensityStructure(String name, Holder<StructureTemplatePool> pool,
                                          BlockPos boundingSize,
                                          BlockPos size, BlockPos location, List<String> grid3D) implements DensityStructurePiece {

    public static final MapCodec<AlternatingDensityStructure> ALTERNATING_STRUCTURE_CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                            Codec.STRING.optionalFieldOf("name", "unnamed").forGetter(AlternatingDensityStructure::name),
                            StructureTemplatePool.CODEC.fieldOf("elements").forGetter(structure -> structure.pool),
                            BlockPos.CODEC.fieldOf("bounding_size").forGetter(AlternatingDensityStructure::boundingSize),
                            BlockPos.CODEC.optionalFieldOf("size", new BlockPos(1, 1, 1)).forGetter(AlternatingDensityStructure::size),
                            BlockPos.CODEC.fieldOf("location").forGetter(AlternatingDensityStructure::location),
                            Codec.STRING.listOf().fieldOf("grid").forGetter(AlternatingDensityStructure::grid3D))
                    .apply(instance, AlternatingDensityStructure::new));

    @Override
    public boolean place(List<PoolElementStructurePiece> pieces, int scale, BlockPos pos, DensityFunction densityFunction,
                         StructureTemplateManager structureTemplateManager, Structure.GenerationContext context,
                         Map<String, CheckValue> values, PoolAliasLookup aliasLookup,
                         LiquidSettings liquidSettings, int top, int bottom) {

        BlockPos distanceFromOrigin = getDistanceFromOrigin(scale, pos, context);


        BlockPos posOrigin = pos.subtract(distanceFromOrigin);

        if (!checkPlace(posOrigin, scale, values, densityFunction, location, boundingSize, grid3D)) {
            return false;
        }

        if (!DensityStructurePiece.checkStructure(posOrigin, context.seed(),
                pool.value())) {
            return false;
        }

        if (!distanceFromOrigin.equals(BlockPos.ZERO)) {

            return true;
        }

        StructurePoolElement structurePoolElement = DensityStructurePiece.getStructure(pos, context.seed(), pool.value());

        pieces.add(new PoolElementStructurePiece(
                structureTemplateManager,
                structurePoolElement,
                pos,
                structurePoolElement.getGroundLevelDelta(),
                Rotation.NONE,
                structurePoolElement.getBoundingBox(structureTemplateManager, pos, Rotation.NONE),
                liquidSettings));

        return true;
    }

    private @NotNull BlockPos getDistanceFromOrigin(int scale, BlockPos pos, Structure.GenerationContext context) {
        if (boundingSize.getX() * boundingSize.getY() * boundingSize.getZ() > grid3D.size()) {
            throw new IndexOutOfBoundsException("Scaling out of bounds for structure " + getName());
        }

        BlockPos boundingScale = size.multiply(scale);

        BlockPos distanceFromOrigin = new BlockPos(
                ((pos.getX() % boundingScale.getX()) + boundingScale.getX()) % boundingScale.getX(),
                ((pos.getY() % boundingScale.getY()) + boundingScale.getY()) % boundingScale.getY(),
                ((pos.getZ() % boundingScale.getZ()) + boundingScale.getZ()) % boundingScale.getZ());

        LegacyRandomSource r = new LegacyRandomSource(context.seed());
        LegacyRandomSource r1 = new LegacyRandomSource(pos.atY(0).asLong());
        LegacyRandomSource random = new LegacyRandomSource(r.nextLong() + r1.nextLong());
        int offset = random.nextInt(size.getY()) * scale;

        distanceFromOrigin = new BlockPos(distanceFromOrigin.getX(),
                (distanceFromOrigin.getY() + offset) % boundingScale.getY(),
                distanceFromOrigin.getZ());
        return distanceFromOrigin;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public @NotNull BlockPos getSize() {
        return size;
    }

    @Override
    public StructurePieceType<?> getType() {
        return StructurePieceTypes.ALTERNATING.get();
    }
}
