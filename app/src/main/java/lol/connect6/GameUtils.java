package lol.connect6;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.util.Log;
import android.util.SparseArray;

public class GameUtils {
	public static final String PREFS_SAVEGAME = "lol.connect6.savegame";

	public static final String KEY_AI1 = "ai1";
	public static final String KEY_AI2 = "ai2";
	
	private static final int[] COORDS = new int[2];
	
	private GameUtils() {
		throw new Error();
	}

	private static final Random mRandom = new Random();
	
	public static void aiMove(List<Integer> moves, int ai) {
		if (ai <= 0) return;
		
		int moveCount = moves.size();
		if (moveCount <= 0) {
			// If AI moves first, go in the center
			moves.add(GridUtils.ORIGIN_INDEX);
			return;
		}
		
		switch(ai) {
			case 1:
				rngAi(moves);
				break;
			case 2:
				builderAi(moves);
				break;
			case 3:
				threatCountAi(moves);
				break;
			default:
				throw new IllegalArgumentException();
		}

		if (moves.size() != moveCount + 2) throw new IllegalStateException("ERROR! AI performed incorrect number of moves!");
	}

	public static void threatCountAi(List<Integer> moves) {
		List<Integer> threats = countThreats(moves);
		switch (threats.size()) {
			case 0:
				builderAi(moves);
				break;
			case 1:
				builderAi(moves);
				moves.set(moves.size()-2, threats.get(0));
				break;
			default:
				moves.add(threats.get(0));
				moves.add(threats.get(1));
				break;
		}
	}

	private static List<Integer> countThreats(List<Integer> allMoves) {

		List<Integer> threats = new ArrayList<>(2);
		if (allMoves.size() < 6) return threats;

		List<Integer> attackerMoves = new ArrayList<>(allMoves.size()/2 + 1);
		List<Integer> defenderMoves = new ArrayList<>(allMoves.size()/2 - 1);
		boolean wasP1Turn = !isP1Turn(allMoves.size());

		int minX = 0, maxX = 0, minY = 0, maxY = 0;

		for (int i = 0, s = allMoves.size(); i < s; i++) {
			if (isP1Turn(i) == wasP1Turn) {
				attackerMoves.add(allMoves.get(i));
			} else {
				defenderMoves.add(allMoves.get(i));
			}

			GridUtils.indexToCoords(i, COORDS);
			minX = -2 + (minX < COORDS[0]? minX : COORDS[0]);
			maxX = 2 + (maxX > COORDS[0]? maxX : COORDS[0]);
			minY = -2 + (minY < COORDS[1]? minY : COORDS[1]);
			maxY = 2 + (maxY > COORDS[1]? maxY : COORDS[1]);
		}
		Log.d("AI", "Looking in range minX="+minX+", maxX="+maxX+", minY="+minY+", maxY="+maxY);

		Log.d("AI","Checking horizontal lines");
		for (int i = minX; i <= maxX; i++) {
			countThreatsLine(attackerMoves, defenderMoves, minX, i, 1, 0, maxX, maxY, threats);
		}
		Log.d("AI","Checking vertical lines");
		for (int i = minY; i <= maxY; i++) {
			countThreatsLine(attackerMoves, defenderMoves, i, minY, 0, 1, maxX, maxY, threats);
		}
		Log.d("AI","Checking diagonal-up lines");
		for (int i = minX - (maxY - minY); i <= maxX; i++) {
			countThreatsLine(attackerMoves, defenderMoves, i, minY, 1, 1, maxX, maxY, threats);
		}
		Log.d("AI","Checking diagonal-down lines");
		for (int i = minX - (maxY - minY); i <= maxX; i++) {
			countThreatsLine(attackerMoves, defenderMoves, i, maxY, 1,-1, maxX, maxY, threats);
		}

		return threats;
	}

	private static void countThreatsLine(List<Integer> attackerMoves, List<Integer> defenderMoves, int x0, int y0, int dx, int dy, int xmax, int ymax, List<Integer> threats) {
		int[] window = new int[6];

		while (true) {
			for (int i = 0; i < window.length; i++) {
				int x = x0 + i*dx;
				int y = y0 + i*dy;
				if (x > xmax || y > ymax) return;
				window[i] = GridUtils.coordsToIndex(x,y);
			}

			int attackerCount = 0, defenderCount = 0, threatCount = 0;
			for (int i = 0; i < window.length; i++) {
				if (attackerMoves.contains(window[i])) attackerCount++;
				if (defenderMoves.contains(window[i])) defenderCount++;
				if (threats.contains(window[i])) threatCount++;
			}
			if (attackerCount >= 4 && defenderCount <= 0 && threatCount <= 0) {
				for (int i = window.length - 1; i >= 0; i--) {
					if (!attackerMoves.contains(window[i])) {
						Log.d("AI","Adding threat in index "+window[i]);
						threats.add(window[i]);
						break;
					}
				}
			}

			x0 += dx;
			y0 += dy;
		}
	}
	
