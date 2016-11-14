package test;

import robocode.*;

/**
 * 
 * @author baiji
 *
 */
public class FirstMove extends AdvancedRobot {
	battleMap map = new battleMap();

	public void run() {
		// 解除锁定，三个部分独立运行
		setAdjustGunForRobotTurn(true);
		setAdjustRadarForGunTurn(true);
		while (true) {
			map.setYourInfo(this);
			setScan();
			setMove();
			setFire();
			execute();
		}
	}

	public void setScan() {
		if (getRadarTurnRemaining() == 0) {
			// 没有事件触发时的普通控制方式
			setTurnRadarLeft(360);
			// map.setYourPlace(this.get);
		}
	}

	public void setMove() {
		if (getDistanceRemaining() == 0) {
			// 没有事件触发时的普通控制方式

		}
	}

	public void setFire() {
		if (getGunTurnRemaining() == 0) {// 这里偷懒了，当检测到炮管停下时发射一枚炮弹
			// 没有事件触发时的普通控制方式
			fire(1);
			// 这里容易出现冲突
			// setTurnGunRight(map.turnNextGunBearing());
		}
	}

	// 事件触发
	/**
	 * 雷达扫描到一个敌人，可以获取到敌人的信息
	 */
	public void onScannedRobot(ScannedRobotEvent e) {
		if (getGunTurnRemaining() == 0) {
			double enemyDirectionFromGun = (e.getBearing() - (getGunHeading() - getHeading())) % 360;
			setTurnGunRight(enemyDirectionFromGun);
		}
	}

	/**
	 * 撞击到敌人，可以获取到如bearing（敌人方位）等信息
	 */
	public void onHitRobot(HitRobotEvent event) {
		// event.get___
	}

	/**
	 * 被子弹击中，可以获得如子弹射过来的方向
	 */
	public void onHitByBullet(HitByBulletEvent event) {
		// event.get___
	}

	// 一些数学函数
	/**
	 * 输入起止坐标，返回角度的反正切值，可以不使用该函数直接调用atan2
	 */
	private static double getAngle(double x1, double y1, double x2, double y2) {
		return Math.atan2(x2 - x1, y2 - y1);
	}

	/**
	 * 输入-2pi~2pi区间的角度值，返回-pi到pi的规范化角度（方便系统使用）
	 */
	private static double normalizeBearing(double angle) {
		if (angle < -Math.PI)
			angle += 2 * Math.PI;
		if (angle > Math.PI)
			angle -= 2 * Math.PI;
		return angle;
	}

	/**
	 * 如果能胜利的话。。执行这一段装逼用
	 */
	public void onWin(WinEvent e) {
		for (int i = 0; i < 50; i++) {

			turnGunRightRadians(Math.PI * 3 / 4);
			turnGunLeftRadians(Math.PI * 3 / 4);
		}
	}
}
