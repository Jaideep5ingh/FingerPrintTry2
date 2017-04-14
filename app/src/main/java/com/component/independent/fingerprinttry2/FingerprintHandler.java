package com.component.independent.fingerprinttry2;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.CancellationSignal;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

/**
 * Created by jai00 on 14-04-2017.
 */

public class FingerprintHandler extends FingerprintManager.AuthenticationCallback {

    private CancellationSignal cancellationSignal;
    private Context appContext;


    public FingerprintHandler(Context context){
        appContext= context;
    }

    public void startAuth(FingerprintManager manager,
                          FingerprintManager.CryptoObject
                                  cryptoObject){
        cancellationSignal = new CancellationSignal();

        if(ActivityCompat.checkSelfPermission
                (appContext,
                        Manifest.permission.USE_FINGERPRINT)
                != PackageManager.PERMISSION_GRANTED){
            return;
        }

        manager.authenticate
                (cryptoObject,cancellationSignal,0,this,null);
    }


    @Override
    public void onAuthenticationError
            (int errorCode, CharSequence errString) {
        Toast.makeText(appContext, "" +
                        "Authentication Error\n" + errString,
                Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onAuthenticationHelp(int helpCode,
                                     CharSequence helpString) {
        Toast.makeText(appContext, "" +
                        "Authentication Help\n" + helpString,
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onAuthenticationFailed() {
        Toast.makeText(appContext, "" +
                        "Authentication Failed.",
                Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onAuthenticationSucceeded(
            FingerprintManager.AuthenticationResult result) {
        Toast.makeText(appContext, "" +
                        "Authentication Successful.",
                Toast.LENGTH_SHORT).show();
    }

}
