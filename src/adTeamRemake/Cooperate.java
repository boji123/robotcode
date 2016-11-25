package adTeamRemake;

public class Cooperate {
	Remake battle;
	BattleMap battleMap;
	static int teammates = 2;
	int teammateRest = 2;
	Force teamForce = new Force();// 0左下1左上2右上3右下
	RobotInfo teammate1 = new RobotInfo(), teammate2 = new RobotInfo();

	int reachCount = 50;
	Force cornerForce = new Force();

	public void init(Remake battle, BattleMap battleMap) {
		this.battle = battle;
		this.battleMap = battleMap;
	}

	public int getEnemyRest() {
		return battle.getOthers() - teammateRest;
	}

	public void divideCornerForTeam() {// 仅仅针对两个队友的策略
		int i = 0;

		for (; i < 3; i++) {
			if (battle.getTeammates()[i].compareTo(battle.getName()) != 0) {
				teammate1 = battleMap.getRobot(battle.getTeammates()[i]);
				break;
			}
		}
		for (i = i + 1; i < 3; i++) {
			if (battle.getTeammates()[i].compareTo(battle.getName()) != 0) {
				teammate2 = battleMap.getRobot(battle.getTeammates()[i]);
				break;
			}
		}
		double teammateBearing1 = BattleMap.getAngle(battle.getX() - teammate1.getX(),
				battle.getY() - teammate1.getY());
		double teammateBearing2 = BattleMap.getAngle(battle.getX() - teammate2.getX(),
				battle.getY() - teammate2.getY());
		double average = (teammateBearing1 + teammateBearing2) / 2;
		System.out.println("bearing:" + average);
		// 注意这里角度是X正方向为0

		double corner;
		if (average >= 0 && average < 90)
			corner = 3;
		else if (average >= 90 && average < 180)
			corner = 2;
		else if (average >= -180 && average < -90)
			corner = 1;
		else
			corner = 4;

		if (corner == 1 || corner == 2)
			cornerForce.xForce = -5;
		else
			cornerForce.xForce = 5;
		if (corner == 2 || corner == 3)
			cornerForce.yForce = 5;
		else
			cornerForce.yForce = -5;

		System.out.println(corner);

	}

	public void calcuTeamForce() {// 该函数不好用
		double teammateBearing1 = BattleMap.getAngle(battle.getX() - teammate1.getX(),
				battle.getY() - teammate1.getY());
		double teammateBearing2 = BattleMap.getAngle(battle.getX() - teammate2.getX(),
				battle.getY() - teammate2.getY());
		double average = (teammateBearing1 + teammateBearing2) / 2;
		System.out.println("bearing:" + average);
		// 注意这里角度是X正方向为0
		teamForce.xForce += 2 * Math.cos(average);
		teamForce.yForce += 2 * Math.sin(average);
		// return teamForce;
	}

	public boolean ifReachPlace() {
		if (reachCount == 0) {
			return true;
		} else {
			reachCount--;
			return false;
		}
	}

}
