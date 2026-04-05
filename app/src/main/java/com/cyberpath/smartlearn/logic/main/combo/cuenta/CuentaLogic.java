package com.cyberpath.smartlearn.logic.main.combo.cuenta;

import android.content.Context;

import androidx.fragment.app.Fragment;

import com.cyberpath.smartlearn.data.model.usuario.Usuario;
import com.cyberpath.smartlearn.data.remote.api.ApiService;
import com.cyberpath.smartlearn.data.remote.api.RetrofitClient;
import com.cyberpath.smartlearn.ui.main.combo.cuenta.CuentaFragment;
import com.cyberpath.smartlearn.ui.main.combo.cuenta.EditarCuentaFragment;
import com.cyberpath.smartlearn.util.constants.UsuarioCst;
import com.cyberpath.smartlearn.util.preferences.PreferencesManager;
import com.cyberpath.smartlearn.util.preferences.ThemeManager;
import com.cyberpath.smartlearn.util.validation.InputValidator;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CuentaLogic {

    private final Fragment cuentaFragment;
    private final Context context;

    public CuentaLogic(Fragment cuentaFragment) {
        this.cuentaFragment = cuentaFragment;
        this.context = cuentaFragment.requireContext();
    }

    public void validarYGuardarCambios(String tipoEdicion, String nuevoValor,
                                       String confirmacion, String contrasenaActual) {
        Usuario usuarioActual = UsuarioCst.USUARIO_ACTUAL;
        if (usuarioActual == null) {
            showToast("Error: usuario no identificado");
            return;
        }

        if (contrasenaActual.isEmpty()) {
            showToast("Ingresa tu contrasena actual");
            return;
        }

        if (!contrasenaActual.equals(usuarioActual.getContrasena())) {
            showToast("Contrasena actual incorrecta");
            return;
        }

        if (!"accesibilidadVisual".equals(tipoEdicion)
                && !"accesibilidadAuditiva".equals(tipoEdicion)
                && nuevoValor.isEmpty()) {
            showToast("Completa todos los campos");
            return;
        }

        if ("contrasena".equals(tipoEdicion) && !nuevoValor.equals(confirmacion)) {
            showToast("Las contrasenas no coinciden");
            return;
        }

        switch (tipoEdicion) {
            case "nombre":
                usuarioActual.setNombreCuenta(nuevoValor);
                break;
            case "nombreCompleto":
                if (!InputValidator.isValidNombreCompleto(nuevoValor)) {
                    showToast("El nombre completo no puede contener numeros");
                    return;
                }
                usuarioActual.setNombreCompleto(nuevoValor);
                break;
            case "correo":
                if (!InputValidator.isValidEmail(nuevoValor)) {
                    showToast("Ingresa un correo valido");
                    return;
                }
                usuarioActual.setCorreo(nuevoValor);
                break;
            case "contrasena":
                if (!InputValidator.isValidPassword(nuevoValor)) {
                    showToast("La contrasena debe tener minimo 6 caracteres y al menos un numero");
                    return;
                }
                usuarioActual.setContrasena(nuevoValor);
                break;
            case "accesibilidadVisual":
                actualizarAccesibilidadVisual(nuevoValor);
                notificarCambiosLocales();
                return;
            case "accesibilidadAuditiva":
                actualizarAccesibilidadAuditiva(nuevoValor);
                notificarCambiosLocales();
                return;
            default:
                showToast("Tipo de edicion no valido");
                return;
        }

        actualizarUsuarioAPI(usuarioActual);
    }

    private void actualizarAccesibilidadVisual(String estado) {
        boolean activar = "activa".equalsIgnoreCase(estado);
        PreferencesManager.setAccesibilidadVisual(context, activar);
        PreferencesManager.setTemaApp(
                context,
                activar ? PreferencesManager.THEME_ACCESSIBLE : PreferencesManager.THEME_LIGHT
        );

        if (cuentaFragment.getActivity() != null) {
            ThemeManager.applyTheme(cuentaFragment.requireActivity());
            cuentaFragment.requireActivity().recreate();
        }
    }

    private void actualizarAccesibilidadAuditiva(String estado) {
        boolean activar = "activa".equalsIgnoreCase(estado);
        PreferencesManager.setAccesibilidadAuditiva(context, activar);
    }

    private void notificarCambiosLocales() {
        if (cuentaFragment instanceof EditarCuentaFragment) {
            ((EditarCuentaFragment) cuentaFragment).onCambiosGuardados();
        } else if (cuentaFragment instanceof CuentaFragment) {
            ((CuentaFragment) cuentaFragment).actualizarDatosUsuario();
        }
    }

    private void actualizarUsuarioAPI(Usuario usuario) {
        ApiService api = RetrofitClient.getApiService();
        Call<Usuario> call = api.update(usuario.getId(), usuario);

        call.enqueue(new Callback<Usuario>() {
            @Override
            public void onResponse(Call<Usuario> call, Response<Usuario> response) {
                if (!cuentaFragment.isAdded()) return;

                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        UsuarioCst.asignarConstantesUsuario(cuentaFragment.requireContext(), response.body().getId());
                    }

                    if (cuentaFragment instanceof EditarCuentaFragment) {
                        ((EditarCuentaFragment) cuentaFragment).onCambiosGuardados();
                    }
                } else {
                    if (cuentaFragment instanceof EditarCuentaFragment) {
                        ((EditarCuentaFragment) cuentaFragment).onErrorGuardar();
                    }
                }
            }

            @Override
            public void onFailure(Call<Usuario> call, Throwable t) {
                if (!cuentaFragment.isAdded()) return;

                if (cuentaFragment instanceof EditarCuentaFragment) {
                    ((EditarCuentaFragment) cuentaFragment).onErrorConexion();
                }
            }
        });
    }

    public void actualizarDatosUsuario(Integer idUsuario) {
        if (idUsuario == null) return;

        ApiService api = RetrofitClient.getApiService();
        Call<Usuario> call = api.getUsuarioById(idUsuario);

        call.enqueue(new Callback<Usuario>() {
            @Override
            public void onResponse(Call<Usuario> call, Response<Usuario> response) {
                if (!cuentaFragment.isAdded()) return;

                if (response.isSuccessful() && response.body() != null) {
                    Usuario usuarioActualizado = response.body();

                    UsuarioCst.USUARIO_ACTUAL.setNombreCuenta(usuarioActualizado.getNombreCuenta());
                    UsuarioCst.USUARIO_ACTUAL.setNombreCompleto(usuarioActualizado.getNombreCompleto());
                    UsuarioCst.USUARIO_ACTUAL.setCorreo(usuarioActualizado.getCorreo());

                    if (cuentaFragment instanceof CuentaFragment) {
                        ((CuentaFragment) cuentaFragment).actualizarDatosUsuario();
                    }
                }
            }

            @Override
            public void onFailure(Call<Usuario> call, Throwable t) {
            }
        });
    }

    public void eliminarCuenta(String contrasenaIngresada) {
        Usuario usuarioActual = UsuarioCst.USUARIO_ACTUAL;

        if (usuarioActual == null || usuarioActual.getId() == null) {
            showToast("Error: usuario no identificado");
            return;
        }

        if (!contrasenaIngresada.equals(usuarioActual.getContrasena())) {
            showToast("Contraseña incorrecta");
            return;
        }

        ApiService api = RetrofitClient.getApiService();
        Call<Void> call = api.delete(usuarioActual.getId());

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (!cuentaFragment.isAdded()) return;

                if (response.isSuccessful()) {
                    showToast("Cuenta eliminada correctamente");

                    if (cuentaFragment instanceof CuentaFragment) {
                        ((CuentaFragment) cuentaFragment).onCuentaEliminada();
                    }
                } else {
                    showToast("Error al eliminar cuenta");
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                if (!cuentaFragment.isAdded()) return;
                showToast("Error de conexión");
            }
        });
    }


    private void showToast(String mensaje) {
        if (cuentaFragment instanceof EditarCuentaFragment) {
            ((EditarCuentaFragment) cuentaFragment).showToast(mensaje);
        } else if (cuentaFragment instanceof CuentaFragment) {
            ((CuentaFragment) cuentaFragment).showToast(mensaje);
        }
    }
}