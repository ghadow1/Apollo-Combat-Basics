package org.apollo.game.model.entity.combat.method;

import org.apollo.game.model.Animation;
import org.apollo.game.model.entity.Mob;
import org.apollo.game.model.entity.combat.CombatType;
import org.apollo.game.model.entity.combat.hit.PendingHit;

/**
 * The melee combat method.
 *
 * @author Gabriel Hannason
 */
public class MeleeCombatMethod implements CombatMethod {

	@Override
	public CombatType getCombatType() {
		return CombatType.MELEE;
	}

	@Override
	public PendingHit[] getHits(Mob in, Mob target) {
		//..Create the pending hit
		PendingHit hit = new PendingHit(in, target, this, true, 0);
		//..Send the hit
		return new PendingHit[]{hit};
	}

	@Override
	public boolean canAttack(Mob in, Mob target) {
		return true;
	}

	@Override
	public void preQueueAdd(Mob in, Mob target) {}

	@Override
	public int getAttackSpeed(Mob in) {
		return 3;
	}

	@Override
	public int getAttackDistance(Mob in) {
		return 1;
	}

	@Override
	public void startAnimation(Mob in) {
		//..Create the animation
		Animation punch = new Animation(422);
		//..Send it to player
		in.playAnimation(punch);
	}

	@Override
	public void finished(Mob in) {}

	@Override
	public void handleAfterHitEffects(PendingHit hit) {}
}
