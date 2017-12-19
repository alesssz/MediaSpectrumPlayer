package com.example.ale.mediaspectrumplayer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;


public class SongsAdapter extends ArrayAdapter<Songs> {

    public SongsAdapter(Context context, ArrayList<Songs> mySongs) {

        super(context, 0, mySongs);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        //Recupero i dati nella determinata posizione
        Songs mySongs = getItem(position);

        //Controllo se la view è in ri-utilizzo, altrimenti carico la view
        if(convertView == null) {

            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_songs, parent, false);
        }

        //Inserisco i vari dati
        TextView SongTitle = (TextView) convertView.findViewById(R.id.ID_item_songs_title);
        TextView BandName = (TextView) convertView.findViewById(R.id.ID_item_songs_band);
        TextView AlbumName = (TextView) convertView.findViewById(R.id.ID_item_songs_album);
        TextView PathName = (TextView) convertView.findViewById(R.id.ID_item_songs_path);

        //Uso il data object per inserire i dati nel template view
        SongTitle.setText(mySongs.mSongname);
        BandName.setText(mySongs.mBandName);
        AlbumName.setText(mySongs.mAlbumName);
        PathName.setText(mySongs.mPathSong);

        //Return la view completa da visualizzare a schermo
        return convertView;
    }
}
