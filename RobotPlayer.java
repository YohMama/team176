package team176;

import battlecode.common.*;

import java.util.*;
//import java.math.*;

public class RobotPlayer {
	static RobotController me;
	static Team goodGuys;
	static Team badGuys;
	static int atkRange;
	static int sightRange;
	static Random rand;
	static Direction[] directions = {Direction.NORTH, Direction.NORTH_EAST, Direction.EAST, Direction.SOUTH_EAST, Direction.SOUTH, Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST};
	
	public static void run(RobotController owl) {
		me = owl;
		int id = me.getID();
		rand = new Random(id);
		MapLocation badHQ = me.senseEnemyHQLocation();
		MapLocation goodHQ = me.senseHQLocation();
		MapLocation[] badTowers = me.senseEnemyTowerLocations();
		MapLocation[] goodTowers = me.senseTowerLocations();
		try {
			sortTowers(goodTowers, goodHQ);
			sortTowers(badTowers, badHQ);
		} catch (Exception e) {
			System.out.println("Tower sorting exception");
			e.printStackTrace();
		}
		atkRange = me.getType().attackRadiusSquared;
		sightRange = me.getType().sensorRadiusSquared;
		goodGuys = me.getTeam();
		badGuys = goodGuys.opponent();
//		MapLocation origin = new MapLocation((goodHQ.x + badHQ.x) / 2, (goodHQ.y + badHQ.y) / 2);
		
		while (true) {
			
			try {
				me.setIndicatorString(0, me.getType().toString());
			} catch (Exception e) {
				System.out.println("Unexpected exception");
				e.printStackTrace();
			}
			
			if (me.getType() == RobotType.HQ) {
				try {
					/*if (Clock.getRoundNum() > 1950) {
						for (int i = 0; i < 14400; i++) {
							System.out.print(me.readBroadcast(i));
							if (i % 120 == 119) {
								System.out.println();
							}
						}
					}*/
					HQBroadcast();
					HQTransfer(goodHQ);
					nearATK();
					trySpawn(badHQ.directionTo(goodHQ));
//					map(origin);
				} catch (Exception e) {
					System.out.println("HQ Exception");
                    e.printStackTrace();
				}
			} else {
				try {
					unitTransfer(goodHQ);
				} catch (Exception e) {
					System.out.println("Non-HQ Transfer Exception");
					e.printStackTrace();
				}
			}
			
			if (me.getType() == RobotType.TOWER) {
				try {
					nearATK();
				} catch (Exception e) {
					System.out.println("TOWER Exception");
					e.printStackTrace();
				}
			}
			
			if (me.getType() == RobotType.BEAVER) {
				try {
					tryBuild(goodHQ.directionTo(badHQ), goodHQ);
					beaverMove(id, goodHQ.directionTo(badHQ), goodHQ, rand);
					nearATK();
					tryMine();
//					HQMove(badHQ);
				} catch (Exception e) {
					System.out.println("BEAVER Exception");
					e.printStackTrace();
				}
			}
			
			if (me.getType() == RobotType.MINER) {
				try {
					mine(id, goodHQ.directionTo(badHQ), id % 2, goodHQ);
				} catch (Exception e) {
					System.out.println("MINER Exception");
					e.printStackTrace();
				}
			}
			
			if (me.getType() == RobotType.SOLDIER) {
				try{
					nearATK();
//					randMove(directions[rand.nextInt(8)]);
					if (Clock.getRoundNum() < me.getRoundLimit() - 400) {
						moveTo(goodTowers[goodTowers.length - 1], goodHQ);
					} else {
						moveTo(badTowers[badTowers.length - 1], goodHQ);
					}
				} catch (Exception e) {
					System.out.println("SOLDIER Exception");
					e.printStackTrace();
				}
			}
			
			if (me.getType() == RobotType.MINERFACTORY) {
				try {
					trySpawn(directions[rand.nextInt(8)]);
				} catch (Exception e) {
					System.out.println("MINERFACTORY Exception");
					e.printStackTrace();
				}
			}
			
			if (me.getType() == RobotType.BARRACKS) {
				try {
					trySpawn(directions[rand.nextInt(8)]);
				} catch (Exception e) {
					System.out.println("BARRACKS Exception");
					e.printStackTrace();
				}
			}
			
			me.yield();
		}
	}
	
