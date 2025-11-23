package com.example.umeventplanner;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "MainActivity";
    private DrawerLayout drawerLayout;
    private BottomNavigationView bottomNavigationView;
    private SwitchMaterial toggleRole;
    private boolean isPlannerMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        toggleRole = findViewById(R.id.toggleRole);

        setupRoleToggle(currentUser);
        setupBottomNavigation();
        updateNavHeader(currentUser);

        if (savedInstanceState == null) {
            loadFragment(new DiscoverFragment());
            navigationView.setCheckedItem(R.id.nav_account);
        }
    }

    private void updateNavHeader(FirebaseUser currentUser) {
        NavigationView navigationView = findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        TextView navHeaderName = headerView.findViewById(R.id.nav_header_name);
        TextView navHeaderEmail = headerView.findViewById(R.id.nav_header_email);

        if (currentUser != null) {
            navHeaderEmail.setText(currentUser.getEmail());
            FirebaseFirestore.getInstance().collection("users").document(currentUser.getUid()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("name");
                        navHeaderName.setText(name);
                    }
                });
        }
    }
    
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.nav_account) {
            startActivity(new Intent(this, AccountActivity.class));
        } else if (itemId == R.id.nav_logout) {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void setupRoleToggle(FirebaseUser currentUser) {
        if (currentUser == null) return;
        FirebaseFirestore.getInstance().collection("users").document(currentUser.getUid()).get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String role = documentSnapshot.getString("role");
                    Log.d(TAG, "User role from Firestore: " + role);
                    if ("Event Planner".equals(role)) {
                        toggleRole.setVisibility(View.VISIBLE);
                        toggleRole.setOnCheckedChangeListener((buttonView, isChecked) -> {
                            isPlannerMode = isChecked;
                            updateNavigationMenu();
                        });
                    } else {
                        toggleRole.setVisibility(View.GONE);
                    }
                } else {
                    Log.d(TAG, "User document does not exist.");
                    toggleRole.setVisibility(View.GONE);
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error fetching user role", e);
                toggleRole.setVisibility(View.GONE);
            });
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (isPlannerMode) {
                if (itemId == R.id.nav_dashboard) {
                    selectedFragment = new PlannerDashboardFragment();
                } else if (itemId == R.id.nav_my_hosted_events) {
                    selectedFragment = new MyHostedEventsFragment();
                } else if (itemId == R.id.nav_create_event) {
                    selectedFragment = new CreateEventFragment();
                } else if (itemId == R.id.nav_forum) {
                    selectedFragment = new ForumFragment();
                }
            } else {
                if (itemId == R.id.nav_discover) {
                    selectedFragment = new DiscoverFragment();
                } else if (itemId == R.id.nav_my_events) {
                    selectedFragment = new MyEventsFragment();
                } else if (itemId == R.id.nav_leaderboard) {
                    selectedFragment = new LeaderboardFragment();
                } else if (itemId == R.id.nav_notifications) {
                    selectedFragment = new NotificationsFragment();
                }
            }

            if (selectedFragment != null) {
                loadFragment(selectedFragment);
            }
            return true;
        });
    }

    private void updateNavigationMenu() {
        bottomNavigationView.getMenu().clear();
        if (isPlannerMode) {
            bottomNavigationView.inflateMenu(R.menu.menu_planner);
            loadFragment(new PlannerDashboardFragment());
        } else {
            bottomNavigationView.inflateMenu(R.menu.menu_participant);
            loadFragment(new DiscoverFragment());
        }
    }

    private void loadFragment(Fragment fragment) {
        if (!isFinishing()) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, fragment);
            fragmentTransaction.commit();
        }
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
