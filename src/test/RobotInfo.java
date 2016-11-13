package test;

/**
 * 在map类中使用，用于保存机器人的方位等信息
 * 
 * @author baiji
 *
 */
public class RobotInfo {
	String name = null;// 机器人名字
	double heading = 0;// 机器人的朝向
	double velocity = 0;// 机器人的速度
	double bearing = 0;// 机器人相对于你的朝向（仅当机器人是敌人时）
	double distance = 0;// 机器人离你的距离
	double locationX = 0;// 机器人在地图上的绝对坐标
	double locationY = 0;

}
