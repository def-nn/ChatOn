package com.app.chaton;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.app.chaton.API_helpers.Message;
import com.app.chaton.adapter.DialogAdapter;

import java.util.List;

public class RightPanelFragment extends Fragment {

    private RecyclerView dialogsView;
    private LinearLayoutManager dialogsManager;
    private DialogAdapter dialogAdapter;

    public void setDialogsList(List<Message> dialogsList) {
        dialogAdapter = new DialogAdapter(getActivity().getApplicationContext(), dialogsList);
        dialogsView.setAdapter(dialogAdapter);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.right_menu, container, false);

        Typeface myriad = Typeface.createFromAsset(getActivity().getAssets(), "fonts/MyriadPro.ttf");
        ((EditText) view.findViewById(R.id.searchInput)).setTypeface(myriad);

        dialogsView = (RecyclerView) view.findViewById(R.id.dialogsList);
        dialogsView.setHasFixedSize(false);

        dialogsManager = new LinearLayoutManager(this.getActivity().getApplicationContext());
        dialogsView.setLayoutManager(dialogsManager);

        return view;
    }
}
