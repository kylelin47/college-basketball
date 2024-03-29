package io.coachapps.collegebasketballcoach.adapters.game;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;
import java.util.ListIterator;

import io.coachapps.collegebasketballcoach.R;
import io.coachapps.collegebasketballcoach.basketballsim.Game;
import io.coachapps.collegebasketballcoach.fragments.BracketDialogFragment;

public class BracketListArrayAdapter extends ArrayAdapter<String>  {
    private final Context context;
    private List<String> gameSummaries;
    private final BracketDialogFragment bracketDialogFragment;
    private final String userTeamName;

    public BracketListArrayAdapter(Context context, BracketDialogFragment bracketDialogFragment,
                                   List<String> games, String userTeamName) {
        super(context, R.layout.game_result_list_item, games);
        this.context = context;
        this.bracketDialogFragment = bracketDialogFragment;
        this.gameSummaries = games;
        this.userTeamName = userTeamName;
        setBoundaries();
    }

    private void setBoundaries() {
        gameSummaries.add(0, "Round 1");
        ListIterator<String> iterator = gameSummaries.listIterator();
        iterator.next();
        int prev = getWeek(iterator.next().split(","));
        int round = 2;
        for (; iterator.hasNext();) {
            int currentWeek = getWeek(iterator.next().split(","));
            if (currentWeek != prev) {
                iterator.previous();
                iterator.add("Round " + round++);
                iterator.next();
            }
            prev = currentWeek;
        }
    }

    private static class ViewHolder {
        TextView textHomeName;
        TextView textAwayName;
        Button gameButton;
    }

    @Override
    @NonNull
    public View getView(final int position, View convertView, @NonNull ViewGroup parent) {
        ViewHolder viewHolder;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        // maybe later
        View rowView = inflater.inflate(R.layout.bracket_list_item, parent, false);
        viewHolder = new ViewHolder();
        viewHolder.textHomeName = (TextView) rowView.findViewById(R.id.bracketListHomeTeam);
        viewHolder.textAwayName = (TextView) rowView.findViewById(R.id.bracketListAwayTeam);
        viewHolder.gameButton = (Button) rowView.findViewById(R.id.bracketListGameButton);
        if (gameSummaries.get(position).contains("Round")) {
            LinearLayout rootLayout = (LinearLayout) rowView.findViewById(R.id.bracketListRootLayout);
            TextView round = new TextView(context);
            round.setText(gameSummaries.get(position));
            round.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            rootLayout.addView(round);
            viewHolder.textAwayName.setVisibility(View.GONE);
            viewHolder.gameButton.setVisibility(View.GONE);
            viewHolder.textHomeName.setVisibility(View.GONE);
            return rowView;
        }
        final String[] gameInfo = gameSummaries.get(position).split(",");
        final String homeSeed;
        final String awaySeed;
        if (gameInfo[7].equals(Game.GameType.MARCH_MADNESS.toString())) {
            homeSeed = "" + ((Integer.parseInt(gameInfo[10]) - 1)/4 + 1);
            awaySeed = "" + ((Integer.parseInt(gameInfo[11]) - 1)/4 + 1);
        } else {
            homeSeed = gameInfo[8];
            awaySeed = gameInfo[9];
        }

        viewHolder.textHomeName.setText("(" + homeSeed + ") " + getShortName(gameInfo[0]));
        viewHolder.textAwayName.setText("(" + awaySeed + ") " + getShortName(gameInfo[1]));
        if (gameInfo[0].equals(userTeamName)) {
            // Highlight user team
            viewHolder.textHomeName.setTextColor(Color.parseColor("#DD5600"));
        } else {
            viewHolder.textHomeName.setTextColor(Color.parseColor("#000000"));
        }
        if (gameInfo[1].equals(userTeamName)) {
            // Highlight user team
            viewHolder.textAwayName.setTextColor(Color.parseColor("#DD5600"));
        } else {
            viewHolder.textAwayName.setTextColor(Color.parseColor("#000000"));
        }

        if (gameInfo[6].equals("true")) {
            viewHolder.gameButton.setText(gameInfo[2] + " - " + gameInfo[3]);
        } else {
            viewHolder.gameButton.setText("---");
        }
        viewHolder.gameButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (gameInfo[6].equals("true")) {
                    bracketDialogFragment.showGameSummaryDialog(getYear(gameInfo),
                           getWeek(gameInfo), gameInfo[0], gameInfo[1],
                            "(" + homeSeed + ") " + gameInfo[0],
                            "(" + awaySeed + ") " + gameInfo[1]);
                }
            }
        });
        viewHolder.textHomeName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bracketDialogFragment.examineTeam(gameInfo[0]);
            }
        });
        viewHolder.textAwayName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bracketDialogFragment.examineTeam(gameInfo[1]);
            }
        });
        return rowView;
    }
    private int getYear(String[] gameSummary) {
        return Integer.valueOf(gameSummary[4]);
    }
    private int getWeek(String[] gameSummary) {
        return Integer.valueOf(gameSummary[5]);
    }
    private String getShortName(String teamName) {
        if (teamName.equals(userTeamName)) return teamName;
        try {
            String[] strSplit = teamName.split(" ");
            String name = "";
            for (int i = 0; i < strSplit.length-1; ++i) {
                if (i != 0) {
                    name += " " + strSplit[i];
                } else {
                    name += strSplit[i];
                }
            }
            return name;
        } catch (Exception e) {
            // whoops
            return teamName;
        }
    }
}
