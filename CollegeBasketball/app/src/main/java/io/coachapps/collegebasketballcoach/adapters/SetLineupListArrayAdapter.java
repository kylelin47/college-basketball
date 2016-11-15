package io.coachapps.collegebasketballcoach.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.List;

import io.coachapps.collegebasketballcoach.R;
import io.coachapps.collegebasketballcoach.SetLineupFragment;
import io.coachapps.collegebasketballcoach.basketballsim.Player;
import io.coachapps.collegebasketballcoach.db.PlayerDao;
import io.coachapps.collegebasketballcoach.util.DataDisplayer;

/**
 * list adapter for displaying the user's team when setting the lineup.
 * Created by Achi Jones on 11/4/2016.
 */

public class SetLineupListArrayAdapter extends ArrayAdapter<Player> {
    private final Context context;
    private final SetLineupFragment frag;
    public final List<Player> players;
    private final PlayerDao playerDao;

    public SetLineupListArrayAdapter(Context context, SetLineupFragment frag, List<Player> values) {
        super(context, R.layout.set_lineup_list_item, values);
        this.context = context;
        this.frag = frag;
        this.players = values;
        this.playerDao = new PlayerDao(context);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        //if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.set_lineup_list_item, parent, false);
        //}

        TextView playerName = (TextView) convertView.findViewById(R.id.textViewName);
        TextView playerPosition = (TextView) convertView.findViewById(R.id.textViewPosition);
        TextView playerOvrPot = (TextView) convertView.findViewById(R.id.textViewOvrPot);

        Player p = players.get(position);
        playerName.setText(p.name + " [" + DataDisplayer.getYearAbbreviation(p.year) + "]");
        String benchOrStart = (position/5) == 0 ? "Start " : "Bench ";
        if (position < 10) {
            playerPosition.setText(benchOrStart +
                    DataDisplayer.getPositionAbbreviation(position % 5 + 1));
        } else {
            playerPosition.setText("Won't Play");
        }
        playerOvrPot.setText(String.valueOf(p.getOverall()) + " / " +
                DataDisplayer.getLetterGrade(p.getPotential()) +
                " (Pref Pos: " + DataDisplayer.getPositionAbbreviation(p.getPosition()) + ")");

        TextView playerShooting   = (TextView) convertView.findViewById(R.id.textViewShooting);
        TextView playerDefense    = (TextView) convertView.findViewById(R.id.textViewDefense);
        TextView playerPassing    = (TextView) convertView.findViewById(R.id.textViewPassing);
        TextView playerRebounding = (TextView) convertView.findViewById(R.id.textViewRebounding);

        final TextView playerMinutes = (TextView) convertView.findViewById(R.id.textViewMinutes);
        final SeekBar seekBarMinutes = (SeekBar) convertView.findViewById(R.id.seekBarMinutes);

        playerShooting.setText(DataDisplayer.getLetterGrade(p.getCompositeShooting()));
        playerDefense.setText(DataDisplayer.getLetterGrade(p.getCompositeDefense()));
        playerPassing.setText(DataDisplayer.getLetterGrade(p.getCompositePassing()));
        playerRebounding.setText(DataDisplayer.getLetterGrade(p.getCompositeRebounding()));

        DataDisplayer.colorizeRatings(playerShooting);
        DataDisplayer.colorizeRatings(playerDefense);
        DataDisplayer.colorizeRatings(playerPassing);
        DataDisplayer.colorizeRatings(playerRebounding);

        Button moveButton = (Button) convertView.findViewById(R.id.buttonMove);
        if (frag.selectedIndex == -1) {
            moveButton.setText("Move");
            moveButton.setBackground(context.getResources().getDrawable(R.drawable.button_shape_primary));
        } else {
            moveButton.setText("Here");
            moveButton.setBackground(context.getResources().getDrawable(R.drawable.button_shape_accent));
        }
        moveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Get ready to swap if none selected, else swap
                if (frag.selectedIndex == -1) {
                    frag.selectedIndex = position;
                    notifyDataSetChanged();
                } else {
                    // swap
                    frag.swapPlayers(frag.selectedIndex, position);
                    notifyDataSetChanged();
                }
            }
        });

        playerMinutes.setText(p.getLineupMinutes() + "min");
        // only the top 5 players can adjust their minutes
        seekBarMinutes.setEnabled(position < 5);
        if (position >= 10) {
            players.get(position).setLineupMinutes(0);
            playerDao.updatePlayerRatings(players.get(position).getId(),
                    players.get(position).ratings);
        }
        seekBarMinutes.setProgress(players.get(position).getLineupMinutes()*3);
        seekBarMinutes.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (position < 5) {
                    playerDao.updatePlayerRatings(players.get(position).getId(),
                            players.get(position).ratings);
                    playerDao.updatePlayerRatings(players.get(position + 5).getId(),
                            players.get(position + 5).ratings);
                }
                notifyDataSetChanged();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Heh
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (position < 5) {
                    players.get(position).setLineupMinutes(seekBarMinutes.getProgress() / 3);
                    players.get(position + 5).setLineupMinutes(40 - seekBarMinutes.getProgress() / 3);
                }
            }
        });

        return convertView;
    }

    public Player getItem(int position) {
        return players.get(position);
    }

}
