package test;

import java.awt.geom.Point2D;

/**
 * 在map类中使用，用于保存机器人的方位等信息
 * 
 * @author baiji
 *
 */
public class RobotInfo {
	private String name;// 机器人名字
	private double heading;// 机器人的朝向
	private double velocity;// 机器人的速度
	private double bearing;// 机器人相对于你的朝向（仅当机器人是敌人时）
	private double distance;// 机器人离你的距离
	private double locationX;// 机器人在地图上的绝对坐标
	private double locationY;
	private double speed;//获取速度
	private boolean isAlive;//标记是否存活
	private long ctime;//扫描时刻的时间
	private int selfFlag;//自身标记1-自身 0-敌人
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
	public double getVelocity() {
		return velocity;
	}
	public void setVelocity(double velocity) {
		this.velocity = velocity;
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
	public double getSpeed() {
		return speed;
	}
	public void setSpeed(double speed) {
		this.speed = speed;
	}
	public boolean isAlive() {
		return isAlive;
	}
	public void setAlive(boolean isAlive) {
		this.isAlive = isAlive;
	}
	public long getCtime() {
		return ctime;
	}
	public void setCtime(long ctime) {
		this.ctime = ctime;
	}
	public int getSelfFlag() {
		return selfFlag;
	}
	public void setSelfFlag(int selfFlag) {
		this.selfFlag = selfFlag;
	}
	public Point2D.Double guessEnemyPosition(long nowTime) {
		double diff,newX,newY;
		if(this.getSelfFlag()!=1){
			diff = nowTime - this.getCtime();
			newY = this.getLocationY() + Math.cos(this.getHeading()) * this.getSpeed() * diff;
			newX = this.getLocationX() + Math.cos(this.getHeading()) * this.getSpeed() * diff;
			return new Point2D.Double(newX, newY);
		}else{
			return null;
		}
	}
	
}