	static void HQBroadcast() throws GameActionException {
		RobotInfo[] myGuys = me.senseNearbyRobots(28800, goodGuys);
		int[] unitNumbers = new int[19];
		for (RobotInfo j : myGuys) {
			RobotType type = j.type;
			if (type == RobotType.BEAVER) {
				unitNumbers[0]++;
			} else if (type == RobotType.MINER) {
				unitNumbers[1]++;
			} else if (type == RobotType.COMPUTER) {
				unitNumbers[2]++;
			} else if (type == RobotType.SOLDIER) {
				unitNumbers[3]++;
			} else if (type == RobotType.BASHER) {
				unitNumbers[4]++;
			} else if (type == RobotType.DRONE) {
				unitNumbers[5]++;
			} else if (type == RobotType.TANK) {
				unitNumbers[6]++;
			} else if (type == RobotType.COMMANDER) {
				unitNumbers[7]++;
			} else if (type == RobotType.LAUNCHER) {
				unitNumbers[8]++;
			} else if (type == RobotType.MISSILE) {
				unitNumbers[9]++;
			} else if (type == RobotType.SUPPLYDEPOT) {
				unitNumbers[10]++;
			} else if (type == RobotType.MINERFACTORY) {
				unitNumbers[11]++;
			} else if (type == RobotType.TECHNOLOGYINSTITUTE) {
				unitNumbers[12]++;
			} else if (type == RobotType.BARRACKS) {
				unitNumbers[13]++;
			} else if (type == RobotType.HELIPAD) {
				unitNumbers[14]++;
			} else if (type == RobotType.TRAININGFIELD) {
				unitNumbers[15]++;
			} else if (type == RobotType.TANKFACTORY) {
				unitNumbers[16]++;
			} else if (type == RobotType.AEROSPACELAB) {
				unitNumbers[17]++;
			} else if (type == RobotType.HANDWASHSTATION) {
				unitNumbers[18]++;
			}
		}
		
		for (int k = 0; k < 19; k++) {
			if (unitNumbers[k] > 0) {
				me.broadcast(20000 + k, unitNumbers[k]);
			}
		}
	}
	
/*	static void map(MapLocation center) throws GameActionException {
		MapLocation here = me.getLocation();
		MapLocation[] range = MapLocation.getAllMapLocationsWithinRadiusSq(here, 24);
		int dx, dy, bIndex;
		for (MapLocation i : range) {
			dx = center.x - i.x;
			dy = center.y - i.y;
			bIndex = 7139 - 120 * dy - dx;
			if (me.readBroadcast(bIndex) == 0) {
				TerrainTile tile = me.senseTerrainTile(i);
				if (tile == TerrainTile.NORMAL) {
					me.broadcast(bIndex, 1);
				} else if (tile == TerrainTile.VOID) {
					me.broadcast(bIndex, 2);
				} else if (tile == TerrainTile.OFF_MAP) {
					me.broadcast(bIndex, 3);
				}
			}
		}
		
		if (me.getType() != RobotType.DRONE && me.getType() != RobotType.MISSILE) {
			
		}
	}*/
	
