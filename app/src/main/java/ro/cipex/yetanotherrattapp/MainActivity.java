package ro.cipex.yetanotherrattapp;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private HashMap<String, String> transportationServiceUrls;
    public HashMap<String, String> liniiIds;

    private static final String DEBUG_TAG = "AppCompatActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        transportationServiceUrls = new HashMap<String, String>();
        liniiIds = new HashMap<String, String> ();
        transportationServiceUrls.put("Tramvai", "http://86.122.170.105:61978/html/timpi/tram.php");



        Spinner transportationServiceSpinner = (Spinner) findViewById(R.id.spinner_transportation_services);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.transportation_services, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        transportationServiceSpinner.setAdapter(adapter);
        transportationServiceSpinner.setOnItemSelectedListener(this);

        Log.d(DEBUG_TAG, "App is ready");


    }
    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        Spinner spinner = (Spinner) parent;
        if ( spinner.getId() == R.id.spinner_transportation_services ) {
            // An item was selected. You can retrieve the selected item using
            String choice = (String) parent.getItemAtPosition(pos);
            Log.d(DEBUG_TAG, "Item Transportation service selected: " + choice);

            String url = transportationServiceUrls.get(choice);
            Log.d(DEBUG_TAG, "Item Transportation service URL: " + url);

            ConnectivityManager connMgr = (ConnectivityManager)
                    getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {
                Log.d(DEBUG_TAG, "Network is connected, starting download task");

                Log.d(DEBUG_TAG, "Network is connected, starting async task");
                new DownloadWebpageTask().execute(url);

                //System.setProperty("http.proxyHost", "global.proxy.alcatel-lucent.com");
                //System.setProperty("http.proxyPort", "8000");

            } else {
                Log.d(DEBUG_TAG, "Network is not connected, doing nothing");
            }



        }

        if ( spinner.getId() == R.id.spinner_line_id ) {
            // An item was selected. You can retrieve the selected item using
            String choice = (String) parent.getItemAtPosition(pos);
            Log.d(DEBUG_TAG, "Line: " + choice);

            String urlLinie = "http://86.122.170.105:61978/html/timpi/" + liniiIds.get(choice);
            Log.d(DEBUG_TAG, "Linie service URL: " + urlLinie);
            ConnectivityManager connMgr = (ConnectivityManager)
                    getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {
                Log.d(DEBUG_TAG, "Network is connected, starting download task");

                Log.d(DEBUG_TAG, "Network is connected, starting async task");
                new DownloadWebpageTask().execute(urlLinie);

                //System.setProperty("http.proxyHost", "global.proxy.alcatel-lucent.com");
                //System.setProperty("http.proxyPort", "8000");

            } else {
                Log.d(DEBUG_TAG, "Network is not connected, doing nothing");
            }



        }

        }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    private class DownloadWebpageTask extends AsyncTask<String, Void, Void> {
        private static final String DEBUG_TAG = "DownloadWebpageTask";

        Document doc;

        @Override
        protected Void doInBackground(String... params) {

            try {
                Log.d(DEBUG_TAG, "Downloading" + params[0]);
                doc = Jsoup.connect(params[0]).get();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }


        protected void onPostExecute(Void result) {

            String title = doc.title();

            if (! doc.select("#apDiv5").isEmpty()) {
                Elements titleJSOUP = doc.select("#apDiv5").select("a");
                Spinner lineIdSpinner = (Spinner) findViewById(R.id.spinner_line_id);
                Log.d(DEBUG_TAG, "Parsing" + titleJSOUP.toString());

                List<String> lineIds = new ArrayList<String>();
                //String[] lineIds = new String[];
                for (Element src : titleJSOUP) {
                    liniiIds.put(src.attr("title"), src.attr("href"));
                    lineIds.add(src.attr("title"));
                }

                ArrayAdapter adapter = new ArrayAdapter(MainActivity.this, android.R.layout.simple_spinner_item, lineIds);

                // Specify the layout to use when the list of choices appears
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                // Apply the adapter to the spinner
                lineIdSpinner.setAdapter(adapter);
                lineIdSpinner.setOnItemSelectedListener(MainActivity.this);
            }

            if (! doc.select("table[bgcolor=FFA500]").isEmpty()) {
                Log.d(DEBUG_TAG, "Parsing Line info");

                Elements sensuri = doc.select("table[bgcolor=FFA500]").select("td").select("b");
                Log.d(DEBUG_TAG, "Sensuri " + sensuri.toString());


                List<String> sensList = new ArrayList<String>();
                //String[] lineIds = new String[];
                for (Element sens : sensuri) {
                    sensList.add(sens.text());
                }

                ArrayAdapter adapter = new ArrayAdapter(MainActivity.this, android.R.layout.simple_spinner_item, sensList);

                // Specify the layout to use when the list of choices appears
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                // Apply the adapter to the spinner
                Spinner spinner = (Spinner) findViewById(R.id.spinner_sens);

                spinner.setAdapter(adapter);
                spinner.setOnItemSelectedListener(MainActivity.this);

            }
//
//
//            Elements maine;
//            Elements titleJSOUP;
//            Elements recipeJSOUP;
//            Elements instructionsJSOUP;
//
//            String recipE;
//
//            maine = doc.select("div#recipeContent");
//
//            titleJSOUP = doc.select("title");
//
//            recipeJSOUP = maine.select("ul.recipe");
//
//            instructionsJSOUP = maine.select("p.instructions");
//
//
//
//            recipE = recipeJSOUP.toString();
//
//
//            drinkNameText.setText("THE "
//                    + Jsoup.parse(titleJSOUP.toString()).text()
//            );
//
//
//
//            dontListenText.setText(Jsoup.parse(titleJSOUP.toString()).text()
//            );
//
//            recipeText.setText(prepareDRINK(recipE));
//
//            instructionsText.setText(Jsoup.parse(instructionsJSOUP.toString())
//                    .text());
//
//            dialog.dismiss();
//
//
        }
    };
};
