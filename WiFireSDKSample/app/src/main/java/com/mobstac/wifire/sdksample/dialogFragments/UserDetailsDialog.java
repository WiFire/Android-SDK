package com.mobstac.wifire.sdksample.dialogFragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;

import com.mobstac.wifire.WiFire;
import com.mobstac.wifire.core.WiFireException;
import com.mobstac.wifire.sdksample.R;

/**
 * Created by Kislay on 12/08/16.
 */
public class UserDetailsDialog extends DialogFragment {

    Context mContext;

    View ok, cancel;
    EditText name, phone, email, country;

    public UserDetailsDialog() {
        //Default constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.dialog_user_details, container, false);
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        setCancelable(false);

        ok = rootView.findViewById(R.id.ok);
        cancel = rootView.findViewById(R.id.cancel);
        phone = (EditText) rootView.findViewById(R.id.phone);
        email = (EditText) rootView.findViewById(R.id.email);
        name = (EditText) rootView.findViewById(R.id.name);
        country = (EditText) rootView.findViewById(R.id.country_code);

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    WiFire.getInstance().setUserDetails(country.getText().toString(), phone.getText().toString(),
                            email.getText().toString(), name.getText().toString());
                    dismiss();
                } catch (WiFireException e) {
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }

            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        return rootView;
    }

    @Override
    public void show(FragmentManager manager, String tag) {
        /*
            Need to override this and add exception handler method if support package is used
        */
        try {
            FragmentTransaction ft = manager.beginTransaction();
            ft.add(this, tag);
            ft.commit();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    public static UserDetailsDialog newInstance() {
        return new UserDetailsDialog();
    }


}