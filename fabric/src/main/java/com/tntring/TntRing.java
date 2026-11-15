package com.tntring;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.phys.Vec3;

import java.util.*;

/**
 * TNT Ring - server-side only.
 *
 * When you light a TNT, a ring of lit TNT appears around it. The whole ring and the
 * center go off at the same moment, and every one of them is much stronger than normal TNT.
 */
public class TntRing implements ModInitializer {

	// ---------------- Tweak these ----------------

	/** How many TNT in the ring. */
	static final int RING_COUNT = 100;
	/** Ring radius in blocks. */
	static final double RING_RADIUS = 16.0;
	/** Explosion power of the center and every ring TNT. Vanilla TNT = 4. Max 128. */
	static final float POWER = 16f;

	// ---------------------------------------------

	static final String RING_TAG = "tntring_ring";       // a TNT that is part of a ring
	static final String DONE_TAG = "tntring_center";     // a center that already got its ring
	static final String NO_RING = "tntring_noring";      // set off by a ring blast: no ring of its own
	static final String CHAIN_NO_KIDS = "tntchain_nokids"; // so the TNT Chain mod doesn't multiply rings

	static final class LevelState {
		final List<PrimedTnt> toRing = new ArrayList<>();
		long lastBlastTick = Long.MIN_VALUE;
		final List<Vec3> lastBlasts = new ArrayList<>();
	}

	static final Map<ServerLevel, LevelState> STATES = new WeakHashMap<>();

	static LevelState state(ServerLevel level) {
		return STATES.computeIfAbsent(level, l -> new LevelState());
	}

	@Override
	public void onInitialize() {
		ServerEntityEvents.ENTITY_LOAD.register(TntRing::onLoad);
		ServerEntityEvents.ENTITY_UNLOAD.register(TntRing::onUnload);
		ServerTickEvents.END_LEVEL_TICK.register(TntRing::onTick);
	}

	static void onLoad(Entity entity, ServerLevel level) {
		if (!(entity instanceof PrimedTnt tnt)) return;
		Set<String> tags = tnt.entityTags();
		if (tags.contains(RING_TAG) || tags.contains(DONE_TAG) || tags.contains(NO_RING)) return;
		if (tags.stream().anyMatch(t -> t.startsWith("tntchain_gen_"))) return; // TNT Chain offspring

		LevelState s = state(level);
		if (level.getGameTime() - s.lastBlastTick <= 1) {
			for (Vec3 b : s.lastBlasts) {
				if (b.distanceTo(tnt.position()) < POWER * 2 + 2) {
					tnt.addTag(NO_RING); // a TNT block caught in a ring blast - just explodes normally
					return;
				}
			}
		}
		s.toRing.add(tnt); // make the ring at the end of this tick
	}

	static void onUnload(Entity entity, ServerLevel level) {
		if (!(entity instanceof PrimedTnt tnt)) return;
		if (tnt.getRemovalReason() != Entity.RemovalReason.DISCARDED || tnt.getFuse() > 0) return;
		Set<String> tags = tnt.entityTags();
		if (!tags.contains(RING_TAG) && !tags.contains(DONE_TAG)) return;
		LevelState s = state(level);
		if (s.lastBlastTick != level.getGameTime()) {
			s.lastBlasts.clear();
			s.lastBlastTick = level.getGameTime();
		}
		s.lastBlasts.add(tnt.position());
	}

	static void onTick(ServerLevel level) {
		LevelState s = state(level);
		if (s.toRing.isEmpty()) return;
		List<PrimedTnt> list = new ArrayList<>(s.toRing);
		s.toRing.clear();

		for (PrimedTnt center : list) {
			if (center.isRemoved()) continue;
			center.addTag(DONE_TAG);
			int fuse = Math.max(1, center.getFuse());

			// make the center powerful too
			run(level, String.format(Locale.ROOT, "data merge entity %s {explosion_power:%.1ff}", center.getStringUUID(), POWER));

			for (int i = 0; i < RING_COUNT; i++) {
				double a = Math.PI * 2 * i / RING_COUNT;
				double x = center.getX() + Math.cos(a) * RING_RADIUS;
				double z = center.getZ() + Math.sin(a) * RING_RADIUS;
				run(level, String.format(Locale.ROOT,
						"summon minecraft:tnt %.3f %.3f %.3f {fuse:%d,explosion_power:%.1ff,Motion:[0d,0.2d,0d],Tags:[\"%s\",\"%s\"]}",
						x, center.getY(), z, fuse, POWER, RING_TAG, CHAIN_NO_KIDS));
			}
		}
	}

	static void run(ServerLevel level, String cmd) {
		MinecraftServer server = level.getServer();
		CommandSourceStack src = server.createCommandSourceStack().withLevel(level).withSuppressedOutput();
		server.getCommands().performPrefixedCommand(src, cmd);
	}
}
