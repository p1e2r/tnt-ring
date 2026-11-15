package com.tntring.neoforge;

import com.tntring.RingCore;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

/** TNT Ring for NeoForge - server-side only. All the logic is in RingCore. */
@Mod("tnt_ring")
public class TntRingNeoForge {
	public TntRingNeoForge() {
		NeoForge.EVENT_BUS.addListener(EntityJoinLevelEvent.class, e -> {
			if (e.getLevel() instanceof ServerLevel level) RingCore.onLoad(e.getEntity(), level);
		});
		NeoForge.EVENT_BUS.addListener(EntityLeaveLevelEvent.class, e -> {
			if (e.getLevel() instanceof ServerLevel level) RingCore.onUnload(e.getEntity(), level);
		});
		NeoForge.EVENT_BUS.addListener(LevelTickEvent.Post.class, e -> {
			if (e.getLevel() instanceof ServerLevel level) RingCore.onTick(level);
		});
	}
}
