package test;

/**
 * 在map类中使用，用于保存机器人的方位等信息
 */
public class RobotInfo {
	private String name = null;// 机器人名字
	private double heading = 0;// 机器人的朝向
	private double velocity = 0;// 机器人的速度
	private double bearing = 0;// 机器人相对于你的朝向（仅当机器人是敌人时）
	private double distance = 0;// 机器人离你的距离
	private double locationX = 0;// 机器人在地图上的绝对坐标
	private double locationY = 0;
	private double gunHeading = 0;// 机器人炮管朝向（仅当机器人是你自己时）
	private long scanTime = 0;// 扫描时刻的时间

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

	public double getGunHeading() {
		return gunHeading;
	}

	public void setGunHeading(double gunHeading) {
		this.gunHeading = gunHeading;
	}

	public long getScanTime() {
		return scanTime;
	}

	public void setScanTime(long scanTime) {
		this.scanTime = scanTime;
	}
}
