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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private HashMap<String, String> transportationServiceUrls;
    private static final String DEBUG_TAG = "DownloadWebpageTask";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        transportationServiceUrls = new HashMap<String, String>();
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
                Log.d(DEBUG_TAG, "Network is connected, starting async task");
                new DownloadWebpageTask(this).execute(url);
            } else {
                Log.d(DEBUG_TAG, "Network is not connected, doing nothing");
            }



        }

    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }


    private class DownloadWebpageTask extends AsyncTask<String, Void, String> {
        private static final String DEBUG_TAG = "DownloadWebpageTask";

        AppCompatActivity parentActivity;

        public DownloadWebpageTask(AppCompatActivity activity) {
            parentActivity = activity;
        }
        // Given a URL, establishes an HttpUrlConnection and retrieves
// the web page content as a InputStream, which it returns as
// a string.
        private String downloadUrl(String myurl) throws IOException {
            InputStream is = null;
            // Only display the first 500 characters of the retrieved
            // web page content.
            int len = 500;

            try {
                URL url = new URL(myurl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                // Starts the query
                conn.connect();
                int response = conn.getResponseCode();
                Log.d(DEBUG_TAG, "The response is: " + response);
                is = conn.getInputStream();

                // Convert the InputStream into a string
                String contentAsString = readIt(is, len);
                return contentAsString;

                // Makes sure that the InputStream is closed after the app is
                // finished using it.
            } finally {
                if (is != null) {
                    is.close();
                }
            }
        }


        public String readIt(InputStream stream, int len) throws IOException, UnsupportedEncodingException {
            Reader reader = null;
            reader = new InputStreamReader(stream, "UTF-8");
            char[] buffer = new char[len];
            reader.read(buffer);
            return new String(buffer);
        }

        @Override
        protected String doInBackground(String... urls) {

            // params comes from the execute() call: params[0] is the url.
            try {
                return downloadUrl(urls[0]);
            } catch (IOException e) {
                return "Unable to retrieve web page. URL may be invalid.";
            }
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {


            Spinner lineIdSpinner = (Spinner) findViewById(R.id.spinner_line_id);

            String [] lineIds = new String [] { result };

            ArrayAdapter adapter = new ArrayAdapter(parentActivity, android.R.layout.simple_spinner_item, lineIds);

            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            // Apply the adapter to the spinner
            lineIdSpinner.setAdapter(adapter);
        }
    }


}
