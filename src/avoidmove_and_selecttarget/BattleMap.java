package avoidmove_and_selecttarget;

import java.util.Enumeration;
import java.util.Hashtable;
import robocode.AdvancedRobot;
import robocode.RobotDeathEvent;
import robocode.ScannedRobotEvent;

/**
 * ��ͼ�࣬���ڱ������е����Լ����Լ���λ����Ϣ ���Ӵ�����ֻ֧��һ�����ˣ���Ҫ������Ҫ�Ľ���֧�ֶ������
 */
public class BattleMap {
	AdvancedRobot battle;// battle�������Լ���ʵʱ��Ϣ�Լ�ս����Ϣ

	BattleMap(AdvancedRobot battle) {
		this.battle = battle;
	}

	Hashtable<String, RobotInfo> enemyList = new Hashtable<String, RobotInfo>();

	// ��ÿ�μ�������һtick���ж�֮�󣬽��ж����ݴ������������������أ����м���nextAimInfoʱ��Ҫ����nextMoveInfo�����ۺ�
	NextMoveInfo nextMoveInfo = new NextMoveInfo();
	NextAimInfo nextAimInfo = new NextAimInfo();

	/**
	 * ���õ��˵���Ϣ��ע�⣡ɨ�������ʱЧ�ԣ�
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
		enemy.setDistance(e.getDistance());// ɨ��ɨ����������룬��Ҫ������ϻ����˵Ĵ�С
		enemy.setHeading(e.getHeading());
		enemy.setVelocity(e.getVelocity());

		double absoluteRadius = Math.toRadians(normalizeAngle(enemy.getBearing() + battle.getHeading()));
		// �˴�Ӧ���������λ�ø��µ��˵ķ�λ;
		enemy.setLocationX(battle.getX() + enemy.getDistance() * Math.sin(absoluteRadius));
		enemy.setLocationY(battle.getY() + enemy.getDistance() * Math.cos(absoluteRadius));
		// System.out.println(enemy.getLocationX());
		// System.out.println(enemy.getLocationY());
	}

	/**
	 * �������������ӵ�ͼ���Ƴ�
	 */
	public void removeEnemyFromMap(RobotDeathEvent event) {
		String robotName = event.getName();
		enemyList.remove(robotName);
	}

	/**
	 * ���ݵ�ͼ��������������һ���ڹ��˶�
	 */
	public NextAimInfo calcuNextGunBearing() {
		RobotInfo target = calcuBestTarget();
		// System.out.println("target:" + target.getName());

		// ------------------------------ȷ����һ֡�ڹ���׼�ķ���---------------------------------------
		double nextGunTurn = predictAim(target);// �ڹ���Ԥ�⿪����ĽǶȣ���Ҫ���ǳ�����˶���������һ֡���ܶ�׼����
		nextAimInfo.setBearing(nextGunTurn);// ����У׼����һ֡����׼ȷ��׼����

		// --------------------------------ȷ����ǰ֡�Ƿ񿪻�-----------------------------------------
		// ��ǰʱ�̵����������ľ��Է���
		double lenX = target.getLocationX() - battle.getX();
		double lenY = target.getLocationY() - battle.getY();
		double enemyBearding = getAngle(lenY, lenX);
		// ��ǰʱ���ڹ������λ�õ�����λ���أ�
		double angleErrorRange = Math.abs(normalizeAngle(enemyBearding - battle.getGunHeading()));
		// ����ǰ�ڹ��Ѿ���׼���ˣ���������㹻С����һ֡�������������36���أ�һ�����18���أ��������18���ڣ����й̶��У�
		if (Math.sin(Math.toRadians(angleErrorRange)) * target.getDistance() < 1) {
			nextAimInfo.setIfCanFire(true);
		} else {
			nextAimInfo.setIfCanFire(false);
		}
		return nextAimInfo;
	}

	/**
	 * Ԥ�⿪����Ŀǰ�Ǽ�����˲��˶������Լ��˶�
	 */
	private double predictAim(RobotInfo target) {
		// ��һ֡�����˶�λ��
		double diffX = Math.sin(Math.toRadians(battle.getHeading())) * battle.getVelocity();
		double diffY = Math.cos(Math.toRadians(battle.getHeading())) * battle.getVelocity();
		// ����̹�˵ĽǶȣ���Ҫ���ݳ�����˶����ڹ���׼���в������������ڳ��ӵ��˶������ȷ�������һ֡����׼����
		double lenX = target.getLocationX() - (battle.getX() + diffX);
		double lenY = target.getLocationY() - (battle.getY() + diffY);

		double enemyBearding = getAngle(lenY, lenX);
		// ���㲢�淶����׼���򣬴���180�ȹ淶������ʱ��ת��ȷ����׼ʱ�����
		double nextGunTurn = normalizeAngle(enemyBearding - battle.getGunHeading());
		return nextGunTurn;
	}

