package adPredict3;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.print.attribute.standard.Media;

import robocode.AdvancedRobot;
import robocode.RobotDeathEvent;
import robocode.Rules;
import robocode.ScannedRobotEvent;

/**
 * ��ͼ�࣬���ڱ������е����Լ����Լ���λ����Ϣ
 */
public class BattleMap {
	AdvancedRobot battle;// battle�������Լ���ʵʱ��Ϣ�Լ�ս����Ϣ
	boolean ifAiming = false;
	RobotInfo aimingTarget = new RobotInfo();
	// pattern match
	private static final int MAX_PATTERN_LENGTH = 30;
	private static Map<String, int[]> matcher = new HashMap<String, int[]>(40000);
	private static String enemyHistory;
	// predict
	private static double FIRE_POWER = 3;
	private static double FIRE_SPEED = Rules.getBulletSpeed(FIRE_POWER);
	private static List<Point2D.Double> predictions = new ArrayList<Point2D.Double>();

	BattleMap(AdvancedRobot battle) {
		this.battle = battle;
	}

	Hashtable<String, RobotInfo> enemyList = new Hashtable<String, RobotInfo>();

	// ��ÿ�μ�������һtick���ж�֮�󣬽��ж����ݴ������������������أ����м���nextAimInfoʱ��Ҫ����nextMoveInfo�����ۺ�
	NextMoveInfo nextMoveInfo = new NextMoveInfo();
	NextAimInfo nextAimInfo = new NextAimInfo();

	/**
	 * ���õ��˵���Ϣ��ע�⣡ɨ�������ʱЧ�ԣ���ʣ��һ������ʱ��Ӧ�ø��٣�
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
		enemy.preScanTime = enemy.getScanTime();
		enemy.setScanTime(e.getTime());
		enemy.setEnergy(e.getEnergy());

		double absoluteRadius = Math.toRadians(normalizeAngle(enemy.getBearing() + battle.getHeading()));
		// �˴�Ӧ���������λ�ø��µ��˵ķ�λ;
		double newX = battle.getX() + enemy.getDistance() * Math.sin(absoluteRadius);
		double newY = battle.getY() + enemy.getDistance() * Math.cos(absoluteRadius);
		double moveDist = Math.sqrt((newX - enemy.getLocationX()) * (newX - enemy.getLocationX())
				+ (newY - enemy.getLocationY()) * (newY - enemy.getLocationY()));
		enemy.averageVelocity = Math.signum(enemy.getVelocity()) * moveDist / (enemy.getScanTime() - enemy.preScanTime);
		enemy.setLocationX(newX);
		enemy.setLocationY(newY);
		// System.out.println(enemy.getLocationX());
		// System.out.println(enemy.averageVelocity);
		if (enemy.thisStep == (char) -1) {
			return;
		}
		record(enemy.thisStep);
		enemyHistory = (char) enemy.thisStep + enemyHistory;
		
	}

	/**
	 * �������������ӵ�ͼ���Ƴ�
	 */
	public void removeEnemyFromMap(RobotDeathEvent event) {
		String robotName = event.getName();
		enemyList.remove(robotName);
	}

	/**
	 * ������ʣ��һ���������״�׷��ģʽ
	 */
	public double trackCurrent(ScannedRobotEvent e) {
		double RadarOffset;
		double absoluteBearing = normalizeAngle(e.getBearing() + battle.getHeading());
		System.out.println(absoluteBearing);
		RadarOffset = normalizeAngle(absoluteBearing - battle.getRadarHeading());
		return RadarOffset;
	}

