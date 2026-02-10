package com.cyberpath.smartlearn.data.model.contenido;

import android.os.Parcel;
import android.os.Parcelable;

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
    private Integer idMateria;

    // Constructor que lee desde Parcel
    protected Tema(Parcel in) {
        if (in.readByte() == 0) {
            id = null;
        } else {
            id = in.readInt();
        }

        nombre = in.readString();

        if (in.readByte() == 0) {
            idMateria = null;
        } else {
            idMateria = in.readInt();
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

        // idMateria
        if (idMateria == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(idMateria);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
