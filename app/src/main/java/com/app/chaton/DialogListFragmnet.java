package com.app.chaton;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.app.chaton.API_helpers.CallService;
import com.app.chaton.API_helpers.Message;
import com.app.chaton.API_helpers.MessageResponseObject;
import com.app.chaton.API_helpers.RequestHelper;
import com.app.chaton.API_helpers.ServiceGenerator;
import com.app.chaton.Utils.PreferenceHelper;
import com.app.chaton.adapter.DialogAdapter;
import com.baoyz.widget.PullRefreshLayout;

import java.util.List;

public class DialogListFragmnet extends Fragment {

    private static final String NOT_EDITED = "not_edited";
    private static final String EDITED = "edited";

    private RecyclerView dialogsView;
    private LinearLayoutManager dialogsManager;
    private DialogAdapter dialogAdapter;

    private PullRefreshLayout refreshDialog;
    private PullRefreshLayout.OnRefreshListener onRefreshListener;

    public void setDialogsList(List<Message> dialogsList) {
        dialogAdapter = new DialogAdapter(getActivity().getApplicationContext(),
                                                dialogsList, new DialogListenerFactory());
        dialogsView.setAdapter(dialogAdapter);
    }

    public void setOnRefreshListener(PullRefreshLayout.OnRefreshListener onRefreshListener) {
        this.onRefreshListener = onRefreshListener;
    }

    public void stopProgress() { refreshDialog.setRefreshing(false); }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d("myLogs", "on create view");
        View view = inflater.inflate(R.layout.dialog_list, container, false);

        refreshDialog = (PullRefreshLayout) view.findViewById(R.id.refreshDialogs);
        refreshDialog.setOnRefreshListener(onRefreshListener);

        refreshDialog.setRefreshStyle(PullRefreshLayout.STYLE_RING);
        refreshDialog.setColorSchemeColors(
                ContextCompat.getColor(getContext(), R.color.colorInputDark),
                ContextCompat.getColor(getContext(), R.color.colorMenuFg),
                ContextCompat.getColor(getContext(), R.color.colorInputDark),
                ContextCompat.getColor(getContext(), R.color.colorMenuFg));

        EditText searchInput = (EditText) view.findViewById(R.id.searchInput);
        searchInput.setOnFocusChangeListener(onFocusChangeListener);

        Typeface myriad = Typeface.createFromAsset(getActivity().getAssets(), "fonts/MyriadPro.ttf");
        searchInput.setTypeface(myriad);

        dialogsView = (RecyclerView) view.findViewById(R.id.dialogsList);
        dialogsView.setHasFixedSize(false);

        dialogsManager = new LinearLayoutManager(this.getActivity().getApplicationContext());
        dialogsView.setLayoutManager(dialogsManager);

        return view;
    }

    View.OnFocusChangeListener onFocusChangeListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View view, boolean hasFocus) {
            if (hasFocus) {
                switch (view.getTag().toString()) {
                    case NOT_EDITED:
                        ((EditText) view).setText("");
                        view.setTag(EDITED);
                        break;
                    default:
                        break;
                }
            }
        }
    };

    public class DialogListenerFactory {
        private CallService callService;
        private PreferenceHelper preferenceHelper;

        public DialogListenerFactory() {
            this.callService = ServiceGenerator.createService(CallService.class);
            this.preferenceHelper = new PreferenceHelper(getActivity().getSharedPreferences(
                    getResources().getString(R.string.PREFERENCE_FILE), Context.MODE_PRIVATE));
        }

        public View.OnClickListener createListener(final Long companionId) {
            return new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d("myLogs", "on click " + companionId);
                    RequestHelper requestHelper = new RequestHelper() {
                        @Override
                        public void onStatusOk(MessageResponseObject response) {
                            Log.d("myLogs", "ok " + response.getData().toString());
                        }

                        @Override
                        public void onStatusServerError(MessageResponseObject response) {
                            Log.d("myLogs", "server error");
                        }

                        @Override
                        public void onFail(Throwable t) {
                            Log.d("myLogs", "fail " + t.toString());
                            t.printStackTrace();
                        }
                    };
                    requestHelper.getDialogsById(callService, companionId,
                            preferenceHelper.getId(), preferenceHelper.getSecretKey());
                }
            };
        }
    }
}
