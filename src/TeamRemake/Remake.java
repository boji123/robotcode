package TeamRemake;

import java.awt.Color;

import robocode.*;

public class Remake extends TeamRobot {
	BattleMap battleMap = new BattleMap(this);
	Cooperate cooperate = new Cooperate();
	long preTick = -1;
	// 坦克状态控制关键字
	int aimingTime = 0;
	int hiding = 0;// 特殊情况需要强制后退闪避

	public void run() {
		cooperate.init(this, battleMap);
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
			if (getTime() == 10 && cooperate.isLeader) {// 确保场上坦克已经扫描了一圈
				cooperate.divideCornerForTeam();
				System.out.println("divideCorner");
			}
			execute();
		}
	}

	public void setScan() {

		if (cooperate.getEnemyRest() > 1 || getTime() < 10) {// 超过一个敌人
			setTurnRadarRight(500);
		}
	}

	public void setMove() {

		if (hiding == 0) {
			if (!cooperate.ifReachPlace()) {
				NextMoveInfo nextMoveInfo = battleMap.calcuNextGravityMove(cooperate.cornerForce);
				setTurnRight(nextMoveInfo.getBearing());
				setAhead(nextMoveInfo.getDistance());// 由于车有加速度，这个函数会根据距离调整车的速度，确保你停在正确位置，因此输入的移动距离大，车速大，距离小，车速小
			} else {// 应重写为在角落的运动
				NextMoveInfo nextMoveInfo = battleMap.calcuNextGravityMove(cooperate.cornerForce);
				setTurnRight(nextMoveInfo.getBearing());
				setAhead(nextMoveInfo.getDistance());// 由于车有加速度，这个函数会根据距离调整车的速度，确保你停在正确位置，因此输入的移动距离大，车速大，距离小，车速小
			}
		} else
			hiding--;
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
			// System.out.println(battleMap.aimingTarget.getName());
		}
	}

	// 事件触发
	/**
	 * 雷达扫描到一个敌人，可以获取到敌人的信息
	 */
	public void onScannedRobot(ScannedRobotEvent e) {
		if (!isTeammate(e.getName())) {
			if (cooperate.getEnemyRest() > 1) {
				battleMap.setEnemyInfo(e);
			} else {
				setTurnRadarRight(battleMap.trackCurrent(e));
				battleMap.setEnemyInfo(e);
			}
		} else {
			battleMap.setEnemyInfo(e);// 方法名字不太对，但是凑合用吧。。。
		}

	}

	public void onMessageReceived(MessageEvent event) {
		cooperate.onMessageReceived(event);
	}

	/**
	 * 敌人死亡
	 */
	public void onRobotDeath(RobotDeathEvent event) {
		battleMap.removeEnemyFromMap(event);
		if (event.getName() == battleMap.aimingTarget.getName()) {
			aimingTime = 0;
		}
		if (isTeammate(event.getName()))
			cooperate.teammatesRest--;
	}

	/**
	 * 撞击到敌人，可以获取到如bearing（敌人方位）等信息
	 */
	public void onHitRobot(HitRobotEvent event) {
		hiding = 5;
		setAhead(-50);
		setTurnRight(event.getBearing());
		System.out.println("hitrobot!:" + event.getName());
	}

	public void onHitWall(HitWallEvent event) {
		hiding = 5;
		setAhead(-50);
		setTurnRight(event.getBearing());
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