	static void HQTransfer(MapLocation home) throws GameActionException {
//		System.out.println(Clock.getBytecodesLeft() + " Transfer start");
		RobotInfo[] nearGoods = me.senseNearbyRobots(GameConstants.SUPPLY_TRANSFER_RADIUS_SQUARED, goodGuys);
//		System.out.println(Clock.getBytecodesLeft() + " Sense robots");
		double sup = me.getSupplyLevel();
//		System.out.println(Clock.getBytecodesLeft() + " Check supply");
		if (nearGoods.length > 1 && sup >= 1) {
	//		System.out.println(Clock.getBytecodesLeft() + " Sense HQ");
			for (int i = nearGoods.length - 1; i > 0; i--) {
				if (nearGoods[i].supplyLevel < nearGoods[i-1].supplyLevel || (int) nearGoods[i].supplyLevel == (int) nearGoods[i-1].supplyLevel && nearGoods[i].location.distanceSquaredTo(home) > nearGoods[i-1].location.distanceSquaredTo(home)) {
					RobotInfo tmp = nearGoods[i];
					nearGoods[i] = nearGoods[i-1];
					nearGoods[i-1] = tmp;
				}
		//		System.out.println(Clock.getBytecodesLeft() + " Sort robots A " + i);
			}
			
			if (nearGoods.length > 2) {
				for (int i = nearGoods.length - 1; i > 1; i--) {
					if (nearGoods[i].supplyLevel < nearGoods[i-1].supplyLevel || (int) nearGoods[i].supplyLevel == (int) nearGoods[i-1].supplyLevel && nearGoods[i].location.distanceSquaredTo(home) > nearGoods[i-1].location.distanceSquaredTo(home)) {
						RobotInfo tmp = nearGoods[i];
						nearGoods[i] = nearGoods[i-1];
						nearGoods[i-1] = tmp;
					}
			//		System.out.println(Clock.getBytecodesLeft() + " Sort robots B " + i);
				}
			}
			
			for (int i = 0; i < 2; i++) {
				if (Clock.getBytecodesLeft() > 525) {
					me.transferSupplies((int) sup/2, nearGoods[i].location);
			//		System.out.println(Clock.getBytecodesLeft() + " Transfer complete " + i);
				}
			}
		} else if (nearGoods.length > 0 && sup >= 1 && Clock.getBytecodesLeft() > 525) {
	//		System.out.println(Clock.getBytecodesLeft() + " Check bytecodes left");	
			me.transferSupplies((int) sup, nearGoods[0].location);
	//		System.out.println(Clock.getBytecodesLeft() + "Transfer complete");
		}
	}
	
	static void unitTransfer(MapLocation home) throws GameActionException {
//		System.out.println(Clock.getBytecodesLeft() + " Transfer start");
		RobotInfo[] nearGoods = me.senseNearbyRobots(GameConstants.SUPPLY_TRANSFER_RADIUS_SQUARED, goodGuys);
//		System.out.println(Clock.getBytecodesLeft() + " Sense robots");
		double sup = me.getSupplyLevel();
//		System.out.println(Clock.getBytecodesLeft() + " Check supply");
		if (nearGoods.length > 1 && sup >= 1) {
	//		System.out.println(Clock.getBytecodesLeft() + " Sense HQ");
			for (int i = nearGoods.length - 1; i > 0; i--) {
				if (nearGoods[i].supplyLevel < nearGoods[i-1].supplyLevel || (int) nearGoods[i].supplyLevel == (int) nearGoods[i-1].supplyLevel && nearGoods[i].location.distanceSquaredTo(home) > nearGoods[i-1].location.distanceSquaredTo(home)) {
					RobotInfo tmp = nearGoods[i];
					nearGoods[i] = nearGoods[i-1];
					nearGoods[i-1] = tmp;
				}
		//		System.out.println(Clock.getBytecodesLeft() + " Sort robots " + i);
			}
		}
		
		if (nearGoods.length > 0 && sup >= 1 && Clock.getBytecodesLeft() > 525) {
	//		System.out.println(Clock.getBytecodesLeft() + " Check bytecodes left");
			if (me.getType() == RobotType.TOWER) {
		//		System.out.println(Clock.getBytecodesLeft() + " Check if Tower type");
				me.transferSupplies((int) sup, nearGoods[0].location);
		//		System.out.println(Clock.getBytecodesLeft() + " Transfer complete");
			} else if (sup > nearGoods[0].supplyLevel) {
		//		System.out.println(Clock.getBytecodesLeft() + " Check if Tower type");
				me.transferSupplies((int) Math.floor((sup - nearGoods[0].supplyLevel) / 1.5), nearGoods[0].location);
		//		System.out.println(Clock.getBytecodesLeft() + "Transfer complete");
			}
		}
	}
	
	static void nearATK() throws GameActionException {
//		System.out.println(Clock.getBytecodesLeft() + " atk start");
		RobotInfo[] nearBads = me.senseNearbyRobots(atkRange, badGuys);
//		System.out.println(Clock.getBytecodesLeft() + " Sense robots");
		
		if (nearBads.length > 0) {
			MapLocation target = nearBads[0].location;
			
			if (nearBads.length > 1) {
				for (int i = 1; i < nearBads.length; i++) {
					if (nearBads[i].location.distanceSquaredTo(me.getLocation()) < target.distanceSquaredTo(me.getLocation())) {
						target = nearBads[i].location;
					}
			//		System.out.println(Clock.getBytecodesLeft() + " Sort robots");
				}
			}
			
			if (me.isWeaponReady()) {
		//		System.out.println(Clock.getBytecodesLeft() + " Check weapon");
				me.attackLocation(target);
		//		System.out.println(Clock.getBytecodesLeft() + " atk complete");
			}
		}
	}
	
