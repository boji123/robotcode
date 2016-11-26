package budabuxiangshi3V3;

//���ڻ�սû�ж��飬Ϊ�˷�ֹ�Ѿ��˺�ǿ���Լ�ʵ���˸����ƶ������
//��Ҫ���İ����ᵼ���Ҳ����Լ��Ķ��ѣ�
public class Cooperate {
	budabuxiangshi battle;
	BattleMap battleMap;

	String[] teammates = { "budabuxiangshi3V3.budabuxiangshi* (1)", "budabuxiangshi3V3.budabuxiangshi* (2)",
			"budabuxiangshi3V3.budabuxiangshi* (3)" };// �����Լ�
	int teammateRest = 2;

	Force teamForce = new Force();// 0����1����2����3����
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

	public void divideCornerForTeam() {// ������ģʽ��3V3������ʱǿ�а��������ѷֵ��������䣬�ô����󣬵��ǿ��Էֿ����Ѽ��ٿ���ʱ���Ѿ��˺�
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
		// ע������Ƕ���X������Ϊ0

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

	public boolean ifReachPlace() {// �����õ���ʱ���ƿ������˶�
		if (reachCount == 0) {
			return true;
		} else {
			reachCount--;
			return false;
		}
	}

}
