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

public class GenerationBandedNoise implements IGenerationPattern
{
	public INoise3D getiNoise3D(int featureSize, long seed)
	{
		SimplexNoise3D simplex = new SimplexNoise3D(seed);
		SimplexNoise3D warpSimplex = new SimplexNoise3D(seed - 1);

		// The decorator chains are built once, here, rather than inside the returned lambda. World generation
		// evaluates these hundreds of thousands of times per vein, and rebuilding the chain per sample allocates
		// a lambda per link and re-runs the Math.pow loop inside octaves() every time. The chain is stateless,
		// so hoisting it is arithmetically identical and safe to share between the worker threads.

		// Warp noise generator for subtle band distortion
		INoise3D warpChain = warpSimplex
				.bias(-0.6f)
				.octaves(2, 0.7f)  // Fewer octaves, reducing finer distortion
				.sinWarp(1.5f, 0.75f)  // Lower warping intensity
				.flattened(-0.8f, 0.8f);  // Slightly lower amplitude
		INoise3D warp = (x, y, z) -> warpChain.noise(x / 80, y / 50, z / 80);  // Gentle stretch in Y-axis

		INoise3D baseChain = simplex
				.flattened(-1, 1)
				.octaves(3, 0.9f)
				.add(warp);  // Apply the warp to distort layers

		// Primary noise generator
		return (x, y, z) -> {
			// Base noise with slight Y stretch for banding
			float baseNoise = baseChain.noise(x / featureSize, y / 10, z / featureSize); // Stretched Y for more controlled banding

			// The band effect asked the warp for the same value twice; it is a pure function of the position,
			// so one evaluation covers both uses.
			float bandWarp = warp.noise(x, y, z);

			// More structured banding with less erratic warping
			float bandEffect = (float) Math.sin(
					(y / (5.5f + bandWarp * 0.2f))  // Less influence from warp
							- (bandWarp * 0.15f)  // Reduce phase shift from warp
			);

			return (baseNoise * bandEffect) * -1; // Apply banding effect to noise
		};
	}
}
