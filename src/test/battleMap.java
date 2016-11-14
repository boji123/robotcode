package test;

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
	public void removeEnemyFromMap(RobotDeathEvent event) {
		String robotName = event.getName();
	}

	/**
	 * ���ݵ�ͼ��������������һ���ڹ��˶�
	 */
	public NextAimInfo calcuNextGunBearing() {
		NextAimInfo nextAimInfo = new NextAimInfo();
		nextAimInfo.setBearing(50);
		nextAimInfo.setIfCanFire(true);
		return nextAimInfo;
	}

	/**
	 * ���ݵ�ͼ��������������һ�������˶�
	 */
	public NextMoveInfo calcuNextMove() {
		NextMoveInfo nextMoveInfo = new NextMoveInfo();
		nextMoveInfo.setBearing(50);
		nextMoveInfo.setDistance(50);
		return nextMoveInfo;
	}

}