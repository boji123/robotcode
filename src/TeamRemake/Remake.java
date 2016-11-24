package TeamRemake;

import java.awt.Color;

import robocode.*;

public class Remake extends TeamRobot {
	BattleMap battleMap = new BattleMap(this);
	Cooperate cooperate = new Cooperate();
	long preTick = -1;
	// ̹��״̬���ƹؼ���
	int aimingTime = 0;
	int hiding = 0;// ���������Ҫǿ�ƺ�������

	public void run() {
		cooperate.init(this, battleMap);
		setColors(Color.gray, Color.ORANGE, Color.ORANGE);
		// ����������������ֶ�������
		setAdjustGunForRobotTurn(true);
		setAdjustRadarForGunTurn(true);
		setTurnRadarRight(500);

		while (true) {
			while (getTime() == preTick)
				;// ��������ֱ����һtick����
			preTick = getTime();
			setScan();
			setMove();
			setFire();
			// ��ʵ��tick�����У������¼�˲�����ڹ��ƶ��복���ƶ�����
			// System.out.println(getOthers());
			if (getTime() == 10 && cooperate.isLeader) {// ȷ������̹���Ѿ�ɨ����һȦ
				cooperate.divideCornerForTeam();
				System.out.println("divideCorner");
			}
			execute();
		}
	}

	public void setScan() {

		if (cooperate.getEnemyRest() > 1 || getTime() < 10) {// ����һ������
			setTurnRadarRight(500);
		}
	}

	public void setMove() {

		if (hiding == 0) {
			if (!cooperate.ifReachPlace()) {
				NextMoveInfo nextMoveInfo = battleMap.calcuNextGravityMove(cooperate.cornerForce);
				setTurnRight(nextMoveInfo.getBearing());
				setAhead(nextMoveInfo.getDistance());// ���ڳ��м��ٶȣ������������ݾ�����������ٶȣ�ȷ����ͣ����ȷλ�ã����������ƶ�����󣬳��ٴ󣬾���С������С
			} else {// Ӧ��дΪ�ڽ�����˶�
				NextMoveInfo nextMoveInfo = battleMap.calcuNextGravityMove(cooperate.cornerForce);
				setTurnRight(nextMoveInfo.getBearing());
				setAhead(nextMoveInfo.getDistance());// ���ڳ��м��ٶȣ������������ݾ�����������ٶȣ�ȷ����ͣ����ȷλ�ã����������ƶ�����󣬳��ٴ󣬾���С������С
			}
		} else
			hiding--;
	}

	public void setFire() {
		if (aimingTime < 20) {
			aimingTime++;
			NextAimInfo nextAimInfo = battleMap.calcuNextGunBearing();
			setTurnGunRight(nextAimInfo.getBearing());
			if (nextAimInfo.getIfCanFire()) {
				setFire(nextAimInfo.getPower());
			}
		} else {
			aimingTime = 0;
			battleMap.calcuBestTarget();
			// System.out.println(battleMap.aimingTarget.getName());
		}
	}

	// �¼�����
	/**
	 * �״�ɨ�赽һ�����ˣ����Ի�ȡ�����˵���Ϣ
	 */
	public void onScannedRobot(ScannedRobotEvent e) {
		if (!isTeammate(e.getName())) {
			if (cooperate.getEnemyRest() > 1) {
				battleMap.setEnemyInfo(e);
			} else {
				setTurnRadarRight(battleMap.trackCurrent(e));
				battleMap.setEnemyInfo(e);
			}
		} else {
			battleMap.setEnemyInfo(e);// �������ֲ�̫�ԣ����Ǵպ��ðɡ�����
		}

	}

	public void onMessageReceived(MessageEvent event) {
		cooperate.onMessageReceived(event);
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
			cooperate.teammatesRest--;
	}

	/**
	 * ײ�������ˣ����Ի�ȡ����bearing�����˷�λ������Ϣ
	 */
	public void onHitRobot(HitRobotEvent event) {
		hiding = 5;
		setAhead(-50);
		setTurnRight(event.getBearing());
		System.out.println("hitrobot!:" + event.getName());
	}

	public void onHitWall(HitWallEvent event) {
		hiding = 5;
		setAhead(-50);
		setTurnRight(event.getBearing());
		System.out.println("hitwall!");
	}

	/**
	 * ���ӵ����У����Ի�����ӵ�������ķ���
	 */
	public void onHitByBullet(HitByBulletEvent event) {
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
