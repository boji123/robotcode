package PredictAndGravity;

import java.awt.Color;

import robocode.*;

public class PredictAndGravity extends AdvancedRobot {
	BattleMap battleMap = new BattleMap(this);
	long preTick = -1;

	public void run() {
		setColors(Color.blue, Color.blue, Color.blue);
		// 解除锁定，三个部分独立运行
		setAdjustGunForRobotTurn(true);
		setAdjustRadarForGunTurn(true);
		while (true) {
			while (getTime() == preTick)
				;// 程序锁死直到下一tick到来
			preTick = getTime();
			setScan();
			setMove();
			setFire();
			// 在实际tick解析中，开火事件瞬发，炮管移动与车体移动叠加
			execute();
		}
	}

	public void setScan() {
		if (getRadarTurnRemaining() <= 30) {
			setTurnRadarLeft(360);
		}
	}

	public void setMove() {
		NextMoveInfo nextMoveInfo = battleMap.calcuNextMove();
		setTurnRight(nextMoveInfo.getBearing());
		setAhead(nextMoveInfo.getDistance());// 由于车有加速度，这个函数会根据距离调整车的速度，确保你停在正确位置，因此输入的移动距离大，车速大，距离小，车速小
		// System.out.println(getVelocity());
		// setTurnRight(0);
		// setAhead(0);
	}

	public void setFire() {
		NextAimInfo nextAimInfo = battleMap.calcuNextGunBearing();
		setTurnGunRight(nextAimInfo.getBearing());
		if (nextAimInfo.getIfCanFire()) {
			setFire(1);
		}
	}

	// 事件触发
	/**
	 * 雷达扫描到一个敌人，可以获取到敌人的信息
	 */
	public void onScannedRobot(ScannedRobotEvent e) {
		battleMap.setEnemyInfo(e);
	}

	/**
	 * 敌人死亡
	 */
	public void onRobotDeath(RobotDeathEvent event) {
		battleMap.removeEnemyFromMap(event);
	}

	/**
	 * 撞击到敌人，可以获取到如bearing（敌人方位）等信息
	 */
	public void onHitRobot(HitRobotEvent event) {
		// event.get___
	}

	/**
	 * 你的子弹击中敌人
	 */
	public void onBulletHit(BulletHitEvent event) {
		// event.get___
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
