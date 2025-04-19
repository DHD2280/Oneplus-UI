package it.dhd.oneplusui.preference;

import static android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;
import androidx.preference.EditTextPreferenceDialogFragmentCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import it.dhd.oneplusui.R;
import it.dhd.oneplusui.appcompat.edittext.OplusEditText;

public class OplusEditTextPreferenceDialogFragment extends EditTextPreferenceDialogFragmentCompat {

    private static final String SAVE_STATE_TEXT = "EditTextPreferenceDialogFragment.text";
    private static final String TAG = "OplusEditTextPreferenceDialogFragment";
    private OplusEditText mEditText;

    private MaterialAlertDialogBuilder initDialog() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        builder.setTitle(getPreference().getDialogTitle())
                .setMessage(getPreference().getDialogMessage())
                .setPositiveButton(getPreference().getPositiveButtonText(), this)
                .setNegativeButton(getPreference().getNegativeButtonText(), this);
        return builder;
    }

    @NonNull
    public static OplusEditTextPreferenceDialogFragment newInstance(String str) {
        OplusEditTextPreferenceDialogFragment editTextPreferenceDialogFragment = new OplusEditTextPreferenceDialogFragment();
        Bundle bundle = new Bundle(1);
        bundle.putString("key", str);
        editTextPreferenceDialogFragment.setArguments(bundle);
        return editTextPreferenceDialogFragment;
    }

    @Override
    @NonNull
    @SuppressLint({"LongLogTag"})
    public Dialog onCreateDialog(Bundle bundle) {
        FragmentActivity activity = getActivity();
        MaterialAlertDialogBuilder materialDialog = initDialog();
        View contentView = onCreateDialogView(activity);
        if (contentView == null) {
            Log.d(TAG, "contentView == null ");
            return materialDialog.create();
        }
        this.mEditText = contentView.findViewById(android.R.id.edit);
        onBindDialogView(contentView);
        materialDialog.setView(contentView);
        if (getPreference() != null) {
            onBindDialogView(contentView);
        }
        onPrepareDialogBuilder(materialDialog);
        final AlertDialog create = materialDialog.create();
        final boolean isSupportEmptyInput = true;
        this.mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {
                Button button = create.getButton(-1);
                if (button == null || isSupportEmptyInput) {
                    return;
                }
                button.setEnabled(!TextUtils.isEmpty(editable));
            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int before, int after) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
            }
        });
        if (create.getWindow() != null) {
            create.getWindow().setWindowAnimations(R.style.DialogAnimation);
        }
        return create;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mEditText != null) {
            mEditText.setFocusable(true);
            this.mEditText.requestFocus();
            if (getDialog() != null) {
                getDialog().getWindow().setSoftInputMode(SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            }
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle bundle) {
        super.onSaveInstanceState(bundle);
        if (mEditText != null) {
            bundle.putCharSequence(SAVE_STATE_TEXT, mEditText.getText());
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
