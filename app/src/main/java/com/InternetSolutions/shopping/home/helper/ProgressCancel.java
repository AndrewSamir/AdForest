package com.InternetSolutions.shopping.home.helper;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.InternetSolutions.shopping.R;
import com.InternetSolutions.shopping.utills.SettingsMain;


/**
 * Created by Glixen Technologies on 18/01/2018.
 */

public class ProgressCancel {

    private Context context;
    private PopupCancelModel popupModel;
    private ConfirmInterface confirmInterface;

    public ProgressCancel(Context context, ConfirmInterface confirmInterface) {
        this.context = context;
        this.confirmInterface = confirmInterface;
    }

    //requires only exit text
    private void showCustomTitleDialog(String title) {
        popupModel = SettingsMain.getPopupSettings(context);
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.upload_file_cancel);
        //  dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        TextView tv_cancelText = dialog.findViewById(R.id.tv_cancel);
        Button confirmButton = dialog.findViewById(R.id.btn_confirm);
        final Button closeButton = dialog.findViewById(R.id.btn_close);

        confirmButton.setText(popupModel.getConfirmButton());
        tv_cancelText.setText(popupModel.getConfirmText());
        closeButton.setText(popupModel.getCancelButton());
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (confirmInterface != null)
                    confirmInterface.onConfirmClick(dialog);
                //dialog.dismiss();
            }
        });
        dialog.show();

        dialog.getWindow().setLayout(RelativeLayout.LayoutParams.MATCH_PARENT, (int) confirmButton.getResources().getDimension(R.dimen.saved_jobs_popup_height));
    }

    public void showPopupWithCustomMessage(String message) {
        showCustomTitleDialog(message);
    }

    public interface ConfirmInterface {
        void onConfirmClick(Dialog dialog);
    }

}

