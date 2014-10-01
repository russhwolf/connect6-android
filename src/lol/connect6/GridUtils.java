package lol.connect6;


/**
 * Consider a grid with index 1 at the origin (0,0),
 * counting out as displayed below. These functions
 * translate between coordinates (x,y) and indices i.
 * 
 *    _|_______|_______|_______|_______|_______|_______|_______|_   
 * ..._|___37__|___36__|___35__|___34__|___33__|___32__|___31__|_...
 * ..._|___38__|___17__|___16__|___15__|___14__|___13__|___30__|_...
 * ..._|___39__|___18__|____5__|____4__|____3__|___12__|___29__|_...
 * ..._|___40__|___19__|____6__|____1__|____2__|___11__|___28__|_...
 * ..._|___41__|___20__|____7__|____8__|____9__|___10__|___27__|_...
 * ..._|___42__|___21__|___22__|___23__|___24__|___25__|___26__|_...
 * ..._|___43__|___44__|___45__|___46__|___47__|___48__|___49__|_...
 *     |       |       |       |       |       |       |       |    
 * 
 * 
 * 
 *    _|_______|_______|_______|_______|_______|_______|_______|_   
 * ..._|_-3,_3_|_-2,_3_|_-1,_3_|__0,_3_|__1,_3_|__2,_3_|__3,_3_|_...
 * ..._|_-3,_2_|_-2,_2_|_-1,_2_|__0,_2_|__1,_2_|__2,_2_|__3,_2_|_...
 * ..._|_-3,_1_|_-2,_1_|_-1,_1_|__0,_1_|__1,_1_|__2,_1_|__3,_1_|_...
 * ..._|_-3,_0_|_-2,_0_|_-1,_0_|__0,_0_|__1,_0_|__2,_0_|__3,_0_|_...
 * ..._|_-3,-1_|_-2,-1_|_-1,-1_|__0,-1_|__1,-1_|__2,-1_|__3,-1_|_...
 * ..._|_-3,-2_|_-2,-2_|_-1,-2_|__0,-2_|__1,-2_|__2,-2_|__3,-2_|_...
 * ..._|_-3,-3_|_-2,-3_|_-1,-3_|__0,-3_|__1,-3_|__2,-3_|__3,-3_|_...
 *     |       |       |       |       |       |       |       |    
 *     
 */
public class GridUtils {

	private GridUtils() {
		throw new Error();
	}
	
	public static int[] indexToCoords(int index) {
		int[] out = new int[2];
		indexToCoords(index, out);
		return out;
	}
	
	public static void indexToCoords(int index, int[] out) {
		int layer = indexToLayer(index);
//		int[] corners = layerToCornerIndex(layer);
//		int c1 = corners[0], c2 = corners[1], c3 = corners[2], c4 = corners[3];
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
	
	public static int coordsToIndex(int x, int y) {
		int layer = coordsToLayer(x, y);
//		int[] corners = layerToCornerIndex(layer);
//		int c1 = corners[0], c2 = corners[1], c3 = corners[2], c4 = corners[3];
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
	
//	private static int[] layerToCornerIndex(int layer) {
//		int a = 2*layer;
//		int c1 = a*a - a + 1;
//		int c2 = c1 + a;
//		int c3 = c2 + a;
//		int c4 = c3 + a;
//		
//		int[] out = {c1, c2, c3, c4};
//		return out;
//	}

}
