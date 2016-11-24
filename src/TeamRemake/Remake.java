package TeamRemake;

import java.awt.Color;
import java.util.Hashtable;

import robocode.*;

public class Remake extends AdvancedRobot {
	BattleMap battleMap = new BattleMap(this);
	Cooperate cooperate = new Cooperate();
	long preTick = -1;
	// ̹��״̬���ƹؼ���
	int aimingTime = 0;
	int bumpHiding = 0;// ���������Ҫǿ�ƺ�������
	int hitHiding = 0;
	String[] teammates = { "TeamRemake.Remake* (1)", "TeamRemake.Remake* (2)", "TeamRemake.Remake* (3)" };

	int thisTurnScanRobotCount = 0;
	int noOnScanTime = 0;
	Hashtable<String, Boolean> thisTurnScanList = new Hashtable<String, Boolean>();

	public void run() {
		cooperate.init(this, battleMap);
		setColors(Color.gray, Color.ORANGE, Color.ORANGE);
		// ����������������ֶ�������
		setAdjustGunForRobotTurn(true);
		setAdjustRadarForGunTurn(true);
		setTurnRadarRight(1000);
		while (getTime() < 10) {// ����ɨ��һȦ
			while (getTime() == preTick)
				;// ��������ֱ����һtick����
			setScan();
			preTick = getTime();
			execute();
		}
		cooperate.divideCornerForTeam();// ȷ������̹���Ѿ�ɨ����һȦ
		while (true) {
			while (getTime() == preTick)
				;// ��������ֱ����һtick����
			preTick = getTime();
			setScan();
			if (hitHiding == 0 && bumpHiding == 0) {
				setMove();
			} else {
				if (hitHiding > 0)
					hitHiding--;
				if (bumpHiding > 0)
					bumpHiding--;
			}
			setFire();
			// ��ʵ��tick�����У������¼�˲�����ڹ��ƶ��복���ƶ�����
			// System.out.println(getOthers());
			execute();
		}
	}

	public boolean isTeammate(String name) {
		if (teammates == null)
			return false;
		for (int i = 0; i < teammates.length; i++)
			if (name.compareTo(teammates[i]) == 0)
				return true;
		return false;
	}

	public String[] getTeammates() {
		return teammates;
	}

	public void setScan() {
		if (noOnScanTime > 3) {// ����������㹻��Ŀ���û��ɨ�赽���ˣ�����ɨ��״̬��ȷ��ɨ��Ƕ������ŵ�
			System.out.println("change");
			noOnScanTime = 0;
			thisTurnScanRobotCount = 0;
			thisTurnScanList = new Hashtable<String, Boolean>();
			setTurnRadarRight(1000 * Math.signum(getRadarTurnRemaining()));
		}
		if (thisTurnScanRobotCount >= getOthers()) {// ���ɨ�赽�����е��ˣ��л�ɨ�跽��
			setTurnRadarRight(-1000 * Math.signum(getRadarTurnRemaining()));
			thisTurnScanRobotCount = 0;
			thisTurnScanList = new Hashtable<String, Boolean>();
		}
		noOnScanTime++;
	}

	public void setMove() {
		if (!cooperate.ifReachPlace()) {
			NextMoveInfo nextMoveInfo = battleMap.calcuNextGravityMove(cooperate.cornerForce);
			setTurnRight(nextMoveInfo.getBearing());
			setAhead(nextMoveInfo.getDistance());// ���ڳ��м��ٶȣ������������ݾ�����������ٶȣ�ȷ����ͣ����ȷλ�ã����������ƶ�����󣬳��ٴ󣬾���С������С
		} else {// Ӧ��дΪ�ڽ�����˶�
			double bearing = battleMap.aimingTarget.getBearing();
			double turnTagency;
			if (bearing >= 0)
				turnTagency = bearing - 90;
			else
				turnTagency = bearing + 90;
			double predictHeadingRadius = Math.toRadians(turnTagency + getHeading());
			Force force = new Force();
			force.xForce = Math.sin(predictHeadingRadius) * 2000 / battleMap.aimingTarget.getDistance() * Math.random();
			force.yForce = Math.cos(predictHeadingRadius) * 2000 / battleMap.aimingTarget.getDistance() * Math.random();

			NextMoveInfo nextMoveInfo = battleMap.calcuNextGravityMove(force);
			setTurnRight(nextMoveInfo.getBearing());
			setAhead(nextMoveInfo.getDistance());// ���ڳ��м��ٶȣ������������ݾ�����������ٶȣ�ȷ����ͣ����ȷλ�ã����������ƶ�����󣬳��ٴ󣬾���С������С
		}
	}

	public void setFire() {
		NextAimInfo nextAimInfo = battleMap.calcuNextGunBearing();
		if (nextAimInfo != null) {
			if (aimingTime < 20) {
				aimingTime++;
				setTurnGunRight(nextAimInfo.getBearing());
				if (nextAimInfo.getIfCanFire()) {
					setFire(nextAimInfo.getPower());
				}
			} else {
				aimingTime = 0;
				battleMap.calcuBestTarget();
			}
		} else {

			battleMap.calcuBestTarget();
			// System.out.println("aimming:" +battleMap.aimingTarget.getName());
		}
	}

	// �¼�����
	/**
	 * �״�ɨ�赽һ�����ˣ����Ի�ȡ�����˵���Ϣ
	 */
	public void onScannedRobot(ScannedRobotEvent e) {
		noOnScanTime = 0;
		if (!thisTurnScanList.containsKey(e.getName())) {
			thisTurnScanRobotCount++;
			thisTurnScanList.put(e.getName(), true);
		}

		battleMap.setEnemyInfo(e);
	}

	/**
	 * ��������
	 */
	public void onRobotDeath(RobotDeathEvent event) {
		battleMap.removeEnemyFromMap(event);
		if (event.getName() == battleMap.aimingTarget.getName()) {
			aimingTime = 0;
		}
	}

	/**
	 * ײ�������ˣ����Ի�ȡ����bearing�����˷�λ������Ϣ
	 */
	public void onHitRobot(HitRobotEvent event) {
		bumpHiding = 10;
		setAhead(-100);
		setTurnRight(event.getBearing());
		System.out.println("hitrobot!:" + event.getName());
	}

	public void onHitWall(HitWallEvent event) {
		bumpHiding = 10;
		setAhead(-100);
		setTurnRight(event.getBearing());
		System.out.println("hitwall!");
	}

	/**
	 * ���ӵ����У����Ի�����ӵ�������ķ���
	 */
	public void onHitByBullet(HitByBulletEvent event) {
		hitHiding = 15 + (int) (10 * Math.random());
		System.out.println(hitHiding);
		double bearing = event.getBearing();
		double turnTagency;
		if (bearing >= 0)
			turnTagency = bearing - 90;
		else
			turnTagency = bearing + 90;

		setTurnRight(turnTagency);
		setAhead(-200);
		// event.get___
	}

	// ------------------------------------------------------------------------------------
	/**
	 * �����ʤ���Ļ�����ִ����һ��װ����
	 */
	public void onWin(WinEvent e) {
		for (int i = 0; i < 50; i++) {

			turnGunRightRadians(Math.PI * 3 / 4);
			turnGunLeftRadians(Math.PI * 3 / 4);
		}
	}
}
