package lol.connect6;

import lol.connect6.GameView.GameState;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class GameFragment extends Fragment implements Toolbar.OnMenuItemClickListener {

	private static final String KEY_GAMESTATE = "gamestate";

	private static final String PREFS_SAVEGAME = "lol.connect6.savegame";
	
	private GameView mGameView;
	private Toolbar mHeader;
	private Toolbar mFooter;
	
	private Drawable mPlayer1Drawable;
	private Drawable mPlayer2Drawable;
	
	public GameFragment() {
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		Activity activity = getActivity();
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
		inflater.inflate(R.menu.game_header, menu);
		
		boolean isP1Turn = mGameView.isP1Turn();
		mFooter.setTitle(isP1Turn? R.string.player1 : R.string.player2);
		mFooter.setNavigationIcon(isP1Turn? mPlayer1Drawable : mPlayer2Drawable);
		
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {		
		return onMenuItemClick(item);
	}

	@Override
	public boolean onMenuItemClick(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_confirm_move:
			if (mGameView != null) {
				boolean wasP1Turn = mGameView.isP1Turn();
				boolean playing = mGameView.confirmMove();
				boolean isP1Turn = mGameView.isP1Turn();
				if (!playing) {
					showWinMessage(!isP1Turn);
					return true;
				}
				if (isP1Turn != wasP1Turn) {
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
		case R.id.action_clear_move:
			if (mGameView != null) {
				mGameView.clearMove();
				return true;
			}
			break;
		case R.id.action_end_game:
			// Clear saved game and exit
			getActivity().getSharedPreferences(PREFS_SAVEGAME, Activity.MODE_PRIVATE).edit().clear().commit();
			getActivity().finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_game, container,
				false);
		
		mHeader = (Toolbar) rootView.findViewById(R.id.header);
		mHeader.setOnMenuItemClickListener(this);
		mHeader.setLogo(R.drawable.logo);
		mHeader.setTitle("");
		((ActionBarActivity)getActivity()).setSupportActionBar(mHeader);
		
		mGameView = (GameView) rootView.findViewById(R.id.game_view);
		
		mFooter = (Toolbar) rootView.findViewById(R.id.footer);
		mFooter.inflateMenu(R.menu.game_footer);
		mFooter.setOnMenuItemClickListener(this);
		
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
				boolean playing = mGameView.setState(state);
				if (!playing) {
					showWinMessage(!mGameView.isP1Turn());
				}
			}
		} else {
			// If we don't currently have state, check for a saved game and load that
			SharedPreferences prefs = getActivity().getSharedPreferences(PREFS_SAVEGAME, Activity.MODE_PRIVATE);
			if (prefs.getAll().size() > 0) {
				boolean playing = mGameView.loadGame(prefs);
				if (!playing) {
					showWinMessage(!mGameView.isP1Turn());
				}
			} else {
				// Initialize AI settings for a new game
				Bundle bundle = getActivity().getIntent().getExtras();
				mGameView.setAi(bundle.getInt(GameUtils.KEY_AI1), bundle.getInt(GameUtils.KEY_AI2));
			}
		}
	}
	
	private void showWinMessage(boolean p1) {
		String playerName = getResources().getString(p1? R.string.player1 : R.string.player2);
		TextView message = (TextView) getView().findViewById(R.id.message_view);
		message.setText(getResources().getString(R.string.win_message, playerName));
		message.setVisibility(View.VISIBLE);
		mFooter.setNavigationIcon(null);
		mFooter.setTitle(null);
		mFooter.getMenu().findItem(R.id.action_confirm_move).setEnabled(false);
		mFooter.getMenu().findItem(R.id.action_clear_move).setEnabled(false);
		getActivity().getSharedPreferences(PREFS_SAVEGAME, Activity.MODE_PRIVATE).edit().clear().commit();
	}
}
