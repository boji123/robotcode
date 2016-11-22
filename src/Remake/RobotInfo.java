package Remake;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import robocode.AdvancedRobot;
import robocode.Rules;

/**
 * ��map����ʹ�ã����ڱ�������˵ķ�λ����Ϣ
 */
public class RobotInfo {
	private String name = "";// ����������
	private double heading = 0;// �����˵ĳ���
	private double velocity = 0;// �����˵��ٶ�
	private double bearing = 0;// �������������ĳ���
	private double distance = 0;// ����������ľ���
	private double locationX = 0;// �������ڵ�ͼ�ϵľ�������
	private double locationY = 0;
	private double energy = 0;
	private long lastScanTime = 0;// ɨ��ʱ�̵�ʱ��
	private double aimPrice = 99999;// �����Ŀ��Ĵ�����û����ǰ����Ϊ����
	// ---------------------------------------------------------------------------------------
	public double predictX = 0;
	public double predictY = 0;
	private int MAX_PATTERN_LENGTH = 30;
	private String history = "";
	private Map<String, int[]> matcher = new HashMap<String, int[]>(40000);// static
	private List<Point2D.Double> predictions = new ArrayList<Point2D.Double>();

	public void recordMatcher(double diffDistance, double diffHeading, double diffScanTime) {
		// ��������ɨ��ʱ����ʱ���������ʱ������ƶ�����һ��Բ�������ÿһ���˶����в���
		double stepCount = diffScanTime;
		double stepMove;
		if (diffHeading != 0) {
			double R = (diffDistance / 2) / Math.sin(Math.toRadians(Math.abs(diffHeading) / 2));
			double circleLen = R * Math.abs(diffHeading) / 180 * Math.PI;
			stepMove = circleLen / stepCount;
		} else {
			stepMove = diffDistance / stepCount;
		}
		double stepTurn = diffHeading / stepCount;

		int thisStep = encode(stepTurn, stepMove);
		System.out.println("stepMove:" + stepMove);
		System.out.println("stepTurn:" + stepTurn);
		System.out.println("thisStep:" + thisStep);

		for (int i = 0; i < stepCount; i++) {
			record(thisStep);
			history = (char) thisStep + history;
		}
	}

	public void predictLocation(AdvancedRobot me, double bulletSpeed) {
		predictions.clear();
		Point2D.Double myP = new Point2D.Double(me.getX(), me.getY());
		Point2D.Double enemyP = new Point2D.Double(locationX, locationY);
		String pattern = history;
		System.out.println("eX:" + locationX + " eY:" + locationY);
		double nextHeading = heading;
		for (double d = 0; d < myP.distance(enemyP); d += bulletSpeed) {
			int nextStep = predict(pattern);
			nextHeading = nextHeading + nextStep / 17 - 10;
			enemyP = project(enemyP, Math.toRadians(nextHeading), nextStep % 17 - 8);
			predictions.add(enemyP);
			pattern = (char) nextStep + pattern;
		}
		System.out.println(predictions);
		predictX = enemyP.getX();
		predictY = enemyP.getY();
	}

	public int predict(String pattern) {
		int[] frequencies = null;
		for (int patternLength = Math.min(pattern.length(), MAX_PATTERN_LENGTH); frequencies == null; --patternLength) {
			frequencies = matcher.get(pattern.substring(0, patternLength));
		}
		int nextTick = 0;
		for (int i = 1; i < frequencies.length; ++i) {
			if (frequencies[nextTick] < frequencies[i]) {
				nextTick = i;
			}
		}
		return nextTick;
	}

	private void record(int thisStep) {
		int maxLength = Math.min(MAX_PATTERN_LENGTH, history.length());
		for (int i = 0; i <= maxLength; ++i) {
			String pattern = history.substring(0, i);
			int[] frequencies = matcher.get(pattern);
			if (frequencies == null) {
				frequencies = new int[21 * 17];
				matcher.put(pattern, frequencies);
			}
			++frequencies[thisStep];
		}

	}

	public static int encode(double dh, double v) {// ������׼
		if (Math.abs(dh) > Rules.MAX_TURN_RATE) {
			return (char) -1;
		}
		// ȡ��
		// -10<toDegrees(dh)<10 ; -8<v<8 ;
		int dhCode = (int) Math.rint(dh) + 10;
		int vCode = (int) Math.rint(v + 8);
		return (char) (17 * dhCode + vCode);
	}

	private static Point2D.Double project(Point2D.Double p, double angle, double distance) {
		double x = p.x + distance * Math.sin(angle);
		double y = p.y + distance * Math.cos(angle);
		return new Point2D.Double(x, y);
	}
	// --------------------------------------------------------------------------------------

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

	public long getLastScanTime() {
		return lastScanTime;
	}

	public void setLastScanTime(long lastScanTime) {
		this.lastScanTime = lastScanTime;
	}

	public double getAimPrice() {
		return aimPrice;
	}

	public void setAimPrice(double aimPrice) {
		this.aimPrice = aimPrice;
	}
}
