package avoidmove_and_selecttarget;

/**
 * 下一步瞄准信息的数据结构
 */
public class NextAimInfo {
	private boolean ifCanFire;
	private double bearing;

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

}
