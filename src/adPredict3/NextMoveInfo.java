package adPredict3;

/**
 * 下一步移动的量的数据结构，包括方向与距离
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
