package com.example.rider.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.rider.R;
import com.example.rider.model.Usuario;
import com.example.rider.helper.UsuariosFirebase;
import com.example.rider.config.ConfigFirebase;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

public class LoginActivity extends AppCompatActivity {

    private TextView editEmailLogin;
    private TextView editSenhaLogin;
    private FirebaseAuth autentica;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Inicializar Componentes
        editEmailLogin = findViewById(R.id.editEmailLogin);
        editSenhaLogin = findViewById(R.id.editSenhaLogin);
    }

    public void validarLoginUsuario(View view){

        String textoEmail = editEmailLogin.getText().toString();
        String textoSenha = editSenhaLogin.getText().toString();

        if (!textoEmail.isEmpty()){
            if (!textoSenha.isEmpty()){
                Usuario usuario = new Usuario();
                usuario.setEmail(textoEmail);
                usuario.setSenha(textoSenha);

                logarUsuario(usuario);

            }else {
                Toast.makeText(this, " Digite sua senha!!", Toast.LENGTH_SHORT).show();
            }

        }else {
            Toast.makeText(this, "Preencha o email!!", Toast.LENGTH_SHORT).show();
        }
    }

    private void logarUsuario(Usuario usuario) {

        autentica = ConfigFirebase.getFirebaseAutentica();
        autentica.signInWithEmailAndPassword(usuario.email, usuario.getSenha()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){

                    //Verificar tipo de usuário logado Motorista ou Passageiro
                    UsuariosFirebase.redirectUserLogin(LoginActivity.this);

                }else {
                    String excecao = "";
                    try {
                        throw task.getException();
                    }catch (FirebaseAuthInvalidUserException e){
                        excecao = "Usuário não cadastrado!!";
                    }catch (FirebaseAuthInvalidCredentialsException e){
                        excecao = "Email e senha incorretas!!";
                    }catch (Exception e){
                        excecao = "Erro ao logar usuário:" + e.getMessage();
                        e.printStackTrace();
                    }
                    Toast.makeText(LoginActivity.this, excecao, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}