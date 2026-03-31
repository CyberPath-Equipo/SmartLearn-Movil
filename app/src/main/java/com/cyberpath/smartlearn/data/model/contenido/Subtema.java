package com.cyberpath.smartlearn.data.model.contenido;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Subtema implements Parcelable {

    public static final Creator<Subtema> CREATOR = new Creator<Subtema>() {
        @Override
        public Subtema createFromParcel(Parcel in) {
            return new Subtema(in);
        }

        @Override
        public Subtema[] newArray(int size) {
            return new Subtema[size];
        }
    };
    private Integer id;
    private String nombre;
    private Integer orden;
    @SerializedName("createdAt")
    private String createdAt;
    @SerializedName("updatedAt")
    private String updatedAt;
    private Integer idTema;

    protected Subtema(Parcel in) {
        if (in.readByte() == 0) {
            id = null;
        } else {
            id = in.readInt();
        }

        nombre = in.readString();

        if (in.readByte() == 0) {
            orden = null;
        } else {
            orden = in.readInt();
        }

        createdAt = in.readString();
        updatedAt = in.readString();

        if (in.readByte() == 0) {
            idTema = null;
        } else {
            idTema = in.readInt();
        }
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        // id
        if (id == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(id);
        }

        // nombre
        dest.writeString(nombre);

        if (orden == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(orden);
        }

        dest.writeString(createdAt);
        dest.writeString(updatedAt);

        // idTema
        if (idTema == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(idTema);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