	/**
	 * ���ݵ�ͼ��������������һ���ڹ��˶�
	 */
	public NextAimInfo calcuNextGunBearing() {
		if (ifAiming == false || aimingTarget.getName() == null) {
			ifAiming = true;
			aimingTarget = calcuBestTarget();// Ŀ��ѡ������Ҫ��Ԥ�⺯�����ʹ�ã�����Ч�����
			// System.out.println("select:" + aimingTarget.getName());
		}
		// ------------------------------ȷ����һ֡�ڹ���׼�ķ���---------------------------------------
		double nextGunTurn = predictAim(aimingTarget);// �ڹ���Ԥ�⿪����ĽǶȣ���Ҫ���ǳ�����˶���������һ֡���ܶ�׼����
		nextAimInfo.setBearing(nextGunTurn);// ����У׼����һ֡����׼ȷ��׼����

		// --------------------------------ȷ����ǰ֡�Ƿ񿪻�-----------------------------------------
		// ��ǰʱ�̵����������ľ��Է���
		double lenX = aimingTarget.predictX - battle.getX();
		double lenY = aimingTarget.predictY - battle.getY();
		double enemyBearding = getAngle(lenY, lenX);
		// ��ǰʱ���ڹ������λ�õ�����λ���أ�
		double angleErrorRange = Math.abs(normalizeAngle(enemyBearding - battle.getGunHeading()));
		// ����ǰ�ڹ��Ѿ���׼���ˣ���������㹻С����һ֡�������������36���أ�һ�����18���أ��������18���ڣ����й̶��У�
		if (Math.sin(Math.toRadians(angleErrorRange)) * aimingTarget.getDistance() < 10 && battle.getGunHeat() == 0) {
			if (aimingTarget.predictDistance < 600 || aimingTarget.getEnergy() == 0) {
				nextAimInfo.setPower((600 - aimingTarget.predictDistance) / 300 + 1);
				nextAimInfo.setIfCanFire(true);
				// System.out.println("power:" + ((400 -
				// aimingTarget.predictDistance) / 200 + 1));
			}
			ifAiming = false;
		} else {
			nextAimInfo.setIfCanFire(false);
		}
		return nextAimInfo;
	}
	
	
	
	private void record(int thisStep) {
		int maxLength = Math.min(MAX_PATTERN_LENGTH, enemyHistory.length());
		for (int i = 0; i <= maxLength; ++i) {
			String pattern = enemyHistory.substring(0, i);
			int[] frequencies = matcher.get(pattern);
			if (frequencies == null) {
				frequencies = new int[21 * 17];
				matcher.put(pattern, frequencies);
			}
			++frequencies[thisStep];
		}

	}

	private int predict(String pattern) {
		int[] frequencies = null;
		for (int patternLength = Math.min(pattern.length(), MAX_PATTERN_LENGTH); frequencies == null; --patternLength) {
			frequencies = matcher.get(pattern.substring(0, patternLength));
		}
		int nextTick = 0;
		for (int i = 1; i < frequencies.length; ++i) {
			if (frequencies[nextTick] < frequencies[i]) {
				nextTick = i;
			}
		}
		return nextTick;
	}
	
	

