package org.apollo.game.model.entity.combat;

import org.apollo.game.model.entity.EntityType;
import org.apollo.game.model.entity.Mob;
import org.apollo.game.model.entity.Player;
import org.apollo.game.model.entity.Skill;
import org.apollo.game.model.entity.combat.hit.HitDamage;
import org.apollo.game.model.entity.combat.hit.HitDamageCache;
import org.apollo.game.model.entity.combat.hit.HitQueue;
import org.apollo.game.model.entity.combat.hit.PendingHit;
import org.apollo.game.model.entity.combat.method.CombatMethod;
import org.apollo.game.model.entity.combat.method.MeleeCombatMethod;

import java.util.HashMap;
import java.util.Map;

public class Combat {

	/**
	 * The default melee combat method.
	 */
	public static final MeleeCombatMethod MELEE_COMBAT = new MeleeCombatMethod();

	/**
	 * Combat class constructor
	 */
	public Combat(Mob mob) {
		this.mob = mob;
		this.hitQueue = new HitQueue();
	}

	//..The mob of this combat class
	private final Mob mob;
	//..The mob's current target
	private Mob target;
	//..The last person who attacked the character this instance belongs to.
	private Mob attacker;
	//..The mobs death state
	private boolean dead;
	//..The mobs attack timer
	private int attackTimer;
	//..The timer of the last attack which occurred
	private final Stopwatch lastAttack = new Stopwatch();
	//..The last combat method used
	private CombatMethod method;
	//..Fight type
	private FightType fightType = FightType.UNARMED_KICK;

	//..The user's damage map
	private final Map<Player, HitDamageCache> damageMap = new HashMap<>();
	//..The user's HitQueue
	private final HitQueue hitQueue;


	/**
	 * This {@link Player}
	 */
	public Mob getMob() {
		return mob;
	}

	/**
	 * Get the attack timer till the next swing
	 */
	public int getAttackTimer() {
		return attackTimer;
	}

	/**
	 * Set {@link Player} current attack timer delay
	 */
	public void setAttackTimer(int attackTimer) {
		this.attackTimer = attackTimer;
	}

	/**
	 * This {@link Player} currently assigned attack method
	 */
	public CombatMethod getMethod() {
		return MELEE_COMBAT;
	}

	/**
	 * Set current attack method data
	 */
	public void setCurrentMethod(CombatMethod method) {
		this.method = method;
	}

	/**
	 * This {@link Player} currently assigned fight type
	 */
	public FightType getFightType() {
		return fightType;
	}

	/**
	 * This {@link Player} hit queue
	 */
	public HitQueue getHitQueue() {
		return hitQueue;
	}


	/**
	 * This {@link Player} current assigned target
	 */
	public Mob getTarget() {
		return target;
	}

	/**
	 * Set {@link Player} current target to attack
	 */
	public void setTarget(Mob target) {
		this.target = target;
	}

	/**
	 * Set target for the {@link Player}.
	 */
	public void initiate(Mob target) {
		setTarget(target);
	}

	/**
	 * Set this {@link Player} under attack by another mob
	 */
	void setUnderAttack(Mob attacker) {
		this.attacker = attacker;
		this.lastAttack.reset();
	}

	/**
	 * Reset target for the {@link Player}.
	 */
	public void reset() {
		if (target != null) {
			mob.resetInteractingMob();
			target = null;
		}
	}

	/**
	 * Set {@link Player} as dead (unable to attack)
	 */
	public void applyDeath() {
		setDead(true);
		if (mob != null) {
			reset();
		}
	}

	/**
	 * Set this {@link Player} as dead
	 */
	public void setDead(boolean dead) {
		this.dead = dead;
	}

	/**
	 * Is this mob dead
	 */
	public boolean isDead() {
		return dead;
	}

	/**
	 * (pre) Combat sequence
	 */
	public void preSequence() {
		//..checkGraniteMaul();
		if (target != null) {
			//..Face attacker towards target
			mob.turnTo(target.getPosition());
			//..If mob is a player
			if (mob instanceof Player) {
				//..Close all open interfaces
				((Player) mob).getInterfaceSet().close();
			}
		}
	}

	/**
	 * Combat sequence
	 */
	public void sequence() {
		//..Process the hit queue
		hitQueue.process(mob);

		//..Decrease attack timer
		if (getAttackTimer() > 0) {
			attackTimer--;
		}

		//..If a target exists, attack.
		if (target != null) {
			attack();
		}

		//..Reset attacker if we haven't been attacked in 6 seconds.
		if (lastAttack.elapsed(6000)) {
			setUnderAttack(null);
		}
	}

