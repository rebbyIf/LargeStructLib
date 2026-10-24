package dev.rebby.largestructlib.worldgen.structure;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.rebby.largestructlib.worldgen.density_function.DensityFunctionWrapper;
import dev.rebby.largestructlib.worldgen.structure.check_value.CheckValue;
import dev.rebby.largestructlib.worldgen.structure.piece.DensityStructurePiece;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pools.alias.PoolAliasLookup;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class DensityBasedStructure extends Structure {

    public static final MapCodec<DensityBasedStructure> CODEC = RecordCodecBuilder.<DensityBasedStructure>mapCodec(
                    instance -> instance.group(
                                    settingsCodec(instance),
                                    Codec.INT.fieldOf("min_y").forGetter(structure -> structure.minY),
                                    Codec.INT.fieldOf("max_y").forGetter(structure -> structure.maxY),
                                    Codec.intRange(2, 4).fieldOf("factor").forGetter(structure -> structure.fac),
                                    DensityFunction.CODEC.fieldOf("density_function").forGetter(structure -> structure.densityFunction),
                                    Codec.unboundedMap(Codec.STRING, CheckValue.DIRECT_CODEC).fieldOf("values").forGetter(structure -> structure.values),
                                    DensityStructurePiece.DIRECT_CODEC.listOf().fieldOf("structures").forGetter(structure -> structure.structures),
                                    LiquidSettings.CODEC.optionalFieldOf("liquid_settings",
                                            LiquidSettings.APPLY_WATERLOGGING).forGetter(structure -> structure.liquidSettings))
                            .apply(instance, DensityBasedStructure::new))
            .validate(DensityBasedStructure::validate);

    public static final String STRUCTURE_PATH = "density_based";

    private static DataResult<DensityBasedStructure> validate(DensityBasedStructure structure) {
        if (structure.minY >= structure.maxY) {
            return DataResult.error(() -> "Structure's min value must not be greater than or equal to it's max");
        }

        for (DensityStructurePiece piece : structure.structures) {
            BlockPos scale = piece.getSize().multiply((int) Math.pow(2, structure.fac));

            if (scale.getY() > structure.maxY - structure.minY) {
                return DataResult.error(() -> "Piece "+piece.getName()+" was too big");
            }
        }

        return DataResult.success(structure);
    }

    private final int minY;
    private final int maxY;
    private final int fac;
    private final Holder<DensityFunction> densityFunction;
    private DensityFunction densityFunctionImpl = null;
    private final Map<String, CheckValue> values;
    private final List<DensityStructurePiece> structures;
    private final LiquidSettings liquidSettings;


    public DensityBasedStructure(StructureSettings config, int minY, int maxY, int fac,
                                 Holder<DensityFunction> densityFunction, Map<String, CheckValue> values,
                                 List<DensityStructurePiece> structures, LiquidSettings liquidSettings) {
        super(config);
        this.minY = minY;
        this.maxY = maxY;
        this.fac = fac;
        this.densityFunction = densityFunction;
        this.values = values;
        this.structures = structures;
        this.liquidSettings = liquidSettings;
    }

    @Override
    public @NotNull Optional<Structure.GenerationStub> findGenerationPoint(Structure.@NotNull GenerationContext context) {
        return getStructurePosition(context);
    }

    protected Optional<Structure.GenerationStub> getStructurePosition(Structure.GenerationContext context) {
        ChunkPos chunkPos = context.chunkPos();
        BlockPos pos = new BlockPos(chunkPos.getMinBlockX(), minY, chunkPos.getMinBlockZ());
        return generate(context, context.structureTemplateManager(), pos);
    }

    private Optional<Structure.GenerationStub> generate(Structure.GenerationContext context, StructureTemplateManager structureTemplateManager, BlockPos startPos) {




        List<PoolElementStructurePiece> pieces = Lists.newArrayList();
        ChunkGenerator chunkGenerator = context.chunkGenerator();

        if (densityFunctionImpl == null && chunkGenerator instanceof NoiseBasedChunkGenerator) {

            densityFunctionImpl = densityFunction.value().mapAll(new DensityFunctionWrapper(context, ((NoiseBasedChunkGenerator) chunkGenerator).generatorSettings().value()));
        }

        int scale = (int) Math.pow(2, fac);

        boolean [][][] completed = new boolean[16/scale][(maxY-minY)/scale][16/scale];

        boolean cantPlace = true;
        int i = 0;
        for (int x = 0; x < 16; x += scale) {
            int j = 0;
            for (int y = 0; y < maxY - minY; y += scale) {
                int k = 0;
                for (int z = 0; z < 16; z += scale) {
                    BlockPos pos = startPos.offset(x,y,z);
                    boolean biomeTest = context.validBiome().test(
                            context.chunkGenerator().getBiomeSource().getNoiseBiome(
                                    QuartPos.fromBlock(pos.getX()),
                                    QuartPos.fromBlock(pos.getY()),
                                    QuartPos.fromBlock(pos.getZ()),
                                    context.randomState().sampler()));
                    if (biomeTest) {
                        cantPlace = false;
                    }

                    completed[i][j][k] = !biomeTest;

                    k++;
                }
                j++;

            }
            i++;
        }

        if (cantPlace) {
            return Optional.empty();
        }



        i = 0;
        for (int x = 0; x < 16; x += scale) {
            int j = 0;
            for (int y = 0; y < maxY - minY; y += scale) {
                int k = 0;
                for (int z = 0; z < 16; z += scale) {

                    BlockPos pos = startPos.offset(x, y, z);

                    for (DensityStructurePiece structure : structures) {

                        if (structure.place(pieces, scale, pos, densityFunctionImpl, structureTemplateManager,
                                context, values, PoolAliasLookup.create(List.of(), pos,
                                        context.seed()), liquidSettings, maxY, minY)) {

                            break;

                        }
                    }
                    k++;
                }
                j++;
            }
            i++;


        }

        if (pieces.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(new Structure.GenerationStub(startPos, collector -> {
            for (PoolElementStructurePiece piece : pieces) {
                collector.addPiece(piece);
            }
        }));
    }

    @Override
    public @NotNull StructureType<?> type() {
        return ModStructureTypes.DENSITY_BASED_STRUCTURE.get();
    }
}
