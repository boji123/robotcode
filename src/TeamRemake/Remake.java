package TeamRemake;

import java.awt.Color;
import java.util.Enumeration;
import java.util.Hashtable;

import robocode.*;

public class Remake extends AdvancedRobot {
	BattleMap battleMap = new BattleMap(this);
	Cooperate cooperate = new Cooperate();
	long preTick = -1;
	// ̹��״̬���ƹؼ���
	int aimingTime = 0;
	int hiding = 0;// ���������Ҫǿ�ƺ�������
	int changeDirection = 0;
	int direction = 1;
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
		// cooperate.divideCornerForTeam();// ȷ������̹���Ѿ�ɨ����һȦ
		while (cooperate.getEnemyRest() > 0) {
			while (getTime() == preTick)
				;// ��������ֱ����һtick����
			preTick = getTime();
			setScan();
			if (hiding == 0) {
				setMove();
			} else {
				if (hiding > 0)
					hiding--;
			}
			setFire();
			// ��ʵ��tick�����У������¼�˲�����ڹ��ƶ��복���ƶ�����
			// System.out.println(getOthers());
			execute();
		}
		teammates = null;// �������⣬���������ɱ
		while (true) {
			while (getTime() == preTick)
				;// ��������ֱ����һtick����
			preTick = getTime();
			setScan();
			setFire();
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

		if (changeDirection > 0)
			changeDirection--;
		else {
			changeDirection = (int) (battleMap.aimingTarget.getDistance() / 30 + (Math.random() * 40));
			if (getTime() % 4 > 0)
				direction = -direction;
		}

		if (!cooperate.ifReachPlace()) {
			// System.out.println("moveToCorner");
			NextMoveInfo nextMoveInfo = battleMap.calcuNextGravityMove(cooperate.cornerForce, 1);
			setTurnRight(nextMoveInfo.getBearing());
			setAhead(nextMoveInfo.getDistance());
		} else {// Ӧ��дΪ�ڽ�����˶�
			Force force = new Force();
			RobotInfo target;
			Enumeration<RobotInfo> enumeration = battleMap.enemyList.elements();
			while (enumeration.hasMoreElements()) {// ����������
				target = (RobotInfo) enumeration.nextElement();
				if (isTeammate(target.getName()))
					continue;
				// System.out.println("consider:" + target.getName());
				double bearing = target.getBearing();

				double turnTagency;
				if (bearing >= 0)
					turnTagency = bearing - 90;
				else
					turnTagency = bearing + 90;

				double headingRadius = Math.toRadians(turnTagency + getHeading());

				force.xForce += +Math.sin(headingRadius) * 1000 / target.getDistance() * direction * Math.random();
				force.yForce += +Math.cos(headingRadius) * 1000 / target.getDistance() * direction * Math.random();
			}
			force.xForce /= cooperate.getEnemyRest();
			force.yForce /= cooperate.getEnemyRest();
			// System.out.println(force.xForce);
			// System.out.println(force.yForce);
			NextMoveInfo nextMoveInfo = battleMap.calcuNextGravityMove(force, direction);
			setTurnRight(nextMoveInfo.getBearing());
			setAhead(nextMoveInfo.getDistance());// ���ڳ��м��ٶȣ������������ݾ�����������ٶȣ�ȷ����ͣ����ȷλ�ã����������ƶ�����󣬳��ٴ󣬾���С������С
		}
		/*
		 * if (getX() < 100 || getBattleFieldWidth() - getX() < 100) { if
		 * (Math.abs(getHeading()) < 90) setTurnRight(0 - getHeading()); else
		 * setTurnRight(BattleMap.normalizeAngle(180 - getHeading())); }
		 */

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
		if (isTeammate(event.getName()))
			cooperate.teammateRest--;
		else {
			// cooperate.divideCornerForTeam();
			// cooperate.reachCount = 20;
		}
	}

	/**
	 * ײ�������ˣ����Ի�ȡ����bearing�����˷�λ������Ϣ
	 */
	public void onHitRobot(HitRobotEvent event) {
		hiding = 10;
		setAhead(-100 * direction);
		setTurnRight(event.getBearing() - 90 + 90 * direction);
		System.out.println("hitrobot!:" + event.getName());
	}

	public void onHitWall(HitWallEvent event) {
		hiding = 10;
		setAhead(-100 * direction - 90 + 90 * direction);
		setTurnRight(event.getBearing());
		System.out.println("hitwall!");
	}

	/**
	 * ���ӵ����У����Ի�����ӵ�������ķ���
	 */
	public void onHitByBullet(HitByBulletEvent event) {
		if (cooperate.getEnemyRest() != 1)
			return;
		double bearing = event.getBearing();
		double turnTagency;
		if (bearing >= 0)
			turnTagency = bearing - 90;
		else
			turnTagency = bearing + 90;
		hiding = 5;

		setTurnRight(turnTagency);
		setAhead(200 * direction);
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
