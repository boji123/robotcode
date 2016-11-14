package test;

/**
 * 数据结构，下一步移动的量，包括方向与距离
 * 
 * @author baiji
 *
 */
public class NextMoveInfo {
	private double distance;// 移动的距离
	private double bearing;// 转弯的朝向
	
	public double getDistance() {
		return distance;
	}
	public void setDistance(double distance) {
		this.distance = distance;
	}
	public double getBearing() {
		return bearing;
	}
	public void setBearing(double bearing) {
		this.bearing = bearing;
	}
	
}
