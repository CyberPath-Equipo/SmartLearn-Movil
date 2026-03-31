package com.cyberpath.smartlearn.ui.acceso;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.cyberpath.smartlearn.R;
import com.cyberpath.smartlearn.data.api.ApiService;
import com.cyberpath.smartlearn.data.api.RetrofitClient;
import com.cyberpath.smartlearn.data.model.usuario.Configuracion;
import com.cyberpath.smartlearn.data.model.usuario.Rol;
import com.cyberpath.smartlearn.data.model.usuario.Usuario;
import com.cyberpath.smartlearn.util.constants.UsuarioCst;
import com.cyberpath.smartlearn.util.preferences.PreferencesManager;

import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignUpFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "SignUpFragment";

    private Button btnRegresar, btnRegistro;
    private EditText etNombre, etContrasena, etCorreo;
    private RadioButton radioActiva, radioInactiva, radioAlumno, radioDocente;
    private RadioGroup grupoAccesibilidad, grupoTipoUsuario;
    private ProgressBar loading;
    private Integer idRolAlumno;
    private Integer idRolDocente;
    private boolean rolesCargados = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sign_up, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnRegresar = view.findViewById(R.id.btn_regresar);
        btnRegistro = view.findViewById(R.id.btn_registro);
        etNombre = view.findViewById(R.id.et_nombre);
        etContrasena = view.findViewById(R.id.et_contraseña);
        etCorreo = view.findViewById(R.id.et_correo);
        radioActiva = view.findViewById(R.id.radio_activa);
        radioInactiva = view.findViewById(R.id.radio_inactiva);
        radioAlumno = view.findViewById(R.id.radio_alumno);
        radioDocente = view.findViewById(R.id.radio_docente);
        grupoAccesibilidad = view.findViewById(R.id.grupo_accesibilidad);
        grupoTipoUsuario = view.findViewById(R.id.grupo_tipo_usuario);
        loading = view.findViewById(R.id.loading);

        btnRegresar.setOnClickListener(this);
        btnRegistro.setOnClickListener(this);
        btnRegistro.setEnabled(false);

        cargarRoles();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_regresar) {
            Navigation.findNavController(v).navigate(R.id.loginFragment);
        } else if (v.getId() == R.id.btn_registro) {
            registrarUsuario();
        }
    }

    private void cargarRoles() {
        ApiService api = RetrofitClient.getApiService();
        api.getRoles().enqueue(new Callback<List<Rol>>() {
            @Override
            public void onResponse(Call<List<Rol>> call, Response<List<Rol>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(requireContext(), "No se pudieron cargar los roles. Intenta nuevamente.", Toast.LENGTH_SHORT).show();
                    return;
                }
                for (Rol rol : response.body()) {
                    if (rol == null || rol.getTipo() == null || rol.getId() == null) continue;
                    String tipo = rol.getTipo().trim().toLowerCase(Locale.ROOT);
                    if (tipo.contains("alumno") || tipo.contains("estudiante")) {
                        idRolAlumno = rol.getId();
                    } else if (tipo.contains("docente") || tipo.contains("profesor")) {
                        idRolDocente = rol.getId();
                    }
                }

                rolesCargados = idRolAlumno != null || idRolDocente != null;
                btnRegistro.setEnabled(rolesCargados);
                if (!rolesCargados) {
                    Toast.makeText(requireContext(), "No se encontraron roles válidos para el registro.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Rol>> call, Throwable t) {
                Log.w(TAG, "No se pudieron cargar roles remotos, se usarán valores por defecto", t);
                rolesCargados = false;
                btnRegistro.setEnabled(false);
                Toast.makeText(requireContext(), "No se pudieron cargar los roles. Revisa la conexión e intenta de nuevo.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void registrarUsuario() {
        String nombre = etNombre.getText().toString().trim();
        String contrasena = etContrasena.getText().toString().trim();
        String correo = etCorreo.getText().toString().trim();

        if (!validarCampos(nombre, contrasena, correo)) {
            return;
        }
        if (!rolesCargados) {
            Toast.makeText(requireContext(), "Aún no se han cargado los roles. Espera un momento e intenta de nuevo.", Toast.LENGTH_SHORT).show();
            return;
        }
        Boolean modoAudio = obtenerModoAccesibilidad();
        if (modoAudio == null) return;
        Integer idRol = obtenerIdRol();
        if (idRol == null) return;

        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setNombreCompleto(nombre);
        nuevoUsuario.setNombreCuenta(nombre);
        nuevoUsuario.setContrasena(contrasena);
        nuevoUsuario.setCorreo(correo);
        nuevoUsuario.setIdRol(idRol);
        nuevoUsuario.setActivo(true);
        nuevoUsuario.setVerificado(false);
        nuevoUsuario.setCreadoEn("2024-01-01T00:00:00Z");

        Log.d(TAG, "Registrando usuario: " + nombre + ", correo: " + correo + ", rol: " + idRol);
        loading.setVisibility(View.VISIBLE);
        btnRegistro.setEnabled(false);

        ApiService api = RetrofitClient.getApiService();
        api.registerUsuario(nuevoUsuario).enqueue(new Callback<Usuario>() {
            @Override
            public void onResponse(Call<Usuario> call, Response<Usuario> response) {
                loading.setVisibility(View.GONE);
                btnRegistro.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    Usuario usuarioRegistrado = response.body();
                    int idUsuario = usuarioRegistrado.getId();

                    PreferencesManager.setUsuarioRegistrado(requireContext(), true);
                    PreferencesManager.setIdUsuario(requireContext(), idUsuario);
                    PreferencesManager.setModoAudio(requireContext(), modoAudio);
                    PreferencesManager.setIdSubtemaUltimaConexion(requireContext(), -1);

                    UsuarioCst.USUARIO_ACTUAL = usuarioRegistrado;
                    guardarConfiguracionInicial(idUsuario, modoAudio);

                    Log.d(TAG, "Registro exitoso. ID de usuario guardado: " + idUsuario);
                    Toast.makeText(requireContext(), "Registro exitoso", Toast.LENGTH_SHORT).show();

                    Navigation.findNavController(requireView()).navigate(R.id.loginFragment);
                } else {
                    String error = "Error " + response.code() + ": " + response.message();
                    Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show();
                    Log.e(TAG, error);
                }
            }

            @Override
            public void onFailure(Call<Usuario> call, Throwable t) {
                loading.setVisibility(View.GONE);
                btnRegistro.setEnabled(true);
                String error = "Error de red: " + t.getMessage();
                Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show();
                Log.e(TAG, error, t);
            }
        });
    }

    private void guardarConfiguracionInicial(int idUsuario, boolean modoAudio) {
        Configuracion configuracion = new Configuracion();
        configuracion.setIdUsuario(idUsuario);
        configuracion.setCuentaCreada(true);
        configuracion.setModoAudio(modoAudio);
        configuracion.setModoOffline(false);
        configuracion.setNotificacionesActivadas(true);
        configuracion.setTamanoFuente(Configuracion.TamanoFuente.MEDIO);

        RetrofitClient.getApiService().saveConfiguracion(configuracion).enqueue(new Callback<Configuracion>() {
            @Override
            public void onResponse(Call<Configuracion> call, Response<Configuracion> response) {
                Log.d(TAG, "Configuración inicial sincronizada: " + response.code());
            }

            @Override
            public void onFailure(Call<Configuracion> call, Throwable t) {
                Log.w(TAG, "No se pudo sincronizar configuración inicial", t);
            }
        });
    }

    private boolean validarCampos(String nombre, String contrasena, String correo) {
        if (nombre.isEmpty() || contrasena.isEmpty() || correo.isEmpty()) {
            Toast.makeText(requireContext(), "Completa todos los campos", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            Toast.makeText(requireContext(), "Ingresa un correo válido", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (contrasena.length() < 6) {
            Toast.makeText(requireContext(), "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private Boolean obtenerModoAccesibilidad() {
        if (radioActiva.isChecked()) {
            return true;
        } else if (radioInactiva.isChecked()) {
            return false;
        } else {
            Toast.makeText(requireContext(), "Selecciona el modo de accesibilidad", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    private Integer obtenerIdRol() {
        if (radioAlumno.isChecked()) {
            return idRolAlumno;
        } else if (radioDocente.isChecked()) {
            return idRolDocente;
        } else {
            Toast.makeText(requireContext(), "Selecciona tipo de usuario", Toast.LENGTH_SHORT).show();
            return null;
        }
    }
}