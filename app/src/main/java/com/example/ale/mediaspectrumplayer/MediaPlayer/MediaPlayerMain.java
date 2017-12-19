package com.example.ale.mediaspectrumplayer.MediaPlayer;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.example.ale.mediaspectrumplayer.R;
import com.example.ale.mediaspectrumplayer.Songs;
import com.example.ale.mediaspectrumplayer.SongsAdapter;

import java.util.ArrayList;

public class MediaPlayerMain extends Activity {

    //Setto i permessi per leggere nello storage dello smartphone
    private static final int MY_PERMISSION_REQUEST = 1;

    //Utilizzo un array di stringhe per i vari path di ogni brano, in modo da poter passare tutti i path in una volta e implementare i tasti Skip/Previous
    String[] mListOfPaths = new String[512];

    //MODIFICATO: costruitsco l'array che conterrà i valori delle canzoni e l'adapter per visualizzarle
    ArrayList<Songs> arrayOfSongs;
    SongsAdapter adapter;

    ListView mSongNames;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.media_player_main);

        //MODIFICATO: costruitsco l'array che conterrà i valori delle canzoni e l'adapter per visualizzarle
        arrayOfSongs = new ArrayList<Songs>();
        adapter = new SongsAdapter(MediaPlayerMain.this, arrayOfSongs);


        //Questo malloppone serve a definire un contesto di permessi da dare/prendere per recuperare i dati dalla storage dello smartphone
        if(ContextCompat.checkSelfPermission(MediaPlayerMain.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        {
            if(ActivityCompat.shouldShowRequestPermissionRationale(MediaPlayerMain.this, Manifest.permission.READ_EXTERNAL_STORAGE)) {

                ActivityCompat.requestPermissions(MediaPlayerMain.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSION_REQUEST);
            }
            else {

                ActivityCompat.requestPermissions(MediaPlayerMain.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSION_REQUEST);
            }
        }
        else {

            doStuff();
        }
    }


    public void doStuff() {

        mSongNames = (ListView) findViewById(R.id.ID_MPM_listView);
        mSongNames.setAdapter(adapter);

        getMusicList();

        //Imposto gli eventi che si creano quando clicco su uno degli elementi della ListView
        mSongNames.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {


                Intent mMediaPlayerPlayIntent = new Intent(MediaPlayerMain.this, MediaPlayerPlay.class);

                mMediaPlayerPlayIntent.putExtra("ExtraPosition", position);
                mMediaPlayerPlayIntent.putExtra("StringArray", mListOfPaths);

                startActivity(mMediaPlayerPlayIntent);

            }
        });
    }



    public void getMusicList() {

        //Uso i come indice per mListOfPaths[i]
        int i = 0;

        //Il content resolver si usa per poter accedere ai dati di un altra app, in questo caso ai dati presenti nella scheda SD
        ContentResolver mContentResolver = getContentResolver();

        //Uri funziona come un URL all'interno del Web. Serve per recuperare i dati forniti da un altra app tramite il content resolver
        //In questo caso sarà l'indirizzo da utilizzare per arrivare alla scheda SD
        Uri mSongUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        //Il Cursor serve per indicare a quale riga si punta dopo l'utilizzo di una query di database. In questo modo il contenuto
        //del database può essere indicizzato e bufferizzato, senza la necessità di salvarlo tutto subito
        Cursor mSongCursor = getContentResolver().query(mSongUri, null, null, null, null, null);

        if ((mSongCursor != null) && mSongCursor.moveToFirst()) {

            //Recupero l'indice nel database della scheda SD per il nome della canzone, il nome dell'artista/gruppo e la posizione in memoria
            int mTempSongTitle = mSongCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int mTempBand = mSongCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
            int mTempAlbum = mSongCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM);
            int mTempLocationPath = mSongCursor.getColumnIndex(MediaStore.Audio.Media.DATA);


            do {

                //Una volta che ho l'indice nel database recuperato sopra, recupero la stringa associata a quell'indice
                String mCurrentTitle = mSongCursor.getString(mTempSongTitle);
                String mCurrentBand = mSongCursor.getString(mTempBand);
                String mCurrentAlbum = mSongCursor.getString(mTempAlbum);
                String mCurrentPath = mSongCursor.getString(mTempLocationPath);

                mListOfPaths[i] = mCurrentPath;
                i++;

                Songs newSong = new Songs(mCurrentTitle, mCurrentBand, mCurrentAlbum, mCurrentPath);
                adapter.add(newSong);


            } while (mSongCursor.moveToNext());
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        switch (requestCode) {

            case MY_PERMISSION_REQUEST: {

                if((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED))
                {

                    if(ContextCompat.checkSelfPermission(MediaPlayerMain.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
                    {

                        Toast.makeText(this, "Permesso accordato", Toast.LENGTH_SHORT).show();

                        doStuff();
                    }
                    else {

                        Toast.makeText(this, "Permesso non accordato", Toast.LENGTH_SHORT).show();

                        finish();
                    }

                    return;
                }
            }
        }
    }
}
