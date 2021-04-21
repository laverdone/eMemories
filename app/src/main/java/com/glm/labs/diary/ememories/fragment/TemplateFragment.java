package com.glm.labs.diary.ememories.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.glm.ememories.R;
import com.glm.labs.diary.ememories.WriteActivity;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link TemplateFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link TemplateFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TemplateFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private ImageView mTemplate1;
    private ImageView mTemplate2;
    private ImageView mTemplate3;
    private ImageView mTemplate4;
    private ImageView mTemplate5;
    private ImageView mTemplate6;

    private TextView mFontTemplate1;
    private TextView mFontTemplate2;
    private TextView mFontTemplate3;
    private TextView mFontTemplate4;
    private TextView mFontTemplate5;
    private TextView mFontTemplate6;
    private OnFragmentInteractionListener mListener;

    public TemplateFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment TemplateFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TemplateFragment newInstance(String param1, String param2) {
        TemplateFragment fragment = new TemplateFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView =  inflater.inflate(R.layout.fragment_template, container, false);
        LinearLayout mTemplate1 = (LinearLayout) rootView.findViewById(R.id.template1);
        LinearLayout mTemplate2 = (LinearLayout) rootView.findViewById(R.id.template2);
        LinearLayout mTemplate3 = (LinearLayout) rootView.findViewById(R.id.template3);
        LinearLayout mTemplate4 = (LinearLayout) rootView.findViewById(R.id.template4);
        LinearLayout mTemplate5 = (LinearLayout) rootView.findViewById(R.id.template5);
        LinearLayout mTemplate6 = (LinearLayout) rootView.findViewById(R.id.template6);

        mFontTemplate1       = (TextView) rootView.findViewById(R.id.FontTemplate1);
        mFontTemplate2       = (TextView) rootView.findViewById(R.id.FontTemplate2);
        mFontTemplate3       = (TextView) rootView.findViewById(R.id.FontTemplate3);
        mFontTemplate4       = (TextView) rootView.findViewById(R.id.FontTemplate4);
        mFontTemplate5       = (TextView) rootView.findViewById(R.id.FontTemplate5);
        mFontTemplate6       = (TextView) rootView.findViewById(R.id.FontTemplate6);

        Typeface tf = Typeface.createFromAsset(getActivity().getAssets(), "template/1/font.ttf");
        mFontTemplate1.setTypeface(tf);

        tf = Typeface.createFromAsset(getActivity().getAssets(), "template/2/font.ttf");
        mFontTemplate2.setTypeface(tf);

        tf = Typeface.createFromAsset(getActivity().getAssets(), "template/3/font.ttf");
        mFontTemplate3.setTypeface(tf);

        tf = Typeface.createFromAsset(getActivity().getAssets(), "template/4/font.ttf");
        mFontTemplate4.setTypeface(tf);

        tf = Typeface.createFromAsset(getActivity().getAssets(), "template/5/font.ttf");
        mFontTemplate5.setTypeface(tf);

        tf = Typeface.createFromAsset(getActivity().getAssets(), "template/6/font.ttf");
        mFontTemplate6.setTypeface(tf);

        mTemplate1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent newIntent = new Intent();
                newIntent.putExtra("template", 1);
                newIntent.putExtra("DiaryID", -1l);
                newIntent.setClass(getActivity(), WriteActivity.class);
                getActivity().startActivity(newIntent);
                getActivity().finish();
            }
        });

        mTemplate2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent newIntent = new Intent();
                newIntent.putExtra("template", 2);
                newIntent.putExtra("DiaryID", -1l);
                newIntent.setClass(getActivity(), WriteActivity.class);
                getActivity().startActivity(newIntent);
                getActivity().finish();
            }
        });

        mTemplate3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent newIntent = new Intent();
                newIntent.putExtra("template", 3);
                newIntent.putExtra("DiaryID", -1l);
                newIntent.setClass(getActivity(), WriteActivity.class);
                getActivity().startActivity(newIntent);
                getActivity().finish();
            }
        });

        mTemplate4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent newIntent = new Intent();
                newIntent.putExtra("template", 4);
                newIntent.putExtra("DiaryID", -1l);
                newIntent.setClass(getActivity(), WriteActivity.class);
                getActivity().startActivity(newIntent);
                getActivity().finish();
            }
        });

        mTemplate5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent newIntent = new Intent();
                newIntent.putExtra("template", 5);
                newIntent.putExtra("DiaryID", -1l);
                newIntent.setClass(getActivity(), WriteActivity.class);
                getActivity().startActivity(newIntent);
                getActivity().finish();
            }
        });

        mTemplate6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent newIntent = new Intent();
                newIntent.putExtra("template", 6);
                newIntent.putExtra("DiaryID", -1l);
                newIntent.setClass(getActivity(), WriteActivity.class);
                getActivity().startActivity(newIntent);
                getActivity().finish();
            }
        });
        // Inflate the layout for this fragment
        return rootView;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


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
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
