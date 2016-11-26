package budabuxiangshi3V3;

//由于混战没有队伍，为了防止友军伤害强行自己实现了个类似队伍的类
//重要！改包名会导致找不到自己的队友！
public class Cooperate {
	budabuxiangshi battle;
	BattleMap battleMap;

	String[] teammates = { "budabuxiangshi3V3.budabuxiangshi* (1)", "budabuxiangshi3V3.budabuxiangshi* (2)",
			"budabuxiangshi3V3.budabuxiangshi* (3)" };// 包括自己
	int teammateRest = 2;

	Force teamForce = new Force();// 0左下1左上2右上3右下
	RobotInfo teammate1 = new RobotInfo(), teammate2 = new RobotInfo();
	int reachCount = 50;
	Force cornerForce = new Force();

	public void init(budabuxiangshi battle, BattleMap battleMap) {
		this.battle = battle;
		this.battleMap = battleMap;
	}

	public boolean isTeammate(String name) {
		if (teammates == null)
			return false;
		for (int i = 0; i < teammates.length; i++)
			if (name.compareTo(teammates[i]) == 0)
				return true;
		return false;
	}

	public String[] getTeammates() {
		return teammates;
	}

	public int getEnemyRest() {
		return battle.getOthers() - teammateRest;
	}

	public void divideCornerForTeam() {// 当合作模式（3V3）开场时强行把三个队友分到三个角落，用处不大，但是可以分开队友减少开场时的友军伤害
		int i = 0;
		for (; i < 3; i++) {
			if (getTeammates()[i].compareTo(battle.getName()) != 0) {
				teammate1 = battleMap.getRobot(getTeammates()[i]);
				break;
			}
		}
		for (i = i + 1; i < 3; i++) {
			if (getTeammates()[i].compareTo(battle.getName()) != 0) {
				teammate2 = battleMap.getRobot(getTeammates()[i]);
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

	public boolean ifReachPlace() {// 这里用倒计时控制开场的运动
		if (reachCount == 0) {
			return true;
		} else {
			reachCount--;
			return false;
		}
	}

}
