package com.cyberpath.smartlearn.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.cyberpath.smartlearn.R;
import com.cyberpath.smartlearn.ui.acceso.AccesoActivity;
import com.cyberpath.smartlearn.util.preferences.ThemeManager;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private ImageView btnPrincipal, btnDesplegarMenu;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private NavHostFragment navHostFragment;
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnPrincipal = findViewById(R.id.btn_principal);
        btnDesplegarMenu = findViewById(R.id.btn_desplegar_menu);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.barra_lateral);

        btnPrincipal.setOnClickListener(this);
        btnDesplegarMenu.setOnClickListener(this);

        navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        navController = navHostFragment.getNavController();

        NavigationUI.setupWithNavController(navigationView, navController);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                if (item.getItemId() == R.id.materiasFragment) {
                    navController.navigate(R.id.action_global_materias);
                    drawerLayout.closeDrawer(GravityCompat.START);
                    return true;
                }

                boolean handled = NavigationUI.onNavDestinationSelected(item, navController);

                if (handled) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                }

                if (item.getItemId() == R.id.cerrarSesion) {
                    Intent intent = new Intent(MainActivity.this, AccesoActivity.class);
                    startActivity(intent);
                    finish();
                    drawerLayout.closeDrawer(GravityCompat.START);
                    return true;
                }

                return handled;
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_principal) {
            navController.navigate(R.id.action_global_materias);
            drawerLayout.closeDrawer(GravityCompat.START);
        } else if (v.getId() == R.id.btn_desplegar_menu) {
            drawerLayout.openDrawer(GravityCompat.START);
        }
    }
}