package Remake;

import java.awt.Color;

import robocode.*;

public class Remake extends AdvancedRobot {
	BattleMap battleMap = new BattleMap(this);
	long preTick = -1;
	// 坦克状态控制关键字
	int aimingTime = 0;
	String friend = "name";// 队友名字

	public void run() {
		setColors(Color.gray, Color.ORANGE, Color.ORANGE);
		// 解除锁定，三个部分独立运行
		setAdjustGunForRobotTurn(true);
		setAdjustRadarForGunTurn(true);
		setTurnRadarRight(500);
		while (true) {
			while (getTime() == preTick)
				;// 程序锁死直到下一tick到来
			preTick = getTime();
			setScan();
			setMove();
			setFire();
			// 在实际tick解析中，开火事件瞬发，炮管移动与车体移动叠加
			// System.out.println(getOthers());
			execute();
		}
	}

	public void setScan() {
		if (getOthers() > 1) {
			setTurnRadarRight(500);
		}
	}

	public void setMove() {
		NextMoveInfo nextMoveInfo = battleMap.calcuNextMove();
		setTurnRight(nextMoveInfo.getBearing());
		setAhead(nextMoveInfo.getDistance());// 由于车有加速度，这个函数会根据距离调整车的速度，确保你停在正确位置，因此输入的移动距离大，车速大，距离小，车速小
	}

	public void setFire() {
		if (aimingTime < 20) {
			aimingTime++;
			NextAimInfo nextAimInfo = battleMap.calcuNextGunBearing();
			setTurnGunRight(nextAimInfo.getBearing());
			if (nextAimInfo.getIfCanFire()) {
				setFire(nextAimInfo.getPower());
			}
		} else {
			aimingTime = 0;
			battleMap.calcuBestTarget();
		}
	}

	// 事件触发
	/**
	 * 雷达扫描到一个敌人，可以获取到敌人的信息
	 */
	public void onScannedRobot(ScannedRobotEvent e) {
		if (e.getName() != friend) {
			if (getOthers() == 1) {
				setTurnRadarRight(battleMap.trackCurrent(e));
				battleMap.setEnemyInfo(e);
			} else {
				battleMap.setEnemyInfo(e);
			}
		}
	}

	/**
	 * 敌人死亡
	 */
	public void onRobotDeath(RobotDeathEvent event) {
		battleMap.removeEnemyFromMap(event);
		if (event.getName() == battleMap.aimingTarget.getName()) {
			aimingTime = 0;
		}
	}

	/**
	 * 撞击到敌人，可以获取到如bearing（敌人方位）等信息
	 */
	public void onHitRobot(HitRobotEvent event) {
		System.out.println("hitrobot!:" + event.getName());
	}

	public void onHitWall(HitWallEvent event) {
		System.out.println("hitwall!");
	}

	/**
	 * 被子弹击中，可以获得如子弹射过来的方向
	 */
	public void onHitByBullet(HitByBulletEvent event) {
		// event.get___
	}

	// ------------------------------------------------------------------------------------
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
