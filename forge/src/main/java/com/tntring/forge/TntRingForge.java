package com.tntring.forge;

import com.tntring.RingCore;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.fml.common.Mod;

/** TNT Ring for Forge - server-side only. All the logic is in RingCore. */
@Mod("tnt_ring")
public class TntRingForge {
	public TntRingForge() {
		EntityJoinLevelEvent.BUS.addListener((EntityJoinLevelEvent e) -> {
			if (e.getLevel() instanceof ServerLevel level) RingCore.onLoad(e.getEntity(), level);
		});
		EntityLeaveLevelEvent.BUS.addListener((EntityLeaveLevelEvent e) -> {
			if (e.getLevel() instanceof ServerLevel level) RingCore.onUnload(e.getEntity(), level);
		});
		TickEvent.LevelTickEvent.Post.BUS.addListener((TickEvent.LevelTickEvent.Post e) -> {
			if (e.level() instanceof ServerLevel level) RingCore.onTick(level);
		});
	}
}
