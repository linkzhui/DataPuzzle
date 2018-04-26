package com.example.raymon.datapuzzle;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SlideAdapter extends PagerAdapter {

    Context context;
    LayoutInflater inflater;

    int[] imageList = {
            R.drawable.calendar,
            R.drawable.growth,
            R.drawable.planning
    };

    String[] titleList = {
            "No. 1",
            "No. 2",
            "No. 3"
    };

    String[] descList = {
            "No. 1 desc",
            "No. 2 desc",
            "No. 3 desc"
    };

    public SlideAdapter(Context context) {
        this.context = context;
    }

    @Override
    public int getCount() {
        return titleList.length;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return (view == (LinearLayout)object);
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        inflater = (LayoutInflater)context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.slide, container, false);

        LinearLayout slideLayout = (LinearLayout)view.findViewById(R.id.id_slider);
        ImageView slideImage = (ImageView)view.findViewById(R.id.id_slider_image);
        TextView slideTitle = (TextView)view.findViewById(R.id.id_slider_title);
        TextView slideDesc = (TextView)view.findViewById(R.id.id_slider_desc);

        if (position == titleList.length - 1) { // the last one, add one button for user to skip
            System.out.println("=========last one");
            Button myButton = new Button(context);
            myButton.setText("Go To App");
            myButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, MainActivity.class);
                    Log.i("SlideAdapter","call main activity");
                    context.startActivity(intent);
                }
            });
            slideLayout.addView(myButton);
        }

        slideImage.setImageResource(imageList[position]);
        slideTitle.setText(titleList[position]);
        slideDesc.setText(descList[position]);



        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((LinearLayout)object);
    }
}