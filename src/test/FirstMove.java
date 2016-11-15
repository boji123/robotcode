package test;

import robocode.*;

/**
 * 
 * @author baiji
 *
 */
public class FirstMove extends AdvancedRobot {
	BattleMap battleMap = new BattleMap();
	long preTick = -1;

	public void run() {
		// ����������������ֶ�������
		setAdjustGunForRobotTurn(true);
		setAdjustRadarForGunTurn(true);
		while (true) {
			while (getTime() == preTick)
				;// ��������ֱ����һtick����
			preTick = getTime();
			updateInfo();
			setScan();
			setMove();
			setFire();
			// ��ʵ��tick�����У������¼�˲�����ڹ��ƶ��복���ƶ�����
			execute();
		}
	}

	public void updateInfo() {
		battleMap.setYourInfo(this);
	}

	public void setScan() {
		if (getRadarTurnRemaining() <= 30) {
			setTurnRadarLeft(360);
		}
	}

	public void setMove() {
		NextMoveInfo nextMoveInfo = battleMap.calcuNextMove();
		setTurnRight(nextMoveInfo.getBearing());
		setAhead(nextMoveInfo.getDistance());
	}

	public void setFire() {
		NextAimInfo nextAimInfo = battleMap.calcuNextGunBearing();
		setTurnGunRight(nextAimInfo.getBearing());
		if (nextAimInfo.getIfCanFire()) {
			setFire(1);
		}
	}

	// �¼�����
	/**
	 * �״�ɨ�赽һ�����ˣ����Ի�ȡ�����˵���Ϣ
	 */
	public void onScannedRobot(ScannedRobotEvent e) {
		battleMap.setEnemyInfo(e);
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

	/**
	 * ���ӵ����У����Ի�����ӵ�������ķ���
	 */
	public void onHitByBullet(HitByBulletEvent event) {
		// event.get___
	}

	// һЩ��ѧ����
	/**
	 * ������ֹ���꣬���ؽǶȵķ�����ֵ�����Բ�ʹ�øú���ֱ�ӵ���atan2
	 */
	private static double getAngle(double x1, double y1, double x2, double y2) {
		return Math.atan2(x2 - x1, y2 - y1);
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