	/**
	 * calcuNextGunBearingר�ã�����һ�����ʺϴ����Ŀ��
	 */
	private RobotInfo calcuBestTarget() {
		RobotInfo target;
		Enumeration<RobotInfo> enumeration = enemyList.elements();
		double lost = 2000000000;
		RobotInfo best = new RobotInfo();
		while (enumeration.hasMoreElements()) {
			// �˴�Ӧ��дΪѡ������Ŀ��
			target = (RobotInfo) enumeration.nextElement();
			if (target.getDistance() * Math.abs(target.getVelocity()) < lost) {
				best = target;
				lost = target.getDistance() * Math.abs(target.getVelocity());
			}
		}
		return best;
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
			force = point.calcuPointForce(battle.getX(), battle.getY());
			xforce += force.xForce;
			yforce += force.yForce;
		}

		// ����ǽ��������
		GravityPoint[] pointList = new GravityPoint[5];
		pointList[0] = new GravityPoint(0, battle.getY(), -10000);
		pointList[1] = new GravityPoint(battle.getBattleFieldWidth(), battle.getY(), -10000);
		pointList[2] = new GravityPoint(battle.getX(), 0, -10000);
		pointList[3] = new GravityPoint(battle.getX(), battle.getBattleFieldHeight(), -10000);
		pointList[4] = new GravityPoint(battle.getBattleFieldWidth() / 2, battle.getBattleFieldHeight() / 2,
				-Math.random() * 20000);// �е����0��20000
		for (int i = 0; i < 5; i++) {
			force = pointList[i].calcuPointForce(battle.getX(), battle.getY());
			xforce += force.xForce;
			yforce += force.yForce;
		}

		double forceDirection = normalizeAngle(getAngle(yforce, xforce));
		// System.out.println("y" + yforce);
		// System.out.println("x" + xforce);
		// �������X��Y�ĺ�������ע������y������Ϊ0
		// û����Ч��ʹ��

		// angle���㳵��Ӧ����ת�ĽǶȣ�����normalize������㳵��Ӧ���˶��ķ��򼰽Ƕ�
		double angle = normalizeAngle(forceDirection - battle.getHeading());
		double power = Math.sqrt(xforce * xforce + yforce * yforce);
		if (power > 4 && Math.abs(angle) > 90) {
			nextMoveInfo.setDistance(4);
			nextMoveInfo.setBearing(angle);
		} else if (power > 2 && Math.abs(angle) > 70) {
			nextMoveInfo.setDistance(8);
			nextMoveInfo.setBearing(angle);
		} else if (power > 1 && Math.abs(angle) > 50) {
			nextMoveInfo.setDistance(12);
			nextMoveInfo.setBearing(angle);
		} else if (power > 0.5 && Math.abs(angle) > 30) {
			nextMoveInfo.setDistance(16);
			nextMoveInfo.setBearing(angle);
		} else {
			nextMoveInfo.setDistance(20);
			nextMoveInfo.setBearing(angle);
		}
		return nextMoveInfo;
	}

	/**
	 * ����-360~359����ĽǶ�ֵ������-180��179�Ĺ淶���Ƕȣ������ڹܻ���ת��
	 */
	private static double normalizeAngle(double angle) {
		if (angle < -180)
			angle += 360;
		if (angle >= 180)
			angle -= 360;
		return angle;
	}

	/**
	 * ����x�����y���룬���ط���ǣ�x������Ϊ0����-180~179��
	 */
	private static double getAngle(double lenX, double lenY) {
		double angler = Math.atan(lenY / lenX) * 180 / Math.PI;
		// �������
		if (lenX == 0 && lenY > 0)
			return 90;
		if (lenX == 0 && lenY < 0)
			return -90;
		if (lenX > 0 && lenY == 0)
			return 0;
		if (lenX < 0 && lenY == 0)
			return -180;
		if (lenX == 0 && lenY == 0)// ���������x=y=0
			return 0;
		// һ�����
		if (lenX < 0 && lenY > 0)// �ڶ�����
			return angler + 180;
		if (lenX < 0 && lenY < 0)// ��������
			return angler - 180;
		return angler;
	}
}