package adTeamRemake;

import java.awt.Color;
import java.util.Enumeration;
import java.util.Hashtable;

import robocode.*;

public class Remake extends AdvancedRobot {
	int teamFireCount = 0;
	// -----
	BattleMap battleMap = new BattleMap(this);
	Cooperate cooperate = new Cooperate();
	long preTick = -1;
	// ̹��״̬���ƹؼ���
	int aimingTime = 0;
	int hiding = 0;// ���������Ҫǿ�ƺ�������
	int changeDirection = 0;
	int direction = 1;
	boolean ifNearWall;
	String[] teammates = { "adTeamRemake.Remake* (1)", "adTeamRemake.Remake* (2)", "adTeamRemake.Remake* (3)" };

	int thisTurnScanRobotCount = 0;
	int noOnScanTime = 0;
	Hashtable<String, Boolean> thisTurnScanList = new Hashtable<String, Boolean>();

	public void run() {
		cooperate.init(this, battleMap);
		setColors(Color.gray, Color.black, Color.black);
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
			avoidWall();
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
			// System.out.println("change");
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
			changeDirection = 4 + (int) ((battleMap.aimingTarget.getDistance() / 30 + (Math.random() * 30)));
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
					continue; // System.out.println("consider:" +
								// target.getName());
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
		// setTurnRight(0);
		// setAhead(-1000);//
		// ���ڳ��м��ٶȣ������������ݾ�����������ٶȣ�ȷ����ͣ����ȷλ�ã����������ƶ�����󣬳��ٴ󣬾���С������С
	}

	public void avoidWall() {// ���ݵ�ǰ̹��״̬ѡ���ǽ
		double avoidDist = 100;
		double moveDirect = BattleMap.normalizeAngle(getHeading() - 90 + 90 * Math.signum(getDistanceRemaining()));
		// System.out.println(moveDirect);
		// turn on top
		double remain = avoidDist;
		boolean ifAvoid = false;
		if (getY() > getBattleFieldHeight() - avoidDist) {// ���ϱ߽�
			if (moveDirect >= 0 && moveDirect < 90) {// ��ת�Ϻ�
				setTurnRight(90 - moveDirect);
			}
			if (moveDirect < 0 && moveDirect > -90) {
				setTurnRight(-90 - moveDirect);
			}
			if (getX() < avoidDist) {// ����߽�
				if (moveDirect > -45 && moveDirect < 90)
					setTurnRight(90 - moveDirect);// ��ת
				else if (moveDirect < -45 && moveDirect > -180) {
					setTurnRight(-180 - moveDirect);// ��ת
				}
			}
			if (getX() > getBattleFieldWidth() - avoidDist) {// ���ұ߽�
				if (moveDirect > 45 && moveDirect < 180)
					setTurnRight(180 - moveDirect);// ��ת
				else if (moveDirect < 45 && moveDirect > -90) {
					setTurnRight(-90 - moveDirect);// ��ת
				}
			}
			remain = Math.min(getBattleFieldHeight() - getY(), remain);
			ifAvoid = true;
		} else if (getY() < avoidDist) {// ���±߽�
			if (moveDirect > 90 && moveDirect < 180) {
				setTurnRight(90 - moveDirect);// �ʺ���ת
			}
			if (moveDirect < -90 && moveDirect >= -180) {
				setTurnRight(-90 - moveDirect);
			}
			if (getX() < avoidDist) {// ����߽�
				if (moveDirect > -135 && moveDirect < 0)
					setTurnRight(0 - moveDirect);// ��ת
				else if (moveDirect < -135 || moveDirect > 90) {
					if (moveDirect < 0)
						moveDirect += 360;
					setTurnRight(90 - moveDirect);// ��ת
				}
			}
			if (getX() > getBattleFieldWidth() - avoidDist) {// ���ұ߽�
				if (moveDirect > 135 || moveDirect < -90) {
					if (moveDirect < 0)
						moveDirect += 360;
					setTurnRight(270 - moveDirect);// ��ת
				} else if (moveDirect < 135 && moveDirect > 0)
					setTurnRight(0 - moveDirect);// ��ת
			}
			remain = Math.min(getY(), remain);
			ifAvoid = true;
		} else if (getX() < avoidDist) {// ����߽��Ҳ������±߽�
			if (moveDirect >= -90 && moveDirect < 0) {// ������ת
				setTurnRight(0 - moveDirect);// ��ת
			}
			if (moveDirect < -90 && moveDirect > -180) {// ������ת
				setTurnRight(-180 - moveDirect);// ��ת
			}
			remain = Math.min(getX(), remain);
			ifAvoid = true;
		} else if (getX() > getBattleFieldWidth() - avoidDist) {// ���ұ߽��Ҳ������±߽�
			if (moveDirect < 180 && moveDirect > 90) {// ������ת
				setTurnRight(180 - moveDirect);
			}
			if (moveDirect > 0 && moveDirect <= 90) {// ������ת
				setTurnRight(0 - moveDirect);// ��ת
			}
			remain = Math.min(getBattleFieldWidth() - getX(), remain);
			ifAvoid = true;
		}
		if (ifAvoid && remain < avoidDist && Math.abs(getTurnRemaining()) > 45) {
			setMaxVelocity(6);
		} else if (ifAvoid && remain < avoidDist / 2 && Math.abs(getTurnRemaining()) > 30) {
			setMaxVelocity(4);
		} else
			setMaxVelocity(8);
		if (ifAvoid)
			ifNearWall = true;
		else
			ifNearWall = false;
	}

