package com.example.pingoapp.models;

public class ModelStory {

    String uid,sId,sImage;
    long sTimeStart,sTimeEnd;


    public ModelStory( String sId, String sImage,  long sTimeEnd,long sTimeStart,String uid) {
        this.uid = uid;
        this.sId = sId;
        this.sImage = sImage;
        this.sTimeStart = sTimeStart;
        this.sTimeEnd = sTimeEnd;
    }

    public ModelStory() {
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getsId() {
        return sId;
    }

    public void setsId(String sId) {
        this.sId = sId;
    }

    public String getsImage() {
        return sImage;
    }

    public void setsImage(String sImage) {
        this.sImage = sImage;
    }

    public long getsTimeStart() {
        return sTimeStart;
    }

    public void setsTimeStart(long sTimeStart) {
        this.sTimeStart = sTimeStart;
    }

    public long getsTimeEnd() {
        return sTimeEnd;
    }

    public void setsTimeEnd(long sTimeEnd) {
        this.sTimeEnd = sTimeEnd;
    }
}
