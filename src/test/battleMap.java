package test;

import java.util.ArrayList;
import java.util.List;

import robocode.AdvancedRobot;
import robocode.ScannedRobotEvent;

/**
 * 地图类，用于保存所有敌人以及你自己的位置信息
 * 
 * @author baiji
 *
 *         留坑待处理，只支持一个敌人，需要根据需要改进成支持多个敌人
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
	 * 设置你自己在地图上的信息，该函数由主循环周期性调用
	 */
	public void setYourInfo(AdvancedRobot you) {
		yourself.locationX = you.getX();
		yourself.locationY = you.getY();
		yourself.heading = you.getHeading();
		yourself.velocity = you.getVelocity();
	}

	/**
	 * 设置敌人的信息，目前只支持一个敌人，应重写为支持多个敌人
	 */
	public void setEnemyInfo(ScannedRobotEvent e) {
		enemy.name = e.getName();
		enemy.bearing = e.getBearing();
		enemy.distance = e.getDistance();
		enemy.heading = e.getHeading();
		enemy.velocity = e.getVelocity();

		// 此处应当根据你自的位置更新敌人的方位;
		// enemy.locationX=0;
		// enemy.locationY=0;
	}

	/**
	 * 若敌人死亡，从地图上移除
	 */
	public void removeEnemyFromMap(String enemyName) {

	}

	/**
	 * 根据地图的情况返回你的下一步炮管运动
	 */
	public double turnNextGunBearing() {
		double nextBearing = 0;
		return nextBearing;
	}

	/**
	 * 根据地图的情况返回你的下一步车体运动
	 */
	public NextMoveInfo turnNextMove() {
		NextMoveInfo nextMoveInfo = new NextMoveInfo();
		nextMoveInfo.setBearing(0);
		nextMoveInfo.setDistance(0);
		return nextMoveInfo;
	}

}