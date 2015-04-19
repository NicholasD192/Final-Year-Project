package com.nicholasdavies.bitalinosensorapplication;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

/**
 * Fragment B containing create patient option
 *
 * @author Nick Davies
 */
public class FragmentB extends Fragment {
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceStat) {
        return inflater.inflate(R.layout.fragment_b, container, false);
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        ImageButton btn2 = (ImageButton) getActivity().findViewById(R.id.imageButton2);

        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(FragmentB.this.getActivity(), CreatePatient.class);
                FragmentB.this.startActivity(intent);

            }
        });

        super.onActivityCreated(savedInstanceState);
    }

}