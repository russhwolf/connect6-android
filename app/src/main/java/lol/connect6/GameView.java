package lol.connect6;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ScaleGestureDetectorCompat;
import android.support.v4.view.WindowCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

public class GameView extends View implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener, ScaleGestureDetector.OnScaleGestureListener {
	
	private static final String KEY_MOVE = "move";
	private static final String KEY_PLAYING = "playing";
	private static final String KEY_BOARD_SIZE = "board_size";
	
	// Why on earth aren't these constants auto-generated in R somewhere?
	public static final int PLACEMENT_STYLE_BOXES = 0;
	public static final int PLACEMENT_STYLE_INTERSECTIONS = 1;
		
	private static final int[] COORDS = new int[2];
	
//	private final int mBoardSize;

	private final float mMinScale;
	private final float mMaxScale;
	
	private final float mDotRadius;
	private final int mDotSpacing;
	private final float mGridSize;
	private final int mPlacementStyle;
	private final int mPlacingAlpha;
	private final Drawable mPlayer1Drawable;
	private final Drawable mPlayer2Drawable;
	private final int mThickLineSpacing;
	
	private final GestureDetector mGestureDetector;
	private final ScaleGestureDetector mScaleGestureDetector;

	private final GameState mState;
	
	private final Paint mLinePaint;
	private final Paint mThickLinePaint;
	private final Paint mBackgroundPaint;
	private final Paint mDotPaint;

	private OnAiMoveListener mListener;
	private boolean mAiMoving = false;
	
	public GameView(Context context) {
		this(context, null, 0);
	}

	public GameView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	
	public GameView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);

		TypedArray ta;
		
		ta = context.getTheme().obtainStyledAttributes(new int[]{R.attr.gameBoardSize, R.attr.maxScale, R.attr.minScale});
		int boardSize = ta.getInteger(0, 0);
		mMaxScale = ta.getFloat(1, 3/2f);
		mMinScale = ta.getFloat(2, 1/3f);
		ta.recycle();

		mState = new GameState(boardSize);
		
		ta = context.obtainStyledAttributes(attrs, R.styleable.GameView, R.attr.gameViewStyle, 0);
		int boardColor = ta.getColor(R.styleable.GameView_boardColor, Color.WHITE);
		int gridColor = ta.getColor(R.styleable.GameView_gridColor, Color.BLACK);
		mGridSize = ta.getDimensionPixelSize(R.styleable.GameView_gridSize,  (int) (50 * getResources().getDisplayMetrics().density + 0.5));
		mDotRadius = ta.getDimensionPixelSize(R.styleable.GameView_gridDotRadius, 0);
		mDotSpacing = ta.getInteger(R.styleable.GameView_gridDotSpacing, 0);
		float lineWidth = ta.getDimensionPixelSize(R.styleable.GameView_lineWidth, 1);
		float lineWidthThick = ta.getDimensionPixelSize(R.styleable.GameView_lineWidthThick, 1);
		mPlacementStyle = ta.getInteger(R.styleable.GameView_placementStyle, R.id.boxes);
		mPlacingAlpha = ta.getInteger(R.styleable.GameView_placingAlpha, 100);
		Drawable player1Drawable = ta.getDrawable(R.styleable.GameView_player1Drawable);
		Drawable player2Drawable = ta.getDrawable(R.styleable.GameView_player2Drawable);
		mThickLineSpacing = ta.getInteger(R.styleable.GameView_thickLineSpacing, 0);
		ta.recycle();
		
//		setBackgroundColor(mBoardColor);

		lineWidth = Math.max(lineWidth, 1);
		lineWidthThick = Math.max(lineWidthThick, 1);

		mPlayer1Drawable = player1Drawable != null? player1Drawable : getResources().getDrawable(R.drawable.player_x);
		mPlayer2Drawable = player2Drawable != null? player2Drawable : getResources().getDrawable(R.drawable.player_o);
		
		mGestureDetector = new GestureDetector(getContext(), this);
		mScaleGestureDetector = new ScaleGestureDetector(getContext(), this);
		mGestureDetector.setOnDoubleTapListener(this);
