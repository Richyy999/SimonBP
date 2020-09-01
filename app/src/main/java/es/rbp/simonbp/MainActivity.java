package es.rbp.simonbp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import java.io.IOException;

import static es.rbp.simonbp.PlayActivity.DIFICULTAD_EXTRA;

/**
 * @author Ricardo Bordería Pi
 * <p>
 * Esta app es una version del famoso juego Simon que consiste en repetir en el orden correcto una serie de números aleatorios
 * generados por la cpu. Para ganar debes terminar la serie completa sin ningún fallo. Si se falla en algún número de la serie
 * se termina el juego y pierdes la partida
 * Menú principal de la app. Aquí se elige la dificultad a la que se desea jugar. También hay un easter egg que lleva a mi GitHub
 */
public class MainActivity extends Activity implements View.OnClickListener, View.OnTouchListener {

    private MediaPlayer mediaPlayer;

    private AssetFileDescriptor doo, re, mi;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimary));
        }

        // Cargo el reproductor de música para que suenen distintas notas al pulsar en los botones de dificultad
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mediaPlayer.start();
            }
        });

        try {
            doo = getAssets().openFd("do.mp3");
            re = getAssets().openFd("re.mp3");
            mi = getAssets().openFd("mi.mp3");
        } catch (IOException e) {
            Log.e("ERROR", e.toString());
        }

        // Inicializo los botones y les asigno los listeners
        Button btnFacil = findViewById(R.id.btnEasy);
        btnFacil.setOnClickListener(this);
        btnFacil.setOnTouchListener(this);

        Button btnMedio = findViewById(R.id.btnMedium);
        btnMedio.setOnClickListener(this);
        btnMedio.setOnTouchListener(this);

        Button btnDificil = findViewById(R.id.btnDifficult);
        btnDificil.setOnClickListener(this);
        btnDificil.setOnTouchListener(this);

        // Inicializo el View invisible con el easter egg
        View easterEgg = findViewById(R.id.easterEgg);
        easterEgg.setOnClickListener(this);
    }

    /**
     * Si pulso en el view con el easter egg abro el navegador o la app de GitHub para acceder a mi Guthub.
     * <p>
     * Si pulso en los botones cargo el nivel con la dificultad seleccionada
     */
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.easterEgg) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://github.com/Richyy999"));
            startActivity(intent);
            Log.d("EASTER EGG", "DESCUBRIDO");
        } else {
            int dificultad = 6;
            switch (v.getId()) {
                case R.id.btnMedium:
                    dificultad = 9;
                    break;
                case R.id.btnDifficult:
                    dificultad = 12;
                    break;
            }
            Intent intent = new Intent(MainActivity.this, PlayActivity.class);
            intent.putExtra(DIFICULTAD_EXTRA, dificultad);
            startActivity(intent);
        }
    }

    /**
     * Suena una nota según el botón que toque el usuario
     */
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            try {
                mediaPlayer.stop();
                mediaPlayer.reset();
                switch (v.getId()) {
                    case R.id.btnEasy:
                        mediaPlayer.setDataSource(doo.getFileDescriptor(), doo.getStartOffset(), doo.getLength());
                        break;
                    case R.id.btnMedium:
                        mediaPlayer.setDataSource(re.getFileDescriptor(), re.getStartOffset(), re.getLength());
                        break;
                    case R.id.btnDifficult:
                        mediaPlayer.setDataSource(mi.getFileDescriptor(), mi.getStartOffset(), mi.getLength());
                        break;
                }
                mediaPlayer.prepare();
            } catch (IOException e) {
                Log.e("ERROR", e.toString());
            }
        }
        return false;
    }
}