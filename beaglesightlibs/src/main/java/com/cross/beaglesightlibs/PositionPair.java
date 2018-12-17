package com.cross.beaglesightlibs;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.Insert;
import androidx.room.PrimaryKey;
import androidx.room.Query;

import static androidx.room.ForeignKey.CASCADE;
import static androidx.room.OnConflictStrategy.REPLACE;

@Entity(foreignKeys = @ForeignKey(entity = BowConfig.class,
        parentColumns = "id",
        childColumns = "bowId",
        onDelete = CASCADE),
        indices = @Index("bowId"))
public class PositionPair implements Parcelable{
    @PrimaryKey
    @NonNull
    private String id;
    private String bowId;
    private float position;
    private float distance;

    @Ignore
    public PositionPair(float distance, float position)
    {
        this.distance = distance;
        this.position = position;
    }

    public PositionPair() {

    }

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    public String getBowId() {
        return bowId;
    }

    public void setBowId(String bowId) {
        this.bowId = bowId;
    }

    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

    public float getPosition() {
        return position;
    }

    public void setPosition(float position) {
        this.position = position;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeString(bowId);
        parcel.writeFloat(position);
        parcel.writeFloat(distance);
    }

    public PositionPair(Parcel in) {
        id = in.readString();
        bowId = in.readString();
        position = in.readFloat();
        distance = in.readFloat();
    }

    public static final Parcelable.Creator<PositionPair> CREATOR = new Parcelable.Creator<PositionPair>() {
        @Override
        public PositionPair createFromParcel(Parcel in) {
            return new PositionPair(in);
        }

        @Override
        public PositionPair[] newArray(int size) {
            return new PositionPair[size];
        }
    };



    @Dao
    public interface PositionPairDao {
        @Query("SELECT * FROM positionpair WHERE bowId IN (:bowId)")
        List<PositionPair> getPositionForBow(String bowId);

        @Insert(onConflict = REPLACE)
        void insert(PositionPair... positionPairs);

        @Delete
        void delete(PositionPair positionPair);
    }
}
