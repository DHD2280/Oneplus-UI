package it.dhd.oneplusui.preference;

import android.app.Dialog;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.ListPreferenceDialogFragmentCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import it.dhd.oneplusui.R;
import it.dhd.oneplusui.appcompat.dialog.adapter.ChoiceListAdapter;

public class OplusListPreferenceDialogFragment extends ListPreferenceDialogFragmentCompat {

    private static final String SAVE_STATE_ENTRIES = "ListPreferenceDialogFragment.entries";
    private static final String SAVE_STATE_ENTRY_VALUES = "ListPreferenceDialogFragment.entryValues";
    private static final String SAVE_STATE_INDEX = "OplusListPreferenceDialogFragment.index";
    private static final String SAVE_STATE_MESSAGE = "OplusListPreferenceDialogFragment.message";
    private static final String SAVE_STATE_TITLE = "OplusListPreferenceDialogFragment.title";
    private CharSequence mDialogMessage;
    private CharSequence mDialogTitle;
    private CharSequence[] mEntries;
    private CharSequence[] mEntryValues;
    private OplusListPreference mPreference;
    private int mClickedDialogEntryIndex = -1;

    @NonNull
    public static OplusListPreferenceDialogFragment newInstance(String key) {
        OplusListPreferenceDialogFragment oplusListPreferenceDialogFragment = new OplusListPreferenceDialogFragment();
        Bundle bundle = new Bundle(1);
        bundle.putString("key", key);
        oplusListPreferenceDialogFragment.setArguments(bundle);
        return oplusListPreferenceDialogFragment;
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        if (bundle == null) {
            OplusListPreference oplusListPreference = (OplusListPreference) getPreference();
            this.mPreference = oplusListPreference;
            if (oplusListPreference.getEntries() != null && this.mPreference.getEntryValues() != null) {
                this.mDialogTitle = this.mPreference.getDialogTitle();
                this.mDialogMessage = this.mPreference.getDialogMessage();
                this.mClickedDialogEntryIndex = mPreference.findIndexOfValue(mPreference.getValue());
                this.mEntries = this.mPreference.getEntries();
                this.mEntryValues = this.mPreference.getEntryValues();
                return;
            }
            throw new IllegalStateException("ListPreference requires an entries array and an entryValues array.");
        }
        this.mClickedDialogEntryIndex = bundle.getInt(SAVE_STATE_INDEX, -1);
        this.mDialogTitle = bundle.getString(SAVE_STATE_TITLE);
        this.mDialogMessage = bundle.getString(SAVE_STATE_MESSAGE);
        this.mEntries = bundle.getCharSequenceArray(SAVE_STATE_ENTRIES);
        this.mEntryValues = bundle.getCharSequenceArray(SAVE_STATE_ENTRY_VALUES);
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle bundle) {
        boolean[] checkedValue;
        int item;
        CharSequence[] entries = this.mEntries;
        if (entries != null && (item = this.mClickedDialogEntryIndex) >= 0 && item < entries.length) {
            boolean[] valueMap = new boolean[entries.length];
            valueMap[item] = true;
            checkedValue = valueMap;
        } else {
            checkedValue = null;
        }

        MaterialAlertDialogBuilder adapter = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(this.mDialogTitle)
                .setMessage(this.mDialogMessage)
                .setNegativeButton(android.R.string.cancel, null)
                .setAdapter(new ChoiceListAdapter(getContext(), R.layout.oplus_select_dialog_singlechoice, this.mEntries, null, checkedValue, false) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view3 = super.getView(position, convertView, parent);
                View findViewById = view3.findViewById(R.id.item_divider);
                int count = getCount();
                if (findViewById != null) {
                    if (count != 1 && position != count - 1) {
                        findViewById.setVisibility(View.VISIBLE);
                    } else {
                        findViewById.setVisibility(View.GONE);
                    }
                }
                return view3;
            }
        }, (dialogInterface, which) -> {
            OplusListPreferenceDialogFragment.this.mClickedDialogEntryIndex = which;
            OplusListPreferenceDialogFragment.this.onClick(dialogInterface, -1);
            dialogInterface.dismiss();
        });
        AlertDialog dialog = adapter.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setWindowAnimations(R.style.DialogAnimation);
        }
        return dialog;
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        int item;
        super.onDialogClosed(positiveResult);
        if (positiveResult && this.mEntries != null && (item = this.mClickedDialogEntryIndex) >= 0) {
            CharSequence[] entryValues = this.mEntryValues;
            if (item < entryValues.length) {
                String charSequence = entryValues[item].toString();
                if (getPreference() != null) {
                    OplusListPreference oplusListPreference = (OplusListPreference) getPreference();
                    if (oplusListPreference.callChangeListener(charSequence)) {
                        oplusListPreference.setValue(charSequence);
                    }
                }
            }
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle bundle) {
        super.onSaveInstanceState(bundle);
        bundle.putInt(SAVE_STATE_INDEX, this.mClickedDialogEntryIndex);
        CharSequence dialogTitle = this.mDialogTitle;
        if (dialogTitle != null) {
            bundle.putString(SAVE_STATE_TITLE, String.valueOf(dialogTitle));
        }
        CharSequence dialogMessage = this.mDialogMessage;
        if (dialogMessage != null) {
            bundle.putString(SAVE_STATE_MESSAGE, String.valueOf(dialogMessage));
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setGravity(Gravity.BOTTOM);
        }
        if (getPreference() == null) {
            dismiss();
        }
    }
}
