package test;

import robocode.AdvancedRobot;
import robocode.ScannedRobotEvent;

/**
 * ��ͼ�࣬���ڱ������е����Լ����Լ���λ����Ϣ
 * 
 * @author baiji
 *
 *         ���Ӵ�����ֻ֧��һ�����ˣ���Ҫ������Ҫ�Ľ���֧�ֶ������
 */
public class battleMap {
	RobotInfo enemy = new RobotInfo();
	RobotInfo yourself = new RobotInfo();

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
	}

	/**
	 * ���õ��˵���Ϣ��Ŀǰֻ֧��һ�����ˣ�Ӧ��дΪ֧�ֶ������
	 */
	public void setEnemyInfo(ScannedRobotEvent e) {
		enemy.name = e.getName();
		enemy.bearing = e.getBearing();
		enemy.distance = e.getDistance();
		enemy.heading = e.getHeading();
		enemy.velocity = e.getVelocity();

		// �˴�Ӧ���������Ե�λ�ø��µ��˵ķ�λ;
		// enemy.locationX=0;
		// enemy.locationY=0;
	}

	/**
	 * �������������ӵ�ͼ���Ƴ�
	 */
	public void removeEnemyFromMap(String enemyName) {

	}

	/**
	 * ���ݵ�ͼ��������������һ���ڹ��˶�
	 */
	public double turnNextGunBearing() {
		double nextBearing = 0;
		return nextBearing;
	}

	/**
	 * ���ݵ�ͼ��������������һ�������˶�
	 */
	public NextMoveInfo turnNextMove() {
		NextMoveInfo nextMoveInfo = new NextMoveInfo();
		nextMoveInfo.bearing = 0;
		nextMoveInfo.distance = 0;
		return nextMoveInfo;
	}

}