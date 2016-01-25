package com.xinay.droid.fm;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.xinay.droid.fm.model.Playlist;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link GenresFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link GenresFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GenresFragment extends Fragment {

    private final String LOG_TAG = GenresFragment.class.getSimpleName();

    private Playlist playlist;

    private GenresListAdapter genresListAdapter;

//    private OnFragmentInteractionListener mListener;

    public GenresFragment() {
        // Required empty public constructor
        genresListAdapter = new GenresListAdapter();
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment GenresFragment.
     */
    // TODO: Rename and change types and number of parameters
//    public static GenresFragment newInstance(String param1, String param2) {
//        GenresFragment fragment = new GenresFragment();
//        Bundle args = new Bundle();
////        args.putString(ARG_PARAM1, param1);
////        args.putString(ARG_PARAM2, param2);
////        fragment.setArguments(args);
//        return fragment;
//    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.v(LOG_TAG, "onCreate");
        super.onCreate(savedInstanceState);
//        if (getArguments() != null) {
//            mParam1 = getArguments().getString(ARG_PARAM1);
//            mParam2 = getArguments().getString(ARG_PARAM2);
//        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.v(LOG_TAG, "onCreateView");
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_genres, container, false);
        GridView gridview = (GridView)view.findViewById(R.id.gridview);

        List<ItemObject> allItems = getAllItemObject();
        CustomAdapter customAdapter = new CustomAdapter(getActivity(), allItems);
        gridview.setAdapter(customAdapter);

        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getActivity(), "Position: " + position, Toast.LENGTH_SHORT).show();
            }
        });
        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
//    public void onButtonPressed(Uri uri) {
//        if (mListener != null) {
//            mListener.onFragmentInteraction(uri);
//        }
//    }

//    @Override
//    public void onAttach(Context context) {
//        super.onAttach(context);
//        if (context instanceof OnFragmentInteractionListener) {
//            mListener = (OnFragmentInteractionListener) context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
//    }

//    @Override
//    public void onDetach() {
//        super.onDetach();
//        mListener = null;
//    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
//    public interface OnFragmentInteractionListener {
//        // TODO: Update argument type and name
//        void onFragmentInteraction(Uri uri);
//    }


    private List<ItemObject> getAllItemObject(){
        List<ItemObject> items = new ArrayList<>();
        items.add(new ItemObject(R.drawable.droid_fm,"Dip It Low", "Christina Milian"));
        items.add(new ItemObject(R.drawable.droid_fm,"Someone like you", "Adele Adkins"));
        items.add(new ItemObject(R.drawable.droid_fm,"Ride", "Ciara"));
        items.add(new ItemObject(R.drawable.droid_fm,"Paparazzi", "Lady Gaga"));
        items.add(new ItemObject(R.drawable.droid_fm,"Forever", "Chris Brown"));
        items.add(new ItemObject(R.drawable.droid_fm,"Stay", "Rihanna"));
        items.add(new ItemObject(R.drawable.droid_fm,"Marry me", "Jason Derulo"));
        items.add(new ItemObject(R.drawable.droid_fm,"Waka Waka", "Shakira"));
        items.add(new ItemObject(R.drawable.droid_fm,"Dark Horse", "Katy Perry"));
        items.add(new ItemObject(R.drawable.droid_fm,"Dip It Low", "Christina Milian"));
        return items;
    }

    public void setPlaylist(Playlist playlist) {
        this.playlist = playlist;
        genresListAdapter.setPlaylist(playlist);
        genresListAdapter.notifyDataSetChanged();
    }
}
