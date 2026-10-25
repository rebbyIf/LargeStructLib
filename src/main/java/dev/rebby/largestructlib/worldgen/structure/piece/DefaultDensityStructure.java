package dev.rebby.largestructlib.worldgen.structure.piece;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.rebby.largestructlib.worldgen.structure.check_value.CheckValue;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.DensityFunction;
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

public record DefaultDensityStructure(String name, Holder<StructureTemplatePool> pool,
                                      BlockPos boundingSize,
                                      BlockPos size, BlockPos location, int yOffset, List<String> grid3D) implements DensityStructurePiece {

    public static final MapCodec<DefaultDensityStructure> DEFAULT_STRUCTURE_CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                            Codec.STRING.optionalFieldOf("name", "unnamed").forGetter(structure -> structure.name),
                            StructureTemplatePool.CODEC.fieldOf("elements").forGetter(structure -> structure.pool),
                            BlockPos.CODEC.fieldOf("bounding_size").forGetter(structure -> structure.boundingSize),
                            BlockPos.CODEC.optionalFieldOf("size", new BlockPos(1, 1, 1)).forGetter(structure -> structure.size),
                            BlockPos.CODEC.fieldOf("location").forGetter(structure -> structure.location),
                            Codec.INT.optionalFieldOf("y_offset", 0).forGetter(DefaultDensityStructure::yOffset),
                            Codec.STRING.listOf().fieldOf("grid").forGetter(structure -> structure.grid3D))
                    .apply(instance, DefaultDensityStructure::new));




    @Override
    public boolean place(List<PoolElementStructurePiece> pieces, int scale, BlockPos pos, DensityFunction densityFunction,
                StructureTemplateManager structureTemplateManager, Structure.GenerationContext context,
                Map<String, CheckValue> values, PoolAliasLookup aliasLookup,
                LiquidSettings liquidSettings, int top, int bottom) {

        BlockPos distanceFromOrigin = getDistanceFromOrigin(scale, pos);




        if (!checkPlace(pos.subtract(distanceFromOrigin), scale, values, densityFunction, location, boundingSize, grid3D)) {
            return false;
        }


        if (!DensityStructurePiece.checkStructure(pos.subtract(distanceFromOrigin), context.seed(),
                pool.value())) {
            return false;
        }

        if (!distanceFromOrigin.equals(BlockPos.ZERO)) {

            return true;
        }

        StructurePoolElement structurePoolElement = DensityStructurePiece.getStructure(pos, context.seed(), pool.value());

        pieces.add(new PoolElementStructurePiece(
                context.structureTemplateManager(),
                structurePoolElement,
                pos,
                structurePoolElement.getGroundLevelDelta(),
                Rotation.NONE,
                structurePoolElement.getBoundingBox(context.structureTemplateManager(), pos, Rotation.NONE),
                liquidSettings));

        return true;
    }

    private @NotNull BlockPos getDistanceFromOrigin(int scale, BlockPos pos) {
        if (boundingSize.getX() * boundingSize.getY() * boundingSize.getZ() > grid3D.size() ||
                yOffset >= size.getY()) {
            throw new IndexOutOfBoundsException("Scaling out of bounds for structure " + getName());
        }

        BlockPos offsetPos = pos.offset(0, yOffset * scale, 0);

        BlockPos boundingScale = size.multiply(scale);

        return new BlockPos(
                ((offsetPos.getX() % boundingScale.getX()) + boundingScale.getX()) % boundingScale.getX(),
                ((offsetPos.getY() % boundingScale.getY()) + boundingScale.getY()) % boundingScale.getY(),
                ((offsetPos.getZ() % boundingScale.getZ()) + boundingScale.getZ()) % boundingScale.getZ());
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
        return StructurePieceTypes.DEFAULT.get();
    }
}
