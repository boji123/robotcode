package TeamRemake;

public class Cooperate {
	Remake battle;
	BattleMap battleMap;
	static int teammates = 2;

	Force cornerForce = new Force();// 0����1����2����3����

	public void init(Remake battle, BattleMap battleMap) {
		this.battle = battle;
		this.battleMap = battleMap;
	}

	public void divideCornerForTeam() {// ��������������ѵĲ���
		RobotInfo teammate1 = battleMap.getRobot(battle.getTeammates()[0]);
		RobotInfo teammate2 = battleMap.getRobot(battle.getTeammates()[1]);

		cornerForce.xForce = 0;
		cornerForce.yForce = 0;
	}

	public boolean ifReachPlace() {
		return true;
	}
}
