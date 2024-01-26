package org.apollo.game.model.entity.combat.hit;

import org.apollo.game.model.entity.EntityType;
import org.apollo.game.model.entity.Mob;
import org.apollo.game.model.entity.Skill;
import org.apollo.game.model.entity.combat.method.CombatMethod;

/**
 * Represents a pending hit.
 *
 * @author Professor Oak
 */
public class PendingHit {

	/**
	 * The attacker instance.
	 */
	private final Mob attacker;

	/**
	 * The victim instance.
	 */
	private final Mob target;

	/**
	 * The combat method that was used in this hit
	 **/
	private final CombatMethod method;

	/**
	 * The damage which will be dealt
	 **/
	private final HitDamage[] hits;
	/**
	 * Check accuracy of the hit?
	 **/
	private final boolean checkAccuracy;
	/**
	 * The total damage this hit will deal
	 **/
	private int totalDamage;
	/**
	 * The delay of this hit
	 **/
	private int delay;
	/**
	 * Was the hit accurate?
	 **/
	private boolean accurate;

	/**
	 * Should processedHit be executed after processing this hit?
	 **/
	private boolean handleAfterHitEffects;

	/**
	 * Constructs a QueueableHit with a total of 1 hit.
	 **/
	public PendingHit(Mob attacker, Mob target, CombatMethod method, boolean checkAccuracy, int delay) {
		this.attacker = attacker;
		this.target = target;
		this.method = method;
		this.checkAccuracy = checkAccuracy;
		this.hits = prepareHits(1);
		this.delay = delay;
		this.handleAfterHitEffects = true;
	}

	private HitDamage[] prepareHits(int hitAmount) {
		// Check the hit amounts.
		if (hitAmount > 4) {
			throw new IllegalArgumentException("Illegal number of hits! The maximum number of hits per turn is 4.");
		} else if (hitAmount < 0) {
			throw new IllegalArgumentException("Illegal number of hits! The minimum number of hits per turn is 0.");
		}

		if (attacker == null || target == null) {
			return null;
		}

		HitDamage[] hits = new HitDamage[hitAmount];

		for (int i = 0; i < hits.length; i++) {
			//..Create the hit damage
			HitDamage damage = new HitDamage(3, HitMask.RED);
			//..Update total damage map
			totalDamage += damage.getDamage(); //The total damage this QueueableHit will deal, for calculating amount of experience to give the attacker.
			//..Set hit accuracy
			accurate = damage.getDamage() != 0;
			//..Hit is final -> set it..
			hits[i] = damage;
		}
		return hits;
	}

	public void updateTotalDamage() {
		totalDamage = 0;
		for (HitDamage hit : hits) {
			totalDamage += hit.getDamage();
		}
	}

	public int[] getSkills() {
		if (attacker.getEntityType().equals(EntityType.NPC)) {
			return new int[]{};
		}
		return new int[]{Skill.ATTACK};
	}


	public Mob getAttacker() {
		return attacker;
	}

	public Mob getTarget() {
		return target;
	}

	public CombatMethod getCombatMethod() {
		return method;
	}

	public HitDamage[] getHits() {
		return hits;
	}

	public int decrementAndGetDelay() {
		return delay--;
	}

	public int getTotalDamage() {
		return totalDamage;
	}

	public boolean isAccurate() {
		return accurate;
	}

	public PendingHit setHandleAfterHitEffects(boolean handleAfterHitEffects) {
		this.handleAfterHitEffects = handleAfterHitEffects;
		return this;
	}

	public boolean handleAfterHitEffects() {
		return handleAfterHitEffects;
	}
}
