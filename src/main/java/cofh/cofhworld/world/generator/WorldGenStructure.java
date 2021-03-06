package cofh.cofhworld.world.generator;

import cofh.cofhworld.util.WeightedRandomBlock;
import cofh.cofhworld.util.WeightedRandomEnum;
import cofh.cofhworld.util.WeightedRandomNBTTag;
import cofh.cofhworld.util.numbers.ConstantProvider;
import cofh.cofhworld.util.numbers.INumberProvider;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.WeightedRandom;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;
import net.minecraft.world.gen.structure.template.PlacementSettings;
import net.minecraft.world.gen.structure.template.Template;

import java.util.List;
import java.util.Random;

public class WorldGenStructure extends WorldGenerator {

	private final PlacementSettings placementSettings = new PlacementSettings();
	private final Template template = new Template();

	private final List<WeightedRandomNBTTag> templates;

	private final List<WeightedRandomBlock> ignoredBlocks;

	private List<WeightedRandomEnum<Rotation>> rots;
	private List<WeightedRandomEnum<Mirror>> mirrors;

	private INumberProvider integrity = new ConstantProvider(2f); // 1++

	public WorldGenStructure(List<WeightedRandomNBTTag> templates, List<WeightedRandomBlock> ignoredBlocks, boolean ignoreEntities) {

		if (templates.size() > 1) {
			this.templates = templates;
		} else {
			this.templates = null;
			template.read(templates.get(0).getCompoundTag());
		}
		if (ignoredBlocks.size() > 1) {
			this.ignoredBlocks = ignoredBlocks;
		} else {
			this.ignoredBlocks = null;
			if (ignoredBlocks.size() > 0) {
				placementSettings.setReplacedBlock(ignoredBlocks.get(0).block);
			}
		}
		placementSettings.setIgnoreEntities(ignoreEntities);
	}

	public WorldGenStructure setIntegrity(INumberProvider itg) {

		integrity = itg;
		return this;
	}

	public WorldGenStructure setDetails(List<WeightedRandomEnum<Rotation>> rot, List<WeightedRandomEnum<Mirror>> mir) {

		if (rot.size() == 1) {
			placementSettings.setRotation(rot.get(0).value);
			rot = null;
		}
		rots = rot;

		if (mir.size() == 1) {
			placementSettings.setMirror(mir.get(0).value);
			mir = null;
		}
		mirrors = mir;
		return this;
	}

	@Override
	public boolean generate(World world, Random random, BlockPos pos) {

		if (templates != null) {
			template.read(WeightedRandom.getRandomItem(random, templates).getCompoundTag());
		}

		// WeightedRandomLong to supply `setSeed` instead of the worldgen random?
		placementSettings.setRandom(random);

		if (rots != null) {
			placementSettings.setRotation(WeightedRandom.getRandomItem(random, rots).value);
		}
		if (mirrors != null) {
			placementSettings.setMirror(WeightedRandom.getRandomItem(random, mirrors).value);
		}

		if (ignoredBlocks != null) {
			placementSettings.setReplacedBlock(WeightedRandom.getRandomItem(random, ignoredBlocks).block);
		}

		BlockPos start = template.getZeroPositionWithTransform(pos, placementSettings.getMirror(), placementSettings.getRotation());

		placementSettings.setIntegrity(integrity.floatValue(world, random, pos));

		template.addBlocksToWorld(world, start, placementSettings, 20);

		return true; // we probably did something. templates don't actually feed back information like that
	}

}