	/**
	 * Ԥ�⿪����
	 */
	private double predictAim(RobotInfo target) {
		predictions.clear();
		Point2D.Double myP = new Point2D.Double(battle.getX(), battle.getY());
		Point2D.Double enemyP = project(myP, target.getHeading()+battle.getHeadingRadians(), target.getDistance());
		String pattern = enemyHistory;
		for (double d = 0; d < myP.distance(enemyP); d += FIRE_SPEED) {
			int nextStep = predict(pattern);
			target.decode(nextStep);
			enemyP = project(enemyP, target.getHeading(), target.getVelocity());
			predictions.add(enemyP);
			pattern = (char) nextStep + pattern;
		}
		target.setLocationX(enemyP.getX());
		target.setLocationY(enemyP.getY());
		double gunTurn = Math.atan2(enemyP.x - myP.x, enemyP.y - myP.y) - battle.getGunHeadingRadians();
		target.predictX = target.getLocationX();
		target.predictY = target.getLocationY();
		target.predictDistance = Math.sqrt((target.predictX - battle.getX()) * (target.predictX - battle.getX())
				+ (target.predictY - battle.getY()) * (target.predictY - battle.getY()));

		double hitTime = Math.floor(target.predictDistance / 20) + battle.getTime() - target.getScanTime() + 1;// ���ԵĹ���
		target.predictX = target.predictX
				+ Math.sin(target.getHeading() / 180 * Math.PI) * hitTime * target.averageVelocity;
		target.predictY = target.predictY
				+ Math.cos(target.getHeading() / 180 * Math.PI) * hitTime * target.averageVelocity;
		if (target.predictX < 0)
			target.predictX = 0;
		else if (target.predictX > battle.getBattleFieldWidth())
			target.predictX = battle.getBattleFieldWidth();
		if (target.predictY < 0)
			target.predictY = 0;
		else if (target.predictY > battle.getBattleFieldHeight())
			target.predictY = battle.getBattleFieldHeight();
		// ��һ֡�����˶�λ��
		double diffX = Math.sin(Math.toRadians(battle.getHeading())) * battle.getVelocity();
		double diffY = Math.cos(Math.toRadians(battle.getHeading())) * battle.getVelocity();
		// ����̹�˵ĽǶȣ���Ҫ���ݳ�����˶����ڹ���׼���в������������ڳ��ӵ��˶������ȷ�������һ֡����׼����
		double lenX = target.predictX - (battle.getX() + diffX);
		double lenY = target.predictY - (battle.getY() + diffY);

		double enemyBearding = getAngle(lenY, lenX);
		// ���㲢�淶����׼���򣬴���180�ȹ淶������ʱ��ת��ȷ����׼ʱ�����
		//double nextGunTurn = normalizeAngle(enemyBearding - battle.getGunHeading());
		double nextGunTurn = normalizeAngle(gunTurn);
		return nextGunTurn;
	}

	/**
	 * calcuNextGunBearingר�ã�����һ�����ʺϴ����Ŀ��
	 */
	private RobotInfo calcuBestTarget() {
		RobotInfo target;
		Enumeration<RobotInfo> enumeration = enemyList.elements();
		double lost = 99999;
		RobotInfo best = new RobotInfo();
		while (enumeration.hasMoreElements()) {
			target = (RobotInfo) enumeration.nextElement();
			// ���ۺ���=�ڿڽǶȲ�/20+����/20+5*abs(sin(���˳���-���˷�λ))+abs(�����ٶ�)/���ٶ�
			// ��ǰʱ�̵����������ľ��Է���
			double lenX = target.getLocationX() - battle.getX();
			double lenY = target.getLocationY() - battle.getY();
			double enemyBearding = getAngle(lenY, lenX);
			// ��ǰʱ�̵��˳���-���˷�λ
			double beardingErrorRadius = Math.toRadians(target.getHeading() - enemyBearding);
			double gunError = Math.abs(normalizeAngle(enemyBearding - battle.getGunHeading()));

			target.setAimPrice((target.getDistance() + gunError) / 20 + 5 * Math.sin(beardingErrorRadius)
					+ Math.abs(target.getVelocity()));
			if (target.getAimPrice() < lost) {
				best = target;
				lost = target.getAimPrice();
			}
			// System.out.println("b:" + beardingErrorRadius * 180 / Math.PI);
			// System.out.println("g:" + gunError);
			// System.out.println("price:" + target.getAimPrice());
		}
		return best;
	}

	/**
	 * ���ݵ�ͼ��������������һ�������˶����ڿ�������ʱ�˶�������Ӧ���Ľ����˶�������Ϊ�˷�ֹײǽ��
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
	
	private static Point2D.Double project(Point2D.Double p, double angle, double distance) {
		double x = p.x + distance * Math.sin(angle);
		double y = p.y + distance * Math.cos(angle);
		return new Point2D.Double(x, y);
	}
}