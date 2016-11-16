package avoidmove_and_selecttarget;

import java.util.Enumeration;
import java.util.Hashtable;
import robocode.AdvancedRobot;
import robocode.RobotDeathEvent;
import robocode.ScannedRobotEvent;

/**
 * 地图类，用于保存所有敌人以及你自己的位置信息 留坑待处理，只支持一个敌人，需要根据需要改进成支持多个敌人
 */
public class BattleMap {
	AdvancedRobot battle;// battleRule包含你自己的实时信息以及战场信息

	BattleMap(AdvancedRobot battle) {
		this.battle = battle;
	}

	Hashtable<String, RobotInfo> enemyList = new Hashtable<String, RobotInfo>();

	// 在每次计算完下一tick的行动之后，将行动数据存于这两个变量并返回，其中计算nextAimInfo时需要传入nextMoveInfo进行综合
	NextMoveInfo nextMoveInfo = new NextMoveInfo();
	NextAimInfo nextAimInfo = new NextAimInfo();

	/**
	 * 设置敌人的信息，注意！扫描敌人有时效性！
	 */
	public void setEnemyInfo(ScannedRobotEvent e) {
		RobotInfo enemy;
		if (enemyList.containsKey(e.getName())) {
			enemy = (RobotInfo) enemyList.get(e.getName());
		} else {
			enemy = new RobotInfo();
			enemyList.put(e.getName(), enemy);
		}
		enemy.setName(e.getName());
		enemy.setBearing(e.getBearing()); // 相对于你车体的朝向
		enemy.setDistance(e.getDistance());// 扫描扫的是最近距离，需要额外加上机器人的大小
		enemy.setHeading(e.getHeading());
		enemy.setVelocity(e.getVelocity());

		double absoluteRadius = Math.toRadians(normalizeAngle(enemy.getBearing() + battle.getHeading()));
		// 此处应当根据你的位置更新敌人的方位;
		enemy.setLocationX(battle.getX() + enemy.getDistance() * Math.sin(absoluteRadius));
		enemy.setLocationY(battle.getY() + enemy.getDistance() * Math.cos(absoluteRadius));
		// System.out.println(enemy.getLocationX());
		// System.out.println(enemy.getLocationY());
	}

	/**
	 * 若敌人死亡，从地图上移除
	 */
	public void removeEnemyFromMap(RobotDeathEvent event) {
		String robotName = event.getName();
		enemyList.remove(robotName);
	}

	/**
	 * 根据地图的情况返回你的下一步炮管运动
	 */
	public NextAimInfo calcuNextGunBearing() {
		RobotInfo target = calcuBestTarget();
		// System.out.println("target:" + target.getName());
		double nextGunTurn = predictAim(target);// 炮管与预测开火方向的角度
		// 这里有个问题，车体运动会影响炮管运动，影响很小，如需要提高精度需要解除运动对瞄准的影响，留坑
		// nextGunTurn = adjustAim(nextGunTurn);
		nextAimInfo.setBearing(nextGunTurn);// 经过校准后下一帧可以准确对准敌人

		// 当前敌人相对于你的绝对方向（与你车体朝向无关）
		double lenX = target.getLocationX() - battle.getX();
		double lenY = target.getLocationY() - battle.getY();
		double enemyBearding = getAngle(lenY, lenX);
		// 炮管与敌人位置的误差（单位像素）
		double angleErrorRange = Math.abs(normalizeAngle(enemyBearding - battle.getGunHeading()));
		// 若当前炮管已经对准敌人（误差足够小）下一帧将射击（车体是36像素，一半就是18像素）
		if (Math.sin(Math.toRadians(angleErrorRange)) * target.getDistance() < 10) {
			System.out.println("diff:" + Math.sin(Math.toRadians(angleErrorRange)) * target.getDistance());
			nextAimInfo.setIfCanFire(true);
		} else {
			nextAimInfo.setIfCanFire(false);
		}
		return nextAimInfo;
	}

	/**
	 * 预测开火方向，目前是假设敌人不运动而你自己运动
	 */
	private double predictAim(RobotInfo target) {
		double lenX = target.getLocationX() - battle.getX();
		double lenY = target.getLocationY() - battle.getY();
		double enemyBearding = getAngle(lenY, lenX);
		// 计算并规范化瞄准方向，这里没有考虑车身本身的运动
		double nextGunTurn = normalizeAngle(enemyBearding - battle.getGunHeading());
		return nextGunTurn;
	}

	/**
	 * 根据车身的运动对炮管瞄准进行补偿 确保如果下一帧能瞄准敌人时，不会由于车子的运动造成误差
	 */
	private double adjustAim(double nextGunTurn) {
		return nextGunTurn;
	}

	/**
	 * calcuNextGunBearing专用，返回一个最适合打击的目标
	 */
	private RobotInfo calcuBestTarget() {
		RobotInfo target;
		Enumeration<RobotInfo> enumeration = enemyList.elements();
		double lost = 2000000000;
		RobotInfo best = new RobotInfo();
		while (enumeration.hasMoreElements()) {
			// 此处应重写为选择最优目标
			target = (RobotInfo) enumeration.nextElement();
			if (target.getDistance() * Math.abs(target.getVelocity()) < lost) {
				best = target;
				lost = target.getDistance() * Math.abs(target.getVelocity());
			}
		}
		return best;
	}

	/**
	 * 根据地图的情况返回你的下一步车体运动
	 */
	public NextMoveInfo calcuNextMove() {
		double xforce = 0;
		double yforce = 0;
		Force force;
		RobotInfo enemy;
		Enumeration<RobotInfo> enumeration = enemyList.elements();
		// 计算敌人的合力
		while (enumeration.hasMoreElements()) {
			enemy = (RobotInfo) enumeration.nextElement();
			GravityPoint point = new GravityPoint(enemy.getLocationX(), enemy.getLocationY(), -20000);
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