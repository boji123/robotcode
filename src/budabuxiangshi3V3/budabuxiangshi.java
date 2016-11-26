package budabuxiangshi3V3;

import java.awt.Color;
import java.util.Enumeration;
import java.util.Hashtable;

import robocode.*;

//3V3混战适用
public class budabuxiangshi extends AdvancedRobot {
	int teamFireCount = 0;// 用于每局战斗统计队友伤害
	// -----
	BattleMap battleMap = new BattleMap(this);
	Cooperate cooperate = new Cooperate();
	long preTick = -1;
	// 坦克状态控制关键字
	int aimingTime = 0;// 瞄准一个目标一段时间的计数（防止频繁切换目标）
	int hiding = 0;// 特殊情况需要强制后退闪避
	int changeDirection = 0;// 一定时间后改变方向的计数
	int direction = 1;// 当前方向，前进/后退
	boolean ifNearWall;// 靠近墙，改变一下策略

	int thisTurnScanRobotCount = 0;
	int noOnScanTime = 0;
	Hashtable<String, Boolean> thisTurnScanList = new Hashtable<String, Boolean>();

	public void run() {
		cooperate.init(this, battleMap);
		setColors(Color.gray, Color.black, Color.black, Color.orange, Color.yellow);
		// 解除锁定，三个部分独立运行
		setAdjustGunForRobotTurn(true);
		setAdjustRadarForGunTurn(true);
		setTurnRadarRight(1000);
		// 开场逻辑
		while (getTime() < 10) {// 开场扫描一圈
			while (getTime() == preTick)
				;// 程序锁死直到下一tick到来
			setScan();
			preTick = getTime();
			execute();
		}
		// 战斗逻辑，还有敌人
		while (cooperate.getEnemyRest() > 0) {
			while (getTime() == preTick)
				;// 程序锁死直到下一tick到来
			preTick = getTime();
			setScan();
			if (hiding == 0) {// 如果不在躲避状态，进行普通的移动
				setMove();
			} else {
				if (hiding > 0)
					hiding--;
			}
			avoidWall();// 对移动状态进行避墙调整
			setFire();
			execute();
		}

		// 敌人死光，自相残杀，刷击中分
		cooperate.teammates = null;
		while (true) {
			while (getTime() == preTick)
				;// 程序锁死直到下一tick到来
			preTick = getTime();
			if (getTime() % 10 == 0) {
				setBodyColor(randomColor());
				setBulletColor(randomColor());
			}
			setScan();
			setFire();
			execute();
		}

	}

	public Color randomColor() {// 没啥用的函数，改颜色
		Color[] color = { Color.gray, Color.black, Color.white, Color.blue, Color.red, Color.yellow, Color.green,
				Color.pink, Color.orange, Color.magenta, Color.cyan };// 准备用来随机
		return color[(int) (Math.random() * 10000) % color.length];
	}

