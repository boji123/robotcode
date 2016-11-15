package test;

import java.awt.geom.Point2D;

import javax.net.ssl.SSLEngineResult.Status;

import robocode.AdvancedRobot;

/**
 * 在map类中使用，用于保存机器人的方位等信息
 * 
 * @author baiji
 *
 */
public class RobotInfo {
	private String name;// 机器人名字
	private double heading;// 机器人的朝向
	private double bearing;// 机器人相对于你的朝向（仅当机器人是敌人时）
	private double distance;// 机器人离你的距离
	private double locationX;// 机器人在地图上的绝对坐标
	private double locationY;
	private double changehead;//转炮台
	private double speed;//获取速度
	private boolean isAlive;//标记是否存活
	private long ctime;//扫描时刻的时间
	private int selfFlag;//自身标记1-自身 0-敌人
	private double fifo;//优先级，仅敌方需要
	//private double m=10;//质量
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
	public double getFifo() {
		return fifo;
	}
	public void setFifo(double fifo) {
		this.fifo = fifo;
	}
	/*
	public double getM() {
		return m;
	}
	public void setM(double m) {
		this.m = m;
	}
	*/
	public double getChangehead() {
		return changehead;
	}
	public void setChangehead(double changehead) {
		this.changehead = changehead;
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
	public static double executeFunc(double d,double theta,double v){
		double result=d+2*theta+100*v;
		return result;
	}
	public void setFifo(AdvancedRobot me){
		double y=Math.abs(me.getY()-this.getLocationY());
		double x=Math.abs(me.getX()-this.getLocationX());
		double theta=FirstMove.normalizeBearing(me.getGunHeading()-Math.atan2(y, x));
		double ang=360-this.getHeading()-(90-Math.atan2(y, x));
		ang=Math.abs(ang);
		if(ang>180)
			ang-=180;
		double v=Math.abs(Math.cos(ang));
		this.executeFunc(this.getDistance(), Math.abs(theta), v);
	}
}
