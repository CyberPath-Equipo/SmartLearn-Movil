package com.cyberpath.smartlearn.ui.main;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.cyberpath.smartlearn.R;
import com.cyberpath.smartlearn.data.model.contenido.Subtema;
import com.cyberpath.smartlearn.data.model.usuario.Usuario;
import com.cyberpath.smartlearn.logic.MainLogic;
import com.cyberpath.smartlearn.ui.acceso.AccesoActivity;
import com.cyberpath.smartlearn.ui.main.combo.principal.materia.MateriasFragmentDirections;
import com.cyberpath.smartlearn.util.accesibilidad.visual.EntradaAudio;
import com.cyberpath.smartlearn.util.accesibilidad.visual.SalidaAudio;
import com.cyberpath.smartlearn.util.constants.UsuarioCst;
import com.cyberpath.smartlearn.util.preferences.PreferencesManager;
import com.cyberpath.smartlearn.util.preferences.ThemeManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "MainActivity";
    private static final long SHAKE_COOLDOWN_MS = 1800L;
    private ImageView btnPrincipal, btnDesplegarMenu;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private NavHostFragment navHostFragment;
    private NavController navController;
    private SensorManager sensorManager;
    private Sensor acelerometro;
    private ShakeDetector shakeDetector;
    private AlertDialog dialogoShake;
    private NavController.OnDestinationChangedListener audioCleanupListener;

    private TextView tvUltimoSubtemaMenu;
    private MenuItem ultimoSubtemaItem;
    private Subtema ultimoSubtema;
    private MainLogic mainLogic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.applyTheme(this);
        super.onCreate(savedInstanceState);

        SalidaAudio.iniciarInstancia(getApplicationContext());
        EntradaAudio.iniciarInstancia(getApplicationContext());

        setContentView(R.layout.activity_main);

        ensureUsuarioLoaded();
        inicializarUltimoSubtema();

        btnPrincipal = findViewById(R.id.btn_principal);
        btnDesplegarMenu = findViewById(R.id.btn_desplegar_menu);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.barra_lateral);

        btnPrincipal.setOnClickListener(this);
        btnDesplegarMenu.setOnClickListener(this);

        navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        navController = navHostFragment.getNavController();
        audioCleanupListener = (controller, destination, arguments) -> detenerAudioAccesibilidad();
        navController.addOnDestinationChangedListener(audioCleanupListener);
        initShakeNavigation();

        NavigationUI.setupWithNavController(navigationView, navController);

        ultimoSubtemaItem = navigationView.getMenu().findItem(R.id.ultimoSubtema);

        if (ultimoSubtemaItem != null && ultimoSubtemaItem.getActionView() != null) {
            View actionView = ultimoSubtemaItem.getActionView();
            tvUltimoSubtemaMenu = actionView.findViewById(R.id.tv_ultimo_subtema_menu);

            actionView.setOnClickListener(v -> {
                if (ultimoSubtema != null) {
                    mostrarDialogoUltimoSubtema();
                } else {
                    Toast.makeText(MainActivity.this, "No hay último subtema disponible", Toast.LENGTH_SHORT).show();
                }
                drawerLayout.closeDrawer(GravityCompat.START);
            });
        }

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.materiasFragment) {
                detenerAudioAccesibilidad();
                navController.navigate(R.id.action_global_materias);
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }

            if (id == R.id.cerrarSesion) {
                PreferencesManager.clearSesion(MainActivity.this);
                UsuarioCst.USUARIO_ACTUAL = null;
                Intent intent = new Intent(MainActivity.this, AccesoActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
                return true;
            }

            boolean handled = NavigationUI.onNavDestinationSelected(item, navController);
            if (handled) {
                drawerLayout.closeDrawer(GravityCompat.START);
            }
            return handled;
        });
    }

    private void initShakeNavigation() {
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (sensorManager == null) {
            Log.w(TAG, "SensorManager no disponible");
            return;
        }

        acelerometro = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (acelerometro == null) {
            Log.w(TAG, "Acelerómetro no disponible");
            return;
        }

        shakeDetector = new ShakeDetector(SHAKE_COOLDOWN_MS, this::mostrarDialogoIrMaterias);
    }

    private void mostrarDialogoIrMaterias() {
        if (isFinishing() || isDestroyed()) {
            return;
        }

        if (dialogoShake != null && dialogoShake.isShowing()) {
            return;
        }

        View vista = LayoutInflater.from(this).inflate(R.layout.dialogo_aceptar_cancelar, null);
        TextView tvTitulo = vista.findViewById(R.id.tvTitulo);
        TextView tvMensaje = vista.findViewById(R.id.tvMensaje);

        tvTitulo.setText(R.string.shake_dialog_title);
        tvMensaje.setText(R.string.shake_dialog_message);

        dialogoShake = new MaterialAlertDialogBuilder(this)
                .setView(vista)
                .setCancelable(true)
                .show();

        vista.findViewById(R.id.btnAceptar).setOnClickListener(v -> {
            dialogoShake.dismiss();
            navigateToMateriasSafely();
        });

        vista.findViewById(R.id.btnCancelar).setOnClickListener(v -> dialogoShake.dismiss());

        dialogoShake.setOnDismissListener(dialog -> dialogoShake = null);
    }

    private void navigateToMateriasSafely() {
        if (navController == null) {
            return;
        }

        NavDestination destinoActual = navController.getCurrentDestination();
        if (destinoActual != null && destinoActual.getId() == R.id.materiasFragment) {
            return;
        }

        try {
            detenerAudioAccesibilidad();
            navController.navigate(R.id.action_global_materias);
        } catch (IllegalArgumentException | IllegalStateException e) {
            Log.w(TAG, "No se pudo navegar a Materias desde shake", e);
        }
    }

    private void inicializarUltimoSubtema() {
        mainLogic = new MainLogic(this);
        mainLogic.cargarUltimoSubtema();
    }

    private void mostrarDialogoUltimoSubtema() {
        if (ultimoSubtema == null) return;

        View vista = LayoutInflater.from(this).inflate(R.layout.dialogo_teoria_practica, null);
        TextView tvMensaje = vista.findViewById(R.id.tv_titulo_subtema);
        tvMensaje.setText(ultimoSubtema.getNombre());

        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setView(vista)
                .setCancelable(true)
                .show();

        vista.findViewById(R.id.btn_teoria).setOnClickListener(v -> {
            dialog.dismiss();
            if (ultimoSubtema != null) {
                detenerAudioAccesibilidad();
                navController.navigate(R.id.action_global_materias);

                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    try {
                        var action = MateriasFragmentDirections
                                .actionMateriasFragmentToTeoriaFragment(ultimoSubtema, null);
                        navController.navigate(action);
                    } catch (Exception e) {
                        Log.e(TAG, "Error navegando a Teoría desde último subtema", e);
                    }
                }, 120);
            }
        });

        vista.findViewById(R.id.btn_practica).setOnClickListener(v -> {
            dialog.dismiss();
            if (ultimoSubtema != null) {
                detenerAudioAccesibilidad();
                navController.navigate(R.id.action_global_materias);

                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    try {
                        var action = MateriasFragmentDirections
                                .actionMateriasFragmentToPracticaFragment(ultimoSubtema);
                        navController.navigate(action);
                    } catch (Exception e) {
                        Log.e(TAG, "Error navegando a Práctica desde último subtema", e);
                    }
                }, 120);
            }
        });

        vista.findViewById(R.id.btn_cancelar).setOnClickListener(v -> dialog.dismiss());
    }

    public void actualizarUltimoSubtemaMenu(String texto, Subtema subtema) {
        this.ultimoSubtema = subtema;
        if (tvUltimoSubtemaMenu != null) {
            tvUltimoSubtemaMenu.setText(texto);
        } else {
            Log.w(TAG, "tvUltimoSubtemaMenu es null al intentar actualizar");
        }
    }

    public void showToast(String mensaje) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
    }

    public void showToastLong(String mensaje) {
        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();
    }

    private void ensureUsuarioLoaded() {
        if (UsuarioCst.USUARIO_ACTUAL != null && UsuarioCst.USUARIO_ACTUAL.getId() != null) {
            Log.d(TAG, "Usuario ya está cargado: " + UsuarioCst.USUARIO_ACTUAL.getNombreCuenta());
            return;
        }

        int idUsuario = PreferencesManager.getIdUsuario(this);
        if (idUsuario <= 0) {
            Log.w(TAG, "No hay usuario en preferencias. Redirigiendo a AccesoActivity.");
            PreferencesManager.clearSesion(this);
            Intent intent = new Intent(this, AccesoActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        Log.d(TAG, "Usuario no está en memoria pero hay sesión. Cargando usuario ID: " + idUsuario);
        UsuarioCst.asignarConstantesUsuario(this, idUsuario,
                new UsuarioCst.UsuarioLoadCallback() {
                    @Override
                    public void onUsuarioLoaded(Usuario usuario) {
                        Log.d(TAG, "Usuario cargado exitosamente: " + usuario.getNombreCuenta());
                    }

                    @Override
                    public void onError(String mensaje) {
                        Log.e(TAG, "Error al cargar usuario desde API: " + mensaje);
                        if (!UsuarioCst.ensureUsuarioLoaded(MainActivity.this)) {
                            Log.w(TAG, "No se pudo cargar usuario desde preferencias. Redirigiendo a AccesoActivity.");
                            PreferencesManager.clearSesion(MainActivity.this);
                            Intent intent = new Intent(MainActivity.this, AccesoActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        }
                    }
                });
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_principal) {
            detenerAudioAccesibilidad();
            navController.navigate(R.id.action_global_materias);
            drawerLayout.closeDrawer(GravityCompat.START);
        } else if (v.getId() == R.id.btn_desplegar_menu) {
            drawerLayout.openDrawer(GravityCompat.START);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (sensorManager != null && acelerometro != null && shakeDetector != null) {
            sensorManager.registerListener(shakeDetector, acelerometro, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    protected void onPause() {
        if (sensorManager != null && shakeDetector != null) {
            sensorManager.unregisterListener(shakeDetector);
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (navController != null && audioCleanupListener != null) {
            navController.removeOnDestinationChangedListener(audioCleanupListener);
        }
        detenerAudioAccesibilidad();
        if (dialogoShake != null && dialogoShake.isShowing()) {
            dialogoShake.dismiss();
        }
        super.onDestroy();
    }

    private void detenerAudioAccesibilidad() {
        try {
            EntradaAudio entradaAudio = EntradaAudio.obtenerInstancia();
            if (entradaAudio != null) {
                entradaAudio.detenerEscucha();
            }
        } catch (Exception ignored) {
        }

        try {
            SalidaAudio salidaAudio = SalidaAudio.obtenerInstancia();
            if (salidaAudio != null) {
                salidaAudio.detener();
            }
        } catch (Exception ignored) {
        }
    }
}