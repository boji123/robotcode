package Remake;

import java.awt.Color;
import java.util.Random;

import robocode.*;

public class Remake extends AdvancedRobot {
	BattleMap battleMap = new BattleMap(this);
	long preTick = -1;
	// 坦克状态控制关键字
	int aimingTime = 0;
	String friend = "name";// 队友名字
	static boolean[] robotPlace = new boolean[4];
	int robotNum;

	public void run() {
		do {// 随机分配一个角落给机器人，未完成
			robotNum = (int) Math.random() * 100 % 4;
		} while (robotPlace[robotNum] == false);
		robotPlace[robotNum] = true;

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
		if (getOthers() > 1) {// 超过一个敌人（小队战不适用）
			setTurnRadarRight(500);
		}
	}

	public void setMove() {
		if (getOthers() > 1) {// 超过一个敌人（小队战不适用）
			NextMoveInfo nextMoveInfo = battleMap.calcuNextGravityMove(new Force());
			setTurnRight(nextMoveInfo.getBearing());
			setAhead(nextMoveInfo.getDistance());// 由于车有加速度，这个函数会根据距离调整车的速度，确保你停在正确位置，因此输入的移动距离大，车速大，距离小，车速小

		} else {
			NextMoveInfo nextMoveInfo = null;
			if (nextMoveInfo == null) {
				nextMoveInfo = battleMap.calcuNextGravityMove(new Force());
				// 此处重写为hidebullet
			}
			setTurnRight(nextMoveInfo.getBearing());
			setAhead(nextMoveInfo.getDistance());
		}
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
			if (getOthers() > 1) {// 超过一个敌人（小队战不适用）
				battleMap.setEnemyInfo(e);
			} else {
				setTurnRadarRight(battleMap.trackCurrent(e));
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
