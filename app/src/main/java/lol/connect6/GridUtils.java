package lol.connect6;


/**
 * Consider a grid with index 1 at the origin (0,0),
 * counting out as displayed below. These functions
 * translate between coordinates (x,y) and indices i.
 * <p>
 * <pre>
 *     |       |       |       |       |       |       |       |    
 * ...-+-------+-------+-------+-------+-------+-------+-------+-...
 * ... |   37  |   36  |   35  |   34  |   33  |   32  |   31  | ...
 * ...-+-------+-------+-------+-------+-------+-------+-------+-...
 * ... |   38  |   17  |   16  |   15  |   14  |   13  |   30  | ...
 * ...-+-------+-------+-------+-------+-------+-------+-------+-...
 * ... |   39  |   18  |    5  |    4  |    3  |   12  |   29  | ...
 * ...-+-------+-------+-------+-------+-------+-------+-------+-...
 * ... |   40  |   19  |    6  |    1  |    2  |   11  |   28  | ...
 * ...-+-------+-------+-------+-------+-------+-------+-------+-...
 * ... |   41  |   20  |    7  |    8  |    9  |   10  |   27  | ...
 * ...-+-------+-------+-------+-------+-------+-------+-------+-...
 * ... |   42  |   21  |   22  |   23  |   24  |   25  |   26  | ...
 * ...-+-------+-------+-------+-------+-------+-------+-------+-...
 * ... |   43  |   44  |   45  |   46  |   47  |   48  |   49  | ...
 * ...-+-------+-------+-------+-------+-------+-------+-------+-...
 *     |       |       |       |       |       |       |       |    
 * </pre>
 * <p>
 * <pre>
 *     |       |       |       |       |       |       |       |    
 * ...-+-------+-------+-------+-------+-------+-------+-------+-...
 * ... | -3, 3 | -2, 3 | -1, 3 |  0, 3 |  1, 3 |  2, 3 |  3, 3 | ...
 * ...-+-------+-------+-------+-------+-------+-------+-------+-...
 * ... | -3, 2 | -2, 2 | -1, 2 |  0, 2 |  1, 2 |  2, 2 |  3, 2 | ...
 * ...-+-------+-------+-------+-------+-------+-------+-------+-...
 * ... | -3, 1 | -2, 1 | -1, 1 |  0, 1 |  1, 1 |  2, 1 |  3, 1 | ...
 * ...-+-------+-------+-------+-------+-------+-------+-------+-...
 * ... | -3, 0 | -2, 0 | -1, 0 |  0, 0 |  1, 0 |  2, 0 |  3, 0 | ...
 * ...-+-------+-------+-------+-------+-------+-------+-------+-...
 * ... | -3,-1 | -2,-1 | -1,-1 |  0,-1 |  1,-1 |  2,-1 |  3,-1 | ...
 * ...-+-------+-------+-------+-------+-------+-------+-------+-...
 * ... | -3,-2 | -2,-2 | -1,-2 |  0,-2 |  1,-2 |  2,-2 |  3,-2 | ...
 * ...-+-------+-------+-------+-------+-------+-------+-------+-...
 * ... | -3,-3 | -2,-3 | -1,-3 |  0,-3 |  1,-3 |  2,-3 |  3,-3 | ...
 * ...-+-------+-------+-------+-------+-------+-------+-------+-...
 *     |       |       |       |       |       |       |       |    
 * </pre>
 */
public class GridUtils {

	public static final int ORIGIN_INDEX = 1;
	public static final int ORIGIN_X = 0;
	public static final int ORIGIN_Y = 0;
	
	private GridUtils() {
		throw new Error();
	}
	
//	/**
//	 * Takes the given index and returns the corresponding coordinates
//	 * into a new array.
//	 */
//	public static int[] indexToCoords(int index) {
//		int[] out = new int[2];
//		indexToCoords(index, out);
//		return out;
//	}
	
	/**
	 * Takes the given index and returns the corresponding coordinates
	 * into the supplied array.
	 */
	public static void indexToCoords(int index, int[] out) {
		int layer = indexToLayer(index);
		int a = 2*layer;
		int c1 = a*a - a + 1;
		int c2 = c1 + a;
		int c3 = c2 + a;
		int c4 = c3 + a;
		
		int x, y;
		if (index <= c1) {
			x = layer;
			y = layer - (c1 - index);
		} else if (index <= c2) {
			x = -layer + (c2 - index);
			y = layer;
		} else if (index <= c3) {
			x = -layer;
			y = -layer + (c3 - index);
		} else if (index <= c4) {
			x = layer - (c4 - index);
			y = -layer;
		} else {
			throw new IllegalStateException();
		}
		
		out[0] = x;
		out[1] = y;
	}
	
	/**
	 * Takes the given coordinates and returns the corresponding index.
	 */
	public static int coordsToIndex(int x, int y) {
		int layer = coordsToLayer(x, y);
		int a = 2*layer;
		int c1 = a*a - a + 1;
		int c2 = c1 + a;
		int c3 = c2 + a;
		int c4 = c3 + a;
		
		int index;
		if (y == -layer) {
			index = c4 - (layer - x);
		} else if (x == -layer) {
			index = c3 - (layer + y);
		} else if (y == layer) {
			index = c2 - (layer + x);
		} else if (x == layer) {
			index = c1 - (layer - y);
		} else {
			throw new IllegalStateException();
		}
		
		return index;
	}

	private static int indexToLayer(int index) {
		int layer = 0;
		while ( (2*layer+1)*(2*layer+1) < index) {
			layer++;
		}
		return layer;
//		return (int) (Math.ceil((Math.sqrt(index)-1)/2) + 0.5);
	}
	
	private static int coordsToLayer(int x, int y) {
		return Math.max(Math.abs(x), Math.abs(y));
	}

}
