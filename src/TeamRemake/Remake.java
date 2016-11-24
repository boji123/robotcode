package TeamRemake;

import java.awt.Color;
import java.util.Hashtable;

import robocode.*;

public class Remake extends AdvancedRobot {
	BattleMap battleMap = new BattleMap(this);
	Cooperate cooperate = new Cooperate();
	long preTick = -1;
	// 坦克状态控制关键字
	int aimingTime = 0;
	int bumpHiding = 0;// 特殊情况需要强制后退闪避
	int hitHiding = 0;
	String[] teammates = { "TeamRemake.Remake* (1)", "TeamRemake.Remake* (2)", "TeamRemake.Remake* (3)" };

	int thisTurnScanRobotCount = 0;
	int noOnScanTime = 0;
	Hashtable<String, Boolean> thisTurnScanList = new Hashtable<String, Boolean>();

	public void run() {
		cooperate.init(this, battleMap);
		setColors(Color.gray, Color.ORANGE, Color.ORANGE);
		// 解除锁定，三个部分独立运行
		setAdjustGunForRobotTurn(true);
		setAdjustRadarForGunTurn(true);
		setTurnRadarRight(1000);
		while (getTime() < 10) {// 开场扫描一圈
			while (getTime() == preTick)
				;// 程序锁死直到下一tick到来
			setScan();
			preTick = getTime();
			execute();
		}
		cooperate.divideCornerForTeam();// 确保场上坦克已经扫描了一圈
		while (true) {
			while (getTime() == preTick)
				;// 程序锁死直到下一tick到来
			preTick = getTime();
			setScan();
			if (hitHiding == 0 && bumpHiding == 0) {
				setMove();
			} else {
				if (hitHiding > 0)
					hitHiding--;
				if (bumpHiding > 0)
					bumpHiding--;
			}
			setFire();
			// 在实际tick解析中，开火事件瞬发，炮管移动与车体移动叠加
			// System.out.println(getOthers());
			execute();
		}
	}

	public boolean isTeammate(String name) {
		if (teammates == null)
			return false;
		for (int i = 0; i < teammates.length; i++)
			if (name.compareTo(teammates[i]) == 0)
				return true;
		return false;
	}

	public String[] getTeammates() {
		return teammates;
	}

	public void setScan() {
		if (noOnScanTime > 3) {// 如果出现了足够大的空裆没有扫描到敌人，重置扫描状态，确保扫描角度是最优的
			System.out.println("change");
			noOnScanTime = 0;
			thisTurnScanRobotCount = 0;
			thisTurnScanList = new Hashtable<String, Boolean>();
			setTurnRadarRight(1000 * Math.signum(getRadarTurnRemaining()));
		}
		if (thisTurnScanRobotCount >= getOthers()) {// 如果扫描到了所有敌人，切换扫描方向
			setTurnRadarRight(-1000 * Math.signum(getRadarTurnRemaining()));
			thisTurnScanRobotCount = 0;
			thisTurnScanList = new Hashtable<String, Boolean>();
		}
		noOnScanTime++;
	}

	public void setMove() {
		if (!cooperate.ifReachPlace()) {
			NextMoveInfo nextMoveInfo = battleMap.calcuNextGravityMove(cooperate.cornerForce);
			setTurnRight(nextMoveInfo.getBearing());
			setAhead(nextMoveInfo.getDistance());// 由于车有加速度，这个函数会根据距离调整车的速度，确保你停在正确位置，因此输入的移动距离大，车速大，距离小，车速小
		} else {// 应重写为在角落的运动
			double bearing = battleMap.aimingTarget.getBearing();
			double turnTagency;
			if (bearing >= 0)
				turnTagency = bearing - 90;
			else
				turnTagency = bearing + 90;
			double predictHeadingRadius = Math.toRadians(turnTagency + getHeading());
			Force force = new Force();
			force.xForce = Math.sin(predictHeadingRadius) * 2000 / battleMap.aimingTarget.getDistance() * Math.random();
			force.yForce = Math.cos(predictHeadingRadius) * 2000 / battleMap.aimingTarget.getDistance() * Math.random();

			NextMoveInfo nextMoveInfo = battleMap.calcuNextGravityMove(force);
			setTurnRight(nextMoveInfo.getBearing());
			setAhead(nextMoveInfo.getDistance());// 由于车有加速度，这个函数会根据距离调整车的速度，确保你停在正确位置，因此输入的移动距离大，车速大，距离小，车速小
		}
	}

	public void setFire() {
		NextAimInfo nextAimInfo = battleMap.calcuNextGunBearing();
		if (nextAimInfo != null) {
			if (aimingTime < 20) {
				aimingTime++;
				setTurnGunRight(nextAimInfo.getBearing());
				if (nextAimInfo.getIfCanFire()) {
					setFire(nextAimInfo.getPower());
				}
			} else {
				aimingTime = 0;
				battleMap.calcuBestTarget();
			}
		} else {

			battleMap.calcuBestTarget();
			// System.out.println("aimming:" +battleMap.aimingTarget.getName());
		}
	}

	// 事件触发
	/**
	 * 雷达扫描到一个敌人，可以获取到敌人的信息
	 */
	public void onScannedRobot(ScannedRobotEvent e) {
		noOnScanTime = 0;
		if (!thisTurnScanList.containsKey(e.getName())) {
			thisTurnScanRobotCount++;
			thisTurnScanList.put(e.getName(), true);
		}

		battleMap.setEnemyInfo(e);
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
		bumpHiding = 10;
		setAhead(-100);
		setTurnRight(event.getBearing());
		System.out.println("hitrobot!:" + event.getName());
	}

	public void onHitWall(HitWallEvent event) {
		bumpHiding = 10;
		setAhead(-100);
		setTurnRight(event.getBearing());
		System.out.println("hitwall!");
	}

	/**
	 * 被子弹击中，可以获得如子弹射过来的方向
	 */
	public void onHitByBullet(HitByBulletEvent event) {
		hitHiding = 15 + (int) (10 * Math.random());
		System.out.println(hitHiding);
		double bearing = event.getBearing();
		double turnTagency;
		if (bearing >= 0)
			turnTagency = bearing - 90;
		else
			turnTagency = bearing + 90;

		setTurnRight(turnTagency);
		setAhead(-200);
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
