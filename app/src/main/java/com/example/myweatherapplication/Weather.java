package com.example.myweatherapplication;

public class Weather {
    String temp,img_code,city,desc;
    long time;

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public Weather(String city, String img_code, String temp, String desc, long time) {
        this.temp = temp;
        this.img_code = img_code;
        this.city = city;
        this.desc = desc;
        this.time=time;
    }

    public String getTemp() {
        return temp;
    }

    public String getImg_code() {
        return img_code;
    }

    public String getCity() {
        return city;
    }

    public String getDesc() {
        return desc;
    }

    public void setTemp(String temp) {
        this.temp = temp;
    }

    public void setImg_code(String img_code) {
        this.img_code = img_code;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
