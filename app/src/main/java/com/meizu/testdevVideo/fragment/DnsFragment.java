package com.meizu.testdevVideo.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.meizu.testdevVideo.R;


public class DnsFragment extends Fragment{
    private Button set_dns;


    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public DnsFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dns, container, false);
        set_dns = (Button) view.findViewById(R.id.set_dns);
        set_dns.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                WifiFunction.set_static("MZ-MEIZU-5G", "172.16.200.87", "172.17.108.1", "172.17.16.99");
            }
        });
        return view;
    }


}



