package com.enyanxia.minesweeper;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class ResultActivity extends AppCompatActivity {
    public static final String Time_Spent = "time_spent";
    public static final String RESULT = "result";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_result);

        // fetch intent content
        Intent intent = getIntent();
        String timeSpent = intent.getStringExtra(Time_Spent);
        String result = intent.getStringExtra(RESULT);

        // update textView
        TextView textViewTime = (TextView) findViewById(R.id.textViewTimeUsed);
        String timeDisplay = "Used " + timeSpent + " seconds.";
        textViewTime.setText(timeDisplay);

        TextView textViewResult = (TextView) findViewById(R.id.textViewGameResult);
        textViewResult.setText(result);

        TextView textViewEncouragement = (TextView) findViewById(R.id.textViewEncouragement);

        if (result.contains("won")) {
            textViewEncouragement.setText(getString(R.string.encourage_won));
        } else {
            textViewEncouragement.setText(getString(R.string.encourage_lost));
        }
    }

    public void backToMain(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}
