package com.enyanxia.minesweeper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.gridlayout.widget.GridLayout;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.os.Handler;

import android.content.Context;
import java.util.Random;

import java.util.ArrayList;

enum Mode {
    Digging,
    Flagging
}

public class MainActivity extends AppCompatActivity {
    private static final int Row_COUNT = 14;
    private static final int COLUMN_COUNT = 12;
    private static final int numMine = 15;

    private int seconds;
    private Mode mode;
    private Context context;

    // icon constant
    String flagIcon = "\uD83D\uDEA9";
    String mineIcon = "\uD83D\uDCA3";
    String pickIcon = "\u26CF";
    String wrongFlagIcon = "❌";

    private int numCellRevealed;
    private Boolean isGameOver;
    private Boolean userWon;

    private ArrayList<TextView> cell_tvs;
    private ArrayList<Integer> mine_idx;
    private ArrayList<Integer> visited_idx;

    private static final int[] directionX = new int[]{-1, -1, 0, 1, 1, 1, 0, -1};
    private static final int[] directionY = new int[]{0, 1, 1, 1, 0, -1, -1, -1};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // init
        context = this;
        isGameOver = false;
        userWon = false;
        Random rand = new Random();
        visited_idx = new ArrayList<>();
        mode = Mode.Digging;

        // randomly assign bombs
        mine_idx = new ArrayList<>();
        int upperBound = Row_COUNT * COLUMN_COUNT;
        int idx;
        for (int i = 0; i < numMine; i++) {
            do {
                idx = rand.nextInt(upperBound);
            } while(mine_idx.contains(idx));

            mine_idx.add(idx);
        }

        // create cells via Inflate
        cell_tvs = new ArrayList<>();
        GridLayout grid = findViewById(R.id.gridLayoutCells);

        LayoutInflater li = LayoutInflater.from(this);
        for (int i = 0; i < Row_COUNT; i++) {
            for (int j = 0; j < COLUMN_COUNT; j++) {
                TextView tv = (TextView) li.inflate(R.layout.custom_cell_layout, grid, false);

                tv.setTextColor(Color.BLACK);
                tv.setBackgroundColor(ContextCompat.getColor(context, android.R.color.holo_green_light));
                tv.setOnClickListener(this::onClickTV);

                GridLayout.LayoutParams lp = (GridLayout.LayoutParams) tv.getLayoutParams();
                lp.rowSpec = GridLayout.spec(i);
                lp.columnSpec = GridLayout.spec(j);

                grid.addView(tv, lp);

                cell_tvs.add(tv);
            }
        }

        runClock();
    }

    private int findIndexOfCellTextView(TextView tv) {
        for (int n = 0; n < cell_tvs.size(); n++) {
            if (cell_tvs.get(n) == tv)
                return n;
        }
        return -1;
    }

    private int calculateCellIndex(int row, int col) {
        return row * COLUMN_COUNT + col;
    }

    public void onClickTV(View view){
        TextView tv = (TextView) view;
        int n = findIndexOfCellTextView(tv);
        int i = n / COLUMN_COUNT;
        int j = n % COLUMN_COUNT;

        if (isGameOver) {
            onSendResult();
            return;
        }

        switch (mode) {
            case Digging:

                if (isFlagged(n)) {
                    return;
                }

                if (mine_idx.contains(n)) { // user lost
                    isGameOver = true;
                    revealAllMines();
                } else {
                    dfs(n, i, j);

                    if (numCellRevealed == (Row_COUNT * COLUMN_COUNT) - numMine) { // user won
                        userWon = true;
                        isGameOver = true;
                        revealAllMines();
                    }
                }
                break;
            case Flagging:
                TextView flagCount = findViewById(R.id.textViewFlagCount);
                int currFlagCount = Integer.parseInt(flagCount.getText().toString());

                // check if already flagged
                if (tv.getText().toString().equals(flagIcon)) {
                    tv.setText(""); // clear
                    currFlagCount++;
                } else if (visited_idx.contains(n)) {
                    return;
                } else {
                    tv.setText(flagIcon);
                    currFlagCount--;
                }

                String newFlagCount = Integer.toString(currFlagCount);
                flagCount.setText(newFlagCount);
        }
    }

    private void runClock() {
        final TextView clockView = findViewById(R.id.textViewClockCount);
        final Handler handler = new Handler();

        handler.post(new Runnable(){
            @Override
            public void run() {
                if (!isGameOver){
                    String time = Integer.toString(seconds);
                    clockView.setText(time);
                    seconds++;
                }
                handler.postDelayed(this, 1000);
            }
        });
    }

    private void dfs(int cellIdx, int row, int col){

        if (!isValidCell(row, col) || mine_idx.contains(cellIdx) || visited_idx.contains(cellIdx)) {
            return;
        }

        numCellRevealed++;
        visited_idx.add(cellIdx);

        // count num of adjacent bombs (in eight directions)
        int numBombs = 0;
        for (int i = 0; i < 8; i++) {
            int x = row + directionX[i];
            int y = col + directionY[i];

            // check if bomb
            int newCellIdx = calculateCellIndex(x, y);
            if (isValidCell(x, y) && mine_idx.contains(newCellIdx)) {
                numBombs++;
            }
        }

        // update textView
        TextView tv = cell_tvs.get(cellIdx);
        tv.setBackgroundColor(Color.LTGRAY);

        if (isFlagged(cellIdx)) {
            // increment flag count
            TextView flagCount = findViewById(R.id.textViewFlagCount);
            int currFlagCount = Integer.parseInt(flagCount.getText().toString());
            currFlagCount++;
            String newFlagCount = Integer.toString(currFlagCount);
            flagCount.setText(newFlagCount);
            tv.setText("");
        }

        if (numBombs > 0) {
            // display num of adjacent bombs
            tv.setText(String.valueOf(numBombs));
        } else {
            for (int i = 0; i < 8; i++) {
                int x = row + directionX[i];
                int y = col + directionY[i];

                dfs(calculateCellIndex(x, y), x, y);
            }
        }
    }

    private Boolean isValidCell(int row, int col) {
        return row >= 0 && row < Row_COUNT && col >= 0 && col < COLUMN_COUNT;
    }

    private Boolean isFlagged(int cellIdx) {
        TextView tv = cell_tvs.get(cellIdx);

        return tv.getText().toString().equals(flagIcon);
    }

    public void onTapping(View view){
        TextView icon = findViewById(R.id.textViewTapIcon);

        switch (mode) {
            case Digging:
                mode = Mode.Flagging;
                icon.setText(flagIcon);
                break;
            case Flagging:
                mode = Mode.Digging;
                icon.setText(pickIcon);
        }
    }

    public void onSendResult(){
        Intent intent = new Intent(this, ResultActivity.class);

        TextView clockView = findViewById(R.id.textViewClockCount);
        intent.putExtra("time_spent", clockView.getText());
        if (userWon) {
            intent.putExtra("result", "You won.");
        } else {
            intent.putExtra("result", "You lost.");
        }

        startActivity(intent);
    }

    private void revealAllMines(){
        for (int idx: mine_idx) {
            TextView mineCell = cell_tvs.get(idx);
            if (!mineCell.getText().toString().equals(mineIcon)) {
                mineCell.setText(mineIcon);
            }
        }

        // deal with wrong flagging
        for (TextView tv:cell_tvs) {
            if (tv.getText().toString().equals(flagIcon)) {
                tv.setText(wrongFlagIcon);
                tv.setTextColor(ContextCompat.getColor(context, android.R.color.holo_red_dark));
            }
        }
    }
}