package lol.connect6;

import lol.connect6.GameView.GameState;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

public class GameFragment extends Fragment {

	private static String KEY_GAMESTATE = "gamestate";
	
	private static String PREFS_SAVEGAME = "lol.connect6.savegame";
	
	private GameView mGameView;
	
	private Drawable mPlayer1Drawable;
	private Drawable mPlayer2Drawable;
	
	private boolean mIsP1Turn = true;
	
	public GameFragment() {
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		TypedValue tv = new TypedValue();
		activity.getTheme().resolveAttribute(R.attr.gameViewStyle, tv, true);
		TypedArray ta = activity.getTheme().obtainStyledAttributes(tv.resourceId, new int[] {R.attr.player1Drawable, R.attr.player2Drawable});
		mPlayer1Drawable = ta.getDrawable(0);
		mPlayer2Drawable = ta.getDrawable(1);
		ta.recycle();
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);	
		setHasOptionsMenu(true);
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		
		// Inflate the menu; this adds items to the action bar if it is present.
		inflater.inflate(R.menu.game, menu);
		ActionBarActivity activity = (ActionBarActivity) getActivity();
		ActionBar ab = activity.getSupportActionBar();
		ab.setTitle(mIsP1Turn? R.string.player1 : R.string.player2);
		ab.setIcon(mIsP1Turn? mPlayer1Drawable : mPlayer2Drawable);
		
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {		
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		switch (item.getItemId()) {
		case R.id.action_confirm_move:
			if (mGameView != null) {
				boolean wasP1Turn = mIsP1Turn;
				mIsP1Turn = mGameView.confirmMove();
				if (mIsP1Turn != wasP1Turn) {
					// Save game as we go.
					SharedPreferences.Editor editor = getActivity().getSharedPreferences(PREFS_SAVEGAME, Activity.MODE_PRIVATE).edit();
					editor.clear();
					mGameView.saveGame(editor);
					editor.commit();
					getActivity().supportInvalidateOptionsMenu();	
				}
				return true;
			}
			break;
		case R.id.action_end_game:
			// Clear saved game and exit
			getActivity().getSharedPreferences(PREFS_SAVEGAME, Activity.MODE_PRIVATE).edit().clear().commit();
			getActivity().finish();
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_game, container,
				false);
		mGameView = (GameView) rootView.findViewById(R.id.gameview);
		
		return rootView;
	}
			
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		if (mGameView != null) {
			GameState state = mGameView.getState();
			if (state != null) {
				outState.putParcelable(KEY_GAMESTATE, state);
			}
		}
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		if (savedInstanceState != null) {
			GameState state = savedInstanceState.getParcelable(KEY_GAMESTATE);
			if (state != null) {
				mGameView.setState(state);
			}
		} else {
			// If we don't currently have state, check for a saved game and load that
			SharedPreferences prefs = getActivity().getSharedPreferences(PREFS_SAVEGAME, Activity.MODE_PRIVATE);
			if (prefs.getAll().size() > 0) {
				mIsP1Turn = mGameView.loadGame(prefs);
			}
		}
	}
}
