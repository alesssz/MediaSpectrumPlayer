package com.example.ale.mediaspectrumplayer;


import android.os.Parcel;
import android.os.Parcelable;

public class Songs implements Parcelable {

    public String mSongname, mBandName, mAlbumName, mPathSong;


    public Songs(String songName, String bandName, String albumName, String pathSong) {

        mSongname = songName;
        mBandName = bandName;
        mAlbumName = albumName;
        mPathSong = pathSong;

    }


    public void setSongname(String songname) {
        mSongname = songname;
    }

    public void setBandName(String bandName) {
        mBandName = bandName;
    }

    public void setAlbumName(String albumName) {
        mAlbumName = albumName;
    }

    public void setPathSong(String pathSong) {
        mPathSong = pathSong;
    }


    public String getSongname() {

        return mSongname;
    }

    public String getBandName() {

        return mBandName;
    }

    public String getAlbumName() {

        return mAlbumName;
    }

    public String getPathSong() {

        return mPathSong;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mSongname);
        dest.writeString(this.mBandName);
        dest.writeString(this.mAlbumName);
        dest.writeString(this.mPathSong);
    }

    protected Songs(Parcel in) {
        this.mSongname = in.readString();
        this.mBandName = in.readString();
        this.mAlbumName = in.readString();
        this.mPathSong = in.readString();
    }

    public static final Parcelable.Creator<Songs> CREATOR = new Parcelable.Creator<Songs>() {
        @Override
        public Songs createFromParcel(Parcel source) {
            return new Songs(source);
        }

        @Override
        public Songs[] newArray(int size) {
            return new Songs[size];
        }
    };
}
