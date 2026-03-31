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

    @SerializedName(value = "id", alternate = {"id_ejercicio"})
    private Integer id;

    private String nombre;

    // Flag local para UI (no persistido en la nueva BD como tabla aparte).
    private boolean hecho;

    @SerializedName(value = "idSubtema", alternate = {"id_subtema"})
    private Integer idSubtema;

    private String tipo;
    private Integer dificultad;
    private Integer orden;
    private Boolean activo;

    @SerializedName(value = "createdAt", alternate = {"created_at"})
    private String createdAt;

    protected Ejercicio(Parcel in) {
        id = (Integer) in.readValue(Integer.class.getClassLoader());
        nombre = in.readString();
        hecho = in.readByte() != 0;
        idSubtema = (Integer) in.readValue(Integer.class.getClassLoader());
        tipo = in.readString();
        dificultad = (Integer) in.readValue(Integer.class.getClassLoader());
        orden = (Integer) in.readValue(Integer.class.getClassLoader());
        activo = (Boolean) in.readValue(Boolean.class.getClassLoader());
        createdAt = in.readString();
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
        dest.writeValue(idSubtema);
        dest.writeString(tipo);
        dest.writeValue(dificultad);
        dest.writeValue(orden);
        dest.writeValue(activo);
        dest.writeString(createdAt);
    }
}