package adAvoidMove;

public class GravityPoint {
	private double locationX = 0;
	private double locationY = 0;
	private double power = 0;

	public GravityPoint(double x, double y, double p) {
		locationX = x;
		locationY = y;
		power = p;
	}

	public Force calcuPointForce(double pointX, double pointY) {
		double lenX = locationX - pointX;
		double lenY = locationY - pointY;
		double range = Math.sqrt(lenX * lenX + lenY * lenY);
		double size = power / Math.pow(range, 2);
		Force force = new Force();
		force.xForce = size * (lenX / range);
		force.yForce = size * (lenY / range);
		return force;
	}
}
