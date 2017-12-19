package com.example.ale.mediaspectrumplayer.SpectrumView;


import android.app.Activity;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.ale.mediaspectrumplayer.MediaPlayer.MediaPlayerPlay;
import com.example.ale.mediaspectrumplayer.R;
import com.example.ale.mediaspectrumplayer.SpectrumView.Visualizer.LineDrawer;
import com.example.ale.mediaspectrumplayer.SpectrumView.Visualizer.VisualizerView;

import java.util.Timer;

public class SpectrumViewPlay extends Activity {

    private static final String TAG = SpectrumViewPlay.class.getName();

    //Dichiaro i TextView che verranno visualizzati all'interno del media player
    TextView mSongTitle, mBandName;

    //Dichiaro gli ImageButton
    ImageButton mPlayPauseButton, mPreviousButton, mSkipButton;

    //Dichiaro la seekBar per visualizzare visivamente i progressi del brano
    SeekBar mSeekBar;

    //Variabile che serve per visualizzare effettivamente lo spettro
    VisualizerView mVisualizerView;

    //L'Handler serve per gestire la seekBar così come il Runnable
    Handler mHandler = new Handler();;
    Runnable mRunnable;

    //Dichiaro l'uri per contenere il path della canzone
    Uri mPath;

    //Dichiaro il metodo che serve per gestire le funzionalità di un media player funzionante
    MediaPlayer mMediaPlayer;

    //Valore booleano per controllare lo scambio tra bottone Play e Pause e Skip Previous
    Boolean mPlayOrPause = true;
    Boolean mSkipPrevious = false;

    //Dichiaro altre variabili che serviranno a visualizzare le informazioni aggiuntive, come per esempio
    //il minuto e secondo corrente, il tempo totale della canzone, avanti e indietro di 5 secondi
    double mStartTime = 0;
    double mFinalTime = 0;

    //Dichiaro un array di stringhe per contenere tutti i paths dei brani passati dall'altra activity compresa di posizione dell'oggetto
    //passato in quel momento (servono per implementare i tasti Skip/Previous
    String[] mListOfPaths;
    int mPosition;

    //Dichiaro l'oggetto Media Data Retriever per recuperare principalmente l'immagine di copertina e la variabile byte
    MediaMetadataRetriever mMediaDataRetriever = new MediaMetadataRetriever();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.spectrum_view_play);


        //Con questa piccola porzione di codice, recupero le informazioni che ho passato precedentemente nell'altra activity
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {

            mListOfPaths = bundle.getStringArray("StringArray");
            mPosition = bundle.getInt("ExtraPosition");
        }

        //Funzione utilizzata per inizializzare l'activity
        getInitialisation();

        //Inizio subito con la funzione di play perché voglio che il brano parta subito appena entrati nella activity, senza dover aspettare l'effettivo click sul Play
        StartSong();


        mPlayPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                StopButton();
            }
        });

        mPreviousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                PreviousButton();
            }
        });

        mSkipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                SkipButton();
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

        mSongTitle = (TextView) findViewById(R.id.ID_SpecP_title);
        mBandName = (TextView) findViewById(R.id.ID_SpecP_band);
        mPlayPauseButton = (ImageButton) findViewById(R.id.ID_SpecP_playPauseButton);
        mPreviousButton = (ImageButton) findViewById(R.id.ID_SpecP_previousButton);
        mSkipButton = (ImageButton) findViewById(R.id.ID_SpecP_skipButton);
        mSeekBar = (SeekBar) findViewById(R.id.ID_SpecP_seekBar);

        mVisualizerView = (VisualizerView) findViewById(R.id.ID_SpecP_visualizerView);

        //mPath = Uri.parse(mLocationPathExtra);
        mPath = Uri.parse(mListOfPaths[mPosition]);

        //Recupero immediatamente (che non ho fatto nell'altra activity) l'immagine di album associata al brano (se presente)
        mMediaDataRetriever.setDataSource(SpectrumViewPlay.this, mPath);

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

        //Ora creo il contesto (il path) entro la quale il media player si muoverà
        mMediaPlayer = MediaPlayer.create(SpectrumViewPlay.this, mPath);

        //Creo la correlazione tra media player e seekBar per visualizzare il progresso del brano
        mSeekBar.setMax(mMediaPlayer.getDuration());

        if(mSkipPrevious) {

            StartSong();
            mSkipPrevious = false;
        }
    }


    public void StartSong() {

        mMediaPlayer.start();

        mFinalTime = mMediaPlayer.getDuration();
        mStartTime = mMediaPlayer.getCurrentPosition();

        //Collego il visualizer view al media player in modo che vadano a braccetto
        mVisualizerView.link(mMediaPlayer);

        //Creo ora la vera e propria realizzazione
        addLineRenderer();

        playCicle();
    }


    public void StopButton() {

        mMediaPlayer.stop();
        mMediaPlayer.reset();

        mSeekBar.setProgress(0);
    }


    public void SkipButton() {

        mVisualizerView.release();

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

        mVisualizerView.release();

        if(((mPosition - 1) >= 0) && (mListOfPaths[mPosition - 1] != null)) {

            mMediaPlayer.stop();
            mMediaPlayer.reset();

            mPosition = mPosition - 1;
            mSkipPrevious = true;
            getInitialisation();
        }
        else {

            for (int z = 511; z > 0; z--) {

                if (mListOfPaths[z] != null) {

                    mPosition = z;
                    mSkipPrevious = true;
                    getInitialisation();

                    break;
                }
            }
        }
    }


    // Creates a Line
    public void addLineRenderer()
    {
        Paint linePaint = new Paint();
        linePaint.setStrokeWidth(1f);
        linePaint.setAntiAlias(true);
        linePaint.setColor(Color.rgb(232,85,5));

        Paint lineFlashPaint = new Paint();
        lineFlashPaint.setStrokeWidth(5f);
        lineFlashPaint.setAntiAlias(true);
        lineFlashPaint.setColor(Color.rgb(255,0,0));
        LineDrawer lineDrawer = new LineDrawer(linePaint, lineFlashPaint, false);
        mVisualizerView.addRenderer(lineDrawer);
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
