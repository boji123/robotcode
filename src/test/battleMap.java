package test;

import java.util.Enumeration;
import java.util.Hashtable;

import robocode.AdvancedRobot;
import robocode.RobotDeathEvent;
import robocode.ScannedRobotEvent;

/**
 * ��ͼ�࣬���ڱ������е����Լ����Լ���λ����Ϣ ���Ӵ�����ֻ֧��һ�����ˣ���Ҫ������Ҫ�Ľ���֧�ֶ������
 */
public class BattleMap {
	AdvancedRobot battleRule;

	BattleMap(AdvancedRobot battleRule) {
		this.battleRule = battleRule;
	}

	RobotInfo yourself = new RobotInfo();
	Hashtable<String, RobotInfo> enemyList = new Hashtable<String, RobotInfo>();

	// ��ÿ�μ�������һtick���ж�֮�󣬽��ж����ݴ������������������أ����м���nextAimInfoʱ��Ҫ����nextMoveInfo�����ۺ�
	NextMoveInfo nextMoveInfo = new NextMoveInfo();
	NextAimInfo nextAimInfo = new NextAimInfo();

	/**
	 * �������Լ��ڵ�ͼ�ϵ���Ϣ���ú�������ѭ�������Ե���
	 */
	public void setYourInfo(AdvancedRobot you) {
		yourself.setLocationX(you.getX());
		yourself.setLocationY(you.getY());
		yourself.setHeading(you.getHeading());
		yourself.setVelocity(you.getVelocity());
		yourself.setGunHeading(you.getGunHeading());
	}

	/**
	 * ���õ��˵���Ϣ��Ŀǰֻ֧��һ�����ˣ�Ӧ��дΪ֧�ֶ������
	 */
	public void setEnemyInfo(ScannedRobotEvent e) {
		RobotInfo enemy;
		if (enemyList.containsKey(e.getName())) {
			enemy = (RobotInfo) enemyList.get(e.getName());
		} else {
			enemy = new RobotInfo();
			enemyList.put(e.getName(), enemy);
		}
		enemy.setName(e.getName());
		enemy.setBearing(e.getBearing()); // ������㳵��ĳ���
		enemy.setDistance(e.getDistance());
		enemy.setHeading(e.getHeading());
		enemy.setVelocity(e.getVelocity());

		double absoluteRadius = normalizeBearing(enemy.getBearing() + yourself.getHeading()) / 180 * Math.PI;
		// �˴�Ӧ���������Ե�λ�ø��µ��˵ķ�λ;
		enemy.setLocationX(yourself.getLocationX() + enemy.getDistance() * Math.sin(absoluteRadius));
		enemy.setLocationY(yourself.getLocationY() + enemy.getDistance() * Math.cos(absoluteRadius));
	}

	/**
	 * �������������ӵ�ͼ���Ƴ�
	 */
	public void removeEnemyFromMap(RobotDeathEvent event) {
		String robotName = event.getName();
		enemyList.remove(robotName);
	}

	/**
	 * 
	 * ���ݵ�ͼ��������������һ���ڹ��˶�
	 */
	public NextAimInfo calcuNextGunBearing() {
		RobotInfo target = calcuBestTarget();
		double angle = predictAim(target);// �ڹ���Ԥ�⿪����ĽǶ�
		// �����и����⣬�����˶���Ӱ���ڹ��˶�����Ҫ����˶�����׼��Ӱ�죬����tick�Ĵ��������е��ֿڣ���������������д
		angle = normalizeBearing(angle);
		nextAimInfo.setIfCanFire(Math.abs(angle) < 1 ? true : false);// ����ǰ�ڹ��Ѿ���׼���ˣ����Ƕ�<1������һ֡�����Խ������
		nextAimInfo.setBearing(angle);
		return nextAimInfo;
	}

