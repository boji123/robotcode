package test;

import java.awt.Robot;
import java.awt.image.SinglePixelPackedSampleModel;
import java.util.ArrayList;
import java.util.List;

import robocode.AdvancedRobot;
import robocode.RobotDeathEvent;
import robocode.ScannedRobotEvent;

/**
 * 地图类，用于保存所有敌人以及你自己的位置信息
 * 
 * @author baiji
 *
 *         留坑待处理，只支持一个敌人，需要根据需要改进成支持多个敌人
 */
public class BattleMap {
	RobotInfo enemy = new RobotInfo();
	RobotInfo yourself = new RobotInfo();
	List<RobotInfo> enemyList = new ArrayList<>();
	// 在每次计算完下一tick的行动之后，将行动数据存于这两个变量并返回，其中计算nextAimInfo时需要传入nextMoveInfo进行综合
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
	 * 设置你自己在地图上的信息，该函数由主循环周期性调用
	 */
	public void setYourInfo(AdvancedRobot you) {
		yourself.locationX = you.getX();
		yourself.locationY = you.getY();
		yourself.heading = you.getHeading();
		yourself.velocity = you.getVelocity();
		yourself.gunHeading = you.getGunHeading();
	}

	/**
	 * 设置敌人的信息，目前只支持一个敌人，应重写为支持多个敌人
	 */
	public void setEnemyInfo(ScannedRobotEvent e) {
		enemy.name = e.getName();
		enemy.bearing = e.getBearing();// 相对于你车体的朝向
		enemy.distance = e.getDistance();
		enemy.heading = e.getHeading();
		enemy.velocity = e.getVelocity();

		double absoluteAngle = normalizeBearing(enemy.bearing + yourself.heading);
		// System.out.println(absoluteAngle);
		// System.out.println(enemy.distance);
		// 此处应当根据你自的位置更新敌人的方位;
		enemy.locationX = yourself.locationX + enemy.distance * Math.sin(absoluteAngle / 180 * Math.PI);
		enemy.locationY = yourself.locationY + enemy.distance * Math.cos(absoluteAngle / 180 * Math.PI);
		// System.out.println("yourlocation:" + yourself.locationX + "," +
		// yourself.locationY);
		// System.out.println("enemylocation:" + enemy.locationX + "," +
		// enemy.locationY);
	}

	/**
	 * 若敌人死亡，从地图上移除
	 */
	public void removeEnemyFromMap(RobotDeathEvent event) {
		String robotName = event.getName();
	}

	/**
	 * 
	 * 根据地图的情况返回你的下一步炮管运动
	 */
	public NextAimInfo calcuNextGunBearing() {
		RobotInfo target = calcuBestTarget();
		double angle = predictAim(target);// 炮管与预测开火方向的角度
		// 这里有个问题，车体运动会影响炮管运动，需要解除运动对瞄准的影响，关于tick的处理这里有点拗口，还请慢慢理解或重写
		angle = normalizeBearing(angle);
		nextAimInfo.setIfCanFire(Math.abs(angle) < 1 ? true : false);// 若当前炮管已经对准敌人（误差角度<1），下一帧将尝试进行射击
		nextAimInfo.setBearing(angle);
		return nextAimInfo;
	}

	/**
	 * 预测开火方向，目前是假设敌人不运动而你自己运动
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
		// 计算并规范化瞄准方向，这里没有考虑车身本身的运动
		double nextGunTurn = normalizeBearing(predictBearing - yourself.gunHeading);
		// 根据车身的运动对炮管瞄准进行补偿
		double nextTurn = nextMoveInfo.getBearing();
		double maxTurn = 10 - 0.75 * Math.abs(yourself.velocity);
		if (nextTurn > maxTurn) {
			nextTurn = maxTurn;
		} else if (nextTurn < -maxTurn) {
			nextTurn = -maxTurn;
		}
		double adjustGunTurn = nextGunTurn;
		// 如果觉得混乱可以注释掉这个if看一下瞄准效果
		if (adjustGunTurn > nextTurn - 20 && adjustGunTurn < nextTurn + 20) {
			adjustGunTurn = adjustGunTurn + nextTurn;
		}
		return adjustGunTurn;
	}

	/**
	 * calcuNextGunBearing专用，返回一个最适合打击的目标
	 */
	private RobotInfo calcuBestTarget() {
		RobotInfo target = new RobotInfo();
		target = enemy;
		return target;
	}

	/**
	 * 根据地图的情况返回你的下一步车体运动
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
	 * 输入-360~360区间的角度值，返回-180到180的规范化角度（方便炮管或车体转向）
	 */
	private static double normalizeBearing(double angle) {
		if (angle < -180)
			angle += 360;
		if (angle > 180)
			angle -= 360;
		return angle;
	}
}