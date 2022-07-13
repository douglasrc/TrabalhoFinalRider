package com.example.rider.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.rider.PassageiroActivity;
import com.example.rider.R;
import com.example.rider.model.Usuario;
import com.example.rider.helper.UsuariosFirebase;
import com.example.rider.config.ConfigFirebase;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;

public class CadastroActivity extends AppCompatActivity {

    private TextView editCadastroNome;
    private EditText editCadastroEmail;
    private EditText editCadastroSenha;
    private Switch switchTipoUsuario;
    private FirebaseAuth auntentica;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);


        editCadastroNome = findViewById(R.id.editCadastroNome);
        editCadastroEmail = findViewById(R.id.editCadastroEmail);
        editCadastroSenha = findViewById(R.id.editCadastroSenha);
        switchTipoUsuario = findViewById(R.id.switchTipoUsuario);
    }

    public void validarCadastroUsuario(View view){
        String textoNome = editCadastroNome.getText().toString();
        String textoEmail = editCadastroEmail.getText().toString();
        String textoSenha = editCadastroSenha.getText().toString();

        //Verificação de dados
        if (!textoNome.isEmpty()){
            if (!textoEmail.isEmpty()){
                if (!textoSenha.isEmpty()){

                    Usuario usuario = new Usuario();
                    usuario.setNome(textoNome);
                    usuario.setEmail(textoEmail);
                    usuario.setSenha(textoSenha);
                    usuario.setTipo(verificaTipoUsuario());

                    cadastrarUsuario(usuario);

                }else {
                    Toast.makeText(CadastroActivity.this, "Crie uma Senha!", Toast.LENGTH_SHORT).show();
                }

            }else {
                Toast.makeText(CadastroActivity.this, "Insira um Email!", Toast.LENGTH_SHORT).show();
            }



        }else {
            Toast.makeText(CadastroActivity.this, "Preencha o Nome!", Toast.LENGTH_SHORT).show();
        }




    }

    public void cadastrarUsuario(Usuario usuario) {
        auntentica = ConfigFirebase.getFirebaseAutentica();
        auntentica.createUserWithEmailAndPassword(usuario.getEmail(),
                usuario.getSenha()).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){

                    try {
                        String idUsuario = task.getResult().getUser().getUid();
                        usuario.setId(idUsuario);
                        usuario.salvar();

                        //Atualizar nome no UserProfile
                        UsuariosFirebase.atualizarNomeUsuario(usuario.getNome());


                        // Redireciona o usuário para sua tele com base no seu tipo
                        // if for passageiro chama activity maps
                        // else chama request activity
                        if (verificaTipoUsuario() == "P"){
                            startActivity(new Intent(CadastroActivity.this, PassageiroActivity.class));
                            finish();

                            Toast.makeText(CadastroActivity.this, "Usuário Cadastrado Com Sucesso!!", Toast.LENGTH_SHORT).show();

                        }else{
                            startActivity(new Intent(CadastroActivity.this, RequestActivity.class));
                            finish();

                            Toast.makeText(CadastroActivity.this, "Motorista Cadastrado Com Sucesso!!", Toast.LENGTH_SHORT).show();
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }else {
                    String excecao = "";
                    try {
                        throw task.getException();
                    }/*catch (FirebaseAuthWeakPasswordException e){
                        excecao = "Digite uma senha mais forte!!";
                    }*/catch (FirebaseAuthInvalidCredentialsException e){
                        excecao = "Por favor, digite um email válido";
                    }catch (FirebaseAuthUserCollisionException e){
                        excecao = "Usuário já cadastrado!!";
                    }catch (Exception e){
                        excecao = "Erro ao cadastrar usuário:" + e.getMessage();
                        e.printStackTrace();
                    }
                    Toast.makeText(CadastroActivity.this, excecao, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public String verificaTipoUsuario(){
        return switchTipoUsuario.isChecked() ? "M" : "P";
    }
}