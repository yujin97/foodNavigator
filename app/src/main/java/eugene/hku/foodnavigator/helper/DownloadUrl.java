package eugene.hku.foodnavigator.helper;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class DownloadUrl {
    public String ReadTheUrl(String placeUrl){
        String data = "";
        InputStream inputStream = null;
        HttpURLConnection httpURLConnection = null;
        try{
            Log.d("tryBlock", "inside Try block");
            URL url =new URL(placeUrl);
            Log.d("downloadUrl",url.toString());
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.connect();
            Log.d("connection","can connect");
            inputStream = httpURLConnection.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuffer stringBuffer = new StringBuffer();

            String line = "";

            while ( (line = bufferedReader.readLine()) != null){
                stringBuffer.append(line);
            }

            data = stringBuffer.toString();
            bufferedReader.close();
        }
        catch(MalformedURLException e){
            e.printStackTrace();
        }
        catch(IOException e){
            e.printStackTrace();
        }
        finally {
            try {
                inputStream.close();
            }
            catch(IOException e){
                e.printStackTrace();
            }
            httpURLConnection.disconnect();
        }

        return data;
    }
}
