package lol.connect6;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;

public class GameActivity extends ActionBarActivity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		
		int theme = Integer.parseInt(settings.getString("board_style_list", "0"));
		switch (theme) {
		default:
		case 0:
			setTheme(R.style.DefaultTheme);
			break;
		case 1:
			setTheme(R.style.GoTheme);
			break;
		}

		int size = Integer.parseInt(settings.getString("board_size_list", "0"));
		switch (size) {
		default:
		case 0:
			getTheme().applyStyle(R.style.InfiniteBoardSizeStyle, true);
			break;
		case 1:
			getTheme().applyStyle(R.style.GoBoardSizeStyle, true);
			break;
		case 2:
			getTheme().applyStyle(R.style.Go3x3BoardSizeStyle, true);
			break;
		}
		
		setContentView(R.layout.activity_game);
		
		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new GameFragment()).commit();
		}
	}
}
