package lol.connect6;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

public class GameView extends View implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener, ScaleGestureDetector.OnScaleGestureListener {

	private float mGridSize;
	
	private static final String P1_SYMBOL = "X";
	private static final String P2_SYMBOL = "O";
	
	private static final float GRID_SIZE_DP = 50;
	private static final float LINE_WIDTH_DP = 2;
	
	private static final float MIN_SCALE = 0.5f;
	private static final float MAX_SCALE = 1.5f;
	
	private GestureDetector mGestureDetector;

	private ScaleGestureDetector mScaleGestureDetector;
	
	private float mX = 0;
	private float mY = 0;
	private float mScale = 1;
	
	private final List<Integer> mMoves = new ArrayList<>();
	
	private final Paint mPaint;
	private final float mLineWidth;

	public GameView(Context context) {
		this(context, null, 0);
	}

	public GameView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	
	public GameView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		
		mGridSize = GRID_SIZE_DP * getResources().getDisplayMetrics().density;
		
		mGestureDetector = new GestureDetector(getContext(), this);
		mGestureDetector.setOnDoubleTapListener(this);
		mGestureDetector.setIsLongpressEnabled(false);
		
		mScaleGestureDetector = new ScaleGestureDetector(getContext(), this);

		mLineWidth = LINE_WIDTH_DP * getResources().getDisplayMetrics().density;
		
		mPaint = new Paint();
		mPaint.setTextAlign(Align.CENTER);
		mPaint.setColor(Color.BLACK);
		mPaint.setTextSize(mGridSize/2);
		mPaint.setStrokeWidth(mLineWidth);
		
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// Call all onTouchEvent() methods and OR the result. Do not short-circuit.
		return mScaleGestureDetector.onTouchEvent(event) | mGestureDetector.onTouchEvent(event) | super.onTouchEvent(event);
	}
	
	@Override
	public void draw(Canvas canvas) {
//		Log.d("draw", "Drawing at coords "+mX+", "+mY+" with scale "+mScale);
		canvas.save();
		canvas.translate(getWidth()/2f, getHeight()/2f);
		canvas.scale(mScale, mScale);
		canvas.translate(-getWidth()/2f, -getHeight()/2f);

		int gridCenterX = (int) ((mX/mGridSize) + 0.5);
		int gridCenterY = (int) ((mY/mGridSize) + 0.5);
//		Log.d("draw", "gridCenterX="+gridCenterX);
//		Log.d("draw", "gridCenterY="+gridCenterY);
		int halfGridWidth = (int) ((getWidth()/mGridSize)/2/mScale + 1);
		int halfGridHeight = (int) ((getHeight()/mGridSize)/2/mScale + 1);
		int gridLeft = gridCenterX - halfGridWidth - 1;
		int gridTop = gridCenterY - halfGridHeight - 1;
		int gridRight = gridCenterX + halfGridWidth + 1;
		int gridBottom = gridCenterY + halfGridHeight + 1;
//		int[] coords = new int[2];
		for (int i = 0, s = mMoves.size(); i < s; i++) {
			String symbol = (i+1)%4 < 2? P1_SYMBOL : P2_SYMBOL;
			int[] coords = GridUtils.indexToCoords(mMoves.get(i));
			float textX = -mX + getWidth()/2f + mGridSize*coords[0];
			float textY = -mY + getHeight()/2f - (mPaint.ascent() + mPaint.descent())/2f + mGridSize*coords[1];
			canvas.drawText(symbol, textX, textY, mPaint);
//			Log.d("draw", "Move "+i+" is symbol "+symbol+" at index "+mMoves.get(i)+"("+coords[0]+", "+coords[1]+")");
		}
//		for (int i = gridLeft; i < gridRight; i++) {
//			for (int j = gridTop; j < gridBottom; j++) {
//				String text = String.valueOf(GridUtils.coordsToIndex(i,  j));
//				float textX = -mX + getWidth()/2f + mGridSize*i;
//				float textY = -mY + getHeight()/2f - (mPaint.ascent() + mPaint.descent())/2f + mGridSize*j;
//				canvas.drawText(text, textX, textY, mPaint);
//			}
//		}
		
		float lineLeft = -mX + getWidth()/2f + mGridSize*(gridLeft - 0.5f);
		float lineTop = -mY + getHeight()/2f + mGridSize*(gridTop - 0.5f);
		float lineRight = -mX + getWidth()/2f + mGridSize*(gridRight + 0.5f);
		float lineBottom = -mY + getHeight()/2f + mGridSize*(gridBottom + 0.5f);
		for (int i = gridLeft; i <= gridRight; i++) {
			float lineX = -mX + getWidth()/2 + mGridSize*(i - 0.5f);
			canvas.drawLine(lineX, lineTop, lineX, lineBottom, mPaint);
		}
		for (int j = gridTop; j <= gridBottom; j++) {
			float lineY = -mY + getHeight()/2 + mGridSize*(j - 0.5f);
			canvas.drawLine(lineLeft, lineY, lineRight, lineY, mPaint);
		}
		
		canvas.restore();
	}
	
	private int getGridIndexFromEvent(MotionEvent e) {
		float px = (-getWidth()/2f + mX) + (1-1/mScale)*getWidth()/2f + e.getX()/mScale;
		float py = (-getHeight()/2f + mY) + (1-1/mScale)*getHeight()/2f + e.getY()/mScale;
				
		Log.d("Gesture", "  Transformed coords "+px+", "+py);
		
		int gx = (int)Math.floor(px/(mGridSize) + 0.5);
		int gy = (int)Math.floor(py/(mGridSize) + 0.5);

		Log.d("Gesture", "  Grid coords "+gx+", "+gy);
		
		int index = GridUtils.coordsToIndex(gx, gy);
		Log.d("Gesture", "  Grid index "+index);
		
		return index;
	}

	@Override
	public boolean onDown(MotionEvent e) {
		Log.d("Gesture", "Down at "+e.getX()+", "+e.getY());
		
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
		Log.d("Gesture", "Long press at "+e.getX()+", "+e.getY());

	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
//		Log.d("Gesture", "Scroll");
		
		mX += distanceX/mScale;
		mY += distanceY/mScale;
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

	@Override
	public boolean onSingleTapConfirmed(MotionEvent e) {
		Log.d("Gesture", "Single tap confirmed at "+e.getX()+", "+e.getY());
		int index = getGridIndexFromEvent(e);
		if (!mMoves.contains(index)) {
			mMoves.add(index);
			invalidate();
		}
		return true;
	}

	@Override
	public boolean onDoubleTap(MotionEvent e) {
		Log.d("Gesture", "Double tap at "+e.getX()+", "+e.getY());
		
		mX = 0;
		mY = 0;
		mScale = 1;
		invalidate();
		
		return true;
	}

	@Override
	public boolean onDoubleTapEvent(MotionEvent e) {
		Log.d("Gesture", "On double tap event at "+e.getX()+", "+e.getY());
		return false;
	}	

	@Override
	public boolean onScale(ScaleGestureDetector detector) {
//		Log.d("Scale Gesture", "Scale");

		float scaleFactor = detector.getScaleFactor();
		float oldScale = mScale;
		float newScale = oldScale * scaleFactor;
		newScale = (newScale > MAX_SCALE? MAX_SCALE : newScale < MIN_SCALE? MIN_SCALE : newScale);

		mScale = newScale;
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

}