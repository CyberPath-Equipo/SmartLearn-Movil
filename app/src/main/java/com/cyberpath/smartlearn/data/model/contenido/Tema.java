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
public class Tema implements Parcelable {

    public static final Creator<Tema> CREATOR = new Creator<Tema>() {
        @Override
        public Tema createFromParcel(Parcel in) {
            return new Tema(in);
        }

        @Override
        public Tema[] newArray(int size) {
            return new Tema[size];
        }
    };
    private Integer id;
    private String nombre;
    private Integer orden;
    @SerializedName("createdAt")
    private String createdAt;
    @SerializedName("updatedAt")
    private String updatedAt;
    private Integer idMateria;
    private String slugMateria;


    protected Tema(Parcel in) {
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
            idMateria = null;
        } else {
            idMateria = in.readInt();
        }

        slugMateria = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        if (id == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(id);
        }


        dest.writeString(nombre);

        if (orden == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(orden);
        }

        dest.writeString(createdAt);
        dest.writeString(updatedAt);


        if (idMateria == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(idMateria);
        }

        dest.writeString(slugMateria);
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
