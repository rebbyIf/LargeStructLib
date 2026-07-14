package dev.rebby.largestructlib.worldgen.structure;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.rebby.largestructlib.LargeStructureLib;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.WorldDimensions;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.minecraft.world.level.levelgen.structure.pools.DimensionPadding;
import net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.pools.alias.PoolAliasBinding;
import net.minecraft.world.level.levelgen.structure.pools.alias.PoolAliasLookup;
import net.minecraft.world.level.levelgen.structure.structures.JigsawStructure;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Mostly copied from Minecraft's JigsawStructure class
 */
public class LargeJigsawStructure extends Structure {
    public static final DimensionPadding DEFAULT_DIMENSION_PADDING;
    public static final LiquidSettings DEFAULT_LIQUID_SETTINGS;
    public static final int MAX_TOTAL_STRUCTURE_RANGE = 1024;
    public static final int MIN_DEPTH = 0;
    public static final int MAX_DEPTH = 256;
    public static final MapCodec<LargeJigsawStructure> CODEC;

    private final Map<ChunkPos, LargeJigsawTemplate> templateMap;

    private final Holder<StructureTemplatePool> startPool;
    private final Optional<ResourceLocation> startJigsawName;
    private final int maxDepth;
    private final HeightProvider startHeight;
    private final boolean useExpansionHack;
    private final Optional<Heightmap.Types> projectStartToHeightmap;
    private final int maxDistanceFromCenter;
    private final int padding;
    private final int rarityFilter;
    private final int salt;
    private final List<PoolAliasBinding> poolAliases;
    private final DimensionPadding dimensionPadding;
    private final LiquidSettings liquidSettings;

    public LargeJigsawStructure(StructureSettings settings,
                                Holder<StructureTemplatePool> startPool,
                                Optional<ResourceLocation> startJigsawName,
                                int maxDepth,
                                HeightProvider startHeight,
                                boolean useExpansionHack,
                                Optional<Heightmap.Types> projectStartToHeightmap,
                                int maxDistanceFromCenter,
                                int padding,
                                int rarityFilter,
                                int salt,
                                List<PoolAliasBinding> poolAliases,
                                DimensionPadding dimensionPadding,
                                LiquidSettings liquidSettings) {
        super(settings);
        this.startPool = startPool;
        this.startJigsawName = startJigsawName;
        this.maxDepth = maxDepth;
        this.startHeight = startHeight;
        this.useExpansionHack = useExpansionHack;
        this.projectStartToHeightmap = projectStartToHeightmap;
        this.padding = padding;
        this.rarityFilter = rarityFilter;
        this.salt = salt;
        this.maxDistanceFromCenter = maxDistanceFromCenter;
        this.poolAliases = poolAliases;
        this.dimensionPadding = dimensionPadding;
        this.liquidSettings = liquidSettings;

        this.templateMap = new HashMap<>();
    }



    @Override
    protected @NotNull Optional<GenerationStub> findGenerationPoint(@NotNull GenerationContext context) {

        ChunkPos chunkpos = context.chunkPos();
        int i = this.startHeight.sample(context.random(), new WorldGenerationContext(context.chunkGenerator(), context.heightAccessor()));
        BlockPos blockpos = new BlockPos(chunkpos.getMinBlockX(), i, chunkpos.getMinBlockZ());
        int j = ((maxDistanceFromCenter >> 4) + padding) * 2;
        ChunkPos key = new ChunkPos((chunkpos.x / j) * j, (chunkpos.z / j) * j);
        if (!templateMap.containsKey(key)) {
            LargeStructureLib.LOGGER.info("Generating large structure at: {}, {}", key.x,key.z);
            templateMap.put(key, new LargeJigsawTemplate(key.getWorldPosition().atY(i)));
        }
        return templateMap.get(key).generate(
                context,
                this.startPool,
                this.startJigsawName,
                this.maxDepth,
                blockpos,
                this.useExpansionHack,
                this.projectStartToHeightmap,
                this.maxDistanceFromCenter,
                PoolAliasLookup.create(this.poolAliases, blockpos, context.seed()),
                this.dimensionPadding,
                this.liquidSettings,
                this.rarityFilter,
                this.salt);
    }

    @Override
    public @NotNull StructureType<?> type() {
        return ModStructureTypes.LARGE_JIGSAW_STRUCTURE.get();
    }

    static {
        DEFAULT_DIMENSION_PADDING = DimensionPadding.ZERO;
        DEFAULT_LIQUID_SETTINGS = LiquidSettings.APPLY_WATERLOGGING;
        CODEC = RecordCodecBuilder.mapCodec(
                (p_227640_) -> p_227640_.group(settingsCodec(p_227640_),
                        StructureTemplatePool.CODEC.fieldOf("start_pool").forGetter((p_227656_) -> p_227656_.startPool),
                        ResourceLocation.CODEC.optionalFieldOf("start_jigsaw_name").forGetter((p_227654_) -> p_227654_.startJigsawName),
                        Codec.intRange(0,MAX_DEPTH).fieldOf("size").forGetter((p_227652_) -> p_227652_.maxDepth),
                        HeightProvider.CODEC.fieldOf("start_height").forGetter((p_227649_) -> p_227649_.startHeight),
                        Codec.BOOL.fieldOf("use_expansion_hack").forGetter((p_227646_) -> p_227646_.useExpansionHack),
                        Heightmap.Types.CODEC.optionalFieldOf("project_start_to_heightmap").forGetter((p_227644_) -> p_227644_.projectStartToHeightmap),
                        Codec.intRange(1, MAX_TOTAL_STRUCTURE_RANGE).fieldOf("max_distance_from_center").forGetter((p_227642_) -> p_227642_.maxDistanceFromCenter),
                        Codec.intRange(1, 4096).fieldOf("padding").forGetter((instance) -> instance.padding),
                        Codec.intRange(0, 4096).fieldOf("rarity_filter").forGetter((instance) -> instance.rarityFilter),
                        ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("salt", 0).forGetter((instance) -> instance.salt),
                        Codec.list(PoolAliasBinding.CODEC).optionalFieldOf("pool_aliases", List.of()).forGetter((p_307187_) -> p_307187_.poolAliases),
                        DimensionPadding.CODEC.optionalFieldOf("dimension_padding", DEFAULT_DIMENSION_PADDING).forGetter((p_348455_) -> p_348455_.dimensionPadding),
                        LiquidSettings.CODEC.optionalFieldOf("liquid_settings", DEFAULT_LIQUID_SETTINGS).forGetter((p_352036_) -> p_352036_.liquidSettings))
                        .apply(p_227640_,LargeJigsawStructure::new));
    }
}
