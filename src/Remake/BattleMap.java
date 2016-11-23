package Remake;

import java.util.Enumeration;
import java.util.Hashtable;

import robocode.AdvancedRobot;
import robocode.RobotDeathEvent;
import robocode.ScannedRobotEvent;
import robocode.Rules;

/**
 * 地图类，用于保存所有敌人以及你自己的位置信息
 */
public class BattleMap {
	AdvancedRobot battle;// battle包含你自己的实时信息以及战场信息
	RobotInfo aimingTarget = new RobotInfo();

	BattleMap(AdvancedRobot battle) {
		this.battle = battle;
	}

	Hashtable<String, RobotInfo> enemyList = new Hashtable<String, RobotInfo>();

	// 在每次计算完下一tick的行动之后，将行动数据存于这两个变量并返回，其中计算nextAimInfo时需要传入nextMoveInfo进行综合
	NextMoveInfo nextMoveInfo = new NextMoveInfo();
	NextAimInfo nextAimInfo = new NextAimInfo();

	/**
	 * 设置敌人的信息，注意！扫描敌人有时效性！当剩下一个敌人时，应该跟踪！
	 */
	public void setEnemyInfo(ScannedRobotEvent e) {
		RobotInfo enemy;
		boolean isNew = false;
		if (enemyList.containsKey(e.getName())) {
			enemy = (RobotInfo) enemyList.get(e.getName());
		} else {
			enemy = new RobotInfo();
			enemyList.put(e.getName(), enemy);
			enemy.setName(e.getName());
			isNew = true;
		}
		double diffHeading = normalizeAngle(e.getHeading() - enemy.getHeading());// 上次记录到现在偏转的朝向差
		double diffScanTime = e.getTime() - enemy.getLastScanTime();
		enemy.setBearing(e.getBearing()); // 相对于你车体的朝向
		enemy.setDistance(e.getDistance());// 扫描扫的是最近距离，需要额外加上机器人的大小
		enemy.setHeading(e.getHeading());
		enemy.setVelocity(e.getVelocity());
		enemy.setLastScanTime(e.getTime());
		enemy.setEnergy(e.getEnergy());

		double absoluteRadius = Math.toRadians(normalizeAngle(enemy.getBearing() + battle.getHeading()));
		// 此处应当根据你的位置更新敌人的方位;
		double newX = battle.getX() + enemy.getDistance() * Math.sin(absoluteRadius);
		double newY = battle.getY() + enemy.getDistance() * Math.cos(absoluteRadius);
		double diffDistance = Math
				.sqrt((newX - enemy.getX()) * (newX - enemy.getX()) + (newY - enemy.getY()) * (newY - enemy.getY()));
		enemy.setX(newX);
		enemy.setY(newY);
		if (!isNew) {
			enemy.recordMatcher(diffDistance, diffHeading, diffScanTime);
		}
	}

	/**
	 * 若敌人死亡，从地图上移除
	 */
	public void removeEnemyFromMap(RobotDeathEvent event) {
		String robotName = event.getName();
		enemyList.remove(robotName);
	}

	/**
	 * 雷达追踪模式
	 */
	public double trackCurrent(ScannedRobotEvent e) {
		double RadarOffset;
		double absoluteBearing = normalizeAngle(e.getBearing() + battle.getHeading());
		// System.out.println(absoluteBearing);
		RadarOffset = normalizeAngle(absoluteBearing - battle.getRadarHeading());
		return RadarOffset;
	}