	public static boolean checkWin(List<Integer> allMoves) { 
		
		// Construct an array of all moves by the player whose turn has just finished
		List<Integer> moves = new ArrayList<>(allMoves.size()/2 + 1);
		boolean wasP1Turn = !isP1Turn(allMoves.size());
		for (int i = 0, s = allMoves.size(); i < s; i++) {
			if (isP1Turn(i) == wasP1Turn) {
				moves.add(allMoves.get(i));
			}
		}

		// This is inefficient because we'll check each line as many times as there are pieces in that line
		for (int i = 0; i < moves.size(); i++) {
			GridUtils.indexToCoords(moves.get(i), COORDS);
			int x = COORDS[0], y = COORDS[1];

			if (winCount(moves, x, y, 0, 1)) return true;
			if (winCount(moves, x, y, 1, 0)) return true;
			if (winCount(moves, x, y, 1, 1)) return true;
			if (winCount(moves, x, y, 1,-1)) return true;
			
		}
		return false;
	}
	
	private static boolean winCount(List<Integer> moves, int x, int y, int dx, int dy) {
		if (Math.abs(dx*dy) > 1) throw new IllegalArgumentException();
		int count1 = 0, count2 = 0;
		for (int i = 1; i < 6; i++) {
			int index = GridUtils.coordsToIndex(x + i*dx, y + i*dy);
			if (!moves.contains(index)) {
				count1 = i-1;
				break;
			}
		}
		for (int i = 1; i < 6; i++) {
			int index = GridUtils.coordsToIndex(x - i*dx, y - i*dy);
			if (!moves.contains(index)) {
				count2 = i-1;
				break;
			}
		}
		return count1 + count2 + 1 >= 6;
	}
	
