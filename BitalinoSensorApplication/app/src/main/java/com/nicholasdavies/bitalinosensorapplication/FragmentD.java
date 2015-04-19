package com.nicholasdavies.bitalinosensorapplication;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

/**
 * Fragment D which links to the Sensor Selection menu for data input
 *
 * @author Nick Davies
 */
public class FragmentD extends Fragment {
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceStat) {
        return inflater.inflate(R.layout.fragment_d, container, false);
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        ImageButton btn3 = (ImageButton) getActivity().findViewById(R.id.imageButton4);

        btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(FragmentD.this.getActivity(), SensorSelect.class);
                FragmentD.this.startActivity(intent);

            }
        });

        super.onActivityCreated(savedInstanceState);
    }

}