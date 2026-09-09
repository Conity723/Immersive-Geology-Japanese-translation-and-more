/*
 * Muddykat
 * Copyright (c) 2025
 *
 * This code is licensed under "GNU LESSER GENERAL PUBLIC LICENSE"
 * Details can be found in the license file in the root folder of this project
 */

package com.igteam.immersivegeology.common.world.features.helper.noise;

import com.igteam.immersivegeology.common.world.noise.INoise3D;
import com.igteam.immersivegeology.common.world.noise.SimplexNoise3D;

public class GenerationDefaultNoise implements IGenerationPattern
{
	public INoise3D getiNoise3D(int featureSize, long seed)
	{
		SimplexNoise3D simplex = new SimplexNoise3D(seed);
		SimplexNoise3D warpSimplex = new SimplexNoise3D(seed-1);

		// The decorator chains are built once, here, rather than inside the returned lambda. World generation
		// evaluates these hundreds of thousands of times per vein, and rebuilding the chain per sample allocates
		// a lambda per link and re-runs the Math.pow loop inside octaves() every time. The chain is stateless,
		// so hoisting it is arithmetically identical and safe to share between the worker threads.

		// Warp noise generator for spacing
		INoise3D warpChain = warpSimplex
				.octaves(2, 1f)
				.sinWarp(2,1)
				.flattened(-1,1)
				.bias(-.5f);
		INoise3D warp = (x, y, z) -> warpChain.noise(x / 24, y / 24, z / 24);

		// Primary noise generator
		INoise3D chain = simplex
				.bias(-0.5f + (Math.max(0, Math.min(0.5f, (float)featureSize/ 100))))
				.flattened(-1,1)
				.octaves(2, 1f)
				.add(warp);
		return (x, y, z) -> chain.noise(x / 24, y /24, z /24);
	}
}