	/**
	 * Ԥ�⿪����Ŀǰ�Ǽ�����˲��˶������Լ��˶�
	 */
	private double predictAim(RobotInfo target) {
		double lenX = target.getLocationX() - yourself.getLocationX();
		double lenY = target.getLocationY() - yourself.getLocationY();
		double predictBearing = Math.atan(lenX / lenY) * 180 / Math.PI;
		if (lenX < 0 && lenY < 0) {
			predictBearing -= 180;
		} else if (lenX > 0 && lenY < 0) {
			predictBearing += 180;
		}
		// ���㲢�淶����׼��������û�п��ǳ�������˶�
		double nextGunTurn = normalizeBearing(predictBearing - yourself.getGunHeading());
		// ���ݳ�����˶����ڹ���׼���в���
		double nextTurn = nextMoveInfo.getBearing();
		double maxTurn = 10 - 0.75 * Math.abs(yourself.getVelocity());
		if (nextTurn > maxTurn) {
			nextTurn = maxTurn;
		} else if (nextTurn < -maxTurn) {
			nextTurn = -maxTurn;
		}
		double adjustGunTurn = nextGunTurn;
		// ������û��ҿ���ע�͵����if��һ����׼Ч��
		if (adjustGunTurn > nextTurn - 20 && adjustGunTurn < nextTurn + 20) {
			adjustGunTurn = adjustGunTurn + nextTurn;
		}
		return adjustGunTurn;
	}

	/**
	 * calcuNextGunBearingר�ã�����һ�����ʺϴ����Ŀ��
	 */
	private RobotInfo calcuBestTarget() {
		RobotInfo target = new RobotInfo();
		Enumeration<RobotInfo> enumeration = enemyList.elements();
		while (enumeration.hasMoreElements()) {
			// �˴�Ӧ��дΪѡ������Ŀ��
			target = (RobotInfo) enumeration.nextElement();
		}
		return target;
	}

	/**
	 * ���ݵ�ͼ��������������һ�������˶�
	 */
	public NextMoveInfo calcuNextMove() {
		double xforce = 0;
		double yforce = 0;
		Force force;
		RobotInfo enemy;
		Enumeration<RobotInfo> enumeration = enemyList.elements();
		// ������˵ĺ���
		while (enumeration.hasMoreElements()) {
			enemy = (RobotInfo) enumeration.nextElement();
			GravityPoint point = new GravityPoint(enemy.getLocationX(), enemy.getLocationY(), -20000);
			force = point.calcuPointForce(yourself.getLocationX(), yourself.getLocationY());
			xforce += force.xForce;
			yforce += force.yForce;
		}

		// ����ǽ��������
		GravityPoint[] pointList = new GravityPoint[5];// = new GravityPoint(0,
		pointList[0] = new GravityPoint(0, yourself.getLocationY(), -10000);
		pointList[1] = new GravityPoint(battleRule.getBattleFieldWidth(), yourself.getLocationY(), -10000);
		pointList[2] = new GravityPoint(yourself.getLocationX(), 0, -10000);
		pointList[3] = new GravityPoint(yourself.getLocationX(), battleRule.getBattleFieldHeight(), -10000);
		pointList[4] = new GravityPoint(battleRule.getBattleFieldWidth() / 2, battleRule.getBattleFieldHeight() / 2,
				-10000);
		for (int i = 0; i < 5; i++) {
			force = pointList[i].calcuPointForce(yourself.getLocationX(), yourself.getLocationY());
			xforce += force.xForce;
			yforce += force.yForce;

		}
		double forceDirection = getDirection(xforce, yforce);
		// System.out.println(forceDirection);
		// �������X��Y�ĺ������򣬵���û����Ч��ʹ��

		double angle = normalizeBearing(forceDirection - yourself.getHeading());
		nextMoveInfo.setBearing(angle);// max 10 per tick
		// max 8 per tick
		if (angle < 40)
			nextMoveInfo.setDistance(8);
		else
			nextMoveInfo.setDistance(6);
		return nextMoveInfo;
	}

	/**
	 * ����-360~360����ĽǶ�ֵ������-180��180�Ĺ淶���Ƕȣ������ڹܻ���ת��
	 */
	private static double normalizeBearing(double angle) {
		if (angle < -180)
			angle += 360;
		if (angle > 180)
			angle -= 360;
		return angle;
	}

	/**
	 * ����x�����y���룬���ط����
	 */
	private static double getDirection(double lenX, double lenY) {
		double angler;
		if (lenX == 0) {
			if (lenY > 0)
				angler = 0;
			else
				angler = -180;
		} else {
			angler = Math.atan(lenX / lenY) * 180 / Math.PI;
			if (lenX < 0 && lenY < 0) {
				angler -= 180;
			} else if (lenX > 0 && lenY < 0) {
				angler += 180;
			}
		}
		return angler;
	}
}