//		mGestureDetector.setIsLongpressEnabled(false);
//		ScaleGestureDetectorCompat.setQuickScaleEnabled(mScaleGestureDetector, false);

		mLinePaint = new Paint();
		mLinePaint.setStyle(Paint.Style.STROKE);
		mLinePaint.setTextAlign(Align.CENTER);
		mLinePaint.setColor(gridColor);
		mLinePaint.setTextSize(mGridSize/2);
		mLinePaint.setStrokeWidth(lineWidth);

		mThickLinePaint = new Paint(mLinePaint);
		mThickLinePaint.setStrokeWidth(lineWidthThick);
		
		mDotPaint = new Paint(mLinePaint);
		mDotPaint.setStyle(Paint.Style.FILL);
		
		mBackgroundPaint = new Paint(mLinePaint);
		mBackgroundPaint.setColor(boardColor);
		mBackgroundPaint.setStyle(Paint.Style.FILL);
	}

	public void setOnAiMoveListener(OnAiMoveListener listener) {
		mListener = listener;
	}

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// Call all onTouchEvent() methods and OR the result. Do not short-circuit.
		return mScaleGestureDetector.onTouchEvent(event) | mGestureDetector.onTouchEvent(event) | super.onTouchEvent(event);
	}
	
	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		centerView();
	}
	
	boolean isP1Turn() {
		return GameUtils.isP1Turn(mState.moves.size());
	}
	
	private void drawMove(Canvas canvas, int gridX, int gridY, int turn, boolean placing) {
		boolean isP1Turn = GameUtils.isP1Turn(turn);
		Drawable drawable = isP1Turn? mPlayer1Drawable : mPlayer2Drawable;
		int left = (int) (-mState.x + getWidth()/2f + mGridSize*(gridX - 0.5f));
		int right = (int) (-mState.x + getWidth()/2f + mGridSize*(gridX + 0.5f));
		int top = (int) (-mState.y + getHeight()/2f + mGridSize*(gridY - 0.5f));
		int bottom = (int) (-mState.y + getHeight()/2f + mGridSize*(gridY + 0.5f));
		drawable.setBounds(left, top, right, bottom);
		drawable.setAlpha(placing? mPlacingAlpha : 255);
		drawable.draw(canvas);
	}
	
	@Override
	public void draw(Canvas canvas) {
		super.draw(canvas);
		
//		Log.d("draw", "Drawing at coords "+mState.x+", "+mState.y+" with scale "+mState.scale);
		canvas.save();
		canvas.translate(getWidth()/2f, getHeight()/2f);
		canvas.scale(mState.scale, mState.scale);
		canvas.translate(-getWidth()/2f, -getHeight()/2f);

		int gridCenterX = (int) ((mState.x/mGridSize) + 0.5);
		int gridCenterY = (int) ((mState.y/mGridSize) + 0.5);
//		Log.d("draw", "gridCenterX="+gridCenterX);
//		Log.d("draw", "gridCenterY="+gridCenterY);
		int halfGridWidth = (int) ((getWidth()/mGridSize)/2/mState.scale + 1);
		int halfGridHeight = (int) ((getHeight()/mGridSize)/2/mState.scale + 1);
		int gridLeft = gridCenterX - halfGridWidth - 1;
		int gridTop = gridCenterY - halfGridHeight - 1;
		int gridRight = gridCenterX + halfGridWidth + 1;
		int gridBottom = gridCenterY + halfGridHeight + 1;
		
		if (mState.boardSize > 0) {
			gridLeft = Math.max(gridLeft, -mState.boardSize);
			gridTop = Math.max(gridTop, -mState.boardSize);
			gridRight = Math.min(gridRight, mState.boardSize);
			gridBottom = Math.min(gridBottom, mState.boardSize);
		}
		
		float boardLeft = -mState.x + getWidth()/2f + mGridSize*(gridLeft - 0.5f);
		float boardTop = -mState.y + getHeight()/2f + mGridSize*(gridTop - 0.5f);
		float boardRight = -mState.x + getWidth()/2f + mGridSize*(gridRight + 0.5f);
		float boardBottom = -mState.y + getHeight()/2f + mGridSize*(gridBottom + 0.5f);
		
		// Draw background
		canvas.drawRect(boardLeft, boardTop, boardRight, boardBottom, mBackgroundPaint);
		canvas.drawRect(boardLeft, boardTop, boardRight, boardBottom, mThickLinePaint);
		
		// Draw grid	
		switch (mPlacementStyle) {
		default:
		case PLACEMENT_STYLE_BOXES:
			for (int i = gridLeft; i < gridRight; i++) {
				float lineX = -mState.x + getWidth()/2 + mGridSize*(i + 0.5f);
				canvas.drawLine(lineX, boardTop, lineX, boardBottom, (mThickLineSpacing > 0 && (i-mThickLineSpacing/2)%mThickLineSpacing == 0)? mThickLinePaint : mLinePaint);
			}
			for (int j = gridTop; j < gridBottom; j++) {
				float lineY = -mState.y + getHeight()/2 + mGridSize*(j + 0.5f);
				canvas.drawLine(boardLeft, lineY, boardRight, lineY, (mThickLineSpacing > 0 && (j-mThickLineSpacing/2)%mThickLineSpacing == 0)? mThickLinePaint : mLinePaint);
			}
			if (mDotSpacing > 0) for (int i = (gridLeft-mDotSpacing/2)/mDotSpacing*mDotSpacing + mDotSpacing/2; i < gridRight; i+=mDotSpacing) {
				for (int j = (gridTop-mDotSpacing/2)/mDotSpacing*mDotSpacing + mDotSpacing/2; j < gridBottom; j+=mDotSpacing) {
					float x = -mState.x + getWidth()/2 + mGridSize*(i + 0.5f);
					float y = -mState.y + getHeight()/2 + mGridSize*(j + 0.5f);
					canvas.drawCircle(x, y, mDotRadius, mDotPaint);
				}
			}
			break;
		case PLACEMENT_STYLE_INTERSECTIONS:
			for (int i = gridLeft; i <= gridRight; i++) {
				float lineX = -mState.x + getWidth()/2 + mGridSize*i;
				canvas.drawLine(lineX, boardTop, lineX, boardBottom, (mThickLineSpacing > 0 && i%mThickLineSpacing == 0)? mThickLinePaint : mLinePaint);
			}
			for (int j = gridTop; j <= gridBottom; j++) {
				float lineY = -mState.y + getHeight()/2 + mGridSize*j;
				canvas.drawLine(boardLeft, lineY, boardRight, lineY, (mThickLineSpacing > 0 && j%mThickLineSpacing == 0)? mThickLinePaint : mLinePaint);
			}
			if (mDotSpacing > 0) for (int i = gridLeft/mDotSpacing*mDotSpacing; i < gridRight; i+=mDotSpacing) {
				for (int j = gridTop/mDotSpacing*mDotSpacing; j < gridBottom; j+=mDotSpacing) {
					float x = -mState.x + getWidth()/2 + mGridSize*i;
					float y = -mState.y + getHeight()/2 + mGridSize*j;
					canvas.drawCircle(x, y, mDotRadius, mDotPaint);
				}
			}
			break;
		}
		
		// Draw moves
		for (int i = 0, s = mState.moves.size(); i < s; i++) {
			GridUtils.indexToCoords(mState.moves.get(i), COORDS);
			drawMove(canvas, COORDS[0], COORDS[1], i, false);
//			Log.d("draw", "Move "+i+" is symbol "+symbol+" at index "+mState.moves.get(i)+"("+coords[0]+", "+coords[1]+")");
		}
		
//		// Debug: draw space indices
//		for (int i = gridLeft; i <= gridRight; i++) {
//			for (int j = gridTop; j <= gridBottom; j++) {
//				String text = String.valueOf(GridUtils.coordsToIndex(i,  j));
//				float textX = -mState.x + getWidth()/2f + mGridSize*i;
//				float textY = -mState.y + getHeight()/2f - (mLinePaint.ascent() + mLinePaint.descent())/2f + mGridSize*j;
//				canvas.drawText(text, textX, textY, mLinePaint);
//			}
//		}

		// Draw current move
		if (mState.move1 > 0) {
			GridUtils.indexToCoords(mState.move1, COORDS);
			drawMove(canvas, COORDS[0], COORDS[1], mState.moves.size(), true);
		}
		if (mState.move2 > 0) {
			GridUtils.indexToCoords(mState.move2, COORDS);
			drawMove(canvas, COORDS[0], COORDS[1], mState.moves.size() + 1, true);
		}
		
		canvas.restore();
	}
	
	private void getGridCoordsFromEvent(MotionEvent e, int[] out) {
		float px = (-getWidth()/2f + mState.x) + (1-1/mState.scale)*getWidth()/2f + e.getX()/mState.scale;
		float py = (-getHeight()/2f + mState.y) + (1-1/mState.scale)*getHeight()/2f + e.getY()/mState.scale;
				
		Log.d("Gesture", "  Transformed coords "+px+", "+py);
		
		int gx = (int)Math.floor(px/(mGridSize) + 0.5);
		int gy = (int)Math.floor(py/(mGridSize) + 0.5);
		Log.d("Gesture", "  Grid coords " + gx + ", " + gy);
		
		out[0] = gx;
		out[1] = gy;
	}
	
	private void centerView() {
		if (mState.moves.size() > 0 || mState.move1 > 0 || mState.move2 > 0) {
			int index0;
			if (mState.moves.size() > 0) {
				index0 = mState.moves.get(0);
			} else if (mState.move1 > 0) {
				index0 = mState.move1;
			} else if (mState.move2 > 0) {
				index0 = mState.move2;
			} else {
				// This should never happen
				index0 = GridUtils.ORIGIN_INDEX;
			}
			GridUtils.indexToCoords(index0, COORDS);
			int minX, maxX, minY, maxY;
			minX = maxX = COORDS[0];
			minY = maxY = COORDS[1];
			for (int index : mState.moves) {
				GridUtils.indexToCoords(index, COORDS);
				minX = minX < COORDS[0]? minX : COORDS[0];
				maxX = maxX > COORDS[0]? maxX : COORDS[0];
				minY = minY < COORDS[1]? minY : COORDS[1];
				maxY = maxY > COORDS[1]? maxY : COORDS[1];
			}
			if (mState.move1 > 0) {
				GridUtils.indexToCoords(mState.move1, COORDS);
				minX = minX < COORDS[0]? minX : COORDS[0];
				maxX = maxX > COORDS[0]? maxX : COORDS[0];
				minY = minY < COORDS[1]? minY : COORDS[1];
				maxY = maxY > COORDS[1]? maxY : COORDS[1];
			}
			if (mState.move2 > 0) {
				GridUtils.indexToCoords(mState.move2, COORDS);
				minX = minX < COORDS[0]? minX : COORDS[0];
				maxX = maxX > COORDS[0]? maxX : COORDS[0];
				minY = minY < COORDS[1]? minY : COORDS[1];
				maxY = maxY > COORDS[1]? maxY : COORDS[1];
			}
			
			mState.x = (minX + maxX)*mGridSize / 2;
			mState.y = (minY + maxY)*mGridSize / 2;

			// Set scale based on size of existing moves
			float gridSizeX = Math.abs(getWidth()/mGridSize);
			float gridSizeY = Math.abs(getHeight()/mGridSize);
			float moveSizeX = 2 + Math.abs(minX - maxX);
			float moveSizeY = 2 + Math.abs(minY - maxY);
			float scaleX = gridSizeX / moveSizeX;
			float scaleY = gridSizeY / moveSizeY;
			float scale = Math.min(scaleX, scaleY);
			
			Log.d("Scale", "gridSizeX="+gridSizeX);
			Log.d("Scale", "gridSizeY="+gridSizeY);
			Log.d("Scale", "moveSizeX="+moveSizeX);
			Log.d("Scale", "moveSizeY="+moveSizeY);
			Log.d("Scale", "scaleX="+scaleX);
			Log.d("Scale", "scaleY="+scaleY);
			Log.d("Scale", "scale="+scale);
			
			mState.scale = scale > 1? 1 : scale < mMinScale? mMinScale : scale;
		} else {
			mState.x = 0;
			mState.y = 0;
			mState.scale = 1;
		}
		postInvalidate();
	}

	@Override
	public boolean onDown(MotionEvent e) {
		Log.d("Gesture", "Down at " + e.getX() + ", " + e.getY());
		return true;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		Log.d("Gesture", "Fling");
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {
		Log.d("Gesture", "Long press at " + e.getX() + ", " + e.getY());

		performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
		centerView();
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
//		Log.d("Gesture", "Scroll");
		
		float x = mState.x + distanceX/mState.scale;
		float y = mState.y + distanceY/mState.scale;
		
		// Don't scroll a finite board too far
		if (mState.boardSize > 0) {
			float max = mState.boardSize * mGridSize;
			float min = -mState.boardSize * mGridSize;
			x = Math.min(x, max);
			x = Math.max(x, min);
			y = Math.min(y, max);
			y = Math.max(y, min);
		}
		
		mState.x = x;
		mState.y = y ;
		
		invalidate();
		return true;
	}

	@Override
	public void onShowPress(MotionEvent e) {
		Log.d("Gesture", "Show press at "+e.getX()+", "+e.getY());
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		Log.d("Gesture", "Single tap up at "+e.getX()+", "+e.getY());
		return false;
	}
	
	private void addMove(int gridIndex) {
		if (!mState.playing || mAiMoving) return;
		
		if (mState.move1 == gridIndex) {
			mState.move1 = 0;
			invalidate();
		} else if (mState.move2 == gridIndex) {
			mState.move2 = 0;
			invalidate();
		} else if (mState.move1 > 0 && (mState.move2 > 0 || mState.moves.size() == 0)) {
			return;
		} else if (!mState.moves.contains(gridIndex)) {
			if (mState.move1 <= 0) {
				mState.move1 = gridIndex;
				invalidate();
			} else if (mState.move2 <= 0) {
				mState.move2 = gridIndex;
				invalidate();
			} else {
				// This shouldn't ever happen
			}
		}
	}

	@Override
	public boolean onSingleTapConfirmed(MotionEvent e) {
		Log.d("Gesture", "Single tap confirmed at "+e.getX()+", "+e.getY());
		
//		int gridIndex = getGridIndexFromEvent(e);
		getGridCoordsFromEvent(e, COORDS);
		
		if (mState.boardSize > 0 && (COORDS[0] < -mState.boardSize || COORDS[0] > mState.boardSize || COORDS[1] < -mState.boardSize || COORDS[1] > mState.boardSize)) {
			// Clicked outside of a finite board
			return true;
		}

		int gridIndex = GridUtils.coordsToIndex(COORDS[0], COORDS[1]);
		addMove(gridIndex);

		return true;
	}

	@Override
	public boolean onDoubleTap(MotionEvent e) {
		Log.d("Gesture", "Double tap at " + e.getX() + ", " + e.getY());
		
//		centerView();
		
		return true;
	}

	@Override
	public boolean onDoubleTapEvent(MotionEvent e) {
		Log.d("Gesture", "On double tap event at " + e.getX() + ", " + e.getY());
		return false;
	}	

	@Override
	public boolean onScale(ScaleGestureDetector detector) {
//		Log.d("Scale Gesture", "Scale");

		float scaleFactor = detector.getScaleFactor();
		float oldScale = mState.scale;
		float newScale = oldScale * scaleFactor;
		newScale = (newScale > mMaxScale? mMaxScale : newScale < mMinScale? mMinScale : newScale);

		mState.scale = newScale;
		invalidate();
		
		return true;
	}

	@Override
	public boolean onScaleBegin(ScaleGestureDetector detector) {
		Log.d("Scale Gesture", "Scale begin");
		return true;
	}

	@Override
	public void onScaleEnd(ScaleGestureDetector detector) {
		Log.d("Scale Gesture", "Scale complete");
	}
		
	boolean setState(GameState state) {
		mState.copyState(state);
		return mState.playing;
	}
	
	GameState getState() {
		return mState;
	}
	
	void setAi(int ai1, int ai2) {
		mState.ai1 = ai1;
		mState.ai2 = ai2;
		aiMove();
	}
	
	/**
	 * 
	 * @return True if the game should continue, false otherwise
	 */
	boolean confirmMove() {
		if (mState.move1 > 0) {
			if (mState.move2 > 0) {
				mState.moves.add(mState.move1);
				mState.moves.add(mState.move2);
				mState.move1 = 0;
				mState.move2 = 0;
				invalidate();
				if (GameUtils.checkWin(mState.moves)) {
					centerView();
					mState.playing = false;
				}
			} else if (mState.moves.size() == 0) {
				mState.moves.add(mState.move1);
				mState.move1 = 0;
				invalidate();
			}
		}
		if (!mState.playing) {
			return false;
		} else {
			aiMove();
		}
		return mState.playing;
	}
	
	private void aiMove() {
		mAiMoving = true;
		if (mListener != null) {
			mListener.onAiBegin();
		}

		new AsyncTask<Void,Void,Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				if (mState.ai1 > 0 && isP1Turn()) {
					GameUtils.aiMove(mState.moves, mState.ai1);
					centerView();
					if (GameUtils.checkWin(mState.moves)) {
						mState.playing = false;
					}
				}
				if (mState.ai2 > 0 && !isP1Turn()) {
					GameUtils.aiMove(mState.moves, mState.ai2);
					centerView();
					if (GameUtils.checkWin(mState.moves)) {
						mState.playing = false;
					}
				}

				return null;
			}

			@Override
			protected void onPostExecute(Void result) {
				mAiMoving = false;
				if (mListener != null) {
					mListener.onAiComplete();
				}
			}
		}.execute();
	}
	
	void clearMove() {
		mState.move1 = 0;
		mState.move2 = 0;
		invalidate();
	}
	
	void saveGame(SharedPreferences.Editor editor) {
		for (int i = 0, s = mState.moves.size(); i < s; i++) {
			editor.putInt(KEY_MOVE+i, mState.moves.get(i));
		}
		editor.putBoolean(KEY_PLAYING, mState.playing);
		editor.putInt(GameUtils.KEY_AI1, mState.ai1);
		editor.putInt(GameUtils.KEY_AI2, mState.ai2);
		editor.putInt(KEY_BOARD_SIZE, mState.boardSize);
	}
	
	boolean loadGame(SharedPreferences prefs) {
		mState.moves.clear();
		for (int i = 0; prefs.contains(KEY_MOVE+i); i++) {
			mState.moves.add(prefs.getInt(KEY_MOVE+i, 0));
		}
		mState.playing = prefs.getBoolean(KEY_PLAYING, true);
		mState.ai1 = prefs.getInt(GameUtils.KEY_AI1, 0);
		mState.ai2 = prefs.getInt(GameUtils.KEY_AI2, 0);
		mState.boardSize = prefs.getInt(KEY_BOARD_SIZE, 0);
		invalidate();
		return mState.playing;
	}
	
	static class GameState implements Parcelable {

		private final List<Integer> moves = new ArrayList<>();
		private float x = 0;
		private float y = 0;
		private float scale = 1;
		private int move1 = 0;
		private int move2 = 0;
		private boolean playing = true;
		private int ai1 = 0;
		private int ai2 = 0;
		private int boardSize = 0;

		GameState(int boardSize) {
			this.boardSize = boardSize;
		}
		
		public void copyState(GameState other) {
			this.moves.clear();
			this.moves.addAll(other.moves);
			this.x = other.x;
			this.y = other.y;
			this.scale = other.scale;
			this.move1 = other.move1;
			this.move2 = other.move2;
			this.playing = other.playing;
			this.ai1 = other.ai1;
			this.ai2 = other.ai2;
			this.boardSize = other.boardSize;
		}

		@Override
		public int describeContents() {
			return 0;
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			dest.writeList(moves);
			dest.writeFloat(x);
			dest.writeFloat(y);
			dest.writeFloat(scale);
			dest.writeInt(move1);
			dest.writeInt(move2);
			dest.writeInt(playing? 1 : 0);
			dest.writeInt(ai1);
			dest.writeInt(ai2);
			dest.writeInt(boardSize);
		}

		public static Creator<GameState> CREATOR = new Creator<GameState>() {

			@Override
			public GameState createFromParcel(Parcel source) {
				GameState out = new GameState(0);
				source.readList(out.moves, null);
				out.x = source.readFloat();
				out.y = source.readFloat();
				out.scale = source.readFloat();
				out.move1 = source.readInt();
				out.move2 = source.readInt();
				out.playing = source.readInt() > 0;
				out.ai1 = source.readInt();
				out.ai2 = source.readInt();
				out.boardSize = source.readInt();
				return out;
			}

			@Override
			public GameState[] newArray(int size) {
				return new GameState[size];
			}

		};
	}

	public interface OnAiMoveListener {
		void onAiBegin();
		void onAiComplete();
	}

}