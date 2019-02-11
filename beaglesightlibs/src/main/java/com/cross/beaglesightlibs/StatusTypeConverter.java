package com.cross.beaglesightlibs;

import androidx.room.TypeConverter;

public class StatusTypeConverter {
    @TypeConverter
    public static String statusToString(LockStatus.Status status) {
        if (status == null) {
            return null;
        }
        return status.toString();
    }

    @TypeConverter
    public static LockStatus.Status stringToStatus(String string) {
        if (string == null) {
            return null;
        }
        return LockStatus.Status.valueOf(string);
    }
}
