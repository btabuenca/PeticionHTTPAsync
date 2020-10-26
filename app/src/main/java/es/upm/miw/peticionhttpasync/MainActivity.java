package es.upm.miw.peticionhttpasync;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

public class MainActivity extends Activity {

    private static final String LOG_TAG = "MiW";

    static final String URL_RECURSO = "http://www.etsisi.upm.es/robots.txt";
    //static final String URL_RECURSO = "http://api.openweathermap.org/data/2.5/find?lat=40.475172&lon=-3.461757&cnt=10&APPID=add7afd148b08ad9e0c06da452f061d5";


    Button botonIniciar, botonCancelar, botonIncrementar;
    TextView tvContenido;
    Chronometer crono;

    TareaAsyncCargarRecurso tarea;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvContenido = (TextView) findViewById(R.id.tvContenido);
        final TextView tvIncrementar = (TextView) findViewById(R.id.tvContador);
        botonIniciar = (Button) findViewById(R.id.botonIniciar);
        botonIncrementar = (Button) findViewById(R.id.botonIncrementar);
        botonCancelar = (Button) findViewById(R.id.botonCancelar);
        crono = (Chronometer) findViewById(R.id.chCrono);
        Log.i(LOG_TAG, "URL=" + URL_RECURSO);


        botonIniciar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                botonIniciar.setEnabled(false);
                botonCancelar.setEnabled(true);
                tvContenido.setTag(URL_RECURSO);    // Envío la URL en la etiqueta del TextView
                tarea = new TareaAsyncCargarRecurso();
                tarea.execute(tvContenido);
            }
        });

        botonCancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                botonCancelar.setEnabled(false);
                botonIniciar.setEnabled(true);
                tarea.cancel(true);
            }
        });

        botonIncrementar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int contador = Integer.parseInt(tvIncrementar.getText().toString());
                tvIncrementar.setText(String.format(Locale.getDefault(), "%d", ++contador));
                Log.i(LOG_TAG, "Contador=" + Integer.toString(contador));
            }
        });


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (tarea != null) tarea.cancel(true);
    }

    /**
     * Carga un recurso en el TextView que recibe como parámetro: execute(TextView textView);
     */
    private class TareaAsyncCargarRecurso extends AsyncTask<TextView, Void, String> {

        private long horaInicio;
        private TextView tvContenidoTarea = null;

        @Override
        /**
         * Guarda la hora de inicio y arranca el crono
         */
        protected void onPreExecute() {
            horaInicio = SystemClock.elapsedRealtime();
            crono.setBase(horaInicio);
            crono.start();
        }

        @Override
        protected String doInBackground(TextView... textViews) {
            tvContenidoTarea = textViews[0];
            String buffer = "";
            HttpURLConnection con = null;

            try {
                @SuppressWarnings("WrongThread")
                URL mUrl = new URL(tvContenidoTarea.getTag().toString());   // Recupera el URL del tag
                Log.i(LOG_TAG, "URL=" + mUrl.toString());

                // Establecer conexión remota
                con = (HttpURLConnection) mUrl.openConnection();
                BufferedReader fin = new BufferedReader(
                        new InputStreamReader(
                                con.getInputStream()
                        )
                );

                // Obtener recurso
                String linea = fin.readLine();
                while (linea != null) {
                    Thread.sleep(100);
                    buffer += linea + '\n';
                    Log.i(LOG_TAG, linea);
                    linea = fin.readLine();
                }

                fin.close();
                Log.i(LOG_TAG, getString(R.string.txtRecursoRecibido));
            } catch (Exception e) {
                Log.e("ERROR", getResources().getString(R.string.errorLoading)+ e.getMessage());
                e.printStackTrace();
            } finally {
                if (con != null) con.disconnect();
            }

            return buffer;
        }

        @Override
        protected void onPostExecute(String result) {
            crono.stop();
            long horaFin = SystemClock.elapsedRealtime();
            Log.i(LOG_TAG, String.format("Tiempo = %.2f s.",  ((horaFin - horaInicio) / 1000.0)));
            tvContenidoTarea.setText(result);
            botonCancelar.setVisibility(View.GONE);
        }

        @Override
        protected void onCancelled() {
            crono.stop();
            Toast.makeText(
                    getApplicationContext(),
                    getString(R.string.tareaCancelada),
                    Toast.LENGTH_LONG
            ).show();
            Log.i(LOG_TAG, getString(R.string.tareaCancelada));
        }
    } // TareaCargarRecurso

}
