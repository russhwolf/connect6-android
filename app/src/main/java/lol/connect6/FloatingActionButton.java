//package lol.connect6;
//
//import android.annotation.TargetApi;
//import android.content.Context;
//import android.content.res.ColorStateList;
//import android.content.res.TypedArray;
//import android.graphics.drawable.Drawable;
//import android.graphics.drawable.GradientDrawable;
//import android.graphics.drawable.LayerDrawable;
//import android.graphics.drawable.RippleDrawable;
//import android.graphics.drawable.StateListDrawable;
//import android.os.Build;
//import android.support.v4.view.ViewCompat;
//import android.util.AttributeSet;
//import android.view.ViewGroup;
//import android.widget.ImageButton;
//
//public class FloatingActionButton extends ImageButton {
//	private int mIntElevation;
//	private boolean mAdjustedMargin = false;
//
//	public FloatingActionButton(Context context) {
//		this(context, null);
//	}
//
//	public FloatingActionButton(Context context, AttributeSet attrs) {
//		this(context, attrs, R.style.FloatingActionButton);
//	}
//
//	public FloatingActionButton(Context context, AttributeSet attrs,
//								int defStyleAttr) {
//		super(context, attrs, defStyleAttr);
//		setClickable(true);
//		setScaleType(ScaleType.CENTER);
//
//		TypedArray ta = context.getTheme().obtainStyledAttributes(attrs, R.styleable.FloatingActionButton, defStyleAttr, R.style.FloatingActionButton);
//		int color = ta.getColor(R.styleable.FloatingActionButton_android_color, 0);
//		int size = (int) (ta.getInteger(R.styleable.FloatingActionButton_size, 54) * getResources().getDisplayMetrics().density + 0.5);
//		float elevation = ta.getDimension(R.styleable.FloatingActionButton_elevation, 0);
//		mIntElevation = ta.getDimensionPixelSize(R.styleable.FloatingActionButton_elevation, 0);
//		ta.recycle();
//
//		ViewCompat.setElevation(this, elevation);
//
//		Drawable background;
//		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//			background = createBackgroundL(color, size);
//		} else {
//			background = createBackgroundBase(color, size, mIntElevation);
//		}
//		setBackground(background);
//
//		setMinimumHeight(size);
//		setMinimumWidth(size);
//
//	}
//
//	@Override
//	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
//		super.onLayout(changed, left, top, right, bottom);
//		if (!mAdjustedMargin && Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
//			ViewGroup.LayoutParams params = getLayoutParams();
//			if (params instanceof ViewGroup.MarginLayoutParams) {
//				ViewGroup.MarginLayoutParams marginParams = (ViewGroup.MarginLayoutParams) params;
//				marginParams.setMargins(marginParams.leftMargin - mIntElevation, marginParams.topMargin, marginParams.rightMargin - mIntElevation, marginParams.bottomMargin - 5 * mIntElevation / 3);
//				setLayoutParams(params);
//				mAdjustedMargin = true;
//			}
//		}
//	}
//
//
//	private Drawable createBackgroundBase(int color, int size, int elevation) {
//		GradientDrawable behind = new GradientDrawable();
//		behind.setColor(0xffffffff);	// TODO get this color from style?
//		behind.setSize(size, size);
//		behind.setShape(GradientDrawable.OVAL);
//
//		GradientDrawable normal = new GradientDrawable();
//		normal.setColor(color);
//		normal.setSize(size, size);
//		normal.setShape(GradientDrawable.OVAL);
//		LayerDrawable unpressed = new LayerDrawable(new Drawable[]{behind, normal});
//
//		GradientDrawable selector = new GradientDrawable();
//		selector.setColor(0x40cccccc);	// TODO get this color from style?
//		selector.setSize(size, size);
//		selector.setShape(GradientDrawable.OVAL);
//		LayerDrawable pressed = new LayerDrawable(new Drawable[]{behind, normal, selector});
//
//		// TODO better replicate lollipop shadow
//		GradientDrawable shadow = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[]{0x37000000, 0x37000000, 0x37000000, 0x37000000, 0x37000000, 0x37000000, 0x37000000, 0x37000000, 0x37000000, 0x03000000});
//		shadow.setSize(size + 2 * elevation, size + 2 * elevation);
//		shadow.setShape(GradientDrawable.OVAL);
//		shadow.setGradientType(GradientDrawable.RADIAL_GRADIENT);
//		shadow.setGradientRadius(size / 2 + elevation);
//		shadow.setGradientCenter(0.5f, 0.5f);
//
//		LayerDrawable shadowedUnpressed = new LayerDrawable(new Drawable[]{shadow, unpressed});
//		LayerDrawable shadowedPressed = new LayerDrawable(new Drawable[]{shadow, pressed});
//		shadowedUnpressed.setLayerInset(0, 0, 5 * elevation / 3, 0, 0);
//		shadowedUnpressed.setLayerInset(1, elevation, 7 * elevation / 3, elevation, 5 * elevation / 3);
//		shadowedPressed.setLayerInset(0, 0, 5 * elevation / 3, 0, 0);
//		shadowedPressed.setLayerInset(1, elevation, 7 * elevation / 3, elevation, 5 * elevation / 3);
//
//		StateListDrawable stateList = new StateListDrawable();
//		stateList.addState(new int[]{android.R.attr.state_pressed}, shadowedPressed);
//		stateList.addState(new int[0], shadowedUnpressed);
//
//		return stateList;
//	}
//
//	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
//	private Drawable createBackgroundL(int color, int size) {
//		GradientDrawable base = new GradientDrawable();
//		base.setColor(color);
//		base.setSize(size, size);
//		base.setShape(GradientDrawable.OVAL);
//
//		ColorStateList colorStateList = new ColorStateList(new int[][]{new int[0]}, new int[]{getResources().getColor(R.color.ripple_material_light)});
//		RippleDrawable ripple = new RippleDrawable(colorStateList, base, null);
//		return ripple;
//	}
//
//	@SuppressWarnings("deprecation")
//	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
//	@Override
//	public void setBackground(Drawable background) {
//		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
//			super.setBackground(background);
//		} else {
//			super.setBackgroundDrawable(background);
//		}
//	}
//}
