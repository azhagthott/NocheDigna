package com.zecovery.android.nochedigna.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.zecovery.android.nochedigna.R;
import com.zecovery.android.nochedigna.activity.MapsActivity;

/**
 * A placeholder fragment containing a simple view.
 */
public class LoginActivityFragment extends Fragment implements View.OnClickListener {

    private Button buttonLogin;

    public LoginActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_login, container, false);

        buttonLogin = (Button) rootView.findViewById(R.id.buttonLogin);
        buttonLogin.setOnClickListener(this);

        return rootView;
    }

    @Override
    public void onClick(View view) {

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getActivity(), MapsActivity.class);
                startActivity(i);
            }
        });
    }
}
