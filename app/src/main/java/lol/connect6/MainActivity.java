package lol.connect6;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, Toolbar.OnMenuItemClickListener, Spinner.OnItemSelectedListener {

	private static final int REQUEST_SETTINGS = 1;
	private static final int REQUEST_GAME = 2;

	private Spinner mPlayer1Select;
	private Spinner mPlayer2Select;

	private Button mStartGame;
	private Button mContinueGame;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		Toolbar toolbar = (Toolbar) findViewById(R.id.action_bar);
		toolbar.setLogo(R.drawable.logo);
		toolbar.setTitle("");
		setSupportActionBar(toolbar);

		mStartGame = (Button) findViewById(R.id.start_game);
		mContinueGame = (Button) findViewById(R.id.continue_game);
		mStartGame.setOnClickListener(this);
		mContinueGame.setOnClickListener(this);

		mPlayer1Select = (Spinner) findViewById(R.id.player1_select);
		mPlayer1Select.setOnItemSelectedListener(this);
		mPlayer2Select = (Spinner) findViewById(R.id.player2_select);
		mPlayer2Select.setOnItemSelectedListener(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (getSharedPreferences(GameUtils.PREFS_SAVEGAME, Activity.MODE_PRIVATE).getAll().size() > 0) {
			mContinueGame.setVisibility(View.VISIBLE);
		} else {
			mContinueGame.setVisibility(View.GONE);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {		
		return onMenuItemClick(item);
	}

	@Override
	public boolean onMenuItemClick(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			startActivityForResult(new Intent(this, SettingsActivity.class), REQUEST_SETTINGS);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		switch (requestCode) {
		case REQUEST_GAME:
			// TODO: Updates after finishing game
			break;
		case REQUEST_SETTINGS:
			// TODO: Updates after changing settings
			break;
		}
	}

	@Override
	public void onClick(View v) {
		Intent intent = new Intent(this, GameActivity.class);
		switch (v.getId()) {
		case R.id.start_game:
			getSharedPreferences(GameUtils.PREFS_SAVEGAME, Activity.MODE_PRIVATE).edit().clear().commit();

			intent.putExtra(GameUtils.KEY_AI1, mPlayer1Select.getSelectedItemPosition());
			intent.putExtra(GameUtils.KEY_AI2, mPlayer2Select.getSelectedItemPosition());
			startActivityForResult(intent, REQUEST_GAME);
			break;
		case R.id.continue_game:
			startActivityForResult(intent, REQUEST_GAME);
			break;
		}
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {
		if (position != 0) {
			// If we just selected an AI, set the other player to human.
			Spinner other = parent.getId() == mPlayer1Select.getId()? mPlayer2Select : mPlayer1Select;
			if (other.getSelectedItemPosition() != 0) other.setSelection(0);
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		// Do nothing
	}
}
