package test;

import java.util.ArrayList;
import java.util.List;

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
	List<RobotInfo> enemyList=new ArrayList<>();

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
		yourself.setLocationX(you.getX());
		yourself.setLocationY(you.getY());
		yourself.setHeading(you.getHeading());
		yourself.setVelocity(you.getVelocity());
	}

	/**
	 * ���õ��˵���Ϣ��Ŀǰֻ֧��һ�����ˣ�Ӧ��дΪ֧�ֶ������
	 */
	public void setEnemyInfo(ScannedRobotEvent e) {
		enemy.setName(e.getName());
		enemy.setBearing(e.getBearing());
		enemy.setDistance(e.getDistance());
		enemy.setHeading(e.getHeading());
		enemy.setVelocity(e.getVelocity());

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
		//�����������ȷ���˶�
		
		
		return nextMoveInfo;
	}

}