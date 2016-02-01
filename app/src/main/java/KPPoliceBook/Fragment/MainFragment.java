package KPPoliceBook.Fragment;

import android.app.Fragment;
import android.content.Context;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.askari.farrukh.trafficpolicebookkp.MainActivity;
import com.askari.farrukh.trafficpolicebookkp.MyApp;
import com.askari.farrukh.trafficpolicebookkp.R;
import com.google.android.gms.analytics.HitBuilders;
import com.squareup.okhttp.Address;

import java.io.IOException;
import java.util.List;

/**
 * Created by FARRU on 11/7/2015.
 */
public class MainFragment extends Fragment{
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container,false);

        // screen name
        MyApp.tracker().setScreenName("App Open");
        // Toast.makeText(getActivity(), "", Toast.LENGTH_SHORT).show();
        MyApp.tracker().send(new HitBuilders.ScreenViewBuilder().build());

        return  rootView;
    }
}
