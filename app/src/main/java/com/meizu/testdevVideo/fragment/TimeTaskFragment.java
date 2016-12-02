package com.meizu.testdevVideo.fragment;


import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.meizu.testdevVideo.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class TimeTaskFragment extends Fragment {

    public TimeTaskFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_time_task, container, false);
    }

}
