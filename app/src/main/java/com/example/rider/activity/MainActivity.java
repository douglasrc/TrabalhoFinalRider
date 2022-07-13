package com.example.rider.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.rider.config.ConfigFirebase;
import com.example.rider.helper.Permissoes;
import com.example.rider.R;
import com.example.rider.helper.UsuariosFirebase;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth autentica;
    private String[] permissoes = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();

        //validar permissões
        Permissoes.validarPermissoes(permissoes, this, 1);

       /* autentica = ConfigFirebase.getFirebaseAutentica();
        autentica.signOut();*/
    }

    public void abrirTelaLogin(View view){
        startActivity(new Intent(this, com.example.rider.activity.LoginActivity.class));
    }

    public void abrirTelaCadastro(View view){
        startActivity(new Intent(this, CadastroActivity.class));
    }

    @Override
    protected void onStart() {
        super.onStart();
        UsuariosFirebase.redirectUserLogin(MainActivity.this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        for (int permissaoResult : grantResults){
            if (permissaoResult == PackageManager.PERMISSION_DENIED){
                alertaValidaPermissao();
            }
        }
    }

    private void alertaValidaPermissao() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Permissões Negadas!!");
        builder.setMessage("Para utilizar o APP é necessário aceitar as permissões!!");
        builder.setCancelable(false);
        builder.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}