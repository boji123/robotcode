package budabuxiangshi3V3;

/**
 * ��һ����׼��Ϣ�����ݽṹ
 */
public class NextAimInfo {
	private boolean ifCanFire;
	private double bearing;
	private double power;

	public void setIfCanFire(boolean ifCanFire) {
		this.ifCanFire = ifCanFire;
	}

	public void setBearing(double bearing) {
		this.bearing = bearing;
	}

	public double getBearing() {
		return bearing;
	}

	public boolean getIfCanFire() {
		return ifCanFire;
	}

	public double getPower() {
		return power;
	}

	public void setPower(double power) {
		this.power = power;
	}
}
