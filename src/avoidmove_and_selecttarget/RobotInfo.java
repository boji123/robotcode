package avoidmove_and_selecttarget;

/**
 * ��map����ʹ�ã����ڱ�������˵ķ�λ����Ϣ
 */
public class RobotInfo {
	private String name = null;// ����������
	private double heading = 0;// �����˵ĳ���
	private double velocity = 0;// �����˵��ٶ�
	private double bearing = 0;// �������������ĳ���
	private double distance = 0;// ����������ľ���
	private double locationX = 0;// �������ڵ�ͼ�ϵľ�������
	private double locationY = 0;
	private long scanTime = 0;// ɨ��ʱ�̵�ʱ��

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

	public long getScanTime() {
		return scanTime;
	}

	public void setScanTime(long scanTime) {
		this.scanTime = scanTime;
	}
}
