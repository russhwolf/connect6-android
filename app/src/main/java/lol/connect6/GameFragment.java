package lol.connect6;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import lol.connect6.GameView.GameState;

public class GameFragment extends Fragment implements Toolbar.OnMenuItemClickListener, View.OnClickListener, GameView.OnAiMoveListener {

	private static final String KEY_GAMESTATE = "gamestate";

	private GameView mGameView;
	private Toolbar mHeader;
	private FloatingActionButton mFloatingActionButton;
	private ProgressBar mProgressBar;
	
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
		inflater.inflate(R.menu.game, menu);
		
		boolean isP1Turn = mGameView.isP1Turn();
		mHeader.setTitle(isP1Turn ? R.string.player1 : R.string.player2);
		mHeader.setLogo(isP1Turn ? mPlayer1Drawable : mPlayer2Drawable);
		
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {		
		return onMenuItemClick(item) || super.onOptionsItemSelected(item);
	}

	@Override
	public void onClick(View view) {
		onClickById(view.getId());
	}

	@Override
	public boolean onMenuItemClick(MenuItem item) {
		return onClickById(item.getItemId());
	}

	private boolean onClickById(int id) {
		switch (id) {
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
					SharedPreferences.Editor editor = getActivity().getSharedPreferences(GameUtils.PREFS_SAVEGAME, Activity.MODE_PRIVATE).edit();
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
			getActivity().getSharedPreferences(GameUtils.PREFS_SAVEGAME, Activity.MODE_PRIVATE).edit().clear().commit();
			getActivity().finish();
			return true;
		}
		return false;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_game, container,
				false);
		
		mHeader = (Toolbar) rootView.findViewById(R.id.header);
		mHeader.setOnMenuItemClickListener(this);
		mHeader.setTitle("");
		((AppCompatActivity)getActivity()).setSupportActionBar(mHeader);
		
		mGameView = (GameView) rootView.findViewById(R.id.game_view);
		mGameView.setOnAiMoveListener(this);

		mFloatingActionButton = (FloatingActionButton) rootView.findViewById(R.id.action_confirm_move);
		mFloatingActionButton.setOnClickListener(this);

		mProgressBar = (ProgressBar) rootView.findViewById(R.id.loading);

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
			SharedPreferences prefs = getActivity().getSharedPreferences(GameUtils.PREFS_SAVEGAME, Activity.MODE_PRIVATE);
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
		mHeader.setLogo(null);
		mHeader.setTitle(null);
		mHeader.getMenu().findItem(R.id.action_clear_move).setEnabled(false);
		mFloatingActionButton.setEnabled(false);
		getActivity().getSharedPreferences(GameUtils.PREFS_SAVEGAME, Activity.MODE_PRIVATE).edit().clear().commit();
	}

	@Override
	public void onAiBegin() {
		if (getView() != null) {
			getView().post(new Runnable() {
				@Override
				public void run() {
					mHeader.getMenu().findItem(R.id.action_clear_move).setEnabled(false);
					mFloatingActionButton.setVisibility(View.INVISIBLE);
					mProgressBar.setVisibility(View.VISIBLE);
				}
			});
		}
	}

	@Override
	public void onAiComplete() {
		if (getView() != null) {
			getView().post(new Runnable() {
				@Override
				public void run() {
					mHeader.getMenu().findItem(R.id.action_clear_move).setEnabled(true);
					mFloatingActionButton.setVisibility(View.VISIBLE);
					mProgressBar.setVisibility(View.INVISIBLE);
				}
			});
		}
	}
}