	static void mine(int ids, Direction hq, int r, MapLocation gq) throws GameActionException{
		if (me.senseOre(me.getLocation()) > 4) {
			if (me.canMine() && me.isCoreReady()) {
				me.mine();
			}
		} else {
			double ore[] = new double[8];
			Direction there = Direction.NONE, here = Direction.NONE;
			double max = 0;
			double orehere = 0;
			for (int i = 0; i < 8; i++) {
				ore[i] = me.senseOre(me.getLocation().add(directions[i]));
				if (ore[i] > max) {
					max = ore[i];
					there = directions[i];
				}
				if (me.getLocation().directionTo(gq).opposite() == directions[i]) {
					orehere = ore[i];
				}
			}
			Arrays.sort(ore);
			
			if (orehere == max && me.isCoreReady() && me.canMove(here)) {
				me.move(here);
			} else if (me.isCoreReady() && me.canMove(there) && max > ore[4] && max > 4) {
				me.move(there);
			} else if (hq == Direction.NORTH || hq == Direction.EAST || hq == Direction.SOUTH || hq == Direction.WEST) {
//				BadHQ is North of GoodHQ
				if (r > 0 && me.canMove(hq.rotateRight().rotateRight())) {
					blockedMove(ids, hq.rotateRight().rotateRight(), gq);
				} else if (r < 1 && me.canMove(hq.rotateLeft().rotateLeft())) {
					blockedMove(ids, hq.rotateLeft().rotateLeft(), gq);
				} else if (r > 0 && me.canMove(hq.rotateLeft().rotateLeft())) {
					blockedMove(ids, hq.rotateLeft().rotateLeft(), gq);
				} else if (r < 1 && me.canMove(hq.rotateRight().rotateRight())) {
					blockedMove(ids, hq.rotateRight().rotateRight(), gq);
				} else {
//					Rotate direction away from HQ
					if (r > 0) {
						blockedMove(ids, hq.rotateRight().rotateRight(), "right2", gq);
					} else {
						blockedMove(ids, hq.rotateLeft().rotateLeft(), "left2", gq);
					}
				}
			} else if (hq == Direction.NORTH_EAST || hq == Direction.SOUTH_EAST || hq == Direction.SOUTH_WEST || hq == Direction.NORTH_WEST) {
//				BadHQ is Northeast of GoodHQ
				if (r > 0 && me.canMove(hq.rotateRight())) {
					blockedMove(ids, hq.rotateRight(), gq);
				} else if (r < 1 && me.canMove(hq.rotateLeft())) {
					blockedMove(ids, hq.rotateLeft(), gq);
				} else if (r > 0 && me.canMove(hq.rotateLeft())) {
					blockedMove(ids, hq.rotateLeft(), gq);
				} else if (r < 1 && me.canMove(hq.rotateRight())) {
					blockedMove(ids, hq.rotateRight(), gq);
				} else {
//					Rotate direction away from HQ
					if (r > 0) {
						blockedMove(ids, hq.rotateRight(), "right", gq);
					} else {
						blockedMove(ids, hq.rotateLeft(), "left", gq);
					}
				}
			}
		}
	}
	
