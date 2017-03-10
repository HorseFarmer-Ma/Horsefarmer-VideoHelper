package com.meizu.testdevVideo.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.meizu.testdevVideo.R;

/**
 * Scheme测试
 */
public class SchemeTestFragment extends Fragment {

    private View view;

    public SchemeTestFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_scheme_test, container, false);

        return view;
    }

}
