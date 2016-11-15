package test;

import java.awt.geom.Point2D;

import javax.net.ssl.SSLEngineResult.Status;

import robocode.AdvancedRobot;

/**
 * ��map����ʹ�ã����ڱ�������˵ķ�λ����Ϣ
 * 
 * @author baiji
 *
 */
public class RobotInfo {
	private String name;// ����������
	private double heading;// �����˵ĳ���
	private double bearing;// �������������ĳ��򣨽����������ǵ���ʱ��
	private double distance;// ����������ľ���
	private double locationX;// �������ڵ�ͼ�ϵľ�������
	private double locationY;
	private double changehead;//ת��̨
	private double speed;//��ȡ�ٶ�
	private boolean isAlive;//����Ƿ���
	private long ctime;//ɨ��ʱ�̵�ʱ��
	private int selfFlag;//������1-���� 0-����
	private double fifo;//���ȼ������з���Ҫ
	//private double m=10;//����
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
