/*
 * Muddykat
 * Copyright (c) 2025
 *
 * This code is licensed under "GNU LESSER GENERAL PUBLIC LICENSE"
 * Details can be found in the license file in the root folder of this project
 */

package com.igteam.immersivegeology.common.pack;

import com.igteam.immersivegeology.core.lib.IGLib;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import com.igteam.immersivegeology.core.material.data.stone.IGStoneTypes;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Puts {@link IGDeclaredStonePack} in front of the resource and data pack stacks.
 */
@Mod.EventBusSubscriber(modid = IGLib.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class IGPackFinder
{
	@SubscribeEvent
	public static void addPackFinders(AddPackFindersEvent event)
	{
		// Decided from the configuration alone. Whether any ore blocks were registered for a declared rock type is
		// not knowable yet at this point in start-up, but we can tell if anything was declared.
		boolean anyDeclared = IGStoneTypes.all().stream().anyMatch(stone -> stone.getHostBlockId()!=null);
		if(!anyDeclared) return;

		PackType type = event.getPackType();
		event.addRepositorySource(consumer -> {
			Pack pack = Pack.readMetaAndCreate(
					IGDeclaredStonePack.PACK_ID,
					Component.literal("Immersive Geology declared stone types"),
					true,
					id -> new IGDeclaredStonePack(type),
					type,
					Pack.Position.BOTTOM,
					PackSource.BUILT_IN);
			if(pack!=null) consumer.accept(pack);
		});
	}
}
