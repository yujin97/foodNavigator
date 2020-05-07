package eugene.hku.foodnavigator.helper;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class DataParser {

    private HashMap<String,String> getPlace(JSONObject googlePlaceJSON) {
        HashMap<String,String> googlePlaceMap = new HashMap<>();
        String nameOfPlace = "NA";
        String vicinity = "NA";
        String latitude = "";
        String longitude = "";
        String placeId = "";

        try{
            if(!googlePlaceJSON.isNull("name")){
                nameOfPlace = googlePlaceJSON.getString("name");
            }
            if(!googlePlaceJSON.isNull("vicinity")){
                vicinity = googlePlaceJSON.getString("vicinity");
            } else {
                vicinity = googlePlaceJSON.getString("formatted_address");
            }
            if(!googlePlaceJSON.isNull("name")){
                nameOfPlace = googlePlaceJSON.getString("name");
            }

            latitude = googlePlaceJSON.getJSONObject("geometry").getJSONObject("location").getString("lat");
            longitude = googlePlaceJSON.getJSONObject("geometry").getJSONObject("location").getString("lng");
            placeId = googlePlaceJSON.getString("place_id");

            googlePlaceMap.put("place_name",nameOfPlace);
            googlePlaceMap.put("vicinity",vicinity);
            googlePlaceMap.put("latitude",latitude);
            googlePlaceMap.put("longitude",longitude);
            googlePlaceMap.put("placeId",placeId);

        }
        catch(JSONException e){
            e.getStackTrace();
        }

        return googlePlaceMap;
    }

    public HashMap<String,String> getPlaceDetails(JSONObject googlePlaceJSON){
        HashMap<String,String> googlePlaceMap = new HashMap<>();

        String placeId = "";
        String nameOfPlace = "NA";
        String formatted_address = "NA";
        String latitude = "";
        String longitude = "";
        String time_open ="";
        String time_closed ="";
        String open_now = "";

        //get weekday for the opening data
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_WEEK) - 1;


        try {
            if(!googlePlaceJSON.isNull("name")){
                nameOfPlace = googlePlaceJSON.getString("name");
            }
            if(!googlePlaceJSON.isNull("formatted_address")){
                formatted_address = googlePlaceJSON.getString("formatted_address");
            }

            latitude = googlePlaceJSON.getJSONObject("geometry").getJSONObject("location").getString("lat");
            longitude = googlePlaceJSON.getJSONObject("geometry").getJSONObject("location").getString("lng");
            placeId = googlePlaceJSON.getString("place_id");

            if(!googlePlaceJSON.isNull("opening_hours")){
                if(googlePlaceJSON.getJSONObject("opening_hours").has("open_now")) {
                    open_now = String.valueOf(googlePlaceJSON.getJSONObject("opening_hours").getBoolean("open_now"));
                }

            }

            if(!googlePlaceJSON.isNull("opening_hours")){
                if(googlePlaceJSON.getJSONObject("opening_hours").has("periods")) {
                    JSONArray periods = googlePlaceJSON.getJSONObject("opening_hours").getJSONArray("periods");
                    JSONObject JsonDay;
                    if(periods.length() >= 7) {
                        if ((periods.length() % 2) != 0) {
                            JsonDay = (JSONObject) periods.get(day);
                            if (!JsonDay.isNull("close") && JsonDay.getJSONObject("close").has("time")) {
                                time_closed = JsonDay.getJSONObject("close").getString("time");
                            }
                            if (!JsonDay.isNull("open") && JsonDay.getJSONObject("open").has("time")) {
                                time_open = JsonDay.getJSONObject("open").getString("time");
                            }
                        } else {
                            JsonDay = (JSONObject) periods.get((day * 2));
                            JSONObject JsonDay1 = (JSONObject) periods.get((day * 2 + 1));
                            if (!JsonDay1.isNull("close") && JsonDay.getJSONObject("close").has("time")) {
                                time_closed = JsonDay.getJSONObject("close").getString("time");
                            }
                            if (!JsonDay1.isNull("open") && JsonDay.getJSONObject("open").has("time")) {
                                time_open = JsonDay.getJSONObject("open").getString("time");
                            }
                        }
                    }
                }

            }


            googlePlaceMap.put("place_name",nameOfPlace);
            googlePlaceMap.put("formatted_address",formatted_address);
            googlePlaceMap.put("latitude",latitude);
            googlePlaceMap.put("longitude",longitude);
            googlePlaceMap.put("placeId",placeId);
            googlePlaceMap.put("open_now",open_now);
            googlePlaceMap.put("time_open",time_open);
            googlePlaceMap.put("time_closed",time_closed);




        }
        catch(JSONException e){
            e.getStackTrace();

        }

        return googlePlaceMap;


    }

    public List<HashMap<String,String>> getAllNearbyPlaces(JSONArray jsonArray){

        int counter = jsonArray.length();
        List<HashMap<String, String>> nearbyPlaceList = new ArrayList<>();

        HashMap<String, String> nearbyPlaceMap = null;

        try {

            for (int i = 0; i < counter; i++) {
                nearbyPlaceMap = getPlace((JSONObject) jsonArray.get(i));
                nearbyPlaceList.add(nearbyPlaceMap);
            }
        }
        catch(JSONException e){
            e.getStackTrace();
        }

        return nearbyPlaceList;

    }

    public List<HashMap<String,String>> parse (String JSONdata){
        JSONArray jsonArray = null;
        JSONObject jsonObject;

        try {
            jsonObject = new JSONObject(JSONdata);
            jsonArray = jsonObject.getJSONArray("results");
        }
        catch(JSONException e) {
            e.getStackTrace();
        }

        return getAllNearbyPlaces(jsonArray);
    }


}
