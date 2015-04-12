package com.nicholasdavies.bitalinosensorapplication;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import java.util.Set;

/**
 * Created by Nick Davies on 17/11/2014.
 */
public class FragmentA extends Fragment {
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceStat) {

        return inflater.inflate(R.layout.fragment_a, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        ImageButton btn1 = (ImageButton) getActivity().findViewById(R.id.imageButton1);

        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(FragmentA.this.getActivity(), Settings.class);
                FragmentA.this.startActivity(intent);

            }
        });

        super.onActivityCreated(savedInstanceState);
    }


}