	public void setFire() {
		NextAimInfo nextAimInfo = battleMap.calcuNextGunBearing();
		if (nextAimInfo != null) {
			if (aimingTime < 20) {
				aimingTime++;
				setTurnGunRight(nextAimInfo.getBearing());
				if (nextAimInfo.getIfCanFire()) {
					if (cooperate.getEnemyRest() > 0)
						setFire(nextAimInfo.getPower());
					else {
						setFire(battleMap.aimingTarget.getEnergy() / 100);// ˢ��
					}
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
		// if (cooperate.getEnemyRest() == 1)
		// cooperate.reachCount = 30;
	}

	/**
	 * ײ�������ˣ����Ի�ȡ����bearing�����˷�λ������Ϣ
	 */
	public void onHitRobot(HitRobotEvent event) {
		hiding = 10;
		int hitDirect = Math.abs(event.getBearing()) < 90 ? 1 : -1;
		setAhead(-100 * hitDirect);
		// setTurnRight(Math.signum(event.getBearing() - 90 + 90 * direction) *
		// 90);
		System.out.println("hitrobot!:" + event.getName());
	}

	public void onHitWall(HitWallEvent event) {
		hiding = 10;
		int hitDirect = Math.abs(event.getBearing()) < 90 ? 1 : -1;
		setAhead(-100 * hitDirect);
		// double hideAngle = event.getBearing();
		// if(hideAngle<9)
		// setTurnRight(Math.signum(event.getBearing());
		System.out.println("hitwall!");
	}

	/**
	 * ���ӵ����У����Ի�����ӵ�������ķ���
	 */
	public void onHitByBullet(HitByBulletEvent event) {
		if (isTeammate(event.getName()))
			teamFireCount++;
		if (battleMap.getRobot(event.getName()).getDistance() < 100)
			battleMap.aimingTarget = battleMap.getRobot(event.getName());
		if (cooperate.getEnemyRest() <= 0 || hiding != 0)
			return;

		if (!ifNearWall) {
			if (!isTeammate(event.getName())) {
				double bearing = event.getBearing();
				double turnTagency;
				if (bearing >= 0)
					turnTagency = bearing - 90;
				else
					turnTagency = bearing + 90;
				hiding = 10;
				setTurnRight(BattleMap.normalizeAngle(turnTagency));
				setAhead(200 * direction);
			} else {// �����Ѵ����������˱�
				hiding = 10;
				direction = Math.abs(event.getBearing()) < 90 ? 1 : -1;
				setAhead(200 * direction);

				double bearing = event.getBearing();
				double turnTagency;
				if (bearing >= 0)
					turnTagency = bearing - 90;
				else
					turnTagency = bearing + 90;
				setTurnRight(BattleMap.normalizeAngle(turnTagency));
			}

		} else {
			if (!isTeammate(event.getName()))
				battleMap.aimingTarget = battleMap.getRobot(event.getName());
		}
	}

	// ------------------------------------------------------------------------------------
	public void onDeath(DeathEvent event) {
		System.out.println("teamFire:" + teamFireCount);
	}

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
