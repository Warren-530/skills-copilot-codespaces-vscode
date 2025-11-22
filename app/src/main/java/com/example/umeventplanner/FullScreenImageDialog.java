package com.example.umeventplanner;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;

public class FullScreenImageDialog extends DialogFragment {

    private static final String ARG_IMAGE_URL = "imageUrl";

    public static FullScreenImageDialog newInstance(String imageUrl) {
        FullScreenImageDialog fragment = new FullScreenImageDialog();
        Bundle args = new Bundle();
        args.putString(ARG_IMAGE_URL, imageUrl);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_full_screen_image, container, false);
        ImageView imageView = view.findViewById(R.id.ivFullScreen);
        view.findViewById(R.id.btnClose).setOnClickListener(v -> dismiss());

        if (getArguments() != null) {
            String imageUrl = getArguments().getString(ARG_IMAGE_URL);
            if (imageUrl != null) {
                Glide.with(this).load(imageUrl).into(imageView);
            }
        }

        return view;
    }
}
