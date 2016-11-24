package TeamRemake;

public class Cooperate {
	Remake battle;
	BattleMap battleMap;
	static int teammates = 2;

	int teammatesRest = 0;
	boolean isLeader;
	Force cornerForce = new Force();// 0����1����2����3����

	public void init(Remake battle, BattleMap battleMap) {
		this.battle = battle;
		this.battleMap = battleMap;
		if (battle.getEnergy() > 150)
			isLeader = true;
		else
			isLeader = false;

		if (battle.getTeammates() == null)
			teammatesRest = 0;
		else
			teammatesRest = battle.getTeammates().length;
	}

	public int getEnemyRest() {
		return battle.getOthers() - teammatesRest;
	}

	public void divideCornerForTeam() {// ��������������ѵĲ���
		if (teammates == 2 && isLeader) {
			RobotInfo teammate1 = battleMap.getRobot(battle.getTeammates()[0]);
			RobotInfo teammate2 = battleMap.getRobot(battle.getTeammates()[1]);

			cornerForce.xForce = 0;
			cornerForce.yForce = 0;
		}
	}

	public boolean ifReachPlace() {
		return false;
	}
}
