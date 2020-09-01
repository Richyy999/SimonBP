package es.rbp.simonbp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.DialogInterface;
import android.content.res.AssetFileDescriptor;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Ricardo Bordería Pi
 * <p>
 * Este es el activity del juego. La cpu irá añadiendo números aleatorios del 1 al 6 al un List, uno por turno. Después de añadir el número
 * el jugador deberá repetir la secuencia de números contenidos en el List. Si el jugador se equivoca en algún número de la serie pierde la partida.
 * Si completa la serie el jugador gana la partida.
 * La partida termina si el jugador completa la serie o si falla.
 * <p>
 * Hay un truco para mostrar qué números de la serie te faltan para terminar la ronda. Se activa al mantener pulsado sobre
 * el 6, el 1 y el 3, respectivamente. En  odo Dificil sólo se podrá usar el truco una vez por partida.
 */
public class PlayActivity extends AppCompatActivity implements View.OnClickListener, View.OnLongClickListener {

    /**
     * Extra del intent con la dificultad
     */
    public static final String DIFICULTAD_EXTRA = "DIFICULTAD";

    /**
     * Duración de la animación de crecer o encoger los botones pulsados
     */
    private static final int DURATION_CRECER_ENCOGER_BOTONES = 200;

    /**
     * Duración de la animación de crecer o encoger el Text View
     *
     * @see PlayActivity#lblInfo
     */
    private static final int DURATION_CRECER_ENCOGER_TEXT_VIEW = 700;

    /**
     * Mayor número que la cpu puede generar aleatoriamente para añadirlo a la serie
     */
    private static final int NUMERO_MAXIMO = 6;

    /**
     * Rondas que dura la partida
     */
    private static int TURNOS;

    /**
     * Botones para jugar
     */
    private Button btn1, btn2, btn3, btn4, btn5, btn6;

    /**
     * Panel semiopaco para tapar los botones mientras se carga el juego o cuando se termina
     *
     * @see PlayActivity#mostrarVictoria()
     * @see PlayActivity#mostrarDerrota()
     * @see PlayActivity#cuentaAtras()
     */
    private View opacityPane;

    /**
     * Label con la cuenta atrás, el mensaje de derrota o el de victoria
     *
     * @see PlayActivity#mostrarVictoria()
     * @see PlayActivity#mostrarDerrota()
     * @see PlayActivity#cuentaAtras()
     */
    private TextView lblInfo;

    /**
     * Lista con la serie de números que debe seguir el jugador para ganar la partida
     */
    private List<Integer> cpu;

    /**
     * Media Player para reproducir las notas correspondientes a cada botón
     *
     * @see PlayActivity#play(int)
     */
    private MediaPlayer mediaPlayer;

    /**
     * Objeto con las notas de cada botón
     *
     * @see PlayActivity#play(int)
     */
    private AssetFileDescriptor doo, re, mi, fa, sol, la;

    /**
     * Indice de la serie en el que se encuantra el jugador. Al finalizar el turno se reinicia
     */
    private int index;
    /**
     * Ronda en la que se encuantra el jugador
     */
    private int turnoActual;

