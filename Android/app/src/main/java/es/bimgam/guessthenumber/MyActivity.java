package es.bimgam.guessthenumber;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import es.bimgam.game.helper.*;

import com.google.android.gms.games.Games;

import java.util.Random;


public class MyActivity extends AppCompatActivity implements GameHelper.GameHelperListener, View.OnClickListener {

    private AdView adView;

    private Toolbar toolbar;

    private GameHelper helper;

    private boolean threadReconnect = true;

    public static final int CLIENT_GAMES = GameHelper.CLIENT_GAMES;

    protected int RequestedClients = CLIENT_GAMES;

    public int myNumber, userNumber, guessCount = 0;
    TextView mTextView;
    TextView mTextView2;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    Random rand;

    int REQUEST_LEADERBOARD = 10003;
    int REQUEST_ACHIEVEMENTS = 10004;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);
        //getActionBar().show();
        toolbar = (Toolbar) findViewById(R.id.app_bar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        helper = new GameHelper(this, RequestedClients);
        helper.setup(this);
        helper.enableDebugLog(false);
        helper.beginUserInitiatedSignIn();

        mTextView = (TextView) findViewById(R.id.textView);
        mTextView2 = (TextView) findViewById(R.id.textView2);
        sharedPreferences = getSharedPreferences("es.bimgam.guessthenumber", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        guessCount = 0;
        this.updateLabels(guessCount, sharedPreferences.getInt("BestScore", 0));
        rand = new Random();
        myNumber = rand.nextInt(99);

        Log.d("Number", ((Integer) myNumber).toString());

        adView = (AdView) this.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        Thread thread = new Thread() {
            public void run() {
                while (threadReconnect) {
                    Log.d(this.getClass().getSimpleName(), "No Signed in");
                    try {
                        sleep(8000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    helper.reconnectClient();
                }
            }
        };
        thread.start();
    }

    private void updateLabels(int guessCount, int bestScore) {
        mTextView.setText(getText(R.string.times_guessed).toString() + ": " + guessCount);
        if (bestScore != -1) {
            mTextView2.setText(getText(R.string.best_score).toString() + ": " + bestScore);
        }
    }

    @Override
    protected void onStart() {
        Log.d(this.getClass().getSimpleName(), "onStart");
        super.onStart();
        helper.onStart(this);
    }

    @Override
    protected void onStop() {
        Log.d(this.getClass().getSimpleName(), "onStop");
        super.onStop();
        helper.onStop();
        threadReconnect = false;
    }

    public void onClick(View view) {

    }

    @Override
    public void onSignInFailed() {
        Log.d(this.getClass().getSimpleName(), "Sign-in failed.");
        helper.reconnectClient();
    }

    @Override
    public void onSignInSucceeded() {
        Log.d(this.getClass().getSimpleName(), "Sign-in succeeded.");
        helper.setConnectOnStart(true);
        threadReconnect = false;
        Games.Achievements.unlock(helper.getApiClient(), getString(R.string.achievement_login_in));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.my, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_leaderboards) {
            if (helper.isSignedIn()) {
                startActivityForResult(Games.Leaderboards.getLeaderboardIntent(helper.getApiClient(), getString(R.string.leaderboard_best_score)),
                        REQUEST_LEADERBOARD);
            } else {
                helper.reconnectClient();
            }
            return true;
        } else if (id == R.id.action_achievements) {
            if (helper.isSignedIn()) {
                startActivityForResult(Games.Achievements.getAchievementsIntent(helper.getApiClient()),
                        REQUEST_ACHIEVEMENTS);
            } else {
                helper.reconnectClient();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void newGame(View view) {
        myNumber = rand.nextInt((100 - 0) + 1) + 0;
        guessCount = 0;

        this.updateLabels(0, -1);

        if (helper.isSignedIn()) {
            Games.Achievements.increment(helper.getApiClient(), getString(R.string.achievement_new_game), 1);
        }
    }

    public void takeTheGuess(View view) {
        EditText editText = (EditText) findViewById(R.id.editText);
        String message = "";

        if (!editText.getText().toString().equals("")) {
            guessCount++;
            int newNumber = 0;
            try {
                newNumber = Integer.parseInt(editText.getText().toString());
            } catch (Exception e) {
            }

            if (newNumber == userNumber && helper.isSignedIn()) {
                Games.Achievements.increment(helper.getApiClient(), getString(R.string.achievement_inquisitive), 1);
            }
            userNumber = newNumber;

            if (userNumber > myNumber) {
                message = getText(R.string.my_number_is_less_tan_yours).toString();

                if (helper.isSignedIn()) {
                    Games.Achievements.increment(helper.getApiClient(), getString(R.string.achievement_i_love_trying), 1);
                }
            } else if (userNumber < myNumber) {
                message = getText(R.string.my_number_is_bigger_than_yours).toString();

                if (helper.isSignedIn()) {
                    Games.Achievements.increment(helper.getApiClient(), getString(R.string.achievement_i_love_trying), 1);
                }
            } else if (userNumber == myNumber) {
                message = getText(R.string.congeats_you_gussed_my_number).toString();

                int bestScore = sharedPreferences.getInt("BestScore", 0);

                if (guessCount < bestScore || bestScore == 0) {
                    editor.putInt("BestScore", guessCount);
                    editor.commit();
                    bestScore = guessCount;
                    if (helper.isSignedIn()) {
                        Games.Leaderboards.submitScore(helper.getApiClient(), getText(R.string.leaderboard_best_score).toString(), (long) bestScore);
                    }
                }

                myNumber = rand.nextInt((100 - 0) + 1) + 0;
                editText.setText("");

                this.updateLabels(guessCount, bestScore);
                if (helper.isSignedIn()) {
                    Games.Achievements.increment(helper.getApiClient(), getString(R.string.achievement_persistent_player), 1);
                }
                guessCount = 0;
            }
        } else {
            message = getText(R.string.enter_the_number).toString();
        }

        Context context = getApplicationContext();

        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();

        this.updateLabels(guessCount, -1);
    }
}