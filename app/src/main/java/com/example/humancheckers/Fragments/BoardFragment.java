package com.example.humancheckers.Fragments;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.example.humancheckers.R;
import com.example.humancheckers.Managers.ScoreManager;

public class BoardFragment extends Fragment {

    private ScoreManager scoreManager;

    public BoardFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_board, container, false);
        scoreManager = new ScoreManager((TextView) view.findViewById(R.id.board_TXT_score));
        return view;
    }

    public void playerPrompt(String playerName) {
        TextView prompt = getActivity().findViewById(R.id.board_TXT_prompt);
        prompt.setText("Turn of: " + playerName);
    }

    public ScoreManager getScoreManager() {
        return scoreManager;
    }

    public void bombPrompt() {
        TextView prompt = getActivity().findViewById(R.id.board_TXT_prompt);
        prompt.setText("Cast a spell to kill any checker on the map!");
    }

    public void winPrompt(String playerName) {
        TextView prompt = getActivity().findViewById(R.id.board_TXT_prompt);
        prompt.setText(playerName + " has won the game!");
    }

    public void stuckPrompt() {
        TextView prompt = getActivity().findViewById(R.id.board_TXT_prompt);
        prompt.setText("Cannot move! switching turns...");
        new Handler().postDelayed(() -> {}, 500); // delay UI
    }

    public void drawPrompt() {
        TextView prompt = getActivity().findViewById(R.id.board_TXT_prompt);
        prompt.setText("It's a draw!");
    }
}