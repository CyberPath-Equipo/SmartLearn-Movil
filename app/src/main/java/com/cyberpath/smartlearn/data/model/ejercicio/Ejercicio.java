package com.cyberpath.smartlearn.data.model.ejercicio;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Ejercicio implements Parcelable {
    public static final Creator<Ejercicio> CREATOR = new Creator<Ejercicio>() {
        @Override
        public Ejercicio createFromParcel(Parcel in) {
            return new Ejercicio(in);
        }

        @Override
        public Ejercicio[] newArray(int size) {
            return new Ejercicio[size];
        }
    };
    private Integer id;
    private String nombre;
    @SerializedName("hecho")
    private boolean hecho;
    @SerializedName("activo")
    private Boolean activo;
    private String tipo;
    private String dificultad;
    private Integer orden;
    private String createdAt;
    private Integer idSubtema;

    protected Ejercicio(Parcel in) {
        id = (Integer) in.readValue(Integer.class.getClassLoader());
        nombre = in.readString();
        hecho = in.readByte() != 0;
        activo = (Boolean) in.readValue(Boolean.class.getClassLoader());
        tipo = in.readString();
        dificultad = in.readString();
        orden = (Integer) in.readValue(Integer.class.getClassLoader());
        createdAt = in.readString();
        idSubtema = (Integer) in.readValue(Integer.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(id);
        dest.writeString(nombre);
        dest.writeByte((byte) (hecho ? 1 : 0));
        dest.writeValue(activo);
        dest.writeString(tipo);
        dest.writeString(dificultad);
        dest.writeValue(orden);
        dest.writeString(createdAt);
        dest.writeValue(idSubtema);
    }
}