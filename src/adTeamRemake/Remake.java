package adTeamRemake;

import java.awt.Color;
import java.util.Enumeration;
import java.util.Hashtable;

import robocode.*;

//3V3��ս����
public class Remake extends AdvancedRobot {
	int teamFireCount = 0;// ����ÿ��ս��ͳ�ƶ����˺�
	// -----
	BattleMap battleMap = new BattleMap(this);
	Cooperate cooperate = new Cooperate();
	long preTick = -1;
	// ̹��״̬���ƹؼ���
	int aimingTime = 0;// ��׼һ��Ŀ��һ��ʱ��ļ�������ֹƵ���л�Ŀ�꣩
	int hiding = 0;// ���������Ҫǿ�ƺ�������
	int changeDirection = 0;// һ��ʱ���ı䷽��ļ���
	int direction = 1;// ��ǰ����ǰ��/����
	boolean ifNearWall;// ����ǽ���ı�һ�²���

	int thisTurnScanRobotCount = 0;
	int noOnScanTime = 0;
	Hashtable<String, Boolean> thisTurnScanList = new Hashtable<String, Boolean>();

	public void run() {
		cooperate.init(this, battleMap);
		setColors(Color.gray, Color.black, Color.black, Color.orange, Color.yellow);
		// ����������������ֶ�������
		setAdjustGunForRobotTurn(true);
		setAdjustRadarForGunTurn(true);
		setTurnRadarRight(1000);
		// �����߼�
		while (getTime() < 10) {// ����ɨ��һȦ
			while (getTime() == preTick)
				;// ��������ֱ����һtick����
			setScan();
			preTick = getTime();
			execute();
		}
		// ս���߼������е���
		while (cooperate.getEnemyRest() > 0) {
			while (getTime() == preTick)
				;// ��������ֱ����һtick����
			preTick = getTime();
			setScan();
			if (hiding == 0) {// ������ڶ��״̬��������ͨ���ƶ�
				setMove();
			} else {
				if (hiding > 0)
					hiding--;
			}
			avoidWall();// ���ƶ�״̬���б�ǽ����
			setFire();
			execute();
		}
		// �������⣬�����ɱ��ˢ���з�
		cooperate.teammates = null;
		while (true) {
			while (getTime() == preTick)
				;// ��������ֱ����һtick����
			preTick = getTime();

			setScan();
			setFire();
			execute();
		}

	}

