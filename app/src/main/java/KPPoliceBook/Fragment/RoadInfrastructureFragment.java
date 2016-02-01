package KPPoliceBook.Fragment;

import android.Manifest;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.askari.farrukh.trafficpolicebookkp.MyApp;
import com.askari.farrukh.trafficpolicebookkp.R;
import com.google.android.gms.analytics.HitBuilders;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by FARRU on 11/7/2015.
 */
public class RoadInfrastructureFragment extends Fragment implements LocationListener {

    // File
    File imageFile;
    // input fields on Road Infra Form

    EditText inputRoadInfra;
    EditText inputDiscriptionInfra;
    Spinner inputDistrictInfra;
    EditText inputLocationInfra;

    //Uri
    Uri pictureUri = null;
    // using for thumbnail
    public Bitmap cameraImg;

    //location
    public String provider;
    public Location location;
    public Double latitude  = null;
    public Double longitude = null;

    //pic name
    String pictureName = null;
    // image view
    public ImageView imageView;
    // PIC request Code
    public static final int REQUEST_CODE = 10;
    // using OKHTTP for JSON (S+R)
    private OkHttpClient client = new OkHttpClient();
    // Tag
    private static final String TAG_SUCCESS = "success";
    // path to file
    String file_path = null;
    //Server IP address with port
    //local
    private static String url = "http://192.168.1.2/trafficBookKP/create_infra.php";


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        //        screen name
        MyApp.tracker().setScreenName("Road Infrastructure");
        // Toast.makeText(getActivity(), "", Toast.LENGTH_SHORT).show();
        MyApp.tracker().send(new HitBuilders.ScreenViewBuilder().build());


        View roadInfrstructureView = inflater.inflate(R.layout.fragment_road_infrastructure,container,false);
        Button btnCam      = (Button) roadInfrstructureView.findViewById(R.id.btnCameraInfra);
        Button btnInfra = (Button) roadInfrstructureView.findViewById(R.id.btnInfra);
        // Edit Text
        inputRoadInfra          = (EditText) roadInfrstructureView.findViewById(R.id.EVRoadInfaRoad);
        inputDiscriptionInfra   = (EditText) roadInfrstructureView.findViewById(R.id.EVRoadInfaDisc);
        inputDistrictInfra      = (Spinner) roadInfrstructureView.findViewById(R.id.EVRoadInfaDistrict);
        inputLocationInfra      = (EditText) roadInfrstructureView.findViewById(R.id.EVRoadInfaLocation);

        // Taking Picture
        btnCam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                File pictureDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                if (!pictureDirectory.mkdirs()) {
                    Log.e("Picture", "Directory not created");
                }

                pictureName = getPictureName();
                imageFile   = new File(pictureDirectory, pictureName);
                file_path   = imageFile.toString();
                pictureUri  = Uri.fromFile(imageFile);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, pictureUri);
                startActivityForResult(cameraIntent, REQUEST_CODE);
            }
        });

        //Location
        LocationManager locationManager;
        LocationListener locationListener;

        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        provider = locationManager.getBestProvider(criteria, false);

        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            //return TODO;
        }
        locationManager.requestLocationUpdates(provider, 400, 1, this);
        location = locationManager.getLastKnownLocation(provider);
        if (location !=null) {
            onLocationChanged(location);
        }else{
            location = locationManager.getLastKnownLocation(locationManager.PASSIVE_PROVIDER);
            onLocationChanged(location);
        }

        // setting thumbnail to image view
        imageView = (ImageView) roadInfrstructureView.findViewById(R.id.imageViewRoadInfa);
        // call on button Road Infra
        btnInfra.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String district   = inputDistrictInfra.getSelectedItem().toString();
                String discription= inputDiscriptionInfra.getText().toString();
                String road       = inputRoadInfra.getText().toString();
                String location1   = inputLocationInfra.getText().toString();
                // calling RoadInfra class
                if (district != null & location1 != null && pictureName != null)
                    addInfra(district, discription, road, location1, latitude, longitude);
                else
                Toast.makeText(getActivity(), "Take Picture & Complete Form", Toast.LENGTH_SHORT).show();

            }
        });
        // returning view

        return roadInfrstructureView;
    }

    public void addInfra(String district , String discription, String road, String location1, Double Latitude, Double Longitude){

        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        String path1 = path+"/"+pictureName;
        File file = new File(path1);

        String lat, lon;

        if (location == null) {
            lat = "";
            lon = "";
        } else{
            lat = latitude.toString();
            lon = longitude.toString();
        }

        // For multipart data
        RequestBody body = new MultipartBuilder()
                .type(MultipartBuilder.FORM)
                .addFormDataPart("district", district)
                .addFormDataPart("discription", discription)
                .addFormDataPart("road", road)
                .addFormDataPart("location", location1)
                .addFormDataPart("latitude", lat)
                .addFormDataPart("longitude", lon)
                .addFormDataPart("imageFile", pictureName, RequestBody.create(MediaType.parse("image/jpg"), file))
                .build();

        // making request
        Request request = new Request.Builder().url(url).post(body).build();

        // making call
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                Log.e("Error", "Registration error: " + e.getMessage());
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getActivity(), "Road Infra error", Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onResponse(Response response) throws IOException {
                try {
                    final String resp = response.body().string();
                    Log.v("done", resp);
                    if (response.isSuccessful()) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getActivity(), "Road Infra Successful", Toast.LENGTH_LONG).show();
                            }
                        });
                    } else {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getActivity(), "Road Infra ERROR", Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    // Setting Picture Name
    private String getPictureName() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyMMdd_HHmmss");
        String timeStamp = simpleDateFormat.format(new Date());
        return "RoadInfra"+timeStamp+".JPG";
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CODE) {
                File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

                String path1 = path+"/"+pictureName;

                cameraImg = BitmapFactory.decodeFile(path1);
                imageView.setImageBitmap(null);
                imageView.setImageBitmap(cameraImg);
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        this.location = location;
        if (location!=null)
        {Toast.makeText(getActivity(), "Lat:"+location.getLatitude()+" Long:"+location.getLongitude(), Toast.LENGTH_SHORT).show();
            latitude  =  location.getLatitude();
            longitude = location.getLongitude();
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
