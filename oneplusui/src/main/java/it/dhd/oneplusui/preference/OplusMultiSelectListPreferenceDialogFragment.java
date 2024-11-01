package it.dhd.oneplusui.preference;

import android.app.Dialog;
import android.content.DialogInterface;
import android.icu.text.LocaleDisplayNames;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.MultiSelectListPreferenceDialogFragmentCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import it.dhd.oneplusui.R;
import it.dhd.oneplusui.appcompat.dialog.adapter.ChoiceListAdapter;

public class OplusMultiSelectListPreferenceDialogFragment extends MultiSelectListPreferenceDialogFragmentCompat {

    private static final String SAVE_STATE_ENTRIES = "MultiSelectListPreferenceDialogFragment.entries";
    private static final String SAVE_STATE_ENTRY_VALUES = "MultiSelectListPreferenceDialogFragment.entryValues";
    private static final String SAVE_STATE_MESSAGE = "OplusMultiSelectListPreferenceDialogFragment.message";
    private static final String SAVE_STATE_TITLE = "OplusMultiSelectListPreferenceDialogFragment.title";
    private static final String SAVE_STATE_VALUES = "OplusMultiSelectListPreferenceDialogFragment.values";
    private ChoiceListAdapter mAdapter;
    private CharSequence mDialogMessage;
    private CharSequence mDialogTitle;
    private CharSequence[] mEntries;
    private CharSequence[] mEntryValues;
    private OplusMultiSelectListPreference mPreference;
    private boolean[] mCheckboxStates;

    @NonNull
    public static OplusMultiSelectListPreferenceDialogFragment newInstance(String key) {
        OplusMultiSelectListPreferenceDialogFragment cOUIListPreferenceDialogFragment = new OplusMultiSelectListPreferenceDialogFragment();
        Bundle bundle = new Bundle(1);
        bundle.putString("key", key);
        cOUIListPreferenceDialogFragment.setArguments(bundle);
        return cOUIListPreferenceDialogFragment;
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        if (bundle == null) {
            OplusMultiSelectListPreference oplusMultiSelectListPreference = (OplusMultiSelectListPreference) getPreference();
            this.mPreference = oplusMultiSelectListPreference;
            this.mDialogTitle = oplusMultiSelectListPreference.getDialogTitle();
            this.mDialogMessage = this.mPreference.getDialogMessage();
            this.mEntries = this.mPreference.getEntries();
            this.mEntryValues = this.mPreference.getEntryValues();
            this.mCheckboxStates = getCheckboxStatesFromValues(this.mPreference.getValues());
            return;
        }
        this.mDialogTitle = bundle.getString(SAVE_STATE_TITLE);
        this.mDialogMessage = bundle.getString(SAVE_STATE_MESSAGE);
        this.mEntries = bundle.getCharSequenceArray(SAVE_STATE_ENTRIES);
        this.mEntryValues = bundle.getCharSequenceArray(SAVE_STATE_ENTRY_VALUES);
        this.mCheckboxStates = bundle.getBooleanArray(SAVE_STATE_VALUES);
    }


    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle bundle) {
        mAdapter = new ChoiceListAdapter(getContext(), R.layout.oplus_select_dialog_multichoice, this.mEntries, null, mCheckboxStates, true) {
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
        };
        MaterialAlertDialogBuilder adapter = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(this.mDialogTitle)
                .setMessage(this.mDialogMessage)
                .setPositiveButton(android.R.string.ok, (dialogInterface, which) -> this.onDialogClosed(true))
                .setNegativeButton(android.R.string.cancel, null)
                .setAdapter(mAdapter, null);
        AlertDialog dialog = adapter.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setWindowAnimations(R.style.DialogAnimation);
        }
        return dialog;
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        Log.d("OplusMultiSelectListPreferenceDialogFragment", "onDialogClosed: " + positiveResult);
        if (getPreference() != null) {
            OplusMultiSelectListPreference oplusMultiSelectListPreference = (OplusMultiSelectListPreference) getPreference();
            Set<String> selectedValues = getSelectedValues();
            Log.d("OplusMultiSelectListPreferenceDialogFragment", "onDialogClosed: " + selectedValues);
            Log.d("OplusMultiSelectListPreferenceDialogFragment", "onDialogClosed: " + Arrays.toString(mCheckboxStates) + " " + oplusMultiSelectListPreference.callChangeListener(selectedValues));
            if (oplusMultiSelectListPreference.callChangeListener(selectedValues)) {
                oplusMultiSelectListPreference.persistStringSet(selectedValues);
                oplusMultiSelectListPreference.setValues(selectedValues);
            }
        }
    }


    @Override
    public void onSaveInstanceState(@NonNull Bundle bundle) {
        super.onSaveInstanceState(bundle);
        bundle.putBooleanArray(SAVE_STATE_VALUES, this.mAdapter.getCheckBoxStates());
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

    @NonNull
    private Set<String> getSelectedValues() {
        HashSet<String> hashSet = new HashSet<>();
        boolean[] checkBoxStates = this.mAdapter.getCheckBoxStates();
        for (int i = 0; i < checkBoxStates.length; i++) {
            CharSequence[] charSequenceArr = this.mEntryValues;
            if (i >= charSequenceArr.length) {
                break;
            }
            if (checkBoxStates[i]) {
                hashSet.add(charSequenceArr[i].toString());
            }
        }
        return hashSet;
    }

    private boolean[] getCheckboxStatesFromValues(Set<String> set) {
        boolean[] checkedValues = new boolean[this.mEntries.length];
        for (int i = 0; i < this.mEntries.length; i++) {
            checkedValues[i] = set.contains(this.mEntryValues[i].toString());
        }
        return checkedValues;
    }


}
