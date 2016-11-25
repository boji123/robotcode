package adTeamRemake;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

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
	private double X = 0;// �������ڵ�ͼ�ϵľ�������
	private double Y = 0;
	private double energy = 0;
	private long lastScanTime = 0;// ɨ��ʱ�̵�ʱ��
	private double aimPrice = 99999;// �����Ŀ��Ĵ�����û����ǰ����Ϊ����
	// ---------------------------------------------------------------------------------------
	public double predictX = 0;
	public double predictY = 0;
	private int MAX_PATTERN_LENGTH = 30;
	public String history = "";
	private Hashtable<String, int[]> matcher = new Hashtable<String, int[]>(40000);// static
	private List<Point2D.Double> predictions = new ArrayList<Point2D.Double>();

	private boolean lock = false;

	public void recordMatcher(double diffDistance, double diffHeading, double diffScanTime) {
		while (lock == true)
			;
		lock = true;

		// ��������ɨ��ʱ����ʱ���������ʱ������ƶ�����һ��Բ�������ÿһ���˶����в���
		double stepCount = diffScanTime;
		// System.out.println("time:" + diffScanTime);
		double stepTurn = diffHeading / stepCount;
		double stepMove;
		if (diffHeading != 0) {
			double R = (diffDistance / 2) / Math.sin(Math.toRadians(Math.abs(diffHeading) / 2));
			double circleLen = R * Math.abs(diffHeading) / 180 * Math.PI;
			stepMove = circleLen / stepCount * Math.signum(velocity);
		} else {
			stepMove = diffDistance / stepCount * Math.signum(velocity);
		}
		// System.out.println("velo" + velocity);
		// System.out.println("move" + stepMove);

		int thisStep = encode(stepTurn, stepMove);
		// System.out.println("stepMove:" + stepMove);
		// System.out.println("stepTurn:" + stepTurn);
		// System.out.println("thisStep:" + thisStep);

		for (int i = 0; i < stepCount; i++) {
			record(thisStep);
			history = (char) thisStep + history;
		}

		lock = false;
	}

	public void predict(AdvancedRobot me, double bulletSpeed) {
		while (lock == true)
			;
		lock = true;

		predictions.clear();
		Point2D.Double myP = new Point2D.Double(me.getX(), me.getY());
		Point2D.Double enemyP = new Point2D.Double(X, Y);
		String pattern = history;
		// System.out.println("eX:" + X + " eY:" + Y);
		double nextHeading = heading;
		for (double d = 0; d < myP.distance(enemyP); d += bulletSpeed) {
			int nextStep = predictOneStep(pattern);
			nextHeading = nextHeading + nextStep / 17 - 10;
			enemyP = project(enemyP, Math.toRadians(nextHeading), nextStep % 17 - 8);
			predictions.add(enemyP);
			pattern = (char) nextStep + pattern;
		}
		// System.out.println(predictions);
		predictX = enemyP.getX();
		// if (predictX < 0)
		// predictX = 0;
		// else if (predictX > me.getBattleFieldWidth())
		// predictX = me.getBattleFieldWidth();

		predictY = enemyP.getY();
		// if (predictY < 0)
		// predictY = 0;
		// else if (predictY > me.getBattleFieldHeight())
		// predictY = me.getBattleFieldHeight();
		if (predictX < 0 || predictX > me.getBattleFieldWidth() || predictY < 0
				|| predictY > me.getBattleFieldHeight()) {
			predictX = getX();
			predictY = getY();
		}

		lock = false;
	}

	private int predictOneStep(String pattern) {
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
		// ��С����dh�������ֵ�����������±���,�ڴ��ȹ���
		if (Math.rint(Math.abs(dh)) > Rules.MAX_TURN_RATE) {
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

	public double getX() {
		return X;
	}

	public void setX(double X) {
		this.X = X;
	}

	public double getY() {
		return Y;
	}

	public void setY(double Y) {
		this.Y = Y;
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
