package com.cyberpath.smartlearn.ui.main;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.cyberpath.smartlearn.R;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private NavController navController; //TODO: AGREGAR EL NOMBRE DEL USUARIO EN EL FRAGMENT DE MATERIAS

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.barra_lateral);

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        navController = navHostFragment.getNavController();

        NavigationUI.setupWithNavController(navigationView, navController);

        findViewById(R.id.deplegar_menu).setOnClickListener(v ->
                drawerLayout.openDrawer(GravityCompat.START));

        findViewById(R.id.btn_principal).setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            navController.navigate(R.id.materiasFragment);
            drawerLayout.closeDrawer(GravityCompat.START);
        });
    }

    @Override
    public void onClick(View view) {
        drawerLayout.closeDrawer(GravityCompat.START);
        navController.navigate(R.id.materiasFragment);
        drawerLayout.closeDrawer(GravityCompat.START);
    }
}