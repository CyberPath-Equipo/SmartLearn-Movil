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

    @SerializedName(value = "id", alternate = {"id_subtema"})
    private Integer id;
    private String nombre;

    @SerializedName(value = "idTema", alternate = {"id_tema"})
    private Integer idTema;

    private Integer orden;

    @SerializedName(value = "createdAt", alternate = {"created_at"})
    private String createdAt;

    @SerializedName(value = "updatedAt", alternate = {"updated_at"})
    private String updatedAt;

    protected Subtema(Parcel in) {
        if (in.readByte() == 0) {
            id = null;
        } else {
            id = in.readInt();
        }

        nombre = in.readString();

        if (in.readByte() == 0) {
            idTema = null;
        } else {
            idTema = in.readInt();
        }

        if (in.readByte() == 0) {
            orden = null;
        } else {
            orden = in.readInt();
        }
        createdAt = in.readString();
        updatedAt = in.readString();
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

        if (idTema == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(idTema);
        }

        if (orden == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(orden);
        }
        dest.writeString(createdAt);
        dest.writeString(updatedAt);
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
