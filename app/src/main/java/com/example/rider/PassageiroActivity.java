package com.example.rider;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.rider.config.ConfigFirebase;
import com.example.rider.databinding.ActivityPassageiroBinding;
import com.example.rider.helper.UsuariosFirebase;
import com.example.rider.model.Destino;
import com.example.rider.model.Requisicao;
import com.example.rider.model.Usuario;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class PassageiroActivity extends AppCompatActivity implements OnMapReadyCallback {

    private TextView editDestino;
    private LinearLayout LayoutDestino;
    private Button chamar_carro_btn;

    private GoogleMap mMap;
    private FirebaseAuth autentica;
    private LocationManager locationManager;
    private ActivityPassageiroBinding binding;
    private LocationListener locationListener;
    private LatLng localPassageiro;
    private boolean carroChamado = false;
    private DatabaseReference firebaseRef;
    private Requisicao requisicao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        inicializarComponents();

        verificaStatusRequisicao();

    }

    private void verificaStatusRequisicao() {
        Usuario usuarioLogado = UsuariosFirebase.getDadosUserLog();
        DatabaseReference requisicoes = firebaseRef.child("requisicoes");
        Query requisicaoPesquisa = requisicoes.orderByChild("passageiro/id").equalTo(usuarioLogado.getId());

        requisicaoPesquisa.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                List<Requisicao> lista = new ArrayList<>();
                for (DataSnapshot ds: snapshot.getChildren()){
                    lista.add(requisicao = ds.getValue(Requisicao.class));
                }
                Collections.reverse(lista);
                if (lista!= null && lista.size()>0){
                    requisicao = lista.get(0);

                    switch (requisicao.getStatus()){
                        case Requisicao.STATUS_AGUARDANDO:
                            LayoutDestino.setVisibility(View.GONE);
                            chamar_carro_btn.setText("Cancelar Carro");
                            carroChamado = true;
                            break;
                    }
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        atualUserLocation();

    }

    public void chamarCarro(View view){

        if (!carroChamado){// Carro nao foi chamado

            //Inicio Chamar Carro
            String endrcDestino = editDestino.getText().toString();

            if (!endrcDestino.equals("") || endrcDestino != null){
                Address addressDestino = rcpEndereco(endrcDestino);
                if (addressDestino != null){
                    Destino destino = new Destino();
                    destino.setCidade(addressDestino.getAdminArea());
                    destino.setCep(addressDestino.getPostalCode());
                    destino.setBairro(addressDestino.getSubLocality());
                    destino.setRua(addressDestino.getThoroughfare());
                    destino.setNumero(addressDestino.getFeatureName());
                    destino.setLatitude(String.valueOf(addressDestino.getLatitude()));
                    destino.setLongitude(String.valueOf(addressDestino.getLongitude()));

                    StringBuilder mensagem = new StringBuilder();
                    mensagem.append("Cidade:" + destino.getCidade());
                    mensagem.append("\nRua:" + destino.getRua());
                    mensagem.append("\nNúmero:" + destino.getNumero());
                    mensagem.append("\nBairro:" + destino.getBairro());
                    mensagem.append("\nCep:" + destino.getCep());

                    AlertDialog.Builder builder = new AlertDialog.Builder(this)
                            .setTitle("Confirmer seu Endereço")
                            .setMessage(mensagem)
                            .setPositiveButton("Confirmar", (dialogInterface, i) -> {
                                salveRequest(destino);
                                carroChamado = true;

                            }).setNegativeButton("Cancelar", (dialogInterface, i) -> {

                            });
                    AlertDialog dialog = builder.create();
                    dialog.show();

                }
            }else {
                Toast.makeText(this, "Informe o endereço de Destino!!", Toast.LENGTH_SHORT).show();
            }//Fim Chamar Carro

        }else {//Cancelar carro
            carroChamado = false;
        }


    }

    private void salveRequest(Destino destino) {
        Requisicao requisicao = new Requisicao();
        requisicao.setDestino(destino);

        Usuario usuarioPassageiro = UsuariosFirebase.getDadosUserLog();
        usuarioPassageiro.setLatitude(String.valueOf(localPassageiro.latitude));
        usuarioPassageiro.setLongitude(String.valueOf(localPassageiro.longitude));

        requisicao.setPassageiro(usuarioPassageiro);
        requisicao.setStatus(Requisicao.STATUS_AGUARDANDO);
        requisicao.salvar();

        LayoutDestino.setVisibility(View.GONE);
        chamar_carro_btn.setText("Cancelar Carro");
    }

    private Address rcpEndereco(String endereco) {

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> listaEndr = geocoder.getFromLocationName(endereco, 1);
            if (listaEndr != null && listaEndr.size() > 0){
                Address address = listaEndr.get(0);

                return address;

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @SuppressLint("MissingPermission")
    private void atualUserLocation(){
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {

                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                localPassageiro = new LatLng(latitude, longitude);

                mMap.clear();
                mMap.addMarker(new MarkerOptions().position(localPassageiro)
                        .title("Meu Local")
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.usuario)));

                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(localPassageiro, 15));
            }

        };
        //Solicitar atualizações e localização
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,10000, 50, locationListener);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){
            case R.id.menuSair:
                autentica.signOut();
                finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void inicializarComponents(){
        binding = ActivityPassageiroBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        //Inicializar Components
        editDestino = findViewById(R.id.editDestino);
        LayoutDestino = findViewById(R.id.LayoutDestino);
        chamar_carro_btn = findViewById(R.id.chamar_carro_btn);

        //config inicias
        autentica = ConfigFirebase.getFirebaseAutentica();
        firebaseRef = ConfigFirebase.getFirebaseDatabase();


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }
}