	/**
	 * 根据地图的情况返回你的下一步炮管运动
	 */
	public NextAimInfo calcuNextGunBearing() {
		if (aimingTarget.getName() == "")
			return nextAimInfo;
		// ------------------------------确定下一帧炮管瞄准的方向---------------------------------------
		double nextFirePower = Math.min(Math.min(1000 / aimingTarget.getDistance(), aimingTarget.getEnergy() / 3),
				battle.getEnergy() / 5);// 综合考虑自己剩余能量、敌人距离、敌人剩余能量

		nextAimInfo.setPower(nextFirePower);
		double nextGunTurn = predictAim(aimingTarget, Rules.getBulletSpeed(nextFirePower));// 炮管与预测开火方向的角度，需要考虑车体的运动，这样下一帧才能对准敌人
		nextAimInfo.setBearing(nextGunTurn);// 经过校准后下一帧可以准确对准敌人

		// --------------------------------确定当前帧是否开火-----------------------------------------
		// 当前时刻敌人相对于你的绝对方向
		double lenX = aimingTarget.predictX - battle.getX();
		double lenY = aimingTarget.predictY - battle.getY();
		double enemyBearding = getAngle(lenY, lenX);
		// 当前时刻炮管与敌人位置的误差（单位像素）
		double angleErrorRange = Math.abs(normalizeAngle(enemyBearding - battle.getGunHeading()));
		// 若当前炮管已经对准敌人（像素误差足够小）下一帧将射击（车体是36像素，一半就是18像素，若误差在18以内，必中固定靶）
		if (Math.sin(Math.toRadians(angleErrorRange)) * aimingTarget.getDistance() < 20 && battle.getGunHeat() == 0) {
			if (aimingTarget.getDistance() < 800 || aimingTarget.getEnergy() == 0) {
				nextAimInfo.setIfCanFire(true);
			}
		} else {
			nextAimInfo.setIfCanFire(false);
		}
		return nextAimInfo;
	}

	/**
	 * 预测开火方向
	 */
	private double predictAim(RobotInfo target, double bulletSpeed) {
		target.predict(battle, bulletSpeed);// 执行该步后target内更新predictX和predictY
		// System.out.println("X:" + target.predictX + " Y:" + target.predictY);
		// 下一帧车体运动位移
		double diffX = Math.sin(Math.toRadians(battle.getHeading())) * battle.getVelocity();
		double diffY = Math.cos(Math.toRadians(battle.getHeading())) * battle.getVelocity();
		// 计算坦克的角度，需要根据车身的运动对炮管瞄准进行补偿，减少由于车子的运动造成误差，确保如果下一帧能瞄准敌人
		double lenX = target.predictX - (battle.getX() + diffX);
		double lenY = target.predictY - (battle.getY() + diffY);

		double enemyBearding = getAngle(lenY, lenX);
		// 计算并规范化瞄准方向，大于180度规范化后将逆时针转，确保对准时间最短
		double nextGunTurn = normalizeAngle(enemyBearding - battle.getGunHeading());
		return nextGunTurn;
	}

	/**
	 * calcuNextGunBearing专用，返回一个最适合打击的目标
	 */
	public void calcuBestTarget() {
		RobotInfo target;
		Enumeration<RobotInfo> enumeration = enemyList.elements();
		double lost = 99999;
		RobotInfo best = new RobotInfo();
		while (enumeration.hasMoreElements()) {
			target = (RobotInfo) enumeration.nextElement();
			// 代价函数=炮口角度差/20+距离/20+5*abs(sin(敌人朝向-敌人方位))+abs(敌人速度)/加速度
			// 当前时刻敌人相对于你的绝对方向
			double lenX = target.getX() - battle.getX();
			double lenY = target.getY() - battle.getY();
			double enemyBearding = getAngle(lenY, lenX);
			// 当前时刻敌人朝向-敌人方位
			double beardingErrorRadius = Math.toRadians(target.getHeading() - enemyBearding);
			double gunError = Math.abs(normalizeAngle(enemyBearding - battle.getGunHeading()));

			target.setAimPrice((target.getDistance() + gunError) / 20 + 5 * Math.sin(beardingErrorRadius)
					+ Math.abs(target.getVelocity()));
			if (target.getAimPrice() < lost) {
				best = target;
				lost = target.getAimPrice();
			}
		}
		aimingTarget = best;
	}

