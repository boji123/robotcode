package adPredict2;

import java.awt.Color;

import robocode.*;

public class adPredict2 extends AdvancedRobot {
	BattleMap battleMap = new BattleMap(this);
	long preTick = -1;

	public void run() {
		setColors(Color.ORANGE, Color.ORANGE, Color.ORANGE);
		// ����������������ֶ�������
		setAdjustGunForRobotTurn(true);
		setAdjustRadarForGunTurn(true);
		setTurnRadarRight(720);// ��ʼ���״��ɨ�裬��ֹ�������߼��ϵ����⣨�״ﲻ�����޷�����׷�٣�
		while (true) {
			while (getTime() == preTick)
				;// ��������ֱ����һtick����
			preTick = getTime();
			setScan();
			setMove();
			setFire();
			// ��ʵ��tick�����У������¼�˲�����ڹ��ƶ��복���ƶ�����
			System.out.println(getOthers());
			execute();
		}
	}

	public void setScan() {
		// ����������Ϊһ��ʱ���л����״�׷��ģʽ����onscan�¼�����������ִ������
		if (getOthers() > 1) {
			setTurnRadarRight(720);// ��Ҫ�������˱�Ϊһ��ʱ���״ﻹ������ת��Ȧ��ȷ������ɨ�赽ʣ�µ��Ǹ����ˣ�����׷��
		}
	}

	public void setMove() {
		NextMoveInfo nextMoveInfo = battleMap.calcuNextMove();
		setTurnRight(nextMoveInfo.getBearing());
		setAhead(nextMoveInfo.getDistance());// ���ڳ��м��ٶȣ������������ݾ�����������ٶȣ�ȷ����ͣ����ȷλ�ã����������ƶ�����󣬳��ٴ󣬾���С������С
		// System.out.println(getVelocity());
		// setTurnRight(0);
		// setAhead(0);
	}

	public void setFire() {
		NextAimInfo nextAimInfo = battleMap.calcuNextGunBearing();
		setTurnGunRight(nextAimInfo.getBearing());
		if (nextAimInfo.getIfCanFire()) {
			setFire(nextAimInfo.getPower());
		}
	}

	// �¼�����
	/**
	 * �״�ɨ�赽һ�����ˣ����Ի�ȡ�����˵���Ϣ
	 */
	public void onScannedRobot(ScannedRobotEvent e) {
		if (getOthers() > 1) {
			battleMap.setEnemyInfo(e);
		} else {// ����������Ϊһ��ʱ�������״�׷��ģʽ
			setTurnRadarRight(battleMap.trackCurrent(e));
			battleMap.setEnemyInfo(e);
		}
	}

	/**
	 * ��������
	 */
	public void onRobotDeath(RobotDeathEvent event) {
		battleMap.removeEnemyFromMap(event);
	}

	/**
	 * ײ�������ˣ����Ի�ȡ����bearing�����˷�λ������Ϣ
	 */
	public void onHitRobot(HitRobotEvent event) {
		// event.get___
	}

	/**
	 * ����ӵ����е���
	 */
	public void onBulletHit(BulletHitEvent event) {
		// event.get___
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
