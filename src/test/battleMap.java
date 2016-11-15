package test;

import java.awt.Robot;
import java.awt.image.SinglePixelPackedSampleModel;
import java.util.ArrayList;
import java.util.List;

import robocode.AdvancedRobot;
import robocode.RobotDeathEvent;
import robocode.ScannedRobotEvent;

/**
 * ��ͼ�࣬���ڱ������е����Լ����Լ���λ����Ϣ
 * 
 * @author baiji
 *
 *         ���Ӵ�����ֻ֧��һ�����ˣ���Ҫ������Ҫ�Ľ���֧�ֶ������
 */
public class BattleMap {
	RobotInfo enemy = new RobotInfo();
	RobotInfo yourself = new RobotInfo();
	List<RobotInfo> enemyList = new ArrayList<>();
	// ��ÿ�μ�������һtick���ж�֮�󣬽��ж����ݴ������������������أ����м���nextAimInfoʱ��Ҫ����nextMoveInfo�����ۺ�
	NextMoveInfo nextMoveInfo = new NextMoveInfo();
	NextAimInfo nextAimInfo = new NextAimInfo();
	// robotinfo:
	// String name;
	// double locationX;
	// double locationY;
	// double bearing;
	// double distance;
	// double heading;
	// double velocity;
	// double energy;

	/**
	 * �������Լ��ڵ�ͼ�ϵ���Ϣ���ú�������ѭ�������Ե���
	 */
	public void setYourInfo(AdvancedRobot you) {
		yourself.locationX = you.getX();
		yourself.locationY = you.getY();
		yourself.heading = you.getHeading();
		yourself.velocity = you.getVelocity();
		yourself.gunHeading = you.getGunHeading();
	}

	/**
	 * ���õ��˵���Ϣ��Ŀǰֻ֧��һ�����ˣ�Ӧ��дΪ֧�ֶ������
	 */
	public void setEnemyInfo(ScannedRobotEvent e) {
		enemy.name = e.getName();
		enemy.bearing = e.getBearing();// ������㳵��ĳ���
		enemy.distance = e.getDistance();
		enemy.heading = e.getHeading();
		enemy.velocity = e.getVelocity();

		double absoluteAngle = normalizeBearing(enemy.bearing + yourself.heading);
		// System.out.println(absoluteAngle);
		// System.out.println(enemy.distance);
		// �˴�Ӧ���������Ե�λ�ø��µ��˵ķ�λ;
		enemy.locationX = yourself.locationX + enemy.distance * Math.sin(absoluteAngle / 180 * Math.PI);
		enemy.locationY = yourself.locationY + enemy.distance * Math.cos(absoluteAngle / 180 * Math.PI);
		// System.out.println("yourlocation:" + yourself.locationX + "," +
		// yourself.locationY);
		// System.out.println("enemylocation:" + enemy.locationX + "," +
		// enemy.locationY);
	}

	/**
	 * �������������ӵ�ͼ���Ƴ�
	 */
	public void removeEnemyFromMap(RobotDeathEvent event) {
		String robotName = event.getName();
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
		double lenX = target.locationX - yourself.locationX;
		double lenY = target.locationY - yourself.locationY;
		double predictBearing = Math.atan(lenX / lenY) * 180 / Math.PI;
		if (lenX < 0 && lenY < 0) {
			predictBearing -= 180;
		} else if (lenX > 0 && lenY < 0) {
			predictBearing += 180;
		}
		// ���㲢�淶����׼��������û�п��ǳ�������˶�
		double nextGunTurn = normalizeBearing(predictBearing - yourself.gunHeading);
		// ���ݳ�����˶����ڹ���׼���в���
		double nextTurn = nextMoveInfo.getBearing();
		double maxTurn = 10 - 0.75 * Math.abs(yourself.velocity);
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
		target = enemy;
		return target;
	}

	/**
	 * ���ݵ�ͼ��������������һ�������˶�
	 */
	public NextMoveInfo calcuNextMove() {
		nextMoveInfo.setBearing(5);// max 10 per tick
		nextMoveInfo.setDistance(8);// max 8 per tick
		return nextMoveInfo;
	}

	private double calcuForce() {
		double force = 0;
		return force;
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
}