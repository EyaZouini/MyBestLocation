package com.example.mybestlocation;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import androidx.core.content.ContextCompat;

public class SmsReceiver extends BroadcastReceiver {

    private static final String TAG = "MySmsReceiver";

    @SuppressLint("MissingPermission")
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive appelé");

        // Vérification des permissions
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "Permissions SMS non accordées.");
            return;  // Si les permissions ne sont pas accordées, on arrête l'exécution
        }

        // Vérifier l'action du message reçu
        if(intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
            Log.d(TAG, "SMS reçu");

            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                Object[] pdus = (Object[]) bundle.get("pdus");
                final SmsMessage[] messages = new SmsMessage[pdus.length];
                for (int i = 0; i < pdus.length; i++) {
                    messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                }

                if (messages.length > 0) {
                    String messageBody = messages[0].getMessageBody();
                    String phoneNumber = messages[0].getDisplayOriginatingAddress();
                    Log.d(TAG, "Message reçu: " + messageBody);
                    Log.d(TAG, "Numéro de l'expéditeur: " + phoneNumber);

                    // Vérifier si le message contient la commande spécifique
                    if (messageBody.contains("FINDFRIENDS : Envoyer moi votre position")) {
                        Log.d(TAG, "Message trouvé : Envoyer moi votre position");

                        // Répondre avec une position fixe
                        String position = "Ma position actuelle est : Latitude: 35.82110135036956, Longitude: 10.631215386092663";
                        envoyerSms(phoneNumber, position, context); // Envoi des coordonnées fixes
                    } else {
                        Log.d(TAG, "Message ne correspond pas à la commande.");
                    }
                }
            }
        }
    }

    // Fonction pour envoyer un SMS
    private void envoyerSms(String numero, String message, Context context) {
        SmsManager smsManager = SmsManager.getDefault();
        try {
            smsManager.sendTextMessage(numero, null, message, null, null);
            Log.d(TAG, "Réponse envoyée à : " + numero);
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors de l'envoi du SMS", e);
        }
    }
}
