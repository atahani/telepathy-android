package com.atahani.telepathy.ui.utility;

import android.content.Context;
import mobi.atahani.telepathy.R;

/**
 * The time Utility
 */
public class TimeUtils {

    private Context mContext;

    public TimeUtils(Context context){
        this.mContext = context;
    }

    /**
     * get matched in time like in 2 min and 10 sec
     *
     * @param numberOfSec int numberOfSec
     * @return String
     */
    public String getMatchedInTime(int numberOfSec) {
        //get number of min base on sec
        int numberOfMin = 0;
        int numberOfDay = 0;
        int numberOfHour = 0;
        if (numberOfSec >= 86400) {
            numberOfDay = Math.abs(numberOfSec / 86400);
            return String.format(this.mContext.getString(R.string.label_time_day), String.format("%d",numberOfDay));
        } else if (numberOfSec >= 3600) {
            numberOfHour = Math.abs(numberOfSec / 3600);
            numberOfSec = numberOfSec - (numberOfHour * 3600);
            if (numberOfSec >= 60) {
                numberOfMin = Math.abs(numberOfSec / 60);
                return String.format(this.mContext.getString(R.string.label_time_hour_and_min), String.format("%d",numberOfHour), String.format("%d",numberOfMin));
            } else {
                return String.format(this.mContext.getString(R.string.label_time_hour), String.format("%d",numberOfHour));
            }
        } else if (numberOfSec >= 60) {
            numberOfMin = Math.abs(numberOfSec / 60);
            numberOfSec = numberOfSec - (numberOfMin * 60);
            if (numberOfSec != 0) {
                return String.format(this.mContext.getString(R.string.label_time_min_and_sec), String.format("%d",numberOfMin), String.format("%d",numberOfSec));
            } else {
                return String.format(this.mContext.getString(R.string.label_time_min), String.format("%d",numberOfMin));
            }
        } else {
            return String.format(this.mContext.getString(R.string.label_time_second), String.format("%d",numberOfSec));
        }
    }
}
