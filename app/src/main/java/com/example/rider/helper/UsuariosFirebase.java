package com.example.rider.helper;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.rider.PassageiroActivity;
import com.example.rider.model.Usuario;
import com.example.rider.activity.RequestActivity;
import com.example.rider.config.ConfigFirebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class UsuariosFirebase {
    public static FirebaseUser getUsuarioAtual(){
        FirebaseAuth usuario = ConfigFirebase.getFirebaseAutentica();
        return usuario.getCurrentUser();
    }

    public static Usuario getDadosUserLog(){
        FirebaseUser firebaseUser  =getUsuarioAtual();

        Usuario usuario = new Usuario();
        usuario.setId(firebaseUser.getUid());
        usuario.setEmail(firebaseUser.getEmail());
        usuario.setNome(firebaseUser.getDisplayName());

        return usuario;
    }

    public static boolean atualizarNomeUsuario(String nome){
        try {

            FirebaseUser user = getUsuarioAtual();
            UserProfileChangeRequest profile = new UserProfileChangeRequest.Builder()
                    .setDisplayName(nome)
                    .build();

            user.updateProfile(profile).addOnCompleteListener(task -> {
                if (!task.isSuccessful()){
                    Log.d("Perfil", "Erro ao atualizar nome do perfil.");
                }
            });
            return true;

        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public static void redirectUserLogin(Activity activity){

        FirebaseUser user = getUsuarioAtual();
        if (user != null){

            DatabaseReference usuariosRef = ConfigFirebase.getFirebaseDatabase()
                    .child("usuarios").child(getIdUser());
            usuariosRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    Usuario usuario = snapshot.getValue(Usuario.class);

                    String tipoUsuario = usuario.getTipo();
                    Intent i;
                    if (tipoUsuario.equals("M")){
                        i = new Intent(activity, RequestActivity.class);
                    }else {
                        i = new Intent(activity, PassageiroActivity.class);
                    }
                    activity.startActivity(i);

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }


    }

    public static String getIdUser(){
        return getUsuarioAtual().getUid();
    }
}
