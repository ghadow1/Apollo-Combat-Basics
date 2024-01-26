package org.apollo.game.model.entity.combat;

import org.apollo.game.model.entity.Skill;

/**
 * A collection of constants that each represent a different fighting style.
 *
 * @author lare96
 */
public enum FightStyle {
	ACCURATE() {
		@Override
		public int[] skill(CombatType type) {
			return type == CombatType.RANGED ? new int[]{Skill.RANGED} : new int[]{Skill.ATTACK};
		}
	}, AGGRESSIVE() {
		@Override
		public int[] skill(CombatType type) {
			return type == CombatType.RANGED ? new int[]{Skill.RANGED} : new int[]{Skill.STRENGTH};
		}
	}, DEFENSIVE() {
		@Override
		public int[] skill(CombatType type) {
			return type == CombatType.RANGED ? new int[]{Skill.RANGED, Skill.DEFENCE} : new int[]{Skill.DEFENCE};
		}
	}, CONTROLLED() {
		@Override
		public int[] skill(CombatType type) {
			return new int[]{Skill.ATTACK, Skill.STRENGTH, Skill.DEFENCE};
		}
	};

	/**
	 * Determines the Skill trained by this fighting style based on the
	 * {@link CombatType}.
	 *
	 * @param type the combat type to determine the Skill trained with.
	 * @return the Skill trained by this fighting style.
	 */
	public abstract int[] skill(CombatType type);
}