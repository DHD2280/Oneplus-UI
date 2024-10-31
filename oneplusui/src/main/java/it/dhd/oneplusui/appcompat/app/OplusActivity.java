package it.dhd.oneplusui.appcompat.app;

import android.content.res.Configuration;
import android.os.Bundle;
import android.provider.Settings;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import it.dhd.oneplusui.R;
import it.dhd.oneplusui.utils.Constants;

public class OplusActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setDarkTheme();
    }

    private void setDarkTheme() {
        if (isNightMode()) {
            int darkStyle = Settings.System.getInt(getContentResolver(), Constants.DARK_MODE_STYLE, Constants.DEFAULT_DARK_MODE_STYLE);
            switch (darkStyle) {
                case 0:
                    setTheme(R.style.Theme_Oneplus_Dark_Hard);
                    break;
                case 1:
                    setTheme(R.style.Theme_Oneplus_Dark_Medium);
                    break;
                case 2:
                    setTheme(R.style.Theme_Oneplus_Dark_Soft);
                    break;
            }
        }
    }

    private boolean isNightMode() {
        return (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
    }

}