	/**
	 * 根据地图的情况返回你的下一步车体运动，在靠近敌人时运动缓慢，应当改进（运动缓慢是为了防止撞墙）
	 */
	public NextMoveInfo calcuNextGravityMove(Force outsideForce) {
		double xforce = 0;
		double yforce = 0;
		Force force;
		RobotInfo enemy;
		Enumeration<RobotInfo> enumeration = enemyList.elements();
		// 计算敌人的合力
		while (enumeration.hasMoreElements()) {
			enemy = (RobotInfo) enumeration.nextElement();
			GravityPoint point = new GravityPoint(enemy.getX(), enemy.getY(), -20000);
			force = point.calcuPointForce(battle.getX(), battle.getY());
			xforce += force.xForce;
			yforce += force.yForce;
		}

		// 计算墙的作用力
		GravityPoint[] pointList = new GravityPoint[5];
		pointList[0] = new GravityPoint(0, battle.getY(), -10000);
		pointList[1] = new GravityPoint(battle.getBattleFieldWidth(), battle.getY(), -10000);
		pointList[2] = new GravityPoint(battle.getX(), 0, -10000);
		pointList[3] = new GravityPoint(battle.getX(), battle.getBattleFieldHeight(), -10000);
		pointList[4] = new GravityPoint(battle.getBattleFieldWidth() / 2, battle.getBattleFieldHeight() / 2,
				-Math.random() * 20000);// 中点随机0～20000
		for (int i = 0; i < 5; i++) {
			force = pointList[i].calcuPointForce(battle.getX(), battle.getY());
			xforce += force.xForce;
			yforce += force.yForce;
		}
		xforce += outsideForce.xForce;
		yforce += outsideForce.yForce;
		double forceDirection = normalizeAngle(getAngle(yforce, xforce));
		// System.out.println("y" + yforce);
		// System.out.println("x" + xforce);
		// 计算出了X和Y的合力方向，注意这里y正方向为0
		// 没有有效地使用

		// angle是你车子应当旋转的角度，经过normalize后换算成你车身应当运动的方向及角度
		double angle = normalizeAngle(forceDirection - battle.getHeading());
		double power = Math.sqrt(xforce * xforce + yforce * yforce);
		if (power > 4 && Math.abs(angle) > 90) {
			nextMoveInfo.setDistance(4);
			nextMoveInfo.setBearing(angle);
		} else if (power > 2 && Math.abs(angle) > 70) {
			nextMoveInfo.setDistance(8);
			nextMoveInfo.setBearing(angle);
		} else if (power > 1 && Math.abs(angle) > 50) {
			nextMoveInfo.setDistance(12);
			nextMoveInfo.setBearing(angle);
		} else if (power > 0.5 && Math.abs(angle) > 30) {
			nextMoveInfo.setDistance(16);
			nextMoveInfo.setBearing(angle);
		} else {
			nextMoveInfo.setDistance(20);
			nextMoveInfo.setBearing(angle);
		}
		return nextMoveInfo;
	}

	/**
	 * 输入-360~359区间的角度值，返回-180到179的规范化角度（方便炮管或车体转向）
	 */
	private static double normalizeAngle(double angle) {
		if (angle < -180)
			angle += 360;
		if (angle >= 180)
			angle -= 360;
		return angle;
	}

	/**
	 * 输入x距离和y距离，返回方向角（x正方向为0）（-180~179）
	 */
	private static double getAngle(double lenX, double lenY) {
		double angler = Math.atan(lenY / lenX) * 180 / Math.PI;
		// 特殊情况
		if (lenX == 0 && lenY > 0)
			return 90;
		if (lenX == 0 && lenY < 0)
			return -90;
		if (lenX > 0 && lenY == 0)
			return 0;
		if (lenX < 0 && lenY == 0)
			return -180;
		if (lenX == 0 && lenY == 0)// 出错情况，x=y=0
			return 0;
		// 一般情况
		if (lenX < 0 && lenY > 0)// 第二象限
			return angler + 180;
		if (lenX < 0 && lenY < 0)// 第三象限
			return angler - 180;
		return angler;
	}
}