package adAvoidMove;

import java.util.Enumeration;
import java.util.Hashtable;
import robocode.AdvancedRobot;
import robocode.RobotDeathEvent;
import robocode.ScannedRobotEvent;

/**
 * 地图类，用于保存所有敌人以及你自己的位置信息 留坑待处理，只支持一个敌人，需要根据需要改进成支持多个敌人
 */
public class BattleMap {
	AdvancedRobot battleRule;

	BattleMap(AdvancedRobot battleRule) {
		this.battleRule = battleRule;
	}

	RobotInfo yourself = new RobotInfo();
	Hashtable<String, RobotInfo> enemyList = new Hashtable<String, RobotInfo>();

	// 在每次计算完下一tick的行动之后，将行动数据存于这两个变量并返回，其中计算nextAimInfo时需要传入nextMoveInfo进行综合
	NextMoveInfo nextMoveInfo = new NextMoveInfo();
	NextAimInfo nextAimInfo = new NextAimInfo();

	/**
	 * 设置你自己在地图上的信息，该函数由主循环周期性调用
	 */
	public void setYourInfo(AdvancedRobot you) {
		yourself.setLocationX(you.getX());
		yourself.setLocationY(you.getY());
		yourself.setHeading(you.getHeading());
		yourself.setVelocity(you.getVelocity());
		yourself.setGunHeading(you.getGunHeading());
	}

	/**
	 * 设置敌人的信息，目前只支持一个敌人，应重写为支持多个敌人
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
		enemy.setDistance(e.getDistance());
		enemy.setHeading(e.getHeading());
		enemy.setVelocity(e.getVelocity());

		double absoluteRadius = normalizeBearing(enemy.getBearing() + yourself.getHeading()) / 180 * Math.PI;
		// 此处应当根据你自的位置更新敌人的方位;
		enemy.setLocationX(yourself.getLocationX() + enemy.getDistance() * Math.sin(absoluteRadius));
		enemy.setLocationY(yourself.getLocationY() + enemy.getDistance() * Math.cos(absoluteRadius));
	}

	/**
	 * 若敌人死亡，从地图上移除
	 */
	public void removeEnemyFromMap(RobotDeathEvent event) {
		String robotName = event.getName();
		enemyList.remove(robotName);
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
		double lenX = target.getLocationX() - yourself.getLocationX();
		double lenY = target.getLocationY() - yourself.getLocationY();
		double predictBearing = Math.atan(lenX / lenY) * 180 / Math.PI;
		if (lenX < 0 && lenY < 0) {
			predictBearing -= 180;
		} else if (lenX > 0 && lenY < 0) {
			predictBearing += 180;
		}
		// 计算并规范化瞄准方向，这里没有考虑车身本身的运动
		double nextGunTurn = normalizeBearing(predictBearing - yourself.getGunHeading());
		// 根据车身的运动对炮管瞄准进行补偿
		double nextTurn = nextMoveInfo.getBearing();
		double maxTurn = 10 - 0.75 * Math.abs(yourself.getVelocity());
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
		Enumeration<RobotInfo> enumeration = enemyList.elements();
		while (enumeration.hasMoreElements()) {
			// 此处应重写为选择最优目标
			target = (RobotInfo) enumeration.nextElement();
		}
		return target;
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
			force = point.calcuPointForce(yourself.getLocationX(), yourself.getLocationY());
			xforce += force.xForce;
			yforce += force.yForce;
		}

		// 计算墙的作用力
		GravityPoint[] pointList = new GravityPoint[5];
		pointList[0] = new GravityPoint(0, yourself.getLocationY(), -10000);
		pointList[1] = new GravityPoint(battleRule.getBattleFieldWidth(), yourself.getLocationY(), -10000);
		pointList[2] = new GravityPoint(yourself.getLocationX(), 0, -10000);
		pointList[3] = new GravityPoint(yourself.getLocationX(), battleRule.getBattleFieldHeight(), -10000);
		pointList[4] = new GravityPoint(battleRule.getBattleFieldWidth() / 2, battleRule.getBattleFieldHeight() / 2,
				Math.random() * 20000 - 10000);
		for (int i = 0; i < 5; i++) {
			force = pointList[i].calcuPointForce(yourself.getLocationX(), yourself.getLocationY());
			xforce += force.xForce;
			yforce += force.yForce;

		}
		double forceDirection = getDirection(xforce, yforce);
		// System.out.println(forceDirection);
		// 计算出了X和Y的合力方向，但是没有有效地使用

		double angle = normalizeBearing(forceDirection - yourself.getHeading());
		double power = Math.sqrt(xforce * xforce + yforce * yforce);
		if (power > 4 && Math.abs(angle) > 90) {
			nextMoveInfo.setDistance(4);// max 8 per tick
			nextMoveInfo.setBearing(angle);// max 10 per tick
		} else if (power > 2 && Math.abs(angle) > 70) {
			nextMoveInfo.setDistance(8);// max 8 per tick
			nextMoveInfo.setBearing(angle);// max 10 per tick
		} else if (power > 1 && Math.abs(angle) > 50) {
			nextMoveInfo.setDistance(12);// max 8 per tick
			nextMoveInfo.setBearing(angle);// max 10 per tick
		} else if (power > 0.5 && Math.abs(angle) > 30) {
			nextMoveInfo.setDistance(16);// max 8 per tick
			nextMoveInfo.setBearing(angle);// max 10 per tick
		} else {
			nextMoveInfo.setDistance(20);// max 8 per tick
			nextMoveInfo.setBearing(angle);// max 10 per tick
		}

		// System.out.println(power);
		return nextMoveInfo;
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

	/**
	 * 输入x距离和y距离，返回方向角
	 */
	private static double getDirection(double lenX, double lenY) {
		double angler;
		if (lenX == 0) {
			if (lenY > 0)
				angler = 0;
			else
				angler = -180;
		} else {
			angler = Math.atan(lenX / lenY) * 180 / Math.PI;
			if (lenX < 0 && lenY < 0) {
				angler -= 180;
			} else if (lenX > 0 && lenY < 0) {
				angler += 180;
			}
		}
		return angler;
	}
}