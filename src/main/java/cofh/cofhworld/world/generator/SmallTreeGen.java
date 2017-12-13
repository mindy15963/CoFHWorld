package cofh.cofhworld.world.generator;

import cofh.cofhworld.feature.Feature;
import cofh.cofhworld.feature.IGenerator;
import cofh.cofhworld.feature.IGeneratorParser;
import cofh.cofhworld.init.FeatureParser;
import cofh.cofhworld.util.WeightedRandomBlock;
import com.typesafe.config.Config;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SmallTreeGen implements IGenerator {

	private final List<WeightedRandomBlock> leaves;
	private final List<WeightedRandomBlock> trunk;
	private final WeightedRandomBlock[] genBlock;

	public WeightedRandomBlock[] genSurface = null;
	public int minHeight = 5;
	public int heightVariance = 3;
	public boolean treeChecks = true;
	public boolean leafVariance = true;
	public boolean relaxedGrowth = false;
	public boolean waterLoving = false;

	public SmallTreeGen(List<WeightedRandomBlock> resource, List<WeightedRandomBlock> leaf, List<WeightedRandomBlock> block) {

		trunk = resource;
		leaves = leaf;
		genBlock = block.toArray(new WeightedRandomBlock[block.size()]);
	}

	protected int getLeafRadius(int height, int level, boolean check) {

		if (check) {
			if (level >= 1 + height - 2) {
				return 2;
			} else {
				return relaxedGrowth ? 0 : 1;
			}
		}

		if (level >= 1 + height - 4) {
			return 1 - ((level - height) / 2);
		} else {
			return 0;
		}
	}

	@Override
	public boolean generate(Feature feature, World world, Random rand, BlockPos pos) {

		int x = pos.getX();
		int y = pos.getY();
		int z = pos.getZ();

		int treeHeight = (heightVariance <= 1 ? 0 : rand.nextInt(heightVariance)) + minHeight;
		int worldHeight = world.getHeight();
		IBlockState state;
		BlockPos offsetPos;

		if (y + treeHeight + 1 <= worldHeight) {
			int xOffset;
			int yOffset;
			int zOffset;

			if (!ClusterGen.canGenerateInBlock(world, x, y - 1, z, genSurface)) {
				return false;
			}

			if (y < worldHeight - treeHeight - 1) {
				if (treeChecks) {
					for (yOffset = y; yOffset <= y + 1 + treeHeight; ++yOffset) {

						int radius = getLeafRadius(treeHeight, yOffset - y, true);

						if (yOffset >= 0 & yOffset < worldHeight) {
							if (radius == 0) {
								offsetPos = new BlockPos(x, yOffset, z);
								state = world.getBlockState(offsetPos);
								if (!(state.getBlock().isLeaves(state, world, offsetPos) || state.getBlock().isAir(state, world, offsetPos) || state.getBlock().isReplaceable(world, offsetPos) || state.getBlock().canBeReplacedByLeaves(state, world, offsetPos) || ClusterGen.canGenerateInBlock(world, offsetPos, genBlock))) {
									return false;
								}

								if (!waterLoving && yOffset >= y + 1) {
									radius = 1;
									for (xOffset = x - radius; xOffset <= x + radius; ++xOffset) {
										for (zOffset = z - radius; zOffset <= z + radius; ++zOffset) {
											offsetPos = new BlockPos(xOffset, yOffset, zOffset);
											state = world.getBlockState(offsetPos);

											if (state.getMaterial().isLiquid()) {
												return false;
											}
										}
									}
								}
							} else {
								for (xOffset = x - radius; xOffset <= x + radius; ++xOffset) {
									for (zOffset = z - radius; zOffset <= z + radius; ++zOffset) {
										offsetPos = new BlockPos(xOffset, yOffset, zOffset);
										state = world.getBlockState(offsetPos);

										if (!(state.getBlock().isLeaves(state, world, offsetPos) || state.getBlock().isAir(state, world, offsetPos) || state.getBlock().canBeReplacedByLeaves(state, world, offsetPos) || ClusterGen.canGenerateInBlock(world, offsetPos, genBlock))) {
											return false;
										}
									}
								}
							}
						} else {
							return false;
						}
					}

					if (!ClusterGen.canGenerateInBlock(world, x, y - 1, z, genSurface)) {
						return false;
					}
					offsetPos = new BlockPos(x, y - 1, z);
					state = world.getBlockState(offsetPos);
					state.getBlock().onPlantGrow(state, world, offsetPos, new BlockPos(x, y, z));
				}

				boolean r = false;

				for (yOffset = y; yOffset <= y + treeHeight; ++yOffset) {

					int var12 = yOffset - (y + treeHeight);
					int radius = getLeafRadius(treeHeight, yOffset - y, false);
					if (radius <= 0) {
						continue;
					}

					for (xOffset = x - radius; xOffset <= x + radius; ++xOffset) {
						int xPos = xOffset - x, t;
						xPos = (xPos + (t = xPos >> 31)) ^ t;

						for (zOffset = z - radius; zOffset <= z + radius; ++zOffset) {
							int zPos = zOffset - z;
							zPos = (zPos + (t = zPos >> 31)) ^ t;
							offsetPos = new BlockPos(xOffset, yOffset, zOffset);
							state = world.getBlockState(offsetPos);

							if (((xPos != radius | zPos != radius) || (!leafVariance || (rand.nextInt(2) != 0 && var12 != 0))) && ((treeChecks && (state.getBlock().isLeaves(state, world, offsetPos) || state.getBlock().isAir(state, world, offsetPos) || state.getBlock().canBeReplacedByLeaves(state, world, offsetPos))) || ClusterGen.canGenerateInBlock(world, offsetPos, genBlock))) {
								r |= ClusterGen.generateBlock(world, xOffset, yOffset, zOffset, leaves);
							}
						}
					}
				}

				for (yOffset = 0; yOffset < treeHeight; ++yOffset) {
					offsetPos = new BlockPos(x, y + yOffset, z);
					state = world.getBlockState(offsetPos);

					if ((treeChecks && (state.getBlock().isAir(state, world, offsetPos) || state.getBlock().isLeaves(state, world, offsetPos) || state.getBlock().isReplaceable(world, offsetPos))) || ClusterGen.canGenerateInBlock(world, offsetPos, genBlock)) {
						r |= ClusterGen.generateBlock(world, x, yOffset + y, z, trunk);
					}
				}

				return r;
			}
		}
		return false;
	}

	public static class Parser implements IGeneratorParser {
		@Override
		public IGenerator parseGenerator(String name, Config genObject, Logger log, List<WeightedRandomBlock> resList, List<WeightedRandomBlock> matList) {

			ArrayList<WeightedRandomBlock> list = new ArrayList<>();
			ArrayList<WeightedRandomBlock> blocks = new ArrayList<>();
			if (genObject.hasPath("surface")) {
				if (!FeatureParser.parseResList(genObject.root().get("surface"), blocks, false)) {
					log.warn("Parsing 'surface' setting for SmallTreeGen on feature {} failed; using defaults", name);
					blocks.clear();
					blocks.add(new WeightedRandomBlock(Blocks.GRASS));
					blocks.add(new WeightedRandomBlock(Blocks.DIRT));
				}
			}

			// TODO: Should this be a required parameter?
			if (genObject.hasPath("leaves")) {
				list = new ArrayList<>();
				if (!FeatureParser.parseResList(genObject.root().get("leaves"), list, true)) {
					log.warn("Parsing 'leaves' setting for SmallTreeGen on feature {} failed", name);
					list.clear();
				}
			}

			SmallTreeGen r = new SmallTreeGen(resList, list, matList);

			if (blocks.size() > 0) {
				r.genSurface = blocks.toArray(new WeightedRandomBlock[blocks.size()]);
			}

			if (genObject.hasPath("min-height")) {
				r.minHeight = genObject.getInt("min-height");
			}
			if (genObject.hasPath("height-variance")) {
				r.heightVariance = genObject.getInt("height-variance");
			}

			if (genObject.hasPath("tree-checks")) {
				r.treeChecks = genObject.getBoolean("tree-checks");
			}
			if (genObject.hasPath("relaxed-growth")) {
				r.relaxedGrowth = genObject.getBoolean("relaxed-growth");
			}
			if (genObject.hasPath("water-loving")) {
				r.waterLoving = genObject.getBoolean("water-loving");
			}
			if (genObject.hasPath("leaf-variance")) {
				r.leafVariance = genObject.getBoolean("leaf-variance");
			}

			return r;
		}
	}
}