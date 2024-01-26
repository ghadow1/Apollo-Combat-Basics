package org.apollo.game.scheduling.impl;

import org.apollo.game.model.Animation;
import org.apollo.game.model.entity.EntityType;
import org.apollo.game.model.entity.Mob;
import org.apollo.game.model.entity.Npc;
import org.apollo.game.model.entity.Skill;
import org.apollo.game.scheduling.ScheduledTask;

/**
 * A {@link ScheduledTask} which checks the {@link Mob} for combat action or death:
 *
 * @author evergreen
 */
public final class CombatListenerTask extends ScheduledTask {

    /**
     * This tasks mob.
     */
    private final Mob mob;

    /**
     * Death animation id
     */
    final int deathAnimationId = 836;

    /**
     * Creates the combat task
     */
    public CombatListenerTask(Mob mob) {
        super(1, true);
        this.mob = mob;
    }

    @Override
    public void execute() {
        //..Is Dead ? -> Apply Death
        //..Check This Mobs Health And Apply Death
        if (mob.getSkillSet().getCurrentLevel(Skill.HITPOINTS) <= 0) {
            //..Animate the death
			mob.playAnimation(new Animation(deathAnimationId));
            //..Apply death
            mob.getCombat().applyDeath();
        } else if (!mob.getCombat().isDead()) {
            //..Pre-Sequence
            mob.getCombat().preSequence();
            //..Sequence
            mob.getCombat().sequence();
        }
    }
}