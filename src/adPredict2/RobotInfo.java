package adPredict2;

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
	private double energy = 0;
	private long scanTime = 0;// ɨ��ʱ�̵�ʱ��
	private double aimPrice = 99999;// �����Ŀ��Ĵ�����û����ǰ����Ϊ����

	// -----Ԥ��Ĺ�������-----
	public double preScanTime = 0;
	public double predictX = 0;
	public double predictY = 0;
	public double predictDistance = 0;
	public double averageVelocity = 0;
	// -------------------

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
