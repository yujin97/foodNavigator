package eugene.hku.foodnavigator.helper;

import android.os.AsyncTask;
import android.util.Log;

import java.util.HashMap;
import java.util.List;

public class GetNearByPlaces extends AsyncTask<Object,String, String> {
    private String googleplaceData, url,placeName;


    @Override
    protected String doInBackground(Object... objects) {

        Log.d("Background","Inside do in background");

        url = (String) objects[0];
        placeName = (String) objects[1];
        Log.d("URL","Do in background " + url);

        DownloadUrl downloadUrl = new DownloadUrl();
        googleplaceData = downloadUrl.ReadTheUrl(url);
        return googleplaceData;

    }

    @Override
    protected void onPostExecute(String s) {
        Log.d("postExecution", s);
        List <HashMap<String,String>> nearbyPlaceList = null;
        DataParser dataParser = new DataParser();
        nearbyPlaceList = dataParser.parse(s);

        displayNearbyPlaces(nearbyPlaceList);
    }

    private void displayNearbyPlaces (List<HashMap<String, String>> nearbyPlaceList){
        for (int i=0; i<nearbyPlaceList.size(); i++){
            HashMap<String, String> googleNearbyPlace = nearbyPlaceList.get(i);
            String nameOfPlace = googleNearbyPlace.get("place_name");
            Log.d("display name", nameOfPlace);
            String vicinity = googleNearbyPlace.get("vicinity");
            double latitude = Double.parseDouble(googleNearbyPlace.get("latitude"));
            double longitude = Double.parseDouble(googleNearbyPlace.get("longitude"));
            String reference = googleNearbyPlace.get("reference");
            if(i ==1 ){
                placeName = nameOfPlace;
            }
            Log.d("nearbyPlace","Found restaurant " + nameOfPlace);
        }


    }
}
