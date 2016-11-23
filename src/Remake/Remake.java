package Remake;

import java.awt.Color;
import java.util.Random;

import robocode.*;

public class Remake extends AdvancedRobot {
	BattleMap battleMap = new BattleMap(this);
	long preTick = -1;
	// ̹��״̬���ƹؼ���
	int aimingTime = 0;
	String friend = "name";// ��������
	static boolean[] robotPlace = new boolean[4];
	int robotNum;

	public void run() {
		do {// �������һ������������ˣ�δ���
			robotNum = (int) Math.random() * 100 % 4;
		} while (robotPlace[robotNum] == false);
		robotPlace[robotNum] = true;

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
			execute();
		}
	}

	public void setScan() {
		if (getOthers() > 1) {// ����һ�����ˣ�С��ս�����ã�
			setTurnRadarRight(500);
		}
	}

	public void setMove() {
		if (getOthers() > 1) {// ����һ�����ˣ�С��ս�����ã�
			NextMoveInfo nextMoveInfo = battleMap.calcuNextGravityMove(new Force());
			setTurnRight(nextMoveInfo.getBearing());
			setAhead(nextMoveInfo.getDistance());// ���ڳ��м��ٶȣ������������ݾ�����������ٶȣ�ȷ����ͣ����ȷλ�ã����������ƶ�����󣬳��ٴ󣬾���С������С

		} else {
			NextMoveInfo nextMoveInfo = null;
			if (nextMoveInfo == null) {
				nextMoveInfo = battleMap.calcuNextGravityMove(new Force());
				// �˴���дΪhidebullet
			}
			setTurnRight(nextMoveInfo.getBearing());
			setAhead(nextMoveInfo.getDistance());
		}
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
		}
	}

	// �¼�����
	/**
	 * �״�ɨ�赽һ�����ˣ����Ի�ȡ�����˵���Ϣ
	 */
	public void onScannedRobot(ScannedRobotEvent e) {
		if (e.getName() != friend) {
			if (getOthers() > 1) {// ����һ�����ˣ�С��ս�����ã�
				battleMap.setEnemyInfo(e);
			} else {
				setTurnRadarRight(battleMap.trackCurrent(e));
				battleMap.setEnemyInfo(e);
			}
		}
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
		System.out.println("hitrobot!:" + event.getName());
	}

	public void onHitWall(HitWallEvent event) {
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
