package budabuxiangshi3V3;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import robocode.AdvancedRobot;
import robocode.Rules;

/**
 * 在map类中使用，用于保存机器人的方位等信息
 */
public class RobotInfo {
	private String name = "";// 机器人名字
	private double heading = 0;// 机器人的朝向
	private double velocity = 0;// 机器人的速度
	private double bearing = 0;// 机器人相对于你的朝向
	private double distance = 0;// 机器人离你的距离
	private double X = 0;// 机器人在地图上的绝对坐标
	private double Y = 0;
	private double energy = 0;
	private long lastScanTime = 0;// 扫描时刻的时间
	private double aimPrice = 99999;// 射击该目标的代价在没计算前设置为极大
	// ---------------------------------------------------------------------------------------
	public double predictX = 0;
	public double predictY = 0;
	private int MAX_PATTERN_LENGTH = 30;// 模式匹配的最大匹配长度
	public String history = "";
	// 如果声明为static，上次匹配的结果将应用到下场战斗，效果更好，但可能导致内存爆炸
	private Hashtable<String, int[]> matcher = new Hashtable<String, int[]>(40000);
	private List<Point2D.Double> predictions = new ArrayList<Point2D.Double>();

	private boolean lock = false;

	// 模式匹配法历史记录
	public void recordMatcher(double diffDistance, double diffHeading, double diffScanTime) {
		while (lock == true)// 锁
			;
		lock = true;

		// 由于两次扫描时间有时间差，想象这段时间敌人移动的是一个圆弧，拆解每一步运动进行补间
		double stepCount = diffScanTime;
		// System.out.println("time:" + diffScanTime);
		double stepTurn = diffHeading / stepCount;
		double stepMove;
		// 重要！由于本机器人逻辑是雷达照顾全场而不是盯着一个人，因此两次雷达扫描之间有时间差，假设这段时间内敌人进行了某种匀速圆周运动，计算到每帧
		if (diffHeading != 0) {
			double R = (diffDistance / 2) / Math.sin(Math.toRadians(Math.abs(diffHeading) / 2));
			double circleLen = R * Math.abs(diffHeading) / 180 * Math.PI;
			stepMove = circleLen / stepCount * Math.signum(velocity);
		} else {
			stepMove = diffDistance / stepCount * Math.signum(velocity);
		}

		// 将每步的方向、位移进行编码，减少计算量
		int thisStep = encode(stepTurn, stepMove);

		for (int i = 0; i < stepCount; i++) {
			record(thisStep);
			history = (char) thisStep + history;
		}

		lock = false;
	}

	// 预测击中敌人的位置
	public void predict(AdvancedRobot me, double bulletSpeed) {
		while (lock == true)
			;
		lock = true;

		predictions.clear();
		Point2D.Double myP = new Point2D.Double(me.getX(), me.getY());
		Point2D.Double enemyP = new Point2D.Double(X, Y);
		String pattern = history;
		// System.out.println("eX:" + X + " eY:" + Y);
		double nextHeading = heading;
		// 炮弹开炮、敌人移动，当炮弹移动距离等于敌我距离时，击中敌人，预测结束
		for (double d = 0; d < myP.distance(enemyP); d += bulletSpeed) {
			int nextStep = predictOneStep(pattern);
			// 解码
			nextHeading = nextHeading + nextStep / 17 - 10;
			enemyP = project(enemyP, Math.toRadians(nextHeading), nextStep % 17 - 8);
			predictions.add(enemyP);
			// 将上次预测的结果作为下次预测的输入
			pattern = (char) nextStep + pattern;
		}
		predictX = enemyP.getX();
		predictY = enemyP.getY();
		if (predictX < 0 || predictX > me.getBattleFieldWidth() || predictY < 0
				|| predictY > me.getBattleFieldHeight()) {
			predictX = getX();
			predictY = getY();
		}

		lock = false;
	}

	// 根据当前历史记录（最长匹配的），得到下一时刻最有可能的运动情况
	private int predictOneStep(String pattern) {
		int[] frequencies = null;
		for (int patternLength = Math.min(pattern.length(), MAX_PATTERN_LENGTH); frequencies == null; --patternLength) {
			frequencies = matcher.get(pattern.substring(0, patternLength));
		}
		int nextTick = 0;
		for (int i = 1; i < frequencies.length; ++i) {
			if (frequencies[nextTick] < frequencies[i]) {
				nextTick = i;
			}
		}
		return nextTick;
	}

	private void record(int thisStep) {
		int maxLength = Math.min(MAX_PATTERN_LENGTH, history.length());
		// 将所有可能的新增的模式都加入到哈希表中，由于新加了一个历史记录，所以新增的模式必须包含这个历史记录
		for (int i = 0; i <= maxLength; ++i) {
			String pattern = history.substring(0, i);
			// 一个模式下当前时刻所有可能的运动
			int[] frequencies = matcher.get(pattern);
			if (frequencies == null) {
				frequencies = new int[21 * 17];
				matcher.put(pattern, frequencies);
			}
			// 提高当前时刻当前运动的频率
			++frequencies[thisStep];
		}

	}

	public static int encode(double dh, double v) {// 量化瞄准
		// 有小概率dh大于最大值（计算误差）导致报错,在此先归整
		if (Math.rint(Math.abs(dh)) > Rules.MAX_TURN_RATE) {
			return (char) -1;
		}
		// 取正，方便编码
		// -10<toDegrees(dh)<10 ; -8<v<8 ;
		int dhCode = (int) Math.rint(dh) + 10;
		int vCode = (int) Math.rint(v + 8);
		return (char) (17 * dhCode + vCode);
	}

	private static Point2D.Double project(Point2D.Double p, double angle, double distance) {
		double x = p.x + distance * Math.sin(angle);
		double y = p.y + distance * Math.cos(angle);
		return new Point2D.Double(x, y);
	}
	// --------------------------------------------------------------------------------------

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getHeading() {
		return heading;
	}

	public void setHeading(double heading) {
		this.heading = heading;
	}

	public double getBearing() {
		return bearing;
	}

	public void setBearing(double bearing) {
		this.bearing = bearing;
	}

	public double getDistance() {
		return distance;
	}

	public void setDistance(double distance) {
		this.distance = distance;
	}

	public double getX() {
		return X;
	}

	public void setX(double X) {
		this.X = X;
	}

	public double getY() {
		return Y;
	}

	public void setY(double Y) {
		this.Y = Y;
	}

	public double getVelocity() {
		return velocity;
	}

	public void setVelocity(double velocity) {
		this.velocity = velocity;
	}

	public double getEnergy() {
		return energy;
	}

	public void setEnergy(double energy) {
		this.energy = energy;
	}

	public long getLastScanTime() {
		return lastScanTime;
	}

	public void setLastScanTime(long lastScanTime) {
		this.lastScanTime = lastScanTime;
	}

	public double getAimPrice() {
		return aimPrice;
	}

	public void setAimPrice(double aimPrice) {
		this.aimPrice = aimPrice;
	}
}
