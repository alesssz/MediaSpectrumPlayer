package com.example.ale.mediaspectrumplayer.MediaPlayer;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ale.mediaspectrumplayer.R;
import com.example.ale.mediaspectrumplayer.Songs;
import com.example.ale.mediaspectrumplayer.SongsAdapter;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class MediaPlayerPlay extends Activity {


    //Dichiaro i TextView che verranno visualizzati all'interno del media player
    TextView mSongTitle, mBandName;

    //Dichiaro l'immagine di copertina che recupererò tramite il Media Metadata Retriever
    ImageView mAlbumImage;

    //Dichiaro gli ImageButton
    ImageButton mPlayPauseButton, mBackwardButton, mForwardButton;

    //Dichiaro la seekBar per visualizzare visivamente i progressi del brano
    SeekBar mSeekBar;

    //L'Handler serve per gestire la seekBar così come il Runnable
    Handler mHandler = new Handler();;
    Runnable mRunnable;

    //Dichiaro l'uri per contenere il path della canzone
    Uri mPath;

    //Dichiaro il metodo che serve per gestire le funzionalità di un media player funzionante
    private MediaPlayer mMediaPlayer;

    //Dichiaro altre variabili che serviranno a visualizzare le informazioni aggiuntive, come per esempio
    //il minuto e secondo corrente, il tempo totale della canzone, avanti e indietro di 5 secondi
    private double mStartTime = 0;
    private double mFinalTime = 0;

    //Dichiaro l'handler che mi servirà nei metodi per la posizione nella seekbar, e il tempo in avanti e indietro
    private Handler myHandler = new Handler();
    private int mForwardTime = 5000;
    private int mBackwardTime = 5000;

    //Dichiaro l'oggetto Media Data Retriever per recuperare principalmente l'immagine di copertina e la variabile byte
    MediaMetadataRetriever mMediaDataRetriever = new MediaMetadataRetriever();
    byte[] mArt;


    //Valore booleano per controllare lo scambio tra bottone Play e Pause e Skip Previous
    Boolean mPlayOrPause = true;
    Boolean mSkipPrevious = false;

    //Dichiaro un array di stringhe per contenere tutti i paths dei brani passati dall'altra activity compresa di posizione dell'oggetto
    //passato in quel momento (servono per implementare i tasti Skip/Previous
    String[] mListOfPaths;
    int mPosition;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.media_player_play);

        //Con questa piccola porzione di codice, recupero le informazioni che ho passato precedentemente nell'altra activity
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {

            mListOfPaths = bundle.getStringArray("StringArray");
            mPosition = bundle.getInt("ExtraPosition");
        }

        //Funzione utilizzata per inizializzare l'activity
        getInitialisation();

        //Inizio subito con la funzione di play perché voglio che il brano parta subito appena entrati nella activity, senza dover aspettare l'effettivo click sul Play
        PlayPauseButton();

        mPlayPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                PlayPauseButton();
            }
        });

        mBackwardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                BackwardButton();
            }
        });

        mBackwardButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                PreviousButton();
                return true;
            }
        });

        mForwardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ForwardButton();
            }
        });

        mForwardButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                SkipButton();
                return true;
            }
        });

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean input) {

                if(input) {

                    mMediaPlayer.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {


            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {


            }
        });

    }


    public void getInitialisation() {

        //Collego ogni elemento visivo del layout con i corrispettivi java
        mSongTitle = (TextView) findViewById(R.id.ID_MPP_title);
        mBandName = (TextView) findViewById(R.id.ID_MPP_band);
        mAlbumImage = (ImageView) findViewById(R.id.ID_MPP_coverAlbum);
        mPlayPauseButton = (ImageButton) findViewById(R.id.ID_MPP_playpauseButton);
        mBackwardButton = (ImageButton) findViewById(R.id.ID_MPP_backwardButton);
        mForwardButton = (ImageButton) findViewById(R.id.ID_MPP_forwardButton);
        mSeekBar = (SeekBar) findViewById(R.id.ID_MPP_seekBar);


        //mPath = Uri.parse(mLocationPathExtra);
        mPath = Uri.parse(mListOfPaths[mPosition]);

        //Recupero immediatamente (che non ho fatto nell'altra activity) l'immagine di album associata al brano (se presente)
        mMediaDataRetriever.setDataSource(MediaPlayerPlay.this, mPath);

        //Setto un try catch per ogni recupero di dati, in modo da gestirli singolarmente
        try {

            mSongTitle.setText(mMediaDataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE));
        }

        catch (Exception e) {

            mSongTitle.setText("Unknown title");
        }

        try {

            mBandName.setText(mMediaDataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST));
        }

        catch (Exception e) {

            mBandName.setText("Unknown band/artist");
        }

        try {

            mArt = mMediaDataRetriever.getEmbeddedPicture();
            Bitmap TEMP_image = BitmapFactory.decodeByteArray(mArt, 0, mArt.length);
            mAlbumImage.setImageBitmap(TEMP_image);
        }

        catch (Exception e) {

            mAlbumImage.setBackgroundColor(Color.GRAY);
        }


        //Ora creo il contesto (il path) entro la quale il media player si muoverà
        mMediaPlayer = MediaPlayer.create(MediaPlayerPlay.this, mPath);

        //Creo la correlazione tra media player e seekBar per visualizzare il progresso del brano
        mSeekBar.setMax(mMediaPlayer.getDuration());

        if(mSkipPrevious) {

            mPlayOrPause = true;
            PlayPauseButton();
        }
    }


    public void PlayPauseButton() {

        //Controllo se la funzione è stata avviata per il click del pulsante Play/pause o per il primo accesso all'activity
        if (mPlayOrPause) {

            //Faccio partire il metodo impostato all'interno della variabile mMediaPlayer
            mMediaPlayer.start();

            //Reimposto il valore booleano a false per cambiare il suo funzionamento
            mPlayOrPause = false;

            //Setto come immagine del bottone l'icona pausa
            mPlayPauseButton.setImageResource(R.drawable.pause);


            mFinalTime = mMediaPlayer.getDuration();
            mStartTime = mMediaPlayer.getCurrentPosition();

            playCicle();

        }
        else if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {

            //Metto in pausa il media player
            mMediaPlayer.pause();

            //Reimposto il valore booleano a true per cambiare il suo funzionamento
            mPlayOrPause = true;

            //Setto come immagine del bottone l'icona play
            mPlayPauseButton.setImageResource(R.drawable.play);
        }
    }


    public void ForwardButton() {

        int temp = (int) mStartTime;

        //Se la posizione corrente (temp) + 5 secondi risulta <= del tempo totale del brano, allora posso saltare in avanti
        if ((temp + mForwardTime) <= mFinalTime) {

            //Eseguo matematicamente il salto in avanti di 5 secondi
            mStartTime = mStartTime + mForwardTime;

            //Con seekTo permetto al media player il salto di posizione
            mMediaPlayer.seekTo((int) mStartTime);
            
        }
        else {

            //Messaggio che compare a video (compare tot secondi senza influenzare lo schermo) e scompare senza influenzare nulla
            Toast.makeText(getApplicationContext(), "Non puoi saltare in avanti di 5 secondi", Toast.LENGTH_SHORT).show();
        }
    }


    public void BackwardButton() {

        int temp = (int) mStartTime;

        //Se la posizione corrente (temp) - i 5 secondi del rewind, risulta > 0, allora posso andare indietro
        if ((temp - mBackwardTime) > 0) {

            //Eseguo matematicamente il salto indietro di 5 secondi
            mStartTime = mStartTime - mBackwardTime;

            //Con seekTo permetto al media player il salto di posizione
            mMediaPlayer.seekTo((int) mStartTime);

        }
        else {

            //Messaggio che compare a video (compare tot secondi senza influenzare lo schermo) e scompare senza influenzare nulla
            Toast.makeText(getApplicationContext(), "Non puoi saltare indietro di 5 secondi", Toast.LENGTH_SHORT).show();
        }
    }


    public void SkipButton() {

        if(((mPosition + 1) < 512) &&  mListOfPaths[mPosition + 1] != null ) {

            mMediaPlayer.stop();
            mMediaPlayer.reset();

            mPosition = mPosition + 1;
            mSkipPrevious = true;
            getInitialisation();
        }
        else {

            mMediaPlayer.stop();
            mMediaPlayer.reset();

            mPosition = 0;
            mSkipPrevious = true;
            getInitialisation();
        }
    }

    public void PreviousButton() {

        if(((mPosition - 1) >= 0) && (mListOfPaths[mPosition - 1] != null)) {

            mMediaPlayer.stop();
            mMediaPlayer.reset();

            mPosition = mPosition - 1;
            mSkipPrevious = true;
            getInitialisation();
        }
        else {

            for(int z = 511; z > 0; z--) {

                if(mListOfPaths[z] != null) {

                    mPosition = z;
                    mSkipPrevious = true;
                    getInitialisation();

                    break;
                }
            }

        }
    }


    public void playCicle() {

        mSeekBar.setProgress(mMediaPlayer.getCurrentPosition());

        if(mMediaPlayer.isPlaying()) {

            mRunnable = new Runnable() {
                @Override
                public void run() {

                    mFinalTime = mMediaPlayer.getDuration();
                    mStartTime = mMediaPlayer.getCurrentPosition();
                    playCicle();
                }
            };

            mHandler.postDelayed(mRunnable, 100);
        }
    }


    //Funzione che serve a fermare completamente il media player nel caso in cui l'utente voglia tornare alla lista di brani (activity precedente)
    @Override
    public void onBackPressed () {

        if (mMediaPlayer != null) {

            mMediaPlayer.stop();
        }
        super.onBackPressed();
    }
}