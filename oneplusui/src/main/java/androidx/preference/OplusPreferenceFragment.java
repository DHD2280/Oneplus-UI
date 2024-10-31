package androidx.preference;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.OplusRecyclerView;
import androidx.recyclerview.widget.RecyclerView;

import it.dhd.oneplusui.R;
import it.dhd.oneplusui.preference.OplusPreferenceItemDecoration;

public class OplusPreferenceFragment extends PreferenceFragmentCompat {

    private boolean mEnableInternalDivider = true;
    private OplusPreferenceItemDecoration mPreferenceItemDecoration;

    public OplusPreferenceItemDecoration getItemDecoration() {
        return this.mPreferenceItemDecoration;
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String str) {
    }

    @NonNull
    @Override
    public RecyclerView onCreateRecyclerView(LayoutInflater layoutInflater, @NonNull ViewGroup viewGroup, Bundle bundle) {
        OplusRecyclerView oplusRecyclerView = (OplusRecyclerView) layoutInflater.inflate(R.layout.oplus_preference_recycler, viewGroup, false);
        oplusRecyclerView.setLayoutManager(onCreateLayoutManager());
        return oplusRecyclerView;
    }

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        View onCreateView = super.onCreateView(layoutInflater, viewGroup, bundle);
        setDivider(null);
        setDividerHeight(0);
        return onCreateView;
    }

    @Override
    public void onDestroyView() {
        OplusPreferenceItemDecoration cOUIPreferenceItemDecoration = this.mPreferenceItemDecoration;
        if (cOUIPreferenceItemDecoration != null) {
            cOUIPreferenceItemDecoration.onDestroy();
        }
        super.onDestroyView();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle bundle) {
        super.onViewCreated(view, bundle);
        if (getListView() != null && this.mPreferenceItemDecoration != null && this.mEnableInternalDivider) {
            getListView().removeItemDecoration(this.mPreferenceItemDecoration);
            if (this.mPreferenceItemDecoration.getPreferenceScreen() == null) {
                this.mPreferenceItemDecoration = new OplusPreferenceItemDecoration(getContext(), getPreferenceScreen());
            }
            getListView().addItemDecoration(this.mPreferenceItemDecoration);
        }
    }

    public void setEnableOplusPreferenceDivider(boolean enable) throws NullPointerException {
        this.mEnableInternalDivider = enable;
        if (enable) {
            if (getListView() != null && this.mPreferenceItemDecoration != null) {
                getListView().removeItemDecoration(this.mPreferenceItemDecoration);
                if (this.mPreferenceItemDecoration.getPreferenceScreen() == null) {
                    this.mPreferenceItemDecoration = new OplusPreferenceItemDecoration(getContext(), getPreferenceScreen());
                }
                getListView().addItemDecoration(this.mPreferenceItemDecoration);
            }
        } else if (getListView() != null) {
            getListView().removeItemDecoration(this.mPreferenceItemDecoration);
        }
    }

    @Override
    public void setPreferenceScreen(PreferenceScreen preferenceScreen) {
        if (preferenceScreen == getPreferenceScreen()) {
            return;
        }
        super.setPreferenceScreen(preferenceScreen);
        if (this.mPreferenceItemDecoration != null && getListView() != null) {
            getListView().removeItemDecoration(this.mPreferenceItemDecoration);
        }
        this.mPreferenceItemDecoration = new OplusPreferenceItemDecoration(getContext(), preferenceScreen);
        if (getListView() != null && this.mEnableInternalDivider) {
            getListView().addItemDecoration(this.mPreferenceItemDecoration);
        }
    }
}
