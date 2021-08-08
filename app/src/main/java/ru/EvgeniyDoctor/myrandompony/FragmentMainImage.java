package ru.EvgeniyDoctor.myrandompony;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class FragmentMainImage extends Fragment {
    // res - http://devcolibri.com/4356
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main_image, container, false);
    }
}
