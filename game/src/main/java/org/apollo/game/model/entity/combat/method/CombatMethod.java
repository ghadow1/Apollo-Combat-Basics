package org.apollo.game.model.entity.combat.method;

import org.apollo.game.model.entity.Mob;
import org.apollo.game.model.entity.combat.CombatType;
import org.apollo.game.model.entity.combat.hit.PendingHit;

public interface CombatMethod {

    boolean canAttack(Mob character, Mob target);

    void preQueueAdd(Mob character, Mob target);

    int getAttackSpeed(Mob character);

    int getAttackDistance(Mob character);

    void startAnimation(Mob character);

    CombatType getCombatType();

    PendingHit[] getHits(Mob character, Mob target);

    void finished(Mob character);

    void handleAfterHitEffects(PendingHit hit);

}
