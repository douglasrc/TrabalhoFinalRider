package com.example.rider.model;

import com.example.rider.config.ConfigFirebase;
import com.google.firebase.database.DatabaseReference;

public class Requisicao {

    private String id;
    private String status;
    private Usuario passageiro;
    private Usuario motorista;
    private Destino destino;

    public static final String STATUS_AGUARDANDO = "Aguardando";
    public static final String STATUS_EM_ROTA = "EM ROTA";
    public static final String STATUS_VIAGEM = "Viagem";
    public static final String STATUS_FINALIZADA = "Finalizada";

    public Requisicao() {
    }

    public void salvar(){
        DatabaseReference firebaseRef = ConfigFirebase.getFirebaseDatabase();
        DatabaseReference requisicoes = firebaseRef.child("requisicoes");

        String idRequisicao = requisicoes.push().getKey();
        setId(idRequisicao);

        requisicoes.child(getId()).setValue(this);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Usuario getPassageiro() {
        return passageiro;
    }

    public void setPassageiro(Usuario passageiro) {
        this.passageiro = passageiro;
    }

    public Usuario getMotorista() {
        return motorista;
    }

    public void setMotorista(Usuario motorista) {
        this.motorista = motorista;
    }

    public Destino getDestino() {
        return destino;
    }

    public void setDestino(Destino destino) {
        this.destino = destino;
    }
}
