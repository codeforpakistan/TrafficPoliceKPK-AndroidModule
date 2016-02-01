package KPPoliceBook.Fragment;

import android.Manifest;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
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
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;


import com.askari.farrukh.trafficpolicebookkp.DatabaseHelper;
import com.askari.farrukh.trafficpolicebookkp.MyApp;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.maps.model.TileOverlay;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import com.askari.farrukh.trafficpolicebookkp.R;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by FARRU on 11/7/2015.
 */
public class ChalanFragment extends Fragment implements LocationListener {

    private static final int PERMISSION_REQUEST_CODE = 0;
    // File
    File imageFile;
    // input fields on Challan Form
    EditText inputBookNo;
    Spinner inputVehicleType;
    EditText inputVehicleNoABC;
    EditText inputVehicleNo123;
    EditText inputChallanNo;
    Spinner inputEducation;
    Spinner inputDistrict;
    EditText inputLocation;

    //Database Helper
    DatabaseHelper databaseHelper;

    public String path1;

    //Uri
    Uri pictureUri = null;
    // using for thumbnail
    public Bitmap cameraImg;

    public Location lastKnownLocation = null;

    //location
    public String provider;
    public Location location;
    public Double latitude  = null;
    public Double longitude = null;

    // date n time
    public String timeStamp;

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
    private static String url = "http://192.168.1.4/trafficBookKP/create_challan.php";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_CODE);
        }


        //        screen name
        MyApp.tracker().setScreenName("Challan");
       // Toast.makeText(getActivity(), "", Toast.LENGTH_SHORT).show();
        MyApp.tracker().send(new HitBuilders.ScreenViewBuilder().build());


        // creating fragment view
        View chalanView = inflater.inflate(R.layout.fragment_challan, container, false);

        Button btnCam = (Button) chalanView.findViewById(R.id.btnCamera);
        Button btnChallan = (Button) chalanView.findViewById(R.id.btnInfra);
        // Edit Text
        inputBookNo = (EditText) chalanView.findViewById(R.id.EVBookNo);
        inputVehicleType = (Spinner) chalanView.findViewById(R.id.EVvehicleType);
        inputVehicleNoABC = (EditText) chalanView.findViewById(R.id.EVvehicleNoABC);
        inputVehicleNo123 = (EditText) chalanView.findViewById(R.id.EVvehicleNo123);
        inputChallanNo = (EditText) chalanView.findViewById(R.id.EVchallanNo);
        inputEducation = (Spinner) chalanView.findViewById(R.id.EVeducation);
        inputDistrict = (Spinner) chalanView.findViewById(R.id.EVdistrict);
        inputLocation = (EditText) chalanView.findViewById(R.id.EVlocation);


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
                imageFile = new File(pictureDirectory, pictureName);
                file_path = imageFile.toString();
                pictureUri = Uri.fromFile(imageFile);
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

        locationManager.requestLocationUpdates(provider, 400, 1, this);
        location = locationManager.getLastKnownLocation(provider);
        if (location !=null) {
            onLocationChanged(location);
        }else{
            location = locationManager.getLastKnownLocation(locationManager.PASSIVE_PROVIDER);
            onLocationChanged(location);
        }



        // setting thumbnail to image view
        imageView = (ImageView) chalanView.findViewById(R.id.imageViewAccident);
        // call on button challan
        btnChallan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String vehicleNo  = inputVehicleNoABC.getText().toString();
                vehicleNo = vehicleNo + inputVehicleNo123.getText().toString();
                String vehicleType= inputVehicleType.getSelectedItem().toString();
                String challanNo  = inputChallanNo.getText().toString();
                String education  = inputEducation.getSelectedItem().toString();
                String bookNo     = inputBookNo.getText().toString();
                String district   = inputDistrict.getSelectedItem().toString();
                String location1  = inputLocation.getText().toString();

                // calling challan class
                if (vehicleNo != null && vehicleType !=  null && education != null && district != null & location1 != null && pictureName != null)
                 addChallan(vehicleNo, vehicleType , challanNo, education, bookNo, district, location1, latitude, longitude);
                else
                    Toast.makeText(getActivity(), "Take Picture & Complete Form", Toast.LENGTH_SHORT).show();


            }
        });
        // returning view
        return chalanView;
    }

    // challan class
    public void addChallan(final String vehicleNo, final String vehicleType , final String challanNo, final String education, final String bookNo, final String district, final String location1, Double Latitude, Double Longitude){



/*
        RequestBody body = new FormEncodingBuilder()
                .add("vehicleNo", vehicleNo)
                .add("challanNo", challanNo)
                .add("education", education)
                .add("bookNo"   , bookNo)
                .build();
*/
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        path1 = path+"/"+pictureName;
        File file = new File(path1);


        final String lat, lon;

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
                .addFormDataPart("vehicleNo", vehicleNo)
                .addFormDataPart("vehicleType", vehicleType)
                .addFormDataPart("challanNo", challanNo)
                .addFormDataPart("education", education)
                .addFormDataPart("bookNo", bookNo)
                .addFormDataPart("district", district)
                .addFormDataPart("location", location1)
                .addFormDataPart("latitude", lat)
                .addFormDataPart("longitude", lon)
                .addFormDataPart("date", timeStamp)
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
                        //Toast.makeText(getActivity(), "Saving locally in Mobile; Adding Challan error", Toast.LENGTH_LONG).show();
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setCancelable(true);
                        builder.setTitle("Saving Data");
                        builder.setMessage("Saving locally in Mobile; No internet available");
                        builder.show();
                        //calling db
                        databaseHelper = new DatabaseHelper(getActivity());
                        //inserting data
                        boolean check = databaseHelper.insertDataChallan(challanNo, vehicleType, vehicleNo, district, location1, education, path1, timeStamp, lat, lon, bookNo, pictureName);
                        if (check == true)
                            Toast.makeText(getActivity(), "Inserted", Toast.LENGTH_SHORT).show();
                        else Toast.makeText(getActivity(), "Not Inserted", Toast.LENGTH_SHORT).show();
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
                                //Toast.makeText(getActivity(), "Challan Successful", Toast.LENGTH_LONG).show();
                                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                builder.setCancelable(true);
                                builder.setTitle("Challan");
                                builder.setMessage("Successful");
                                builder.show();
                            }
                        });
                    } else {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getActivity(), "Server! Adding Chalan Error", Toast.LENGTH_LONG).show();
                                //calling db
                                databaseHelper = new DatabaseHelper(getActivity());
                                //inserting data
                                boolean check = databaseHelper.insertDataChallan(challanNo, vehicleType, vehicleNo, district, location1, education, path1, timeStamp, lat, lon, bookNo, pictureName);
                                if (check == true)
                                    Toast.makeText(getActivity(), "Inserted", Toast.LENGTH_SHORT).show();
                                else Toast.makeText(getActivity(), "Not Inserted", Toast.LENGTH_SHORT).show();
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
        timeStamp = simpleDateFormat.format(new Date());
        return "Challan"+timeStamp+".JPG";
    }

    // Camera Call
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
        {
            //Toast.makeText(getActivity(), "Lat:"+location.getLatitude()+" Long:"+location.getLongitude(), Toast.LENGTH_SHORT).show();
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