	static void blockedMove(int i, Direction target, MapLocation hq) throws GameActionException {
		int counterl = 0, counterr = 0, m = me.readBroadcast(i), n = me.readBroadcast(i + 1);
		if (m == 0) {
			while (counterl < 8 && counterr < 8 && !me.canMove(target)) {
				if (me.canMove(target.rotateLeft()) && me.canMove(target.rotateRight()) && counterl == counterr || !me.canMove(target.rotateLeft()) && !me.canMove(target.rotateRight()) && counterl == counterr ) {
					switch (i % 2) {
						case 0:
							target = target.rotateLeft();
							counterl++;
							break;
						case 1:
							target = target.rotateRight();
							counterr++;
							break;
						default:
							target = null;
					}
				} else if (me.canMove(target.rotateLeft()) && counterl >= counterr) {
					target = target.rotateLeft();
					break;
				} else if (me.canMove(target.rotateRight()) && counterr >= counterl) {
					target = target.rotateRight();
					break;
				} else if (counterl > counterr) {
					target = target.rotateLeft();
				} else {
					target = target.rotateRight();
				}
			}
			
			if (counterl < 8 && counterr < 8 && me.isCoreReady()) {
				me.move(target);
				me.broadcast(i + 1, direct(target));
				if (counterl > 4 || counterr > 4) {
					me.broadcast(i, 1);
				}
			}
		} else {
			TerrainTile tile;
			if (me.getLocation().add(target.rotateLeft().rotateLeft()).distanceSquaredTo(hq) > me.getLocation().add(target.rotateRight().rotateRight()).distanceSquaredTo(hq)) {
				target = target.rotateLeft().rotateLeft();
				tile = me.senseTerrainTile(me.getLocation().add(target));
			} else {
				target = target.rotateRight().rotateRight();
				tile = me.senseTerrainTile(me.getLocation().add(target));
			}
			if (tile == TerrainTile.VOID) {
				randMove(directions[n]);
			} else if (tile == TerrainTile.NORMAL) {
				me.broadcast(i, 0);
				blockedMove(i, target, hq);
			}
		}
	}
	
	static void blockedMove(int i, Direction target, String turn, MapLocation hq) throws GameActionException {
		int counter = 0, m = me.readBroadcast(i), n = me.readBroadcast(i + 1);
		
		if (m == 0) {
			switch (turn) {
			case "left":
				while (counter < 8 && !me.canMove(target)) {
					target = target.rotateLeft();
					counter++;
				}
				break;
			case "right":
				while (counter < 8 && !me.canMove(target)) {
					target = target.rotateRight();
					counter++;
				}
				break;
			case "left2":
				while (counter < 4 && !me.canMove(target)) {
					target = target.rotateLeft().rotateLeft();
					counter++;
				}
				break;
			case "right2":
				while (counter < 4 && !me.canMove(target)) {
					target = target.rotateRight().rotateRight();
					counter++;
				}
				break;
			}
			
			if (counter < 8 && me.isCoreReady()) {
				me.broadcast(i + 1, direct(target));
				me.move(target);
			}
		} else {
			TerrainTile tile;
			if (me.getLocation().add(target.rotateLeft().rotateLeft()).distanceSquaredTo(hq) > me.getLocation().add(target.rotateRight().rotateRight()).distanceSquaredTo(hq)) {
				target = target.rotateLeft().rotateLeft();
				tile = me.senseTerrainTile(me.getLocation().add(target));
			} else {
				target = target.rotateRight().rotateRight();
				tile = me.senseTerrainTile(me.getLocation().add(target));
			}
			if (tile == TerrainTile.VOID) {
				randMove(directions[n]);
			} else if (tile == TerrainTile.NORMAL) {
				me.broadcast(i, 0);
				blockedMove(i, target, hq);
			}
		}
		
	}
	
	static void HQMove(int i, MapLocation bad, MapLocation good) throws GameActionException {
//		System.out.println(Clock.getBytecodesLeft() + " Move start");
		Direction toHQ = me.getLocation().directionTo(bad);
//		System.out.println(Clock.getBytecodesLeft() + " get HQ direction");
		
		blockedMove(i, toHQ, good);
	}
	
	static void randMove(Direction d) throws GameActionException {
		int counter = 0;
		while (counter < 8 && !me.canMove(d)) {
			d = d.rotateRight();
			counter++;
		}
		if (me.isCoreReady()) {
			me.move(d);
		}
	}
	
	static void trySpawn(Direction d) throws GameActionException {
//		System.out.println(Clock.getBytecodesLeft() + " Spawn start");
		int counter = 0;
		RobotType type = RobotType.HQ;

		if (me.getType() == RobotType.HQ && me.readBroadcast(20000) < 1) {
			type = RobotType.BEAVER;
		} else if (me.getType() == RobotType.MINERFACTORY && me.readBroadcast(20001) < 15) {
			type = RobotType.MINER;
		} else if (me.getType() == RobotType.BARRACKS && me.getTeamOre() > 60) {
			type = RobotType.SOLDIER;
		}
		
		if (type != RobotType.HQ) {
			while (counter < 8 && !me.canSpawn(d, type)) {
//				System.out.println(Clock.getBytecodesLeft() + " Check Spawn");
				d = d.rotateLeft();
				counter++;
			}
			if (me.isCoreReady() && counter < 8) {
//				System.out.println(Clock.getBytecodesLeft() + " Check ready");
				me.spawn(d, type);
//				System.out.println(Clock.getBytecodesLeft() + " Spawn complete");
			}
		}
	}
	
