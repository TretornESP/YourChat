package tretornesp.clickerchat3;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

public class SizeExceeded {
    public static void exceeded(Context ctx) {
        new AlertDialog.Builder(ctx)
                .setIcon(R.drawable.image_error)
                .setTitle("Error")
                .setMessage("El archivo es demasiado grande")
                .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .show();
    }
}
