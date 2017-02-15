package com.app.chaton.ui;


import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.app.chaton.R;

public class ProgressDialog extends DialogFragment {

    private Window window;

    public void setWindow(Window window) { this.window = window; };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                                @Nullable final Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.progress_dialog, container, false);

        Rect displayRectangle = new Rect();
        window.getDecorView().getWindowVisibleDisplayFrame(displayRectangle);

        view.setMinimumWidth((int)(displayRectangle.width() * 0.8f));

        Typeface myriad = Typeface.createFromAsset(getActivity().getAssets(), "fonts/MyriadPro.ttf");
        ((TextView) view.findViewById(R.id.tvProgress)).setTypeface(myriad);

        ((ProgressBar) view.findViewById(R.id.progressBar)).getIndeterminateDrawable().setColorFilter(
                ContextCompat.getColor(getContext(), R.color.colorAccent),
                android.graphics.PorterDuff.Mode.SRC_IN
        );

        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(android.support.v4.app.DialogFragment.STYLE_NO_TITLE,
                android.support.v7.appcompat.R.style.Base_Theme_AppCompat_Light_Dialog);
    }
}
