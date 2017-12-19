package com.example.ale.mediaspectrumplayer;

import android.content.Intent;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.ale.mediaspectrumplayer.MediaPlayer.MediaPlayerMain;
import com.example.ale.mediaspectrumplayer.SpectrumView.SpectrumViewMain;

public class MainActivity extends AppCompatActivity {

    //Dichiaro i due bottoni che verranno linkati ai bottoni posti all'interno di activity_main.xml
    Button mMediaPlayerActivity, mSpectrumViewActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Collego i due bottoni
        mMediaPlayerActivity = (Button) findViewById(R.id.ID_MediaPlayer_Button);
        mSpectrumViewActivity = (Button) findViewById(R.id.ID_SpectrumView_Button);

        //Imposto l'evento che si attiva cliccando i bottoni (in questo caso solo l'avvio di altre attività
        mMediaPlayerActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent mMediaPlayerIntent = new Intent(MainActivity.this, MediaPlayerMain.class);
                startActivity(mMediaPlayerIntent);
            }
        });


        mSpectrumViewActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent mSpectrumViewIntent = new Intent(MainActivity.this, SpectrumViewMain.class);
                startActivity(mSpectrumViewIntent);
            }
        });
    }
}
