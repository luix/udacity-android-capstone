package com.xinay.droid.fm;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.xinay.droid.fm.R;

/**
 * A placeholder fragment containing a simple view.
 */
public class SearchResultsFragment extends Fragment {

    public SearchResultsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search_results, container, false);
    }
}