	private static void builderAi(List<Integer> allMoves) {

		final boolean isP1 = isP1Turn(allMoves.size());

		List<Integer> moves1 = new ArrayList<>(allMoves.size()/2 + 1);
		List<Integer> moves2 = new ArrayList<>(allMoves.size()/2 + 1); 
		for (int i = 0, s = allMoves.size(); i < s; i++) {
			if (isP1Turn(i)) {
				moves1.add(allMoves.get(i));
			} else {
				moves2.add(allMoves.get(i));
			}
		}
		
		List<Integer> moves = isP1? moves1 : moves2;
		List<Integer> other = isP1? moves2 : moves1;
		SparseArray<Integer> count1 = new SparseArray<>();
		SparseArray<Integer> count2 = new SparseArray<>();
		SparseArray<Integer> count3 = new SparseArray<>();
		SparseArray<Integer> count4 = new SparseArray<>();
		SparseArray<Integer> max1 = new SparseArray<>();
		SparseArray<Integer> max2 = new SparseArray<>();
		SparseArray<Integer> max3 = new SparseArray<>();
		SparseArray<Integer> max4 = new SparseArray<>();
		for (int i = 0, s = moves.size(); i < s; i++) {
			int index = moves.get(i);
			GridUtils.indexToCoords(index, COORDS);
			int x = COORDS[0], y = COORDS[1];

			builderCount(moves, other, x, y, 0, 1, COORDS);
			count1.put(index, COORDS[0]);
			max1.put(index, COORDS[1]);

			builderCount(moves, other, x, y, 1, 0, COORDS);
			count2.put(index, COORDS[0]);
			max2.put(index, COORDS[1]);

			builderCount(moves, other, x, y, 1, 1, COORDS);
			count3.put(index, COORDS[0]);
			max3.put(index, COORDS[1]);

			builderCount(moves, other, x, y, 1,-1, COORDS);
			count4.put(index, COORDS[0]);
			max4.put(index, COORDS[1]);
		}
		
		int bestCount = 0;
		int bestIndex = 0;
		int bestDX = 0;
		int bestDY = 0;
		for (int i = 0, s = count1.size(); i < s; i++) {
			int index = count1.keyAt(i);
			if (max1.get(index) > 6) {
				int count = count1.get(index);
				if (count > bestCount) {
					bestCount = count;
					bestIndex = index;
					bestDX = 0;
					bestDY = 1;
				}
			}
		}
		for (int i = 0, s = count2.size(); i < s; i++) {
			int index = count2.keyAt(i);
			if (max2.get(index) > 6) {
				int count = count2.get(index);
				if (count > bestCount) {
					bestCount = count;
					bestIndex = index;
					bestDX = 1;
					bestDY = 0;
				}
			}
		}
		for (int i = 0, s = count3.size(); i < s; i++) {
			int index = count3.keyAt(i);
			if (max3.get(index) > 6) {
				int count = count3.get(index);
				if (count > bestCount) {
					bestCount = count;
					bestIndex = index;
					bestDX = 1;
					bestDY = 1;
				}
			}
		}
		for (int i = 0, s = count4.size(); i < s; i++) {
			int index = count4.keyAt(i);
			if (max4.get(index) > 6) {
				int count = count4.get(index);
				if (count > bestCount) {
					bestCount = count;
					bestIndex = index;
					bestDX = 1;
					bestDY = -1;
				}
			}
		}
		
		if (bestIndex == 0) {
			rngAi(allMoves);
			return;
		}
		
		GridUtils.indexToCoords(bestIndex, COORDS);
		int x = COORDS[0], y = COORDS[1], index1 = 0, index2 = 0;
		for (int i = 1; i < 6; i ++) {
			int index = GridUtils.coordsToIndex(x + i*bestDX, y + i*bestDY);
			if (other.contains(index)) break;
			if (!moves.contains(index)) {
				if (index1 == 0) {
					index1 = index;
				} else {
					index2 = index;
					break;
				}
			}
		}
		if (index2 == 0) for (int i = 1; i < 6; i ++) {
			int index = GridUtils.coordsToIndex(x - i*bestDX, y - i*bestDY);
			if (other.contains(index)) break;
			if (!moves.contains(index)) {
				if (index1 == 0) {
					index1 = index;
				} else {
					index2 = index;
					break;
				}
			}
		}
		
		if (index2 == 0) {
			rngAi(allMoves);
			if (index1 != 0) {
				allMoves.set(allMoves.size()-1, index1);
			}
		} else {
			allMoves.add(index1);
			allMoves.add(index2);
		}
		
		
		
	}
	
	private static void builderCount(List<Integer> moves, List<Integer> other, int x, int y, int dx, int dy, int[] out) {
		if (Math.abs(dx*dy) > 1) throw new IllegalArgumentException();
		int count1 = 0, count2 = 0, max1 = 6, max2 = 6;
		for (int i = 1; i < 6; i++) {
			int index = GridUtils.coordsToIndex(x + i*dx, y + i*dy);
			if (moves.contains(index)) {
				count1++;
			} else if (other.contains(index)) {
				max1 = i-1;
				break;
			}
		}
		for (int i = 1; i < 6; i++) {
			int index = GridUtils.coordsToIndex(x - i*dx, y - i*dy);
			if (moves.contains(index)) {
				count2++;
			} else if (other.contains(index)) {
				max2 = i-1;
				break;
			}
		}
		
		int count = count1 + count2 + 1;
		int max = max1 + max2 + 1;
		
		out[0] = count;
		out[1] = max;
	}
	
	private static void rngAi(List<Integer> allMoves) {
		int minX = 0, maxX = 0, minY = 0, maxY = 0;
		for (int i = 0, s = allMoves.size(); i < s; i++) {
			GridUtils.indexToCoords(i, COORDS);
			minX = minX < COORDS[0]? minX : COORDS[0];
			maxX = maxX > COORDS[0]? maxX : COORDS[0];
			minY = minY < COORDS[1]? minY : COORDS[1];
			maxY = maxY > COORDS[1]? maxY : COORDS[1];
		}
		randomMove(allMoves, minX, maxX, minY, maxY);
		randomMove(allMoves, minX, maxX, minY, maxY);
	}
	
	private static void randomMove(List<Integer> allMoves, int minX, int maxX, int minY, int maxY) {
		int index = allMoves.get(0);
		while (allMoves.contains(index)) {
			int x = mRandom.nextInt(maxX-minX + 4) + minX - 2;
			int y = mRandom.nextInt(maxY-minY + 4) + minY - 2;
			index = GridUtils.coordsToIndex(x, y);
		}
		allMoves.add(index);
	}

	static boolean isP1Turn(int turn) {
		return (turn+1)%4 < 2;
	}
}
