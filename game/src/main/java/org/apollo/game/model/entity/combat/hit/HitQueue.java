package org.apollo.game.model.entity.combat.hit;

import org.apollo.game.model.entity.Mob;
import org.apollo.game.model.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Represents a hit-queue, processing pending hits
 * as well as pending damage.
 *
 * @author Professor Oak
 */
public class HitQueue {

	//..Our list containing all our incoming hits waiting to be processed.
	private final List<PendingHit> pendingHits = new ArrayList<>();

	//..Our queue of current damage waiting to be dealt.
	private final Queue<HitDamage> pendingDamage = new ConcurrentLinkedQueue<>();

	public void process(Mob character) {
		//..If we are dead, clear all pending and current hits.
		if (character.getCombat().isDead()) {
			pendingHits.clear();
			pendingDamage.clear();
			return;
		}

		//..Process the pending hits..
		Iterator<PendingHit> iterator = pendingHits.iterator();
		while (iterator.hasNext()) {
			PendingHit hit = iterator.next();
			//..Make sure we only process the hit if it should be processed.
			//..For example - if attacker died or target is un-targetable, don't process.
			if (hit == null || hit.getTarget() == null
				|| hit.getAttacker() == null
				|| hit.getTarget().getCombat().isDead()
				|| hit.getAttacker().getCombat().isDead()) {
				iterator.remove();
				continue;
			}
			if (hit.decrementAndGetDelay() <= 0) {
				hit.getTarget().getCombat().executeHit(hit);
				iterator.remove();
			}

			//..Process damage.
			//..Make sure our hits queue isn't empty and that we aren't dead...
			if (!pendingDamage.isEmpty()) {
				//..FIRST HIT
				//..Attempt to fetch a first hit.
				HitDamage firstHit = pendingDamage.poll();

				//..Check if it's present
				if (!Objects.isNull(firstHit)) {
					hit.getTarget().damage(firstHit.getDamage(), firstHit.getDamage() > 0 ? 1 : 0, false);
				}

				//..SECOND HIT
				//..Attempt to fetch a second hit.
				HitDamage secondHit = pendingDamage.poll();

				//..Check if it's present
				if (!Objects.isNull(secondHit)) {
					hit.getTarget().damage(secondHit.getDamage(), secondHit.getDamage() > 0 ? 1 : 0, true);
				}
			}
		}
	}

	/**
	 * Add a pending hit to our queue.
	 */
	public void addPendingHit(PendingHit c) {
		pendingHits.add(c);
	}

	/**
	 * Add pending damage to our queue.
	 */
	public void addPendingDamage(HitDamage... hits) {
		Arrays.stream(hits).filter(h -> !Objects.isNull(h)).forEach(pendingDamage::add);
	}

	/***
	 * Checks if the pending hit queue is empty, except
	 * from the specified {@link Player}.
	 * Used for anti-pjing.
	 */
	public boolean isEmpty(Player exception) {
		for (PendingHit hit : pendingHits) {
			if (hit == null) {
				continue;
			}
			if (hit.getAttacker() != null) {
				if (!hit.getAttacker().equals(exception)) {
					return false;
				}
			}
		}
		return true;
	}
}