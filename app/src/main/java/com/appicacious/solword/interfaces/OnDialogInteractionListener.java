package com.appicacious.solword.interfaces;

import android.os.Bundle;

public interface OnDialogInteractionListener {


    void onDialogShown(int reqCode);

    void onPositivePressed(int reqCode);

    void onPositivePressed(int reqCode, Bundle data);

    void onNegativePressed(int reqCode);


}