	static void beaverMove(int i, Direction d, MapLocation hq, Random r) throws GameActionException {
		if (me.readBroadcast(20011) < 1 && me.getLocation() != hq.add(d.opposite())) {
			randMove(me.getLocation().directionTo(hq.add(d.opposite())));
		} else if (me.readBroadcast(20011) < 2 && me.getLocation() != hq.add(d.opposite()) && me.getTeamOre() >= 500) {
			randMove(me.getLocation().directionTo(hq.add(d.opposite())));
		} else if (me.readBroadcast(20013) < 3 && me.getLocation() != hq.add(d, 2) && me.getTeamOre() >= 300) {
			randMove(me.getLocation().directionTo(hq.add(d, 2)));
		} else {
			randMove(directions[r.nextInt(8)]);
		}
	}
	
	static void tryBuild(Direction d, MapLocation hq) throws GameActionException {
		int counter = 0;
		RobotType type = RobotType.HQ;
		if (me.readBroadcast(20011) < 2 && me.getTeamOre() >= 500 && me.getLocation().directionTo(hq) == d) {
			type = RobotType.MINERFACTORY;
			d = d.opposite().rotateRight();
		} else if (me.readBroadcast(20013) < 3 && me.getLocation().directionTo(hq) == d.opposite() && me.getTeamOre() >= 300) {
			type = RobotType.BARRACKS;
		} else if (me.readBroadcast(20011) >= 2 && me.readBroadcast(20013) >= 3 && me.getTeamOre() >= 100) { // Faulty. Builds too many blocks paths.
			type = RobotType.SUPPLYDEPOT;
		}
		
		if (type != RobotType.HQ && me.isCoreReady()) {
			while (counter < 4 && !me.canBuild(d, type)) {
//				System.out.println(Clock.getBytecodesLeft() + " Check Spawn");
				d = d.rotateLeft().rotateLeft();
				counter++;
			}
			if (me.isCoreReady() && counter < 8) {
//				System.out.println(Clock.getBytecodesLeft() + " Check ready");
				me.build(d, type);
//				System.out.println(Clock.getBytecodesLeft() + " Spawn complete");
			}
		}
	}
	
	static void tryMine() throws GameActionException {
//		System.out.println(Clock.getBytecodesLeft() + " Mine start");
		if (me.senseOre(me.getLocation()) > 0 && me.canMine() && me.isCoreReady()) {
	//		System.out.println(Clock.getBytecodesLeft() + " Check location + Check mine + Check ready");
			me.mine();
	//		System.out.println(Clock.getBytecodesLeft() + " Mine complete");
		}
	}
	
	static void sortTowers(MapLocation towers[], MapLocation hq) throws GameActionException {
//		System.out.println(Clock.getBytecodesLeft() + " Tower Sort start");
		int a = towers.length;
		int[] threat = new int[a];
		threat[a - 1] = towers[a-1].distanceSquaredTo(hq);
		
//		Threat level
		for (int i = 0; i < a - 1; i++) {
			int distHQ = towers[i].distanceSquaredTo(hq);
			for (int j = i + 1; j < a; j++) {
				for (int k = 0; k < a; k++) {
					if (i == k || j == k) {
//						Distance between towers
						threat[k] += towers[i].distanceSquaredTo(towers[j]);
					}
				}
			}
			
			threat[i] += distHQ;
		}
		
//		Threat sort
		Arrays.sort(threat);
	}
	
	static void moveTo(MapLocation m, MapLocation hq) throws GameActionException {
		if (me.getLocation() != m) {
			blockedMove(me.getID(), me.getLocation().directionTo(m), hq);
		}
	}
	
	static int direct(Direction d) throws GameActionException {
		int a = 0;
		for (int i = 0; i < 8; i++) {
			if (directions[i] == d) {
				a = i;
			}
		}
		return a;
	}
}
