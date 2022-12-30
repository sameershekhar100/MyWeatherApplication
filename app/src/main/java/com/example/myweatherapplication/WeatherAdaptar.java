package com.example.myweatherapplication;

import android.content.Context;
import android.view.ContentInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myweatherapplication.databinding.ItemViewBinding;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

public class WeatherAdaptar extends RecyclerView.Adapter<WeatherAdaptar.WeatherHolder>{
    ArrayList<Weather> weathers;
    Context context;
    public WeatherAdaptar(ArrayList<Weather> weathers, Context context) {
        this.weathers=weathers;
        this.context=context;
    }

    @NonNull
    @Override
    public WeatherHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View myView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_view, parent, false);
        return new WeatherHolder(myView);
    }

    @Override
    public void onBindViewHolder(@NonNull WeatherHolder holder, int position) {
        Weather weather=weathers.get(position);
        holder.cityName.setText(weather.getCity());
        holder.city_desc.setText(weather.getDesc());
        holder.city_temp.setText(weather.getTemp());
        String getTime=TimeUtils.getTime(weather.getTime());

        Date date = new Date();
        DateFormat df = new SimpleDateFormat("HH:mm:ss");

// Use Madrid's time zone to format the date in
        df.setTimeZone(TimeZone.getTimeZone(""));

        System.out.println("Date and time in Madrid: " + df.format(date));
        holder.city_time.setText("Updated "+getTime);


        Glide.with(context).load(weather.getImg_code()).into(holder.imageView);

    }

    @Override
    public int getItemCount() {
        return weathers.size();
    }

    static class WeatherHolder extends RecyclerView.ViewHolder {
            TextView cityName,city_time,city_desc,city_temp;
            ImageView imageView;
            public WeatherHolder(@NonNull View itemView) {
                super(itemView);
                imageView=itemView.findViewById(R.id.city_img);
                cityName=itemView.findViewById(R.id.city_name);
                city_time=itemView.findViewById(R.id.city_time);
                city_desc=itemView.findViewById(R.id.city_desc);
                city_temp=itemView.findViewById(R.id.city_temp);

            }
    }
}