	public void setScan() {
		if (noOnScanTime > 3) {// ����������㹻��Ŀ���û��ɨ�赽���ˣ�����ɨ��״̬��ȷ��ɨ��Ƕ������ŵ�
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
		if (changeDirection > 0)// ����л����������
			changeDirection--;
		else {// �л������������л�����
			changeDirection = 4 + (int) ((battleMap.aimingTarget.getDistance() / 30 + (Math.random() * 30)));
			direction = -direction;
		}
		if (!cooperate.ifReachPlace()) {// ����ǿ�а����������˷ֵ��������䣬��ռ���������
			NextMoveInfo nextMoveInfo = battleMap.calcuNextGravityMove(cooperate.cornerForce, 1);
			setTurnRight(nextMoveInfo.getBearing());
			setAhead(nextMoveInfo.getDistance());
		} else {// ʹ�÷���������������������ƶ����˶�
			Force force = new Force();

			RobotInfo target;
			Enumeration<RobotInfo> enumeration = battleMap.enemyList.elements();
			while (enumeration.hasMoreElements()) {// ����������
				target = (RobotInfo) enumeration.nextElement();
				if (cooperate.isTeammate(target.getName()))
					continue;

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
			// ̹���������������ķ����˶�
			NextMoveInfo nextMoveInfo = battleMap.calcuNextGravityMove(force, direction);
			setTurnRight(nextMoveInfo.getBearing());
			setAhead(nextMoveInfo.getDistance());
		}
	}

	public void avoidWall() {// ���ݵ�ǰ̹��״̬ѡ���ǽ���߼��е����ܣ��п�Ҫ��д
		double avoidDist = 100;
		double moveDirect = BattleMap.normalizeAngle(getHeading() - 90 + 90 * Math.signum(getDistanceRemaining()));
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

		// ���̫����ǽ������ǽ�Ƕȹ��ڴ�ֱ����Ϊ������ǰ�߻�ײ������
		if (ifAvoid && remain < avoidDist && Math.abs(getTurnRemaining()) > 45) {
			setMaxVelocity(6);
		} else if (ifAvoid && remain < avoidDist / 2 && Math.abs(getTurnRemaining()) > 30) {
			setMaxVelocity(4);
		} else
			setMaxVelocity(8);
		if (ifAvoid)// ���ý�ǽ�ؼ���
			ifNearWall = true;
		else
			ifNearWall = false;
	}

	public void setFire() {
		// ʹ��ģʽƥ�䷨����
		NextAimInfo nextAimInfo = battleMap.calcuNextGunBearing();
		if (nextAimInfo != null) {
			if (aimingTime < 20) {// �����׼һ��Ŀ��ʱ��϶̣�����������
				aimingTime++;
				setTurnGunRight(nextAimInfo.getBearing());
				if (nextAimInfo.getIfCanFire()) {
					if (cooperate.getEnemyRest() > 0)// ������е��ˣ�������
						setFire(nextAimInfo.getPower());
					else {// ������������ˣ����࿪��ˢ��
						setFire(battleMap.aimingTarget.getEnergy() / 100);// ˢ��
					}
				}
			} else {// �����׼һ��Ŀ��ʱ���㹻�������Ըı�Ŀ��
				aimingTime = 0;
				battleMap.calcuBestTarget();
			}
		} else {// ���û��Ŀ�꣬���޷��õ���׼��Ϣ���ʳ��Ի�ȡ��һ��
			battleMap.calcuBestTarget();
		}
	}

	// �¼�����
	/**
	 * �״�ɨ�赽һ�����ˣ����Ի�ȡ�����˵���Ϣ
	 */
	public void onScannedRobot(ScannedRobotEvent e) {
		noOnScanTime = 0;// �����״���������ɨ����һ��Ŀ��
		if (!thisTurnScanList.containsKey(e.getName())) {// �����״����������һ��ɨ�˼������ˣ��Ƿ�Ӧ������
			thisTurnScanRobotCount++;
			thisTurnScanList.put(e.getName(), true);
		}
		battleMap.setEnemyInfo(e);// �ڵ�ͼ�ϱ��Ŀ��
	}

	/**
	 * ��������
	 */
	public void onRobotDeath(RobotDeathEvent event) {
		battleMap.removeEnemyFromMap(event);
		if (event.getName() == battleMap.aimingTarget.getName()) {
			aimingTime = 0;// ˢ����׼
		}
		if (cooperate.isTeammate(event.getName()))
			cooperate.teammateRest--;// ����Ƕ��ѣ����ٶ���
	}

	/**
	 * ײ�������ˣ����Ի�ȡ����bearing�����˷�λ������Ϣ
	 */
	public void onHitRobot(HitRobotEvent event) {
		hiding = 10;
		int hitDirect = Math.abs(event.getBearing()) < 90 ? 1 : -1;
		setAhead(-100 * hitDirect);// ��ײ������ķ��������
		System.out.println("hitrobot!:" + event.getName());
	}

	public void onHitWall(HitWallEvent event) {
		hiding = 10;
		int hitDirect = Math.abs(event.getBearing()) < 90 ? 1 : -1;
		setAhead(-100 * hitDirect);// ��ײ������ķ��������
		System.out.println("hitwall!");
	}

	/**
	 * ���ӵ����У����Ի�����ӵ�������ķ���
	 */
	public void onHitByBullet(HitByBulletEvent event) {
		// ����Ƕ��Ѵ�ģ�����ͳ����
		if (cooperate.isTeammate(event.getName()))
			teamFireCount++;
		// ��������ڶ��״̬����ֹ������ܣ������ߵ��������ˣ��Ͳ�����ӵ�
		if (cooperate.getEnemyRest() <= 0 || hiding != 0)
			return;
		// �������ܽ��������ǵ��ˣ�ǿ�ƹ���
		if (!cooperate.isTeammate(event.getName()) && battleMap.getRobot(event.getName()).getDistance() < 100) {
			aimingTime = 20;
			battleMap.aimingTarget = battleMap.getRobot(event.getName());
		}

		if (!ifNearWall) {// ���������ǽ��ʹ��һ���ܹ���
			if (!cooperate.isTeammate(event.getName())) {// ������Ƕ��ѣ����������Ҷ��
				double bearing = event.getBearing();
				double turnTagency;
				if (bearing >= 0)
					turnTagency = bearing - 90;
				else
					turnTagency = bearing + 90;
				hiding = 10;
				setTurnRight(BattleMap.normalizeAngle(turnTagency));
				setAhead(200 * direction);
			} else {// �����Ѵ�������Զ������ڵ����еķ�����
				direction = Math.abs(event.getBearing()) < 90 ? 1 : -1;
				double bearing = event.getBearing();
				double turnTagency;
				if (bearing >= 0)
					turnTagency = bearing - 90;
				else
					turnTagency = bearing + 90;
				hiding = 10;
				setAhead(200 * direction);
				setTurnRight(BattleMap.normalizeAngle(turnTagency));
			}
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
