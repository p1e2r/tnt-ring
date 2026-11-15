package com.tntring.paper;

import com.destroystokyo.paper.event.entity.EntityAddToWorldEvent;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.*;

/**
 * TNT Ring for Paper - a server plugin.
 *
 * When you light a TNT, a ring of lit TNT appears around it. The whole ring and the
 * center go off at the same moment, and every one of them is much stronger than normal TNT.
 */
public class TntRingPaper extends JavaPlugin implements Listener {

	// ---------------- Tweak these ----------------

	/** How many TNT in the ring. */
	static final int RING_COUNT = 100;
	/** Ring radius in blocks. */
	static final double RING_RADIUS = 16.0;
	/** Explosion power of the center and every ring TNT. Vanilla TNT = 4. */
	static final float POWER = 16f;

	// ---------------------------------------------

	static final String RING_TAG = "tntring_ring";
	static final String DONE_TAG = "tntring_center";
	static final String NO_RING = "tntring_noring";
	static final String CHAIN_NO_KIDS = "tntchain_nokids";

	static final class WorldState {
		long lastBlastTick = Long.MIN_VALUE;
		final List<Location> lastBlasts = new ArrayList<>();
	}

	final Map<UUID, WorldState> states = new HashMap<>();

	WorldState state(World w) {
		return states.computeIfAbsent(w.getUID(), k -> new WorldState());
	}

	@Override
	public void onEnable() {
		getServer().getPluginManager().registerEvents(this, this);
	}

	@EventHandler
	public void onAdd(EntityAddToWorldEvent e) {
		if (!(e.getEntity() instanceof TNTPrimed tnt)) return;
		Set<String> tags = tnt.getScoreboardTags();
		if (tags.contains(RING_TAG) || tags.contains(DONE_TAG) || tags.contains(NO_RING)) return;
		if (tags.stream().anyMatch(t -> t.startsWith("tntchain_gen_"))) return;

		World w = tnt.getWorld();
		WorldState s = state(w);
		if (w.getGameTime() - s.lastBlastTick <= 1) {
			for (Location b : s.lastBlasts) {
				if (b.getWorld() == w && b.distance(tnt.getLocation()) < POWER * 2 + 2) {
					tnt.addScoreboardTag(NO_RING); // a TNT block caught in a ring blast - just explodes normally
					return;
				}
			}
		}
		tnt.addScoreboardTag(DONE_TAG);
		// spawning entities while one is being added isn't safe - build the ring right after
		getServer().getScheduler().runTask(this, () -> makeRing(tnt));
	}

	void makeRing(TNTPrimed center) {
		if (!center.isValid()) return;
		int fuse = Math.max(1, center.getFuseTicks());
		center.setYield(POWER);
		Location c = center.getLocation();
		World w = c.getWorld();
		for (int i = 0; i < RING_COUNT; i++) {
			double a = Math.PI * 2 * i / RING_COUNT;
			Location at = c.clone().add(Math.cos(a) * RING_RADIUS, 0, Math.sin(a) * RING_RADIUS);
			w.spawn(at, TNTPrimed.class, t -> {
				t.addScoreboardTag(RING_TAG);
				t.addScoreboardTag(CHAIN_NO_KIDS);
				t.setFuseTicks(fuse);
				t.setYield(POWER);
				t.setVelocity(new Vector(0, 0.2, 0));
			});
		}
	}

	/** Remember where ring TNT blew up, so TNT blocks it sets off don't make rings of their own. */
	@EventHandler
	public void onPrime(ExplosionPrimeEvent e) {
		if (!(e.getEntity() instanceof TNTPrimed tnt)) return;
		Set<String> tags = tnt.getScoreboardTags();
		if (!tags.contains(RING_TAG) && !tags.contains(DONE_TAG)) return;
		World w = tnt.getWorld();
		WorldState s = state(w);
		if (s.lastBlastTick != w.getGameTime()) {
			s.lastBlasts.clear();
			s.lastBlastTick = w.getGameTime();
		}
		s.lastBlasts.add(tnt.getLocation());
	}
}
