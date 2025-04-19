package androidx.preference;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.OplusRecyclerView;
import androidx.recyclerview.widget.RecyclerView;

import it.dhd.oneplusui.R;
import it.dhd.oneplusui.preference.OplusEditTextPreference;
import it.dhd.oneplusui.preference.OplusEditTextPreferenceDialogFragment;
import it.dhd.oneplusui.preference.OplusListPreferenceDialogFragment;
import it.dhd.oneplusui.preference.OplusMultiSelectListPreferenceDialogFragment;
import it.dhd.oneplusui.preference.OplusPreferenceItemDecoration;

/**
 * This is the entry point to using preference library.
 * See {@link androidx.preference.PreferenceFragmentCompat} for more information.
 * <p>
 * This class replaces the default divider with a custom one.
 * See {@link OplusPreferenceItemDecoration} for more information.
 */
public class OplusPreferenceFragment extends PreferenceFragmentCompat {

    private static final String DIALOG_FRAGMENT_TAG = "androidx.preference.PreferenceFragment.DIALOG";
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
        OplusPreferenceItemDecoration oplusPreferenceItemDecoration = this.mPreferenceItemDecoration;
        if (oplusPreferenceItemDecoration != null) {
            oplusPreferenceItemDecoration.onDestroy();
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

    /**
     * Set whether to enable the custom divider.
     * @param enable true to enable, false to disable
     */
    public void setEnableOplusPreferenceDivider(boolean enable) {
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

    /**
     * Display the {@link DialogFragment} associated with the {@link Preference} object.
     * This method give us the ability to display custom dialogs.
     * @param preference The {@link Preference} object requesting the dialog
     */
    @Override
    public void onDisplayPreferenceDialog(@NonNull Preference preference) {
        DialogFragment newInstance;
        if (getFragmentManager().findFragmentByTag(DIALOG_FRAGMENT_TAG) != null) {
            return;
        }
        if (preference instanceof EditTextPreference) {
            newInstance = OplusEditTextPreferenceDialogFragment.newInstance(preference.getKey());
        } else if (preference instanceof MultiSelectListPreference) {
            newInstance = OplusMultiSelectListPreferenceDialogFragment.newInstance(preference.getKey());
        } else if (preference instanceof ListPreference) {
            newInstance = OplusListPreferenceDialogFragment.newInstance(preference.getKey());
        } else {
            super.onDisplayPreferenceDialog(preference);
            return;
        }
        newInstance.setTargetFragment(this, 0);
        newInstance.show(getFragmentManager(), DIALOG_FRAGMENT_TAG);
    }


}
