package net.skyestudios.mtgcardquery.ui.Fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import net.skyestudios.mtgcardquery.R;

/**
 * Created by arkeonet64 on 3/14/2017.
 */

public class QueryFragment extends Fragment implements View.OnClickListener {

    private Button openColorIdentityDialog;
    private Button openColorDialog;
    private AlertDialog colorDialog;
    private CheckBox cb_white;
    private CheckBox cb_green;
    private CheckBox cb_red;
    private CheckBox cb_black;
    private CheckBox cb_blue;
    private CheckBox cb_colorless;
    private boolean cb_whiteState;
    private boolean cb_redState;
    private boolean cb_blueState;
    private boolean cb_blackState;
    private boolean cb_greenState;
    private boolean cb_colorlessState;
    private Context context;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_query, container, false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_options, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_reset_query:
                resetQuery();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void resetQuery() {
        cb_whiteState = false;
        cb_redState = false;
        cb_colorlessState = false;
        cb_blackState = false;
        cb_blueState = false;
        cb_greenState = false;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        TextView title = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            context = getContext();
        } else {
            context = getActivity().getApplicationContext();
        }
        title = new TextView(context);
        title.setText("Color Selection");
        title.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        title.setTextSize(getResources().getDimension(R.dimen.dialog_textsize));

        colorDialog = new AlertDialog.Builder(context, R.style.AppTheme_Dialog)
                .setCustomTitle(title)
                .setIcon(R.mipmap.ic_launcher)
                .setView(R.layout.layout_dialog_color)
                .setCancelable(false)
                .setPositiveButton("Apply", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        cb_whiteState = cb_white.isChecked();
                        cb_redState = cb_white.isChecked();
                        cb_blueState = cb_white.isChecked();
                        cb_blackState = cb_white.isChecked();
                        cb_greenState = cb_white.isChecked();
                        cb_colorlessState = cb_colorless.isChecked();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .create();
        colorDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                if (cb_white == null) {
                    cb_white = (CheckBox) colorDialog.findViewById(R.id.color_query_white);
                }
                if (cb_green == null) {
                    cb_green = (CheckBox) colorDialog.findViewById(R.id.color_query_green);
                }
                if (cb_red == null) {
                    cb_red = (CheckBox) colorDialog.findViewById(R.id.color_query_red);
                }
                if (cb_black == null) {
                    cb_black = (CheckBox) colorDialog.findViewById(R.id.color_query_black);
                }
                if (cb_blue == null) {
                    cb_blue = (CheckBox) colorDialog.findViewById(R.id.color_query_blue);
                }
                if (cb_colorless == null) {
                    cb_colorless = (CheckBox) colorDialog.findViewById(R.id.color_query_colorless);
                }

                cb_white.setChecked(cb_whiteState);
                cb_black.setChecked(cb_blackState);
                cb_blue.setChecked(cb_blueState);
                cb_green.setChecked(cb_greenState);
                cb_colorless.setChecked(cb_colorlessState);
                cb_red.setChecked(cb_redState);
            }
        });
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        openColorDialog = (Button) getActivity().findViewById(R.id.openColorDialog);
        openColorDialog.setOnClickListener(this);
        openColorIdentityDialog = (Button) getActivity().findViewById(R.id.openColorIdentityDialog);
        openColorIdentityDialog.setOnClickListener(this);
        super.onResume();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.openColorDialog:
                colorDialog.show();
                break;
            case R.id.openColorIdentityDialog:
                break;
        }
    }
}
