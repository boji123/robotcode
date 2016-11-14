package test;

import java.awt.geom.Point2D;

/**
 * ��map����ʹ�ã����ڱ�������˵ķ�λ����Ϣ
 * 
 * @author baiji
 *
 */
public class RobotInfo {
	private String name;// ����������
	private double heading;// �����˵ĳ���
	private double velocity;// �����˵��ٶ�
	private double bearing;// �������������ĳ��򣨽����������ǵ���ʱ��
	private double distance;// ����������ľ���
	private double locationX;// �������ڵ�ͼ�ϵľ�������
	private double locationY;
	private double speed;//��ȡ�ٶ�
	private boolean isAlive;//����Ƿ���
	private long ctime;//ɨ��ʱ�̵�ʱ��
	private int selfFlag;//������1-���� 0-����
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