	public void setScan() {
		if (noOnScanTime > 3) {// 如果出现了足够大的空裆没有扫描到敌人，重置扫描状态，确保扫描角度是最优的
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
		if (changeDirection > 0)// 如果切换方向计数中
			changeDirection--;
		else {// 切换方向计数到达，切换方向
			changeDirection = 4 + (int) ((battleMap.aimingTarget.getDistance() / 30 + (Math.random() * 30)));
			direction = -direction;
		}
		if (!cooperate.ifReachPlace()) {// 开场强行把三个机器人分到三个角落，抢占交叉火力点
			NextMoveInfo nextMoveInfo = battleMap.calcuNextGravityMove(cooperate.cornerForce, 1);
			setTurnRight(nextMoveInfo.getBearing());
			setAhead(nextMoveInfo.getDistance());
		} else {// 使用反重力法加外加外力合理推动的运动
			Force force = new Force();

			RobotInfo target;
			Enumeration<RobotInfo> enumeration = battleMap.enemyList.elements();
			while (enumeration.hasMoreElements()) {// 考虑切向力
				target = (RobotInfo) enumeration.nextElement();
				if (cooperate.isTeammate(target.getName()))
					continue;

				double bearing = target.getBearing();

				double turnTagency;
				if (bearing >= 0)
					turnTagency = bearing - 90;
				else
					turnTagency = bearing + 90;

				double headingRadius = Math.toRadians(turnTagency + getHeading());

				force.xForce += +Math.sin(headingRadius) * 1000 / target.getDistance() * direction * Math.random();
				force.yForce += +Math.cos(headingRadius) * 1000 / target.getDistance() * direction * Math.random();
			}
			// 坦克倾向于往合力的方向运动
			NextMoveInfo nextMoveInfo = battleMap.calcuNextGravityMove(force, direction);
			setTurnRight(nextMoveInfo.getBearing());
			setAhead(nextMoveInfo.getDistance());
		}
	}

	public void avoidWall() {// 根据当前坦克状态选择避墙，逻辑有点难受，有空要重写
		double avoidDist = 100;
		double moveDirect = BattleMap.normalizeAngle(getHeading() - 90 + 90 * Math.signum(getDistanceRemaining()));
		double remain = avoidDist;
		boolean ifAvoid = false;
		if (getY() > getBattleFieldHeight() - avoidDist) {// 在上边界
			if (moveDirect >= 0 && moveDirect < 90) {// 右转较好
				setTurnRight(90 - moveDirect);
			}
			if (moveDirect < 0 && moveDirect > -90) {
				setTurnRight(-90 - moveDirect);
			}
			if (getX() < avoidDist) {// 在左边界
				if (moveDirect > -45 && moveDirect < 90)
					setTurnRight(90 - moveDirect);// 右转
				else if (moveDirect < -45 && moveDirect > -180) {
					setTurnRight(-180 - moveDirect);// 左转
				}
			}
			if (getX() > getBattleFieldWidth() - avoidDist) {// 在右边界
				if (moveDirect > 45 && moveDirect < 180)
					setTurnRight(180 - moveDirect);// 右转
				else if (moveDirect < 45 && moveDirect > -90) {
					setTurnRight(-90 - moveDirect);// 左转
				}
			}
			remain = Math.min(getBattleFieldHeight() - getY(), remain);
			ifAvoid = true;
		} else if (getY() < avoidDist) {// 在下边界
			if (moveDirect > 90 && moveDirect < 180) {
				setTurnRight(90 - moveDirect);// 适合左转
			}
			if (moveDirect < -90 && moveDirect >= -180) {
				setTurnRight(-90 - moveDirect);
			}
			if (getX() < avoidDist) {// 在左边界
				if (moveDirect > -135 && moveDirect < 0)
					setTurnRight(0 - moveDirect);// 右转
				else if (moveDirect < -135 || moveDirect > 90) {
					if (moveDirect < 0)
						moveDirect += 360;
					setTurnRight(90 - moveDirect);// 左转
				}
			}
			if (getX() > getBattleFieldWidth() - avoidDist) {// 在右边界
				if (moveDirect > 135 || moveDirect < -90) {
					if (moveDirect < 0)
						moveDirect += 360;
					setTurnRight(270 - moveDirect);// 右转
				} else if (moveDirect < 135 && moveDirect > 0)
					setTurnRight(0 - moveDirect);// 左转
			}
			remain = Math.min(getY(), remain);
			ifAvoid = true;
		} else if (getX() < avoidDist) {// 在左边界且不在上下边界
			if (moveDirect >= -90 && moveDirect < 0) {// 建议右转
				setTurnRight(0 - moveDirect);// 右转
			}
			if (moveDirect < -90 && moveDirect > -180) {// 建议左转
				setTurnRight(-180 - moveDirect);// 左转
			}
			remain = Math.min(getX(), remain);
			ifAvoid = true;
		} else if (getX() > getBattleFieldWidth() - avoidDist) {// 在右边界且不在上下边界
			if (moveDirect < 180 && moveDirect > 90) {// 建议右转
				setTurnRight(180 - moveDirect);
			}
			if (moveDirect > 0 && moveDirect <= 90) {// 建议左转
				setTurnRight(0 - moveDirect);// 左转
			}
			remain = Math.min(getBattleFieldWidth() - getX(), remain);
			ifAvoid = true;
		}

		// 如果太靠近墙而且入墙角度过于垂直，认为继续往前走会撞，减速
		if (ifAvoid && remain < avoidDist && Math.abs(getTurnRemaining()) > 45) {
			setMaxVelocity(6);
		} else if (ifAvoid && remain < avoidDist / 2 && Math.abs(getTurnRemaining()) > 30) {
			setMaxVelocity(4);
		} else
			setMaxVelocity(8);
		if (ifAvoid)// 设置近墙关键字
			ifNearWall = true;
		else
			ifNearWall = false;
	}

	public void setFire() {
		// 使用模式匹配法开火
		NextAimInfo nextAimInfo = battleMap.calcuNextGunBearing();
		if (nextAimInfo != null) {
			if (aimingTime < 20) {// 如果瞄准一个目标时间较短，继续跟踪其
				aimingTime++;
				setTurnGunRight(nextAimInfo.getBearing());
				if (nextAimInfo.getIfCanFire()) {
					if (cooperate.getEnemyRest() > 0)// 如果还有敌人，正常打
						setFire(nextAimInfo.getPower());
					else {// 如果敌人死光了，互相开炮刷分
						setFire(battleMap.aimingTarget.getEnergy() / 100);// 刷分
					}
				}
			} else {// 如果瞄准一个目标时间足够长，尝试改变目标
				aimingTime = 0;
				battleMap.calcuBestTarget();
			}
		} else {// 如果没有目标，将无法得到瞄准信息，故尝试获取到一个
			battleMap.calcuBestTarget();
		}
	}

	// 事件触发
	/**
	 * 雷达扫描到一个敌人，可以获取到敌人的信息
	 */
	public void onScannedRobot(ScannedRobotEvent e) {
		noOnScanTime = 0;// 告诉雷达控制器你刚扫到了一个目标
		if (!thisTurnScanList.containsKey(e.getName())) {// 告诉雷达控制器你这一轮扫了几个人了，是否应当换向
			thisTurnScanRobotCount++;
			thisTurnScanList.put(e.getName(), true);
		}
		battleMap.setEnemyInfo(e);// 在地图上标记目标
	}

	/**
	 * 敌人死亡
	 */
	public void onRobotDeath(RobotDeathEvent event) {
		battleMap.removeEnemyFromMap(event);
		if (event.getName() == battleMap.aimingTarget.getName()) {
			aimingTime = 0;// 刷新瞄准
		}
		if (cooperate.isTeammate(event.getName()))
			cooperate.teammateRest--;// 如果是队友，减少队友
	}

	/**
	 * 撞击到敌人，可以获取到如bearing（敌人方位）等信息
	 */
	public void onHitRobot(HitRobotEvent event) {
		hiding = 10;
		int hitDirect = Math.abs(event.getBearing()) < 90 ? 1 : -1;
		setAhead(-100 * hitDirect);// 往撞击方向的反方向后退
		System.out.println("hitrobot!:" + event.getName());
	}

	public void onHitWall(HitWallEvent event) {
		hiding = 10;
		int hitDirect = Math.abs(event.getBearing()) < 90 ? 1 : -1;
		setAhead(-100 * hitDirect);// 往撞击方向的反方向后退
		System.out.println("hitwall!");
	}

	/**
	 * 被子弹击中，可以获得如子弹射过来的方向
	 */
	public void onHitByBullet(HitByBulletEvent event) {
		// 如果是队友打的，计数统计用
		if (cooperate.isTeammate(event.getName()))
			teamFireCount++;
		// 如果正处于躲避状态（防止反复躲避），或者敌人死光了，就不躲避子弹
		if (cooperate.getEnemyRest() <= 0 || hiding != 0)
			return;
		// 如果距离很近，而且是敌人，强制攻击
		if (!cooperate.isTeammate(event.getName()) && battleMap.getRobot(event.getName()).getDistance() < 100) {
			aimingTime = 20;
			battleMap.aimingTarget = battleMap.getRobot(event.getName());
		}

		if (!ifNearWall) {// 如果不靠近墙，使用一般躲避规则
			if (!cooperate.isTeammate(event.getName())) {// 如果不是队友，则切向左右躲避
				double bearing = event.getBearing();
				double turnTagency;
				if (bearing >= 0)
					turnTagency = bearing - 90;
				else
					turnTagency = bearing + 90;
				hiding = 10;
				setTurnRight(BattleMap.normalizeAngle(turnTagency));
				setAhead(200 * direction);
			} else {// 被队友打中则往远离队友炮弹击中的方向躲避
				direction = Math.abs(event.getBearing()) < 90 ? 1 : -1;
				double bearing = event.getBearing();
				double turnTagency;
				if (bearing >= 0)
					turnTagency = bearing - 90;
				else
					turnTagency = bearing + 90;
				hiding = 10;
				setAhead(200 * direction);
				setTurnRight(BattleMap.normalizeAngle(turnTagency));
			}
		}
	}

	// ------------------------------------------------------------------------------------
	public void onDeath(DeathEvent event) {
		System.out.println("teamFire:" + teamFireCount);
	}

	/**
	 * 如果能胜利的话。。执行这一段装逼用
	 */
	public void onWin(WinEvent e) {
		setBodyColor(randomColor());
		setGunColor(randomColor());
		setRadarColor(randomColor());
		for (int i = 0; i < 50; i++) {
			turnGunRightRadians(Math.PI * 3 / 4);
			turnGunLeftRadians(Math.PI * 3 / 4);
		}
	}
}
