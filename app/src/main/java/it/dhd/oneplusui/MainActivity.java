package it.dhd.oneplusui;

import android.os.Bundle;

import com.google.android.material.color.DynamicColors;

import it.dhd.oneplusui.appcompat.app.OplusActivity;
import it.dhd.oneplusui.sample.R;

public class MainActivity extends OplusActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DynamicColors.applyToActivityIfAvailable(this);
        setContentView(R.layout.activity_main);

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new PrefFragment()).commit();
    }

}
