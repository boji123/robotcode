package adPredict3;

import robocode.Rules;

/**
 * 在map类中使用，用于保存机器人的方位等信息
 */
public class RobotInfo {
	private String name = null;// 机器人名字
	private double heading = 0;// 机器人的朝向
	private double velocity = 0;// 机器人的速度
	private double bearing = 0;// 机器人相对于你的朝向
	private double distance = 0;// 机器人离你的距离
	private double locationX = 0;// 机器人在地图上的绝对坐标
	private double locationY = 0;
	private double energy = 0;
	private long scanTime = 0;// 扫描时刻的时间
	private double aimPrice = 99999;// 射击该目标的代价在没计算前设置为极大
	
	public int thisStep;

	// -----预测的攻击坐标-----
	public double preScanTime = 0;
	public double predictX = 0;
	public double predictY = 0;
	public double predictDistance = 0;
	public double averageVelocity = 0;
	// -------------------

	public static int encode(double dh, double v) {
		if (Math.abs(dh) > Rules.MAX_TURN_RATE_RADIANS) {
			return (char) -1;
		}
		// 取正
		// -10<toDegrees(dh)<10 ; -8<v<8 ;
		// so we add with 10 and 8
		int dhCode = (int) Math.rint(Math.toDegrees(dh)) + 10;
		int vCode = (int) Math.rint(v + 8);
		return (char) (17 * dhCode + vCode);
	}

	public void decode(int symbol) {
		heading += Math.toRadians(symbol / 17 - 10);
		velocity = symbol % 17 - 8;
	}
	
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

	public double getLocationX() {
		return locationX;
	}

	public void setLocationX(double locationX) {
		this.locationX = locationX;
	}

	public double getLocationY() {
		return locationY;
	}

	public void setLocationY(double locationY) {
		this.locationY = locationY;
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

	public long getScanTime() {
		return scanTime;
	}

	public void setScanTime(long scanTime) {
		this.scanTime = scanTime;
	}

	public double getAimPrice() {
		return aimPrice;
	}

	public void setAimPrice(double aimPrice) {
		this.aimPrice = aimPrice;
	}
}