	/**
	 * Attack a target if one is present otherwise nothing
	 */
	void attack() {

		//..TODO @ Fetch the combat method the character will be attacking with
		method = MELEE_COMBAT;
		//..Face Target
		mob.setInteractingMob(getTarget());
		//..Distance Check
		if (validatePosition(method, target)) {
			//..Attack Timer Check
			if (getAttackTimer() <= 0) {
				//..Weapon Method Check
				if (method.canAttack(mob, target)) {
					//..Weapon Animation
					method.startAnimation(mob);
					//..Create a new {PendingHit}[] using the player's combat method (melee/range/magic)
					PendingHit[] hits = getMethod().getHits(mob, target);
					if (hits == null) return;
					//..Perform the abstract method "preQueueAdd" before adding the hit for the target
					method.preQueueAdd(mob, target);
					//..Put all the {PendingHit} in the target's HitQueue
					//..And also do other things, such as reward attacker experience
					//..If they're a player.
					for (PendingHit hit : hits) {
						//..Add the hit to the que
						addPendingHit(hit);
					}
					//..Perform final actions
					method.finished(mob);
					//..Reset attack
					setAttackTimer(method.getAttackSpeed(mob));
				}
			}
		}
	}

	/**
	 * Checks if an entity can reach a target.
	 *
	 * @param method The combat type the attacker is using.
	 * @param target The victim.
	 * @return True if attacker has the proper distance to attack, otherwise false.
	 */
	public boolean validatePosition(CombatMethod method, Mob target) {
		//..First check if target is valid
		if (!validateTarget(target)) {
			return false;
		}

		//..Attack distance for this attack method
		int distance = method.getAttackDistance(mob);

		//..Check target size for melee attacks
		if (mob.getEntityType().equals(EntityType.PLAYER) && method.getCombatType() == CombatType.MELEE) {
			if (target.size() >= 2) {
				distance += target.size() - 1;
			}
		}

		//..If we are in distance or not
		return mob.getPosition().getDistance(target.getPosition()) <= distance;
	}

	/**
	 * Checks if an entity is a valid target.
	 *
	 * @return if the target is attack-able
	 */
	public boolean validateTarget(Mob target) {
		//..Check this mob
		if (!mob.isActive() || isDead()) {
			reset();
			return false;
		}

		//..Check target
		if (!target.isActive() || target.getCombat().isDead()) {
			reset();
			return false;
		}

		//..Check Teleporting Away
		if (mob.getPosition().getDistance(target.getPosition()) >= 40) {
			reset();
			return false;
		}
		return true;
	}

	/**
	 * Adds damage to the damage map, as long as the argued amount of damage is
	 * above 0 and the argued entity is a player.
	 *
	 * @param entity the entity to add damage for.
	 * @param amount the amount of damage to add for the argued entity.
	 */
	public void addDamageToMap(Player entity, int amount) {
		if (amount <= 0 || entity.getEntityType().equals(EntityType.NPC)) {
			return;
		}
		if (damageMap.containsKey(entity)) {
			damageMap.get(entity).incrementDamage(amount);
			return;
		}
		damageMap.put(entity, new HitDamageCache(amount));
	}

	/**
	 * Adds a hit to a target's queue.
	 */
	public void addPendingHit(PendingHit qHit) {
		Mob attacker = qHit.getAttacker();
		Mob target = qHit.getTarget();
		HitDamage[] damage = qHit.getHits();
		//..If target is dead or damage is null return
		if (damage == null || target.getCombat().isDead()) {
			return;
		}

		//..Check if the player should be skulled for making this attack
		if (attacker.getEntityType().equals(EntityType.PLAYER)) {
			if (target.getEntityType().equals(EntityType.PLAYER)) {
				((Player) attacker).setSkulled(true);
				//..handleSkull(attacker.getAsPlayer(), target.getAsPlayer());
			}
		}

		//..Add this hit to the target's hitQueue
		target.getCombat().getHitQueue().addPendingHit(qHit);
	}

	/**
	 * Executes a hit that has been ticking until now.
	 *
	 * @param qHit The QueueableHit to execute.
	 */
	public void executeHit(PendingHit qHit) {
		//..Hit data
		final Mob target = qHit.getTarget();
		final Mob attacker = qHit.getAttacker();
		final int damage = qHit.getTotalDamage();
		final CombatMethod qHitMethod = qHit.getCombatMethod();
		//..Remaining health
		int health = target.getSkillSet().getCurrentLevel(Skill.HITPOINTS);

		//..If target/attacker is dead, don't continue.
		if (target.getCombat().isDead() || attacker.getCombat().isDead()) {
			return;
		}

		//..Here, we take the damage.
		//..BUT, don't take damage if the attack was a magic splash by a player.
		target.getCombat().getHitQueue().addPendingDamage(qHit.getHits());

		//..Make sure to let the combat method know we finished the attack
		//..Only if this isn't custom hit (handleAfterHitEffects() will be false then)
		if (qHit.handleAfterHitEffects()) {
			if (qHitMethod != null) {
				qHitMethod.handleAfterHitEffects(qHit);
			}
		}

		//..Set target under attack
		target.getCombat().setUnderAttack(attacker);

		//..Add damage to target damage map
		int override_hit = 0;
		if (damage > health) {
			override_hit = health;
		}
		//..Finally, add the damage
		target.getCombat().addDamageToMap((Player) attacker, override_hit > 0 ? override_hit : damage);
	}
}