    /**
     * Pasos a seguir para realizar el truco
     *
     * @see PlayActivity#truco()
     */
    private boolean paso1, paso2;
    /**
     * Indica si la partida se ha terminado, ya sea por que el jugador haya ganado o perdido, o no.
     */
    private boolean fin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        }

        TURNOS = getIntent().getIntExtra(DIFICULTAD_EXTRA, 6);

        cpu = new ArrayList<>();

        mediaPlayer = new MediaPlayer();

        index = 1;
        turnoActual = 1;

        paso1 = false;
        paso2 = false;

        fin = false;

        cargarVista();
        cargarListeners();
        loadMusic();
    }

    /**
     * Si pulsa un botón se anima, se reproduce la nota correspondiente y se verifica si el jugador acierta o no.
     * <p>
     * Si pulsa el panel opaco y la partida se ha terminado vuelve al menú principal.
     *
     * @see MainActivity
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn1:
                jugar(1);
                break;
            case R.id.btn2:
                jugar(2);
                break;
            case R.id.btn3:
                jugar(3);
                break;
            case R.id.btn4:
                jugar(4);
                break;
            case R.id.btn5:
                jugar(5);
                break;
            case R.id.btn6:
                jugar(6);
                break;
            case R.id.opacityPane:
                if (fin)
                    finish();
                break;
        }
    }

    /**
     * Si se mantiene pulsado los botones 1, 3 y 6 en el orden correcto se activa el truco.
     *
     * @see PlayActivity#truco()
     */
    @Override
    public boolean onLongClick(View v) {
        switch (v.getId()) {
            case R.id.btn1:
                if (paso1)
                    paso2 = true;
                Log.d("PASO", "1");
                break;
            case R.id.btn3:
                if (paso1 && paso2)
                    truco();
                break;
            case R.id.btn6:
                paso1 = true;
                Log.d("PASO", "2");
                break;
        }
        return false;
    }

    /**
     * Muestra un Alert advertiendo al jugador que si sale se perderá su progreso actual.
     *
     * @see PlayActivity#showAlertDialog()
     */
    @Override
    public void onBackPressed() {
        showAlertDialog();
    }

    /**
     * Inicializa los elementos de la vista.
     */
    private void cargarVista() {
        btn1 = findViewById(R.id.btn1);

        btn2 = findViewById(R.id.btn2);

        btn3 = findViewById(R.id.btn3);

        btn4 = findViewById(R.id.btn4);

        btn5 = findViewById(R.id.btn5);

        btn6 = findViewById(R.id.btn6);

        opacityPane = findViewById(R.id.opacityPane);

        lblInfo = findViewById(R.id.lblInfo);
    }

    /**
     * Carga los listeners.
     */
    private void cargarListeners() {
        btn1.setOnClickListener(this);
        btn1.setOnLongClickListener(this);

        btn2.setOnClickListener(this);

        btn3.setOnClickListener(this);
        btn3.setOnLongClickListener(this);

        btn4.setOnClickListener(this);

        btn5.setOnClickListener(this);

        btn6.setOnClickListener(this);
        btn6.setOnLongClickListener(this);

        opacityPane.setOnClickListener(this);

        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mediaPlayer.start();
            }
        });
    }

    /**
     * Carga los archivos con las notas.
     */
    private void loadMusic() {
        try {
            doo = getAssets().openFd("do.mp3");
            re = getAssets().openFd("re.mp3");
            mi = getAssets().openFd("mi.mp3");
            fa = getAssets().openFd("fa.mp3");
            sol = getAssets().openFd("sol.mp3");
            la = getAssets().openFd("la.mp3");
        } catch (IOException e) {
            Log.e("LOAD MUSICA", e.toString());
            finish();
        }
        cuentaAtras();
    }

    /**
     * Muestra el alert al jugador. Si pulsa "Salir" regresa al menú principal perdiendo el progreso que llevaba.
     * Si no, regresa a la partida.
     *
     * @see PlayActivity#onBackPressed()
     */
    private void showAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.exit);
        builder.setMessage(R.string.wannaExit);
        builder.setPositiveButton(R.string.exit, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        builder.setNeutralButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    /**
     * Aumenta el tamaño de la vista indicada un 120% de su tamaño original.
     *
     * @param boton vista que se desea que crezca.
     */
    private void crecer(View boton) {
        ObjectAnimator scaleDownX2 = ObjectAnimator.ofFloat(boton, "scaleX", 1.2f);
        ObjectAnimator scaleDownY2 = ObjectAnimator.ofFloat(boton, "scaleY", 1.2f);
        scaleDownX2.setDuration(DURATION_CRECER_ENCOGER_BOTONES);
        scaleDownY2.setDuration(DURATION_CRECER_ENCOGER_BOTONES);

        AnimatorSet scaleDown2 = new AnimatorSet();
        scaleDown2.play(scaleDownX2).with(scaleDownY2);

        scaleDown2.start();
    }

    /**
     * Encoge el tamaño de la vista indicada un 80% de su tamaño original.
     *
     * @param boton vista que se desea que encoja.
     */
    private void encoger(View boton) {
        ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(boton, "scaleX", 1f);
        ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(boton, "scaleY", 1f);
        scaleDownX.setDuration(DURATION_CRECER_ENCOGER_BOTONES);
        scaleDownY.setDuration(DURATION_CRECER_ENCOGER_BOTONES);

        AnimatorSet scaleDown = new AnimatorSet();
        scaleDown.play(scaleDownX).with(scaleDownY);

        scaleDown.start();
    }

    /**
     * Deshabilita los botones durante el turno de la cpu y los habilita en el turno del jugador.
     *
     * @param enabled true si se desea habilitar los botones. false si se desea deshabilitar los botones.
     */
    private void enableBotones(boolean enabled) {
        btn1.setEnabled(enabled);
        btn2.setEnabled(enabled);
        btn3.setEnabled(enabled);
        btn4.setEnabled(enabled);
        btn5.setEnabled(enabled);
        btn6.setEnabled(enabled);
    }

    /**
     * Anima y reproduce la nota correxpondiente al botón con el número actual de la serie.
     *
     * @param numBoton número que contiene el botón pulsado por el usuario o el número actual de la serie.
     */
    private void animate(int numBoton) {
        switch (numBoton) {
            case 1:
                crecer(btn1);
                play(numBoton);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        encoger(btn1);
                    }
                }, DURATION_CRECER_ENCOGER_BOTONES);
                break;
            case 2:
                crecer(btn2);
                play(numBoton);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        encoger(btn2);
                    }
                }, DURATION_CRECER_ENCOGER_BOTONES);
                break;
            case 3:
                crecer(btn3);
                play(numBoton);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        encoger(btn3);
                    }
                }, DURATION_CRECER_ENCOGER_BOTONES);
                break;
            case 4:
                crecer(btn4);
                play(numBoton);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        encoger(btn4);
                    }
                }, DURATION_CRECER_ENCOGER_BOTONES);
                break;
            case 5:
                crecer(btn5);
                play(numBoton);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        encoger(btn5);
                    }
                }, DURATION_CRECER_ENCOGER_BOTONES);
                break;
            case 6:
                crecer(btn6);
                play(numBoton);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        encoger(btn6);
                    }
                }, DURATION_CRECER_ENCOGER_BOTONES);
                break;
        }
    }

    /**
     * Detiene y reinicia el Media Player antes de reproducir la nota correspondiente al botón pulsado.
     *
     * @param numBoton número del botón pulsado.
     */
    private void play(int numBoton) {
        mediaPlayer.stop();
        mediaPlayer.reset();
        try {
            switch (numBoton) {
                case 1:
                    mediaPlayer.setDataSource(doo.getFileDescriptor(), doo.getStartOffset(), doo.getLength());
                    break;
                case 2:
                    mediaPlayer.setDataSource(re.getFileDescriptor(), re.getStartOffset(), re.getLength());
                    break;
                case 3:
                    mediaPlayer.setDataSource(mi.getFileDescriptor(), mi.getStartOffset(), mi.getLength());
                    break;
                case 4:
                    mediaPlayer.setDataSource(fa.getFileDescriptor(), fa.getStartOffset(), fa.getLength());
                    break;
                case 5:
                    mediaPlayer.setDataSource(sol.getFileDescriptor(), sol.getStartOffset(), sol.getLength());
                    break;
                case 6:
                    mediaPlayer.setDataSource(la.getFileDescriptor(), la.getStartOffset(), la.getLength());
                    break;
            }
            mediaPlayer.prepare();
        } catch (IOException e) {
            Log.e("ERROR", e.toString());
        }
    }

    /**
     * Realiza la cuenta atrás para empezar el juego.
     */
    private void cuentaAtras() {
        final Animation animation = AnimationUtils.loadAnimation(this, R.anim.encoger_text_view);
        lblInfo.setTextColor(Color.WHITE);
        lblInfo.setText(R.string.three);
        lblInfo.startAnimation(animation);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                lblInfo.setText(R.string.two);
                lblInfo.startAnimation(animation);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        lblInfo.setText(R.string.one);
                        lblInfo.startAnimation(animation);
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                lblInfo.setVisibility(View.GONE);
                                opacityPane.setVisibility(View.GONE);
                                cpu();
                            }
                        }, DURATION_CRECER_ENCOGER_TEXT_VIEW);
                    }
                }, DURATION_CRECER_ENCOGER_TEXT_VIEW);
            }
        }, DURATION_CRECER_ENCOGER_TEXT_VIEW);
    }

    /**
     * Reproduce la nota correspondiente al botón pulsado por el jugador.
     * <p>
     * Anima el botón con el número pulsado por el jugador.
     * <p>
     * Comprueba que el número pulsado sea correcto o no. Si es correcto y no se ha acabado la partia el jugador
     * pasa a la siguiente ronda y se deshabilitan los botones hasta que la cpu termine de mostrar la serie.
     * Si es correcto y se ha terminado la partida muestra el mensaje de "VICTORIA".
     * Si se equivoca se termina la partida y se muestra el mensaje de "Buen intento".
     *
     * @param numero número del botón pulsado
     * @see PlayActivity#play(int)
     * @see PlayActivity#animate(int)
     * @see PlayActivity#mostrarVictoria()
     * @see PlayActivity#mostrarDerrota()
     * @see PlayActivity#enableBotones(boolean)
     * @see PlayActivity#cpu()
     */
    private void jugar(int numero) {
        play(numero);
        animate(numero);
        if (index == TURNOS && numero == cpu.get(index - 1))
            mostrarVictoria();
        else if (numero != cpu.get(index - 1))
            mostrarDerrota();
        if ((index == turnoActual && turnoActual < TURNOS) && !fin) {
            index = 0;
            turnoActual++;
            enableBotones(false);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    cpu();
                }
            }, 1000);
        }
        index++;
    }

    /**
     * Termina la partida y muestra el mensaje de "Buen inento".
     *
     * @see PlayActivity#jugar(int)
     */
    private void mostrarDerrota() {
        opacityPane.setVisibility(View.VISIBLE);
        lblInfo.setVisibility(View.VISIBLE);
        lblInfo.setTextColor(Color.parseColor("#FF5500"));
        lblInfo.setText(R.string.defeated);
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.crecer_text_view);
        lblInfo.startAnimation(animation);
        fin = true;
    }

    /**
     * Termina la partida y muestra el mensaje de "VICTORIA".
     *
     * @see PlayActivity#jugar(int)
     */
    private void mostrarVictoria() {
        opacityPane.setVisibility(View.VISIBLE);
        lblInfo.setVisibility(View.VISIBLE);
        lblInfo.setTextColor(Color.parseColor("#FFFE00"));
        lblInfo.setText(R.string.victory);
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.crecer_text_view);
        lblInfo.startAnimation(animation);
        fin = true;
        Log.d("INDEX", String.valueOf(index));
    }

    /**
     * Añade un número aleatorio a la serie entre el 1 y el 6.
     * Después muestra la serie al usuario
     *
     * @see PlayActivity#jugar(int)
     */
    private void cpu() {
        enableBotones(false);
        int numeroNuevo = (int) (Math.random() * NUMERO_MAXIMO) + 1;
        cpu.add(numeroNuevo);
        Log.d("NUMERO", String.valueOf(numeroNuevo));
        for (int i = 0; i < cpu.size(); i++) {
            final int finalI = i;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    animate(cpu.get(finalI));
                }
            }, DURATION_CRECER_ENCOGER_BOTONES * 2 * i + 100);
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                enableBotones(true);
            }
        }, DURATION_CRECER_ENCOGER_BOTONES * 2 * cpu.size());
    }

    /**
     * Muestra en el orden correcto los números que le faltan al jugador para completar la serie.
     * Si la dificultad es "Dificil" sólo se puede usar una vez.
     */
    private void truco() {
        StringBuilder numeros = new StringBuilder();
        for (int i = index - 1; i < cpu.size(); i++) {
            numeros.append(cpu.get(i)).append(" ");
        }
        Toast.makeText(this, numeros.toString().trim(), Toast.LENGTH_LONG).show();
        if (TURNOS < 12) {
            paso1 = false;
            paso2 = false;
        }
    }
}