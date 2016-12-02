package com.meizu.testdevVideo.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.meizu.testdevVideo.R;


/**
 * 自定义Monkey页面
 */
public class DefinedMonkeyFragment extends Fragment {

    public DefinedMonkeyFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_defined_monkey, container, false);
    }

